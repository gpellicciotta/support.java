// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.config;

import java.text.ParseException;
import java.util.*;

import com.pellicciotta.support.utils.CollectionUtils;
import com.pellicciotta.support.utils.StringConversionUtils;

/**
 *  A command-line consists of three parts:<ul>
 *    <li>Flags, which are names preceded by two dashes.</li>
 *    <li>Properties, which are names also preceded by two dashes, but followed by an equal sign and a value.</li>
 *    <li>Arguments, which are the ordered list of all other names or any names after a standalone -- (i.e. double-dash).
 *        The first such position argument is often the main action of a tool.</li>
 *  </ul>
 *  Example:
 *  <pre>
 *    some-tool.exe --verbose --log-file=/usr/var/some.log start-server --port=8080 foo bar
 *
 *         1 flag: 'verbose'
 *   2 properties: 'log-file' with value '/user/var/some.log'
 *                 'port'     with value '8080'
 *    3 arguments: 'start-server', 'foo' and 'bar'
 *  </pre>
 *  Some typical features of command lines are explicitly <em>not supported</em>:<ul>
 *    <li>Using short-option names preceded by a single dash: in this impl. these will be treated as arguments.</li>
 *    <li>Using <code>:</code> as name and value separator.</li>
 *  </ul>
 */
public class CommandLine {
  // <editor-fold desc="Factory Methods">

  /**
   * Create a command-line from the provided CLI arguments array.
   *
   * @param commandLine The command line to be parsed.
   * @return The parsed command-line.
   *
   * @throws java.text.ParseException If parsing fails.
   */
  public static CommandLine of(String commandLine) throws ParseException {
    CommandLineParser clip = new CommandLineParser();
    return clip.parse(commandLine);
  }

  /**
   * Create a command-line from the provided CLI arguments array.
   *
   * @param cliArgs The command line arguments to be parsed.
   * @return The parsed command-line.
   *
   * @throws java.text.ParseException If parsing fails.
   */
  public static CommandLine of(String[] cliArgs) throws ParseException {
    CommandLineParser clip = new CommandLineParser();
    return clip.parse(cliArgs);
  }

  /**
   * Create a command-line from the provided CLI arguments array.
   *
   * @param cliArgs The command line arguments to be parsed.
   * @return The parsed command-line.
   *
   * @throws java.text.ParseException If parsing fails.
   */
  public static CommandLine of(List<String> cliArgs) throws ParseException {
    CommandLineParser clip = new CommandLineParser();
    return clip.parse(cliArgs);
  }

  // </editor-fold>
  // <editor-fold desc="Construction">

  /** Creates a command line from the given flags, properties and arguments, copying all three. */
  public CommandLine(Set<String> flags, Map<String, List<String>> properties, List<String> arguments) {
    this.flags = new HashSet<>(flags);
    this.properties = new HashMap<>(properties);
    this.arguments = new ArrayList(arguments);
  }

  /** Creates a command line from the given flags, properties and arguments, copying all three. */
  public CommandLine(String[] flags, Map<String, List<String>> properties, String[] arguments) {
    this.flags = CollectionUtils.asSet(flags);
    this.properties = new HashMap<>(properties);
    this.arguments = CollectionUtils.asList(arguments);
  }

  private final Set<String> flags;
  private final Map<String, List<String>> properties;
  private final List<String> arguments;

  // </editor-fold>
  // <editor-fold desc="Properties">

  /**
   * @param name The flag name. Comparison will be case-sensitive.
   * @return Whether a flag with the provided name is part of this command line.
   */
  public boolean hasFlag(String name) {
    return this.flags.contains(name);
  }

  /**
   * Get all flags.
   *
   * @return A possibly empty set of all flags.
   */
  public Set<String> getFlags() {
    return new HashSet<>(this.flags);
  }

  /**
   * @param name The property name. Comparison will be case-sensitive.
   * @return Whether a property with the provided name is part of this command line.
   */
  public boolean hasPropertyValue(String name) {
    return this.properties.containsKey(name);
  }

  /**
   * Get a property value.
   *
   * @param name The property name. Comparison will be case-sensitive.
   * @return Either the last specified property value or <code>null</code>.
   */
  public String getLastPropertyValue(String name) {
    List<String> vals = this.properties.get(name);
    if ((vals != null) && !vals.isEmpty()) {
      return vals.get(vals.size() - 1);
    }
    return null;
  }

  /**
   * Get the property value or return the fallback value if no such property is known to this commandline.
   *
   * @param name The property name. Comparison will be case-sensitive.
   * @param fallback Fallback value to be used when the property doesn't exist.
   * @return Either the property value or the fallback value.
   */
  public String getLastPropertyValue(String name, String fallback) {
    List<String> vals = this.properties.get(name);
    if ((vals != null) && !vals.isEmpty()) {
      return vals.get(vals.size() - 1);
    }
    return fallback;
  }

