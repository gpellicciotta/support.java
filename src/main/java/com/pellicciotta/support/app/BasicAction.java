// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.app;

import com.pellicciotta.support.config.CommandLine;
import com.pellicciotta.support.utils.CollectionUtils;

public abstract class BasicAction<T extends BasicConfig> {
  public BasicAction(String name, String description) {
    this.name = name;
    this.aliasNames = new String[0];
    this.description = description;
    this.commandLineSyntax = name;
  }

  public BasicAction(String name, String[] aliasNames, String description, String commandLineSyntax) {
    this.name = name;
    this.aliasNames = aliasNames == null ? new String[0] : aliasNames;
    this.description = description;
    this.commandLineSyntax = commandLineSyntax != null ? commandLineSyntax : this.name;
  }

  protected final String name;

  public String getName() {
    return this.name;
  }

  protected final String[] aliasNames;

  public String[] getAliasNames() {
    return this.aliasNames;
  }

  protected final String description;

  public String getDescription() {
    return this.description;
  }

  protected final String commandLineSyntax;

  public String getCommandLineSyntax() {
    return this.commandLineSyntax;
  }

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
