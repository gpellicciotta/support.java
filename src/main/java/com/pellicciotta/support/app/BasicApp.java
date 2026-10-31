// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.app;

import com.pellicciotta.support.annotations.CommandLineAction;
import com.pellicciotta.support.config.CommandLine;
import com.pellicciotta.support.config.ListSetting;
import com.pellicciotta.support.config.Setting;
import com.pellicciotta.support.exceptions.ConfigurationException;
import com.pellicciotta.support.logging.Log;
import com.pellicciotta.support.logging.LogLevel;
import com.pellicciotta.support.logging.LogService;
import com.pellicciotta.support.utils.*;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 *  Basic skeleton for implementing an app that can be started from the command-line.
 */
public abstract class BasicApp<T extends BasicConfig> {
  // <editor-fold desc="Basic Information">

  /**
   * Returns the short, machine-friendly tool name.
   *
   * @return The name.
   */
  public abstract String getName();

  /**
   * Returns the app version.
   *
   * @return The version.
   */
  public abstract Version getVersion();

  /**
   * Returns the human-friendly app name.
   *
   * @return The pretty name.
   */
  public abstract String getPrettyName();

  /**
   * Returns the copyright line shown with the version.
   *
   * @return <code>Copyright (c) Giovanni Pellicciotta</code> unless overridden.
   */
  public String getCopyright() {
    return "Copyright (c) Giovanni Pellicciotta";
  }

  /**
   * Returns the one-line description of the app, shown by the help action.
   *
   * @return The description.
   */
  public abstract String getDescription();

  // </editor-fold>
  // <editor-fold desc="Configuration">

  /**
   * Creates the configuration holding default values, before the command line is applied.
   *
   * @return A new configuration instance.
   */
  protected abstract T getInitialConfiguration();

  /**
   * Returns the log the app uses for its own messages.
   *
   * @return The app log.
   */
  protected abstract Log getAppLog();

  // </editor-fold>
  // <editor-fold desc="Command-line Handling">

  /**
   * Hook for process-level setup, called first by {@link #run(String[])}; does nothing by default.
   * A thrown exception ends the run with {@link ExitCode#CONFIG_ERROR}.
   *
   * @param args The command-line arguments.
   * @throws Exception If initialization fails.
   */
  protected void initializeProcess(String[] args) throws Exception {
    // Do nothing
  }

  /**
   * Hook called after the action has run, even if it failed; does nothing by default.
   */
  protected void finalizeProcess() {
    // Do nothing
  }

  /**
   * Configures the {@link LogService} from the logging section of the configuration.
   *
   * @param config The loaded configuration.
   * @throws Exception If logging cannot be configured.
   */
  protected void configureLogging(T config) throws Exception {
    LogService.configure(config.logging);
  }

  /**
   * Names of the registered actions, in registration order.
   */
  protected final List<String> actionsInOrder = new ArrayList<>();
  /**
   * Registered actions, keyed by action name.
   */
  protected final Map<String, BasicAction<T>> actionsByName = new HashMap<>();

  /**
   * Registers the default actions and then those declared with {@link CommandLineAction}.
   * Subclasses may override to register more.
   */
  protected void initializeApp() {
    this.initializeDefaultActions();
    this.initializeActionsFromAnnotations();
  }

