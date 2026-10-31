// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.logging.spi;

/**
 *  Enumeration of how context data items can get associated,
 *  which translates to where and when they will have effect.
 */
public enum ContextDataItemScope {
  /**
   *  Association with a particular Log instance.
   *
   *  A ContextDataItem i that was associated via Log.addContextDataItem(ContextType.LOG_INSTANCE, item)
   *  will be automatically appended to all Log.[log|error|warning|info|debug] method invocations
   *  on that Log instance only.
   *
   *  This context is typically used for associating data items representing a session ID
   *  or other data for which log method invocations will happen only from within a particular
   *  class or method.
   *
   *  This is the most specific scope.
   */
  LOG_INSTANCE,

  /**
   *  Association with a particular thread.
   *
   *  A ContextDataItem that was associated via Log.addContextDataItem(ContextType.THREAD_INSTANCE, item)
   *  will be automatically appended to all Log.[log|error|warning|info|debug] method invocations
   *  from the particular Thread on which the addContextDataItem invocation was done.
   *
   *  This context is typically used for associating data items representing a session ID
   *  or other data for which log method invocations will happen from within multiple classes and methods,
   *  but always restricted to a single thread.
   */
  THREAD_INSTANCE,

  /**
   *  Association with a particular log name.
   *
   *  A ContextDataItem i that was associated via Log.addContextDataItem(ContextType.LOG_NAME, item)
   *  will be automatically appended to all Log.[log|error|warning|info|debug] method invocations
   *  on Log instances that have the same name as the Log instance's name on which the
   *  addContextDataItem invocation happened.
   *
   *  This context is typically used for associating data items representing a session ID
   *  or other data for which log method invocations will happen from multiple classes,
   *  typically during a specific time period and possibly from multiple threads.
   *
   *  This is the least specific scope.
   */
  LOG_NAME
}
