// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.app;

import com.pellicciotta.support.config.CommandLine;
import com.pellicciotta.support.utils.CollectionUtils;

/**
 * A named command-line action of a {@link BasicApp}, with optional aliases, a description and a usage syntax.
 * Subclasses implement {@link #run(CommandLine, BasicConfig, String[])}.
 *
 * @param <T> The configuration type of the owning app.
 */
public abstract class BasicAction<T extends BasicConfig> {
  /**
   * Creates an action without aliases whose command-line syntax is just its name.
   *
   * @param name The action name, as typed on the command line.
   * @param description A one-line description shown in the usage output.
   */
  public BasicAction(String name, String description) {
    this.name = name;
    this.aliasNames = new String[0];
    this.description = description;
    this.commandLineSyntax = name;
  }

  /**
   * Creates an action.
   *
   * @param name The action name, as typed on the command line.
   * @param aliasNames Alternative names; <code>null</code> is treated as no aliases.
   * @param description A one-line description shown in the usage output.
   * @param commandLineSyntax The usage syntax; <code>null</code> falls back to the action name.
   */
  public BasicAction(String name, String[] aliasNames, String description, String commandLineSyntax) {
    this.name = name;
    this.aliasNames = aliasNames == null ? new String[0] : aliasNames;
    this.description = description;
    this.commandLineSyntax = commandLineSyntax != null ? commandLineSyntax : this.name;
  }

  /**
   * The action name.
   */
  protected final String name;

  /**
   * Returns the action name.
   *
   * @return The name this action is invoked by.
   */
  public String getName() {
    return this.name;
  }

  /**
   * Alternative names for the action, never <code>null</code>.
   */
  protected final String[] aliasNames;

  /**
   * Returns the alternative names for this action.
   *
   * @return The aliases; empty if none.
   */
  public String[] getAliasNames() {
    return this.aliasNames;
  }

  /**
   * The one-line description of the action.
   */
  protected final String description;

  /**
   * Returns the one-line description shown in the usage output.
   *
   * @return The description.
   */
  public String getDescription() {
    return this.description;
  }

  /**
   * The usage syntax of the action, never <code>null</code>.
   */
  protected final String commandLineSyntax;

  /**
   * Returns the usage syntax shown in the usage output.
   *
   * @return The syntax; the action name if none was given.
   */
  public String getCommandLineSyntax() {
    return this.commandLineSyntax;
  }

  /**
   * Executes the action.
   *
   * @param cli The parsed command line.
   * @param config The loaded app configuration.
   * @param positionalArguments The positional arguments that follow the action name.
   * @return The process exit code.
   */
  protected abstract int run(CommandLine cli, T config, String[] positionalArguments);

  @Override
  public String toString() {
    if (this.aliasNames.length > 0) {
      return String.format("%s%n  Aliases: %s%n  %s", this.commandLineSyntax, CollectionUtils.toString(this.aliasNames), this.description);
    }
    else {
      return String.format("%s%n  %s", this.commandLineSyntax, this.description);
    }
  }
}