  /**
   * Registers every method of this class annotated with {@link CommandLineAction} as an action.
   *
   * @throws InternalError If an annotated method does not return <code>int</code> or does not take
   *                       (CommandLine, config type, String[]) parameters.
   * @throws ConfigurationException If an action with the same name is already registered.
   */
  protected final void initializeActionsFromAnnotations() {
    for (Method m : ReflectionUtils.listAnnotatedMethods(this.getClass(), CommandLineAction.class)) {
      if (m.getReturnType() != int.class) {
        throw new InternalError("@CommandLineAction annotation applied to method '" + this.getClass() + "." + m.getName() + "' with invalid signature: no int return type");
      }
      Class<?>[] paramTypes = m.getParameterTypes();
      if (paramTypes.length != 3) {
        throw new InternalError("@CommandLineAction annotation applied to method '" + this.getClass() + "." + m.getName() + "' with invalid signature: having " + paramTypes.length + " parameters instead of 3");
      }
      if (!CommandLine.class.isAssignableFrom(paramTypes[0])) {
        throw new InternalError("@CommandLineAction annotation applied to method '" + this.getClass() + "." + m.getName() + "' with invalid signature: having first parameter of type '" + paramTypes[0].getName() + "' which is not assignable to the expected CommandLine type");
      }
      // See also: https://stackoverflow.com/questions/1901164/get-type-of-a-generic-parameter-in-java-with-reflection
      Type configType = ((ParameterizedType)this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
      if (!((Class<?>)configType).isAssignableFrom(paramTypes[1])) {
        throw new InternalError("@CommandLineAction annotation applied to method '" + this.getClass() + "." + m.getName() + "' with invalid signature: having first parameter of type '" + paramTypes[1].getName() + "' which is not assignable to the expected BasicConfig type");
      }
      if (!String[].class.isAssignableFrom(paramTypes[2])) {
        throw new InternalError("@CommandLineAction annotation applied to method '" + this.getClass() + "." + m.getName() + "' with invalid signature: having first parameter of type '" + paramTypes[2].getName() + "' which is not assignable to the expected String[] type");
      }
      CommandLineAction annot = m.getAnnotation(CommandLineAction.class);
      String name = annot.name();
      String description = annot.description();
      String[] aliases = annot.aliases();
      if (aliases == null) {
        aliases = new String[0];
      }
      String cmdLineSyntax = annot.commandLineSyntax();
      if (cmdLineSyntax.isEmpty()) {
        cmdLineSyntax = null;
      }
      final BasicApp app = this;
      final Method method = m;
      method.setAccessible(true);
      final BasicAction<T> action = new BasicAction<>(name, aliases, description, cmdLineSyntax) {
        @Override
        protected int run(CommandLine cli, T config, String[] positionalArguments) {
          try {
            return (int) m.invoke(app, cli, config, positionalArguments);
          }
          catch (InvocationTargetException e) {
            if (e.getCause() instanceof RuntimeException) {
              throw (RuntimeException)e.getCause();
            }
            else {
              throw new RuntimeException(e);
            }
          }
          catch (IllegalAccessException e) {
            throw new RuntimeException(e);
          }
        }
      };
      this.initializeAction(action);
    }
  }

  /**
   * Registers the built-in help, version, list-config and list-config-options actions.
   */
  protected final void initializeDefaultActions() {
    this.initializeAction(this.helpAction);
    this.initializeAction(this.versionAction);
    this.initializeAction(this.listConfigAction);
    this.initializeAction(this.listConfigOptionsAction);
  }

  /**
   * Registers an action under its name, after any already registered actions.
   *
   * @param action The action to register.
   * @throws ConfigurationException If an action with the same name is already registered.
   */
  protected final void initializeAction(BasicAction<T> action) {
    String actionName = action.getName();
    if (this.actionsByName.containsKey(actionName)) {
      throw new ConfigurationException("action", action, "Another action named '%s' is already initialized", actionName);
    }
    this.actionsInOrder.add(actionName);
    this.actionsByName.put(actionName, action);
  }

  /**
   *  Main command-line entry-point to the app.
   *  @param args Command-line arguments. The following are recognized by this base class:
   *              <pre>
   *              Options:
   *                 --config-file=FILE_PATH                    Config file to use.
   *                 --save-config-file=FILE_PATH               File to write final, active, configuration in.
   *                 --verbose                                  Shorthand for --logging.min-level=DEBUG, unless
   *                                                            --logging.min-level is explicitly set too, in which case
   *                                                            the explicit value wins.
   *                 --logging.back-end-type=CLASS_NAME         The fully-qualified class name of logging back-end to be used.
   *                                                            Defaults to <code>com.pellicciotta.support.logging.stdout.StdoutBasedLogBackEnd</code>.
   *                 --logging.min-level=LOG_LEVEL              The min. log level for all non-explicitly configured loggers.
   *                 --logging.include-stack-traces=BOOLEAN     Whether stack-traces should get logged.
   *                 --logging.logger.{number}.name=STRING      Unique logger name.
   *                 --logging.logger.{number}.level=LOG_LEVEL  The min. log level for this named logger.
   *
   *              Where:
   *                 LOG_LEVEL=INTEGER | NONE | ERROR | WARNING | INFO | DEBUG | ALL
   *
   *              Actions:
   *                 version                          Show version info and exit
   *                 help                             Show help info and exit
   *                 list-config                      List active configuration
   *                 list-config-options              List all possible configuration settings
   *              </pre>
   *  @return Process exit code.
   */
  public int run(String[] args) {
    // Process-level initialization (e.g. set JVM flags):
    int exitCode = ExitCode.NORMAL;
    try {
      this.initializeProcess(args);
    }
    catch (Exception e) {
      LogService.error("Failed to initialize process (current directory: '%s'): %s ", EnvironmentUtils.getCurrentDirectory(), e);
      exitCode = ExitCode.CONFIG_ERROR;
      return exitCode;
    }
    // App initialization
    try {
      this.initializeApp();
    }
    catch (Exception e) {
      LogService.error("Failed to initialize application: %s ", e);
      exitCode = ExitCode.CONFIG_ERROR;
      return exitCode;
    }
    // Determine command line provided config:
    T config = this.getInitialConfiguration();
    Log log = this.getAppLog();
    CommandLine cli = null;
    try {
      cli = CommandLine.of(args);
    }
    catch (Exception e) {
      log.error("Invalid command line: %s", e);
      exitCode = ExitCode.CONFIG_ERROR;
      return exitCode;
    }
    Pair<BasicAction<T>, String[]> actionAndPositionalArguments;
    try {
      config.loadFrom(cli);
      this.applyVerboseOption(cli, config);
      actionAndPositionalArguments = this.getMainActionAndPositionalArguments(cli);
    }
    catch (Exception e) {
      log.error("Configuration error: %s", e);
      exitCode = ExitCode.CONFIG_ERROR;
      return exitCode;
    }
    try {
      this.configureLogging(config);
      exitCode = actionAndPositionalArguments.getFirst().run(cli, config, actionAndPositionalArguments.getSecond());
    }
    catch (Exception e) {
      log.error("Unexpected error: %s", e);
      exitCode = ExitCode.PROCESSING_ERROR;
    }
    finally {
      this.finalizeProcess();
    }
    return exitCode;
  }

  /**
   *  Apply the <code>--verbose</code> flag, if present on the command-line, as a shorthand for
   *  <code>--logging.min-level=DEBUG</code> - unless <code>--logging.min-level</code> was itself explicitly
   *  provided on the command-line, in which case that explicit value takes precedence.
   *
   *  @param cli The command-line the config was loaded from.
   *  @param config The configuration to apply the shorthand to.
   */
  protected void applyVerboseOption(CommandLine cli, T config) {
    if (cli.hasFlag("verbose") && !cli.hasPropertyValue("logging.min-level")) {
      config.logging.setMinLevel(LogLevel.DEBUG);
    }
  }

  /**
   * Determines the action to run and its positional arguments from the command line.
   * The first positional argument selects the action by name or alias; if it matches none, the default action
   * is used and all positional arguments are passed to it. Without positional arguments, action flags such as
   * <code>--help</code> are looked up, falling back to the default action.
   *
   * @param cli The parsed command line.
   * @return The chosen action and the arguments to pass to it.
   */
  protected Pair<BasicAction<T>, String[]> getMainActionAndPositionalArguments(CommandLine cli) {
    BasicAction<T> chosenAction = null;
    String[] actionArgs = new String[0];
    String cliProvidedMainAction = cli.getMainArgument();
    if (cliProvidedMainAction == null) {
      chosenAction = this.actionWhenNotExplicitlyChosen(cliProvidedMainAction);
      // But check for actions provided as option too:
    check_action_options:
      for (String actionName : this.actionsInOrder) {
        BasicAction<T> action = this.actionsByName.get(actionName);
        if (cli.hasFlag(action.getName())) {
          chosenAction = action;
          break;
        }
        for (String aliasName : action.getAliasNames()) {
          if (aliasName.startsWith("-")) {
            aliasName = aliasName.replaceAll("^[-]*", "");
            if (cli.hasFlag(aliasName)) {
              chosenAction = action;
              break check_action_options;
            }
          }
          if (aliasName.equals(cliProvidedMainAction)) {
            chosenAction = action;
            break check_action_options;
          }
        }
      }
    }
    else {
      actionArgs = cli.getOtherArguments();
      for (String actionName : this.actionsInOrder) {
        BasicAction<T> action = this.actionsByName.get(actionName);
        if (action.getName().equals(cliProvidedMainAction)) {
          chosenAction = action;
          break;
        }
        for (String aliasName : action.getAliasNames()) {
          if (aliasName.equals(cliProvidedMainAction)) {
            chosenAction = action;
            break;
          }
        }
      }
      if (chosenAction == null) { // So there were positional arguments and the first positional argument is not an action name: this means we should select the first action
        chosenAction = this.actionWhenNotExplicitlyChosen(cliProvidedMainAction);
        actionArgs = cli.getArguments();
      }
    }
    return Pair.of(chosenAction, actionArgs);
  }

  /**
   *  When no main action can be found, either return a default action or throw a {@link ConfigurationException}.
   *
   *  @param cliProvidedMainAction The CLI provided main action, or <code>null</code> if there are no position arguments.
   *  @return The {@link BasicAction} to be used.
   */
  protected BasicAction<T> actionWhenNotExplicitlyChosen(String cliProvidedMainAction) {
    String defaultAction = "help";
    if (!this.actionsInOrder.isEmpty()) {
      defaultAction = this.actionsInOrder.get(0);
    }
    BasicAction<T> chosenAction = this.actionsByName.get(defaultAction);
    return chosenAction;
  }

  /**
   * Runs the registered action with the given name.
   *
   * @param actionName The name of the action.
   * @param cli The parsed command line.
   * @param config The loaded configuration.
   * @param positionalArguments The arguments to pass to the action.
   * @return The action exit code, or {@link ExitCode#CONFIG_ERROR} if no such action exists.
   */
  protected int runAction(String actionName, CommandLine cli, T config, String[] positionalArguments) {
    int exitCode;
    BasicAction<T> action = this.actionsByName.get(actionName);
    if (action == null) {
      this.getAppLog().error("Invalid action '%s' specified", action);
      exitCode = ExitCode.CONFIG_ERROR;
    }
    else {
      exitCode = action.run(cli, config, positionalArguments);
    }
    return exitCode;
  }

  /**
   * Returns the text that prefixes each usage line.
   *
   * @return A <code>java -cp [CLASSPATH] ClassName</code> style prefix.
   */
  protected String getCommandLinePrefix() {
    return String.format("java -cp [CLASSPATH] %s", this.getClass().getName());
  }

  /**
   * Prints a usage line and its indented description to standard output.
   *
   * @param cmdLineSyntax The usage syntax.
   * @param description The description printed below it.
   */
  protected void addUsageSyntax(String cmdLineSyntax, String description) {
    System.out.printf("%s %s%n  %s%n", this.getCommandLinePrefix(), cmdLineSyntax, description);
  }

  /**
   * Prints the usage syntax, aliases (if any) and description of an action to standard output.
   *
   * @param prefix Text printed before the first line, such as <code>Usage: </code>; continuation lines align to it.
   * @param action The action to describe.
   */
  protected void addUsageSyntax(String prefix, BasicAction<T> action) {
    String[] aliases = action.getAliasNames();
    String firstLinePrefix = prefix;
    String continueLinePrefix = StringUtils.makeSpacesString(firstLinePrefix.length());
    if (aliases.length == 0) {
      System.out.printf("%s%s %s%n%s  %s%n", firstLinePrefix, this.getCommandLinePrefix(), action.getCommandLineSyntax(), continueLinePrefix, action.getDescription());
    }
    else {
      System.out.printf("%s%s %s%n%s  Aliases: %s%n%s  %s%n", firstLinePrefix, this.getCommandLinePrefix(), action.getCommandLineSyntax(), continueLinePrefix, CollectionUtils.toString(aliases, "", ", ", ""), continueLinePrefix, action.getDescription());
    }
  }

  /**
   * The built-in <code>help</code> action: shows version and description, and usage with <code>usage</code>.
   */
  protected final BasicAction<T> helpAction = new BasicAction<T>("help", new String[] { "--help", "-h", "-?", "usage", "--usage", "-u" }, "Show help info and exit", "help [--usage]") {
    @Override
    protected int run(CommandLine cli, T config, String[] positionalArguments) {
      boolean showUsageToo = (cli.hasFlag("usage") || cli.hasFlag("u") || cli.hasPropertyValue("usage") || CollectionUtils.contains(cli.getArguments(), "usage"));
      return runInfoAction(true, showUsageToo);
    }
  };

  /**
   *  Show version and, optionally, description and usage info, in the standard
   *  <code>{name} v{version} - {copyright}</code> format.
   *
   *  @param showDescription Whether to also print {@link #getDescription()}.
   *  @param showUsage Whether to also print the command-line usage syntax for every available action.
   *  @return {@link ExitCode#NORMAL}.
   */
  protected int runInfoAction(boolean showDescription, boolean showUsage) {
    System.out.printf("%s v%s - %s%n", this.getName(), this.getVersion(), this.getCopyright());
    if (showDescription) {
      System.out.printf("%n%s%n", this.getDescription());
    }
    if (showUsage) {
      System.out.println();
      boolean first = true;
      for (String an : this.actionsInOrder) {
        BasicAction<T> action = this.actionsByName.get(an);
        this.addUsageSyntax(first ? "Usage: " : "   or: ", action);
        first = false;
      }
      System.out.println();
      System.out.println("Options:");
      System.out.println("  --verbose    Shorthand for --logging.min-level=DEBUG");
    }

    return ExitCode.NORMAL;
  }

  /**
   * The built-in <code>version</code> action: shows the name, version and copyright.
   */
  protected final BasicAction<T> versionAction = new BasicAction<T>("version", new String[] { "--version", "-v" }, "Show version and exit", "version") {
    @Override
    protected int run(CommandLine cli, T config, String[] positionalArguments) {
      return runInfoAction(false, false);
    }
  };

  private static void addRow(List<String[]> rows, String... colVals) {
    rows.add(colVals);
  }

  /**
   * The built-in <code>list-config</code> action: lists the active configuration.
   */
  protected final BasicAction<T> listConfigAction = new BasicAction<T>("list-config", new String[0], "List active configuration options", "list-config [--include-environment]") {
    @Override
    protected int run(CommandLine cli, T config, String[] positionalArguments) {
      return runListConfigAction(cli, config, positionalArguments);
    }
  };

  /**
   * Prints application info, the command line and all active settings, plus system properties and
   * environment variables when <code>--include-environment</code> is a positional argument.
   *
   * @param cli The parsed command line.
   * @param config The loaded configuration.
   * @param positionalArguments The action arguments.
   * @return {@link ExitCode#NORMAL}.
   */
  protected int runListConfigAction(CommandLine cli, T config, String[] positionalArguments) {
    List<String[]> lines = new ArrayList<>();
    addRow(lines, "Application Info");
    addRow(lines, "  Simple Name", this.getName());
    addRow(lines, "  Full Name", this.getPrettyName());
    addRow(lines, "  Version", this.getDescription());
    addRow(lines, "Current Java runtime", System.getProperty("java.version"));
    addRow(lines, "Current directory", System.getProperty("user.dir"));
    addRow(lines, "Command line", cli.toString());
    addRow(lines, "Configuration file", cli.getLastPropertyValue("config-file", "(none)"));
    addRow(lines, "Active settings", "");
    lines.addAll(this.getActiveSettings(config, "  "));
    if (CollectionUtils.contains(positionalArguments, "--include-environment")) {
      addRow(lines, "System properties & environment variables", "");
      EnvironmentUtils.forEachProperty((name, value) -> {
        addRow(lines, "  " + name, "" + value);
      });
    }
    for (String line : StringUtils.makeTableLines(lines)) {
      System.out.println(line);
    }
    return ExitCode.NORMAL;
  }

  private List<String[]> getActiveSettings(T config, String prefix) {
    SortedSet<String> sortedSettingNames = new TreeSet<String>(config.elementarySettingNames());
    List<String[]> optionLines = new ArrayList<>();
    for (String settingName : sortedSettingNames) {
      String[] row = new String[2];
      Setting setting = config.getSetting(settingName);
      row[0] = prefix + settingName;
      if (setting instanceof ListSetting) {
        ListSetting<Object> listSetting = (ListSetting)setting;
        String valStr = setting.isValueSet() ? "" : " (default)";
        List<?> val = (List<?>)setting.getValueOrDefault();
        row[1] = (val != null ? "(" + val.size() + " values)" : "(none)");
        optionLines.add(row);
        int i = 1;
        String elPrefix = prefix;
        for (Object el : val) {
          String[] elRow = new String[2];
          elRow[0] = elPrefix + "  #" + i;
          //String[] propLines = StringUtils.makePairTableLines(listSetting.toElementProps(el).entrySet().stream().map((e) -> Pair.of(e.getKey(), e.getValue())).collect(Collectors.toList()));
          elRow[1] = listSetting.toElementString(el);
          //elRow[1] = String.join("\n", propLines);
          optionLines.add(elRow);
          i += 1;
        }
      }
      else {
        String valStr = setting.isValueSet() ? "" : " (default)";
        Object val = setting.getValueOrDefault();
        row[1] = (val != null ? setting.toString(val) + valStr : "(none)");
        optionLines.add(row);
      }
    }
    return optionLines;
  }

  /**
   * The built-in <code>list-config-options</code> action: lists all settings with descriptions and defaults.
   */
  protected final BasicAction<T> listConfigOptionsAction = new BasicAction<T>("list-config-options", new String[0], "List all available configuration options", "list-config-options") {
    @Override
    protected int run(CommandLine cli, T config, String[] positionalArguments) {
      return runListConfigOptionsAction(config);
    }
  };

  /**
   * Prints every setting with its description and default value to standard output.
   *
   * @param config The configuration whose settings are listed.
   * @return {@link ExitCode#NORMAL}.
   */
  protected int runListConfigOptionsAction(T config) {
    SortedSet<String> sortedSettingNames = new TreeSet<String>(config.elementarySettingNames());
    for (String settingName : sortedSettingNames) {
      Setting setting = config.getSetting(settingName);
      Object val = setting.getDefaultValue();
      String defValStr = (val != null ? setting.toString(val) : "(none)");
      System.out.printf("  %s%n    %s%n    Default: %s%n", setting.getFullName(), setting.getDescription(), defValStr);
    }
    return ExitCode.NORMAL;
  }

  /**
   * Builds a table of all settings (name, default value, description), sorted by name, with a header row.
   *
   * @param config The configuration whose settings are listed.
   * @param prefix Text prepended to each setting name.
   * @return The table rows.
   */
  protected List<String[]> getAvailableSettings(T config, String prefix) {
    SortedSet<String> sortedSettingNames = new TreeSet<String>(config.elementarySettingNames());
    List<String[]> optionLines = new ArrayList<>();
    optionLines.add(new String[] { "Name", "Default Value", "Description" });
    for (String settingName : sortedSettingNames) {
      String[] row = new String[3];
      Setting setting = config.getSetting(settingName);
      row[0] = prefix + settingName;
      Object val = setting.getDefaultValue();
      row[1] = (val != null ? setting.toString(val) : "(none)");
      row[2] = setting.getDescription();
      optionLines.add(row);
    }
    return optionLines;
  }

  // </editor-fold>
}
