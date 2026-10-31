package com.pellicciotta.support.cache;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import com.pellicciotta.support.logging.Log;
import com.pellicciotta.support.logging.LogService;
import com.pellicciotta.support.utils.Contract;
import com.pellicciotta.support.utils.DateTimeUtils;

/**
 * Common interface for implementing a pool of resources.
 *
 * @param <R> The type of resources to be pooled.
 */
public class AbstractPool<R extends PoolResource>
{
  public AbstractPool(int maxPoolSize, Supplier<R> factory) {
    Contract.isPositive("maxPoolSize", maxPoolSize);
    Contract.isNotNull("factory", factory);
    this.maxPoolSize = maxPoolSize;
    this.factory = factory;
  }

  protected static final Log log = LogService.fromCurrentClass();
  protected static final int MAX_CREATION_ATTEMPTS = 3;
  protected static final int WAIT_TIME_IN_MS_BETWEEN_CREATION_ATTEMPTS = 500;

  protected final Supplier<R> factory;
  protected final int maxPoolSize;

  protected final AtomicLong startLeaseCount = new AtomicLong();
  protected final AtomicLong endLeaseCount = new AtomicLong();
  protected final AtomicLong createCount = new AtomicLong();
  protected final AtomicLong unhealthyCount = new AtomicLong();
  protected final AtomicLong discardCount = new AtomicLong();

  protected final Queue<R> pool = new ConcurrentLinkedDeque<>();

  /**
   *  Get a resource for use.
   *
   *  @return A newly created or re-used resource that is ready for use.
   *
   *  @throws PoolException If no healthy resource could be created and/or re-used.
   */
  public R startLease() {
    R resource = this.pool.poll();
    // First try to re-use existing
    while ((resource != null) && !resource.isHealthy()) {
      this.unhealthyCount.incrementAndGet();
      this.discardResource(resource);
      resource = this.pool.poll();
    }
    if (resource == null) { // Create new resource
      for (int i = 0; i < MAX_CREATION_ATTEMPTS; i++) {
        resource = this.factory.get();
        if (resource == null) {
          if (i + 1 < MAX_CREATION_ATTEMPTS) {
            log.warning("Could not create new resource: re-trying creation");
            DateTimeUtils.sleep(WAIT_TIME_IN_MS_BETWEEN_CREATION_ATTEMPTS);
            continue; // Try again
          }
          break; // Fail
        }
        long created = this.createCount.incrementAndGet();
        log.debug("New resource created: #%d - %s", created, this.toString());
        if (resource.isHealthy()) {
          break;
        }
        this.unhealthyCount.incrementAndGet();
        this.discardResource(resource);
        if (i + 1 < MAX_CREATION_ATTEMPTS) {
          log.warning("Created resource was not healthy: re-trying creation");
          DateTimeUtils.sleep(WAIT_TIME_IN_MS_BETWEEN_CREATION_ATTEMPTS);
          continue; // Try again
        }
        break; // Fail
      }
    }
    if (resource == null) {
      throw new PoolException("Failed to create a healthy resource (%d attempts were made)", MAX_CREATION_ATTEMPTS);
    }
    log.debug("Starting lease of resource: %d resource are still pooled.%n%s", this.pool.size(), this);
    this.startLeaseCount.incrementAndGet();
    return resource;
  }

  /**
   *  Discard a resource (e.g. because it is unhealthy or no longer needed) by closing it
   *  and removing it from the pool.
   *  <p>
   *  Overriding implementations should always call this super-class method too.
   *  </p>
   *  @param resource The resource to discard.
   */
  protected void discardResource(R resource) {
    this.discardCount.incrementAndGet();
    try {
      resource.close();
    }
    catch (Exception e) {
      log.warning("Failed to close discarded resource: %s", e);
    }
  }

  /**
   *  Give back a previously leased resource.
   *
   *  @param resource The resource to give back for destruction or re-use.
   */
  public void endLease(R resource) {
    assert resource != null;
    this.endLeaseCount.incrementAndGet();
    if (this.pool.size() < this.maxPoolSize) {
      if (resource.isHealthy()) {
        log.debug("Ending lease of resource: %d resources are now pooled.%n%s", this.pool.size(), this);
        this.pool.add(resource);
        return ;
      }
      this.unhealthyCount.incrementAndGet();
    }
    this.discardResource(resource); // Either the pool is full or the resource is no longer healthy
  }

  @Override
  public String toString() {
    return String.format("Pool with %d resources: %d creations, %d active leases, %d leases have started, %d leases have ended, %d resources have been discarded, %d resources were found unhealthy",
        this.pool.size(), this.createCount.get(), this.startLeaseCount.get() - this.endLeaseCount.get(), this.startLeaseCount.get(), this.endLeaseCount.get(), this.discardCount.get(), this.unhealthyCount.get());
  }
}
