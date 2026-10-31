// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.tools;

import java.lang.reflect.*;
import java.util.*;

import com.pellicciotta.support.Info;
import com.pellicciotta.support.app.BasicApp;
import com.pellicciotta.support.app.ExitCode;
import com.pellicciotta.support.text.TextBuilder;
import com.pellicciotta.support.utils.ReflectionUtils;
import com.pellicciotta.support.utils.StringUtils;

/**
 *  Start a command-line tool by matching its name to its implementing code.
 */
public final class ToolDispatcher
{
  private static class Tool {
    public Tool(Class<? extends BasicApp> appClass) throws Exception {
      this.toolClass = appClass;

      Constructor<? extends BasicApp> ctor = this.toolClass.getDeclaredConstructor();
      this.tool = ctor.newInstance();

      this.name = this.tool.getName();
      this.version = this.tool.getVersion().getVersionString();
      this.description = this.tool.getDescription();

      this.toolRunMethod = this.toolClass.getMethod("run", new Class[]{ String[].class });
      this.toolRunMethod.setAccessible(true);
    }

    private String getPrivateStaticFieldValue(Class<?> appClass, String fieldName) {
      return "";
    }

    public final String name;
    public final String version;
    public final String description;
    public final Class<? extends BasicApp> toolClass;
    public final BasicApp tool;
    public final Method toolRunMethod;

    public int run(String[] cliArgs) throws Exception {
      int exitCode = (int)this.toolRunMethod.invoke(this.tool, new Object[] {  cliArgs });
      return exitCode;
    }
  }

  private static final List<Tool> tools = new ArrayList<>();
  private static final Map<String, Tool> toolsByName = new HashMap<>();

  static void determineAvailableTools() {
    try {
      for (Class<?> toolClass : ReflectionUtils.listClassesInPackage(ToolDispatcher.class.getPackage().getName(), true)) {
        if (!BasicApp.class.isAssignableFrom(toolClass)) {
          continue;
        }
        try {
          Tool tool = new Tool((Class<? extends BasicApp>)toolClass);
          tools.add(tool);
          toolsByName.put(tool.name, tool);
        }
        catch (NoSuchMethodException nme) { /* Ignore */ }
      }
    }
    catch (Exception e) {
      System.out.printf("Failed to determine available tools: %s\n", e);
    }
  }

  /**
   *  Main CLI entry-point.
   *
   *  @param args The arguments as provided on the command line.
   */
  public static void main(String[] args) {
    ToolDispatcher.determineAvailableTools();
    if ((args.length == 0) || ("--list-tools".equals(args[0]) || "list-tools".equals(args[0]))) {
      ToolDispatcher.listTools();
    }
    else if ((args.length == 1) && valueIsOneOf(args[0], "version", "--version", "-v")) {
      emitVersion();
      System.exit(0);
    }
    else if ((args.length == 1) && valueIsOneOf(args[0], "help", "--help", "-h", "-?", "usage", "--usage", "-u")) {
      emitHelp();
      System.exit(0);
    }
    else { // Assume a tool invocation
      String toolName = args[0];
      String[] toolArgs = new String[args.length - 1];
      System.arraycopy(args, 1, toolArgs, 0, args.length - 1);
      ToolDispatcher.runTool(toolName, toolArgs);
    }
  }

  private static boolean valueIsOneOf(Object value, Object... validValues) {
    if ((validValues == null) || (validValues.length == 0)) {
      return false;
    }
    for (Object vv : Arrays.asList(validValues)) {
      if (vv.equals(value)) {
        return true;
      }
    }
    return false;
  }


  /**
   * Select the right tool and then run it.
   *
   * @param toolName The name of the tool to be started.
   * @param cliArgs The command line arguments to be passed to the tool.
   */
  public static void runTool(String toolName, String... cliArgs) {
    Tool tool = toolsByName.get(toolName);
    if (tool == null) {
      System.out.printf("There is no tool available with name '%s'\n", toolName);
      System.exit(ExitCode.CONFIG_ERROR);
    }
    try {
      tool.run(cliArgs);
    }
    catch (Exception e) {
      System.out.printf("Failed to start tool with name '%s': %s\n", toolName, e);
      System.exit(ExitCode.CONFIG_ERROR);
    }
  }

  /**
   * Print the application's command help information.
   */
  public static void emitHelp() {
    TextBuilder tb = new TextBuilder();
    tb.appendLine("%s v%s - %s", Info.NAME, Info.VERSION, Info.COPYRIGHT);
    tb.appendLine("");
    tb.appendLine("%s", Info.DESCRIPTION);
    tb.appendLine("");
    tb.appendLine("Usage: java -jar support.java.jar --help");
    tb.appendLine("         Show this help and exit");
    tb.appendLine("   or: java -jar support.java.jar --version");
    tb.appendLine("         Show version info and exit");
    tb.appendLine("   or: java -jar support.java.jar --list-tools");
    tb.appendLine("         Show all available command-line tools and exit");
    tb.appendLine("   or: java -jar support.java.jar <tool-name> [tool-options]");
    tb.appendLine("         Start a specific command-line tool");
    tb.appendLine("   or: java -jar support.java.jar <tool-name> --usage");
    tb.appendLine("         Show the command-line usage of a specific tool and exit");
    tb.appendLine("   or: java -jar support.java.jar <tool-name> --verbose [tool-options]");
    tb.appendLine("         Start a specific command-line tool with verbose (DEBUG-level) logging");
    tb.appendLine("");
    for (String line : StringUtils.split(tb.build())) {
      System.out.println(line);
    }
  }

  /**
   * Print the application's version information.
   */
  public static void emitVersion() {
    System.out.printf("%s v%s - %s%n", Info.NAME, Info.VERSION, Info.COPYRIGHT);
  }

  /**
   *  List the name of the available tools.
   */
  public static void listTools() {
    TextBuilder tb = new TextBuilder();
    if (tools.isEmpty()) {
      tb.appendLine("No tools are available");
    }
    else {
      boolean first = true;
      tb.appendLine("Following tools are available:");
      for (Tool tool : tools) {
        if (first) {
          first = false;
        }
        else {
          tb.appendLine();
        }
        tb.appendLine("  %s", tool.name);
        tb.appendLine("    %s", tool.description);
      }
    }
    for (String line : StringUtils.split(tb.build())) {
      System.out.println(line);
    }
  }
}