  /**
   * Get all property values.
   *
   * @param name The property name. Comparison will be case-sensitive.
   * @return All property values. Can be an empty array.
   */
  public String[] getPropertyValues(String name) {
    List<String> vals = this.properties.get(name);
    String[] propVals = new String[0];
    if (vals == null) {
      return propVals;
    }
    return vals.toArray(propVals);
  }

  /**
   * Get all properties and all their last associated values.
   *
   * @return A possibly empty map of all properties. Values will be the last associated value only.
   */
  public Map<String, String> getLastPropertyValues() {
    return this.getLastPropertyValues(false);
  }

  /**
   * Get all properties and all their last associated values.
   *
   * @param includeFlags Also include flags as properties with an associated "true" value.
   * @return A possibly empty map of all properties. alues will be the last associated value only.
   */
  public Map<String, String> getLastPropertyValues(boolean includeFlags) {
    Map<String, String> lastVals = new HashMap<>();
    for (String propName : this.properties.keySet()) {
      lastVals.put(propName, this.getLastPropertyValue(propName));
    }
    if (includeFlags) {
      for (String flag : this.flags) {
        lastVals.put(flag, "true");
      }
    }
    return lastVals;
  }

  /**
   * Get all properties and all their last associated values combined into a single value.
   *
   * @return A possibly empty map of all properties. Values will be a single string representation the list of actually associated values.
   */
  public Map<String, String> getCombinedPropertyValues() {
    return this.getLastPropertyValues(false);
  }

  /**
   * Get all properties and all their last associated values combined into a single value.
   *
   * @param includeFlags Also include flags as properties with an associated "true" value.
   * @return A possibly empty map of all properties. Values will be a single string representation the list of actually associated values.
   */
  public Map<String, String> getCombinedPropertyValues(boolean includeFlags) {
    Map<String, String> lastVals = new HashMap<>();
    for (String propName : this.properties.keySet()) {
      String[] propVals = this.getPropertyValues(propName);
      lastVals.put(propName, CollectionUtils.toString(propVals, "", ",", "")); // TODO: store as JSON array
    }
    if (includeFlags) {
      for (String flag : this.flags) {
        lastVals.put(flag, "true");
      }
    }
    return lastVals;
  }

  /**
   * Get all properties and all their associated values.
   *
   * @return A possibly empty map of all properties. Values will be lists of values.
   */
  public Map<String, List<String>> getAllPropertyValues() {
    return this.getAllPropertyValues(false);
  }

  /**
   * Get all properties and all their associated values.
   *
   * @param includeFlags Also include flags as properties with an associated "true" value.
   * @return A possibly empty map of all properties. Values will be lists of values.
   */
  public Map<String, List<String>> getAllPropertyValues(boolean includeFlags) {
    Map<String, List<String>> props = new HashMap<>(this.properties);
    if (includeFlags) {
      for (String flag : this.flags) {
        props.put(flag, CollectionUtils.asList("true"));
      }
    }
    return props;
  }

  /**
   * Get the first argument (since it often has special meaning, i.e. indicating the main activity to be performed).
   *
   * @return The first argument or <code>null</code> if there are no positional arguments.
   */
  public String getMainArgument() {
    if (this.arguments.isEmpty()) {
      return null;
    }
    return this.arguments.get(0);
  }

  /**
   * Get the ordered list of all but the first argument.
   *
   * @return A possibly empty array of arguments.
   */
  public String[] getOtherArguments() {
    if (this.arguments.size() > 1) {
      return this.arguments.subList(1, this.arguments.size()).toArray(new String[0]);
    }
    return new String[0];
  }

  /**
   * Get the ordered list of all arguments.
   *
   * @return A possibly empty array of arguments.
   */
  public String[] getArguments() {
    return this.arguments.toArray(new String[0]);
  }

  // </editor-fold>
  // <editor-fold desc="Debugging Aid">

  private String cliRepresentation;

  /** {@inheritDoc} */
  @Override
  public String toString() {
    if (this.cliRepresentation == null) {
      StringBuilder sb = new StringBuilder();
      for (String flag : this.flags) {
        sb.append("--");
        sb.append(flag);
        sb.append(" ");
      }
      for (Map.Entry<String, List<String>> prop : this.properties.entrySet()) {
        String propName = prop.getKey();
        for (String propVal : prop.getValue()) {
          StringBuilder valSb = new StringBuilder();
          valSb.append("--");
          valSb.append(propName);
          valSb.append("=");
          valSb.append(propVal);
          sb.append(CommandLineUtils.quoteArg(valSb.toString()));
          sb.append(" ");
        }
      }
      for (String arg : this.arguments) {
        sb.append(arg);
        sb.append(" ");
      }
      this.cliRepresentation = sb.toString().trim();
    }
    return this.cliRepresentation;
  }

  // </editor-fold>
}
