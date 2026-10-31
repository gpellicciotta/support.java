// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.config;

import java.text.ParseException;
import java.util.*;

import com.pellicciotta.support.utils.CollectionUtils;

/**
 *  A command-line syntax parser, which is capable of parsing a single command-line string or array of command-line strings
 *  into a {@link CommandLine} object, while also optionally checking command line flags and properties against
 *  a predefined list of expected names.
 *  <p>
 *  This parser is by design not capable of understanding all command-line syntax that is out there in the world: it
 *  only supports <code>--</code> prefixed flags, <code>--</code> and <code>=</code> using properties and positional
 *  argument: see {@link CommandLine} for a more verbose description.</p>
 */
public class CommandLineParser {
  // <editor-fold desc="Construction">

  /**
   *  Create a parser that optionally checks flag and property names.
   *
   * @param validNames The names to check flag and property names against.
   *                   Each one that isn't in the list, will lead to a {@link java.text.ParseException}.
   *                   If the list is empty, no name checking will happen.
   */
  public CommandLineParser(String... validNames) {
    this.validNames = CollectionUtils.asSet(validNames);
  }

  /**
   * Create a parser that checks flag and property names.
   *
   * @param validNames The names to check flag and property names against.
   *                   Each one that isn't in the list, will lead to a {@link java.text.ParseException}.
   *                   If the set is empty, no name checking will happen.
   */
  public CommandLineParser(Set<String> validNames) {
    this.validNames = new HashSet(validNames);
  }

  private Set<String> validNames;

  // </editor-fold>
  // <editor-fold desc="Parse Methods">

  /**
   * Create a command-line from the provided CLI arguments array.
   *
   * @param commandLine The command line to be parsed.
   * @return The parsed command-line.
   *
   * @throws java.text.ParseException If parsing fails.
   */
  public CommandLine parse(String commandLine) throws ParseException {
    return this.parse(CommandLineUtils.split(commandLine));
  }

  /**
   * Create a command-line from the provided CLI arguments array.
   *
   * @param cliArgs The command line arguments to be parsed.
   * @return The parsed command-line.
   *
   * @throws java.text.ParseException If parsing fails.
   */
  public CommandLine parse(List<String> cliArgs) throws ParseException {
    return this.parse(cliArgs.toArray(new String[0]));
  }

  /**
   * Create a command-line from the provided CLI arguments array.
   *
   * @param cliArgs The command line arguments to be parsed.
   * @return The parsed command-line.
   *
   * @throws java.text.ParseException If parsing fails.
   */
  public CommandLine parse(String... cliArgs) throws ParseException {
    Set<String> flags = new HashSet<>();
    Map<String, List<String>> properties = new HashMap<>();
    List<String> arguments = new ArrayList<>();
    for (int i = 0; i < cliArgs.length; i++) {
      String cliArg = cliArgs[i];
      if ("--".equals(cliArg)) { // End of option parsing: all the rest are positional args
        i += 1;
        for (; i < cliArgs.length; i++) {
          arguments.add(cliArgs[i]);
        }
      }
      else if (cliArg.startsWith("--")) { // Flag or property found
        // Split into name/value
        String name = cliArg.substring(2);
        String val = null;
        int valIdx = name.indexOf("=");
        if (valIdx >= 0) {
          val = name.substring(valIdx + 1);
          name = name.substring(0, valIdx);
        }
        // Check option
        if (!this.validNames.isEmpty() && !this.validNames.contains(name)) {
          String msg = String.format("The property or flag '%s' is not a recognized name", name);
          throw new ParseException(msg, i);
        }
        // Store
        if (val != null) {
          List<String> propVals = properties.getOrDefault(name, new ArrayList<>());
          propVals.add(val);
          properties.put(name, propVals);
        }
        else {
          flags.add(name);
        }
      }
      else if (cliArg.startsWith("-")) { // Flags only
        // Split into name/value
        String names = cliArg.substring(1);
        // Check flags names
        if (!this.validNames.isEmpty()) {
          for (char flag : names.toCharArray()) {
            if (!this.validNames.contains("" + flag)) {
              String msg = String.format("The flag '%c' is not a recognized name", flag);
              throw new ParseException(msg, i);
            }
          }
        }
        // Store:
        for (char flag : names.toCharArray()) {
          flags.add("" + flag);
        }
      }
      else { // No named option, so must be interpreted as positional
        arguments.add(cliArg);
      }
    }
    return new CommandLine(flags, properties, arguments);
  }

  // </editor-fold>
}
