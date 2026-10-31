// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.config;

import java.util.ArrayList;

import com.pellicciotta.support.utils.Contract;

/**
 *  Utilities for working with command line arguments.
 */
public final class CommandLineUtils {
  /** Prevent instantiation */
  private CommandLineUtils() { }

  /**
   *  Create a full command line string, that can be split into individual arguments again by the {@link #split(String)} method,
   *  unless it contains unmatched quotes.
   *  <p>
   *  Unless an argument already contains whitespace, it will be left unchanged.
   *  If it contains whitespace, and no quotes, it will be surrounded entirely with double quotes.
   *  If it contains whitespace and quotes, it will be left unchanged.</p>
   *
   *  @param cliArgs The separate command line arguments. Must never be <code>null</code>.
   *  @return A single string that is re-splittable into the individual arguments. Will be empty if cliArgs is.
   */
  public static String join(String[] cliArgs) {
    Contract.isNotNull("cliArgs", cliArgs);
    StringBuilder cli = new StringBuilder();
    boolean first = true;
    for (String cliArg : cliArgs) {
      if (first) {
        first = false;
      }
      else {
        cli.append(" ");
      }
      cli.append(quoteArg(cliArg));
    }
    return cli.toString();
  }

  /**
   *  From a full command line string, split it into individual arguments.
   *  <p>
   *  Splitting happens on whitespace unless there are quotes.
   *  Pairs of double or single quotes make sure that the intervening characters will always belong to a single argument.
   *  The resulting parts will also cleaned: if it would have started (= first character) and ended (= last character) with the same quote character, both quote chars will be removed.</p>
   *
   *  @param commandLineString The full command line. Must never be <code>null</code>.
   *  @return The separate command line arguments. Can be an empty array if commandLineString is empty. Individual arguments will never be quoted in the sense that if the same type of quote would have appeared at the start and end, it will have been removed.
   */
  public static String[] split(String commandLineString) {
    Contract.isNotNull("commandLine", commandLineString);
    commandLineString = commandLineString.trim(); // Note: very important for state machine - see below
    if (commandLineString.isEmpty()) {
      return new String[0];
    }
    ArrayList<String> args = new ArrayList<>();
    StringBuilder lastArg = new StringBuilder();
    boolean inSingleQuote = false;
    boolean inDoubleQuote = false;
    boolean inWhitespace = false;
    char[] commandLineChars = commandLineString.toCharArray();
    for (int i = 0; i < commandLineChars.length; i++) {
      char c = commandLineChars[i];
      if (inWhitespace) {
        if (Character.isWhitespace(c)) {
          continue; // Skipping whitespace
        }
        else {
          inWhitespace = false;
        }
      }
      else if (Character.isWhitespace(c)) {
        if (inDoubleQuote || inSingleQuote) {
          lastArg.append(c);
        }
        else { // Finish last arg - there is no chance this happens on the first char since we trim at the beginning
          args.add(unquoteArg(lastArg.toString()));
          lastArg = new StringBuilder();
        }
        inWhitespace = true;
      }
      else if (c == '\'') {
        inSingleQuote = !inSingleQuote;
        lastArg.append(c);
      }
      else if (c == '\"') {
        inDoubleQuote = !inDoubleQuote;
        lastArg.append(c);
      }
      else {
        lastArg.append(c);
      }
    }
    args.add(unquoteArg(lastArg.toString())); // Finish last arg - there is no chance this happens to be just started since we trim at the beginning and we only start a new arg on whitespace
    return args.toArray(new String[0]);
  }

  /**
   *  If the argument doesn't include spaces or quotes, return it as is.
   *  If it contains double quotes, use single quotes.
   *  Otherwise, just surround the argument with double quotes.
   */
  static String quoteArg(String arg) {
    if (arg.contains("\"")) {
      return "'" + arg + "'";
    }
    else if (arg.contains("'") || arg.contains(" ")) {
      return "\"" + arg + "\"";
    }
    else {
      return arg;
    }
  }

  /**
   *  If the first and last character are the same quote char: remove those 2 chars.
   *  Otherwise return as-is.
   */
  static String unquoteArg(String arg) {
    // First and last character are the same quote character
    if ((arg.length() > 1) && (arg.charAt(0) == arg.charAt(arg.length() - 1)) && ((arg.charAt(0) == '\'') || (arg.charAt(0) == '"'))) {
      return arg.substring(1, arg.length() - 1);
    }
    return arg;
  }
}
