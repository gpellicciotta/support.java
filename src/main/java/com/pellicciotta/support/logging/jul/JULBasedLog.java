// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.logging.jul;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.*;

import com.pellicciotta.support.logging.*;
import com.pellicciotta.support.logging.spi.ContextData;
import com.pellicciotta.support.logging.spi.ContextDataItemScope;
import com.pellicciotta.support.utils.Contract;

/**
 *  Adapts a standard {@link Logger} object to the {@link Log} interface,
 *  while taking care of {@link ContextDataItem} storage and management too.
 */
public final class JULBasedLog implements Log {
  // [Support Types]

  private class StandardContextDataItem implements ContextDataItem {

    public StandardContextDataItem(String name, Object value) {
      assert name != null;
      this.name = name;
      this.value = value;
      this.disposed = false;
    }

    private boolean disposed;

    private final String name;

    @Override
    public String getName() {
      if (this.disposed) {
        throw new IllegalStateException("The data item has been disassociated");
      }
      return this.name;
    }

    private final Object value;

    @Override
    public Object getValue() {
      if (this.disposed) {
        throw new IllegalStateException("The data item has been disassociated");
      }
      return this.value;
    }

    @Override
    public boolean isClosed() {
      return this.disposed;
    }

    @Override
    public void close() {
      if (this.disposed) {
        throw new IllegalStateException("The data item has been disassociated");
      }
      JULBasedLog.this.removeContextData(null, this.name);
    }
  }

  // [Log name and Thread specific context data stores]

  private static final Map<String, Map<String, ContextDataItem>> logNameContextData = new HashMap<>();

  private static final ThreadLocal<Map<String, ContextDataItem>> threadInstanceContextData = new ThreadLocal<Map<String, ContextDataItem>>() {

    @Override
    protected Map<String, ContextDataItem> initialValue() {
      return new HashMap<>();
    }
  };

  // [Construction]

  /**
   *  Create a new Log object that is a wrapper around the native Java Logger object associated with the given name.
   *
   *  @param julLoggerName The actual log name to get a Logger object for, and wrap it.
   */
  public JULBasedLog(String julLoggerName) {
    this(Logger.getLogger(julLoggerName));
  }

  /**
   *  Create a new Log object that is a wrapper around the native Java Logger object.
   *
   *  @param wrappedLogger The actual logger to be used when emitting log messages.
   */
  public JULBasedLog(Logger wrappedLogger) {
    Contract.isNotNull("wrappedLogger", wrappedLogger);
    this.logger = wrappedLogger;
    this.logInstanceContextData = new HashMap<>();
    this.closed = false;
  }

  private boolean closed;
  private Logger logger;

  /**
   *  @return The standard Java logger being wrapped. Null if this Log has been closed.
   */
  public Logger getJULLogger() {
    return this.logger;
  }

  /**
   * @return The name of the wrapped JUL logger.
   * @throws IllegalStateException If this log has been closed.
   */
  public String getName() {
    if (this.closed) {
      throw new IllegalStateException("This log has been closed");
    }
    return this.logger.getName();
  }

  /**
   *  Whether this log has been closed. If true, it can no longer be used in any sensible way.
   *
   *  @return Whether currently closed (which will be true once the close method gets called).
   */
  public boolean isClosed() {
    return this.closed;
  }

  /**
   *  Translate feedback request into a LogRecord and then call the underlying Logger.
   */
  private void log(Level level, String messageOrFormat, Object... args) {
    if (this.closed) {
      throw new IllegalStateException("This log has been closed");
    }
    // Check whether logging at this level is enabled
    if (!this.logger.isLoggable(level)) {
      return;
    }
    // Build message
    String msg = messageOrFormat;
    // Build LogRecord
    LogRecord lr = new LogRecord(level, msg);
    lr.setLoggerName(this.getName());
    if (args.length > 0) {
      // Check for last exception argument
      for (int i = args.length - 1; i >= 0; i--) {
        Object currentArg = args[i];
        if (currentArg instanceof Throwable) {
          lr.setThrown((Throwable) currentArg);
          break;
        }
      }
    }
    // Get caller's location + add to context data
    StackTraceElement callerLocation = getCallerStackTrace();
    if (callerLocation != null) {
      lr.setSourceClassName(callerLocation.getClassName());
      this.addContextData(ContextDataItemScope.LOG_INSTANCE, "origin-class", callerLocation.getClassName());
      lr.setSourceMethodName(callerLocation.getMethodName());
      this.addContextData(ContextDataItemScope.LOG_INSTANCE, "origin-method", callerLocation.getMethodName());
      String source = callerLocation.getFileName();
      if (source != null) {
        this.addContextData(ContextDataItemScope.LOG_INSTANCE, "origin-file", source);
      }
      int line = callerLocation.getLineNumber();
      if (line >= 0) {
        this.addContextData(ContextDataItemScope.LOG_INSTANCE, "origin-line", line);
      }
    }
    // Check for context data + add as extra parameter
    ContextData contextData = this.getContextData(null);
    // Add context data
    if (!contextData.isEmpty()) {
      Object[] actualData = new Object[args.length + 1];
      if (args.length > 0) {
        System.arraycopy(args, 0, actualData, 0, args.length);
      }
      actualData[actualData.length - 1] = contextData;
      args = actualData;
    }
    lr.setParameters(args);
    // Pass off to actual logger
    this.logger.log(lr);
  }

