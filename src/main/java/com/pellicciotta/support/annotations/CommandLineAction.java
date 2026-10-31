// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.annotations;

import java.lang.annotation.*;

/**
 *  Mark a method as defining a command-line action.
 *  The method must have the following signature: <code>int xxx(CommandLine cli, BasicConfig config, String[] positionalArguments)</code>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
public @interface CommandLineAction
{
  /** The simple name of the command-line action. */
  String name();

  /** Aliases for the main command-line action name. */
  String[] aliases() default {};

  /** The full, human-readable, multi-line description for this command-line action. */
  String description();

  /** A short command-line syntax summary, which should start with the command-line action name. */
  String commandLineSyntax() default "";
}