  private static StackTraceElement getCallerStackTrace() {
    Throwable t = new Throwable();
    t.fillInStackTrace();
    StackTraceElement[] stackTrace = t.getStackTrace();
    for (int i = 0; i < stackTrace.length; i++) {
      String className = stackTrace[i].getClassName();
      if (className.equals(Logger.class.getName())) {
        continue;
      }
      if (className.equals(JULBasedLog.class.getName())) {
        continue;
      }
      if (className.equals(Log.class.getName())) {
        continue;
      }
      if (className.equals(LogService.class.getName())) {
        continue;
      }
      return stackTrace[i];
    }
    return null;
  }

  // [Log Interface Implementation]

  private final Map<String, ContextDataItem> logInstanceContextData;

  /**
   * Collects the context data visible to this log, merged from least to most specific scope (log name, thread, log
   * instance) so that more specific items override less specific ones with the same name.
   *
   * @param scope The scope to read; if <code>null</code>, all scopes are merged.
   * @return A new snapshot of the matching context data.
   * @throws IllegalStateException If this log has been closed.
   */
  public ContextData getContextData(ContextDataItemScope scope) {
    if (this.closed) {
      throw new IllegalStateException("This log has been closed");
    }
    Map<String, ContextDataItem> contextData = new HashMap<>();
    synchronized (JULBasedLog.class) {
      Map<String, ContextDataItem> items;
      // Note: order is important here; adding from least to most specific scope
      if ((scope == null) || (scope == ContextDataItemScope.LOG_NAME)) {
        items = JULBasedLog.logNameContextData.get(this.getName());
        if (items != null) {
          contextData.putAll(items);
        }
      }
      if ((scope == null) || (scope == ContextDataItemScope.THREAD_INSTANCE)) {
        items = JULBasedLog.threadInstanceContextData.get();
        if (items != null) {
          contextData.putAll(items);
        }
      }
      if ((scope == null) || (scope == ContextDataItemScope.LOG_INSTANCE)) {
        items = this.logInstanceContextData;
        if (items != null) {
          contextData.putAll(items);
        }
      }
    }
    return new ContextData(contextData.values());
  }


  @Override
  public ContextDataItem add(String itemName, Object itemValue) {
    return this.addContextData(ContextDataItemScope.LOG_INSTANCE, itemName, itemValue);
  }

  /**
   * Adds or replaces a context data item in the given scope.
   *
   * @param scope The scope to store the item in; if <code>null</code>, {@link ContextDataItemScope#LOG_INSTANCE}.
   * @param name The item name; must not be <code>null</code>.
   * @param value The item value.
   * @return The item that was stored.
   * @throws IllegalStateException If this log has been closed.
   */
  public ContextDataItem addContextData(ContextDataItemScope scope, String name, Object value) {
    Contract.isNotNull("name", name);
    if (this.closed) {
      throw new IllegalStateException("This log has been closed");
    }
    if (scope == null) {
      scope = ContextDataItemScope.LOG_INSTANCE;
    }
    ContextDataItem item = new StandardContextDataItem(name, value);
    synchronized (JULBasedLog.class) {
      Map<String, ContextDataItem> items = null;
      switch (scope) {
        case LOG_NAME:
          String logName = this.getName();
          items = JULBasedLog.logNameContextData.get(logName);
          if (items == null) {
            items = new HashMap<>();
            JULBasedLog.logNameContextData.put(logName, items);
          }
          break;
        case THREAD_INSTANCE:
          items = JULBasedLog.threadInstanceContextData.get();
          break;
        case LOG_INSTANCE:
        default:
          items = this.logInstanceContextData;
          break;
      }
      if (items != null) {
        items.put(name, item);
      }
      return item;
    }
  }

  /**
   * Removes the named context data item from the given scope.
   *
   * @param scope The scope to remove from; if <code>null</code>, the item is removed from all scopes.
   * @param name The item name; must not be <code>null</code>.
   * @throws IllegalStateException If this log has been closed.
   */
  public void removeContextData(ContextDataItemScope scope, String name) {
    Contract.isNotNull("name", name);
    if (this.closed) {
      throw new IllegalStateException("This log has been closed");
    }
    synchronized (JULBasedLog.class) {
      if ((scope == null) || (scope == ContextDataItemScope.LOG_NAME)) {
        Map<String, ContextDataItem> items = JULBasedLog.logNameContextData.get(this.getName());
        if (items != null) {
          items.remove(name);
        }
      }
      if ((scope == null) || (scope == ContextDataItemScope.THREAD_INSTANCE)) {
        JULBasedLog.threadInstanceContextData.get().remove(name);
      }
      if ((scope == null) || (scope == ContextDataItemScope.LOG_INSTANCE)) {
        if (this.logger == null) {
          throw new IllegalStateException("This log has been closed");
        }
        this.logInstanceContextData.remove(name);
      }
    }
  }

  /**
   * Removes all context data items from the given scope.
   *
   * @param scope The scope to clear; if <code>null</code>, all scopes are cleared.
   * @throws IllegalStateException If this log has been closed.
   */
  public void clearContextData(ContextDataItemScope scope) {
    if (this.closed) {
      throw new IllegalStateException("This log has been closed");
    }
    synchronized (JULBasedLog.class) {
      if ((scope == null) || (scope == ContextDataItemScope.LOG_NAME)) {
        Map<String, ContextDataItem> items = JULBasedLog.logNameContextData.get(this.getName());
        if (items != null) {
          items.clear();
        }
      }
      if ((scope == null) || (scope == ContextDataItemScope.THREAD_INSTANCE)) {
        JULBasedLog.threadInstanceContextData.get().clear();
      }
      if ((scope == null) || (scope == ContextDataItemScope.LOG_INSTANCE)) {
        this.logInstanceContextData.clear();
      }
    }
  }

  /**
   * @param level A {@link LogLevel} constant, mapped to the nearest JUL level.
   * @return True when the wrapped JUL logger would log at that level.
   * @throws IllegalStateException If this log has been closed.
   */
  public boolean isLevelEnabled(int level) {
    if (this.closed) {
      throw new IllegalStateException("This log has been closed");
    }
    return this.logger.isLoggable(JULUtils.toNearestLevel(level));
  }

  /**
   * Logs a message at the JUL level nearest to the given {@link LogLevel} constant.
   *
   * @param level A {@link LogLevel} constant.
   * @param messageOrFormat The message, or a format string for the parameters.
   * @param messageParamsOrData Format parameters and/or data to attach to the event.
   */
  public void log(int level, String messageOrFormat, Object... messageParamsOrData) {
    this.log(JULUtils.toNearestLevel(level), messageOrFormat, messageParamsOrData);
  }

  @Override
  public boolean isErrorEnabled() {
    return this.logger.isLoggable(Level.SEVERE);
  }

  @Override
  public boolean isWarningEnabled() {
    return this.logger.isLoggable(Level.WARNING);
  }

  @Override
  public boolean isInfoEnabled() {
    return this.logger.isLoggable(Level.INFO);
  }

  @Override
  public boolean isDebugEnabled() {
    return this.logger.isLoggable(Level.FINE);
  }

  @Override
  public void error(String messageOrFormat, Object... messageParamsOrData) {
    this.log(Level.SEVERE, messageOrFormat, messageParamsOrData);
  }

  @Override
  public void warning(String messageOrFormat, Object... messageParamsOrData) {
    this.log(Level.WARNING, messageOrFormat, messageParamsOrData);
  }

  @Override
  public void info(String messageOrFormat, Object... messageParamsOrData) {
    this.log(Level.INFO, messageOrFormat, messageParamsOrData);
  }

  @Override
  public void debug(String messageOrFormat, Object... messageParamsOrData) {
    this.log(Level.FINE, messageOrFormat, messageParamsOrData);
  }

  /**
   * Flushes all handlers attached directly to the wrapped JUL logger.
   *
   * @throws IllegalStateException If this log has been closed.
   */
  public void flush() {
    if (this.closed) {
      throw new IllegalStateException("This log has been closed");
    }
    for (Handler h : this.logger.getHandlers()) {
      h.flush();
    }
  }

  @Override
  public Span start(String spanName) {
    return null;
  }

  /**
   *  Get rid of Log specific context data only and mark this Log instance as closed/unuseable.
   */
  public void close() {
    if (this.closed) {
      return;
    }
    this.flush();
    this.closed = true; // Passed this point we should be considered unuseable/closed.
    this.logger = null;
    this.logInstanceContextData.clear();
  }
}
