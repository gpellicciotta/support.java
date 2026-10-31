// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support;

/**
 *  Library info.
 */
public final class Info {
  private Info() {}

  static {
    NAME = valueOrDefault(Info.class.getPackage().getImplementationTitle(), "support.java");
    VERSION = valueOrDefault(Info.class.getPackage().getImplementationVersion(), "0.0.0+unknown");
    AUTHOR = valueOrDefault(Info.class.getPackage().getImplementationVendor(), "Giovanni Pellicciotta");
    COPYRIGHT = valueOrDefault(Info.class.getPackage().getSpecificationVendor(), "Copyright (c) Giovanni Pellicciotta");
    DESCRIPTION = valueOrDefault(Info.class.getPackage().getSpecificationTitle(), "A general-purpose Java support library");
  }

  private static String valueOrDefault(String val, String defaultValue) {
    if ((val != null) && !val.isEmpty()) {
      return val;
    }
    return defaultValue;
  }

  public static final String NAME;
  public static final String VERSION;
  public static final String AUTHOR;
  public static final String COPYRIGHT;
  public static final String DESCRIPTION;

  /**
   *  CLI interface with following actions/options:
   *  <pre>
   *    version, --version, -v : show version only, then exit 0
   *    help, --help, -h, -?   : show description, usage and exit codes, then exit 0
   *    --verbose               : show the long form of the help usage guidelines
   *  </pre>
   *
   *  @param args The actual CLI arguments.
   */
  public static void main(String[] args) {
    boolean verbose = false;
    String action = "help";
    for (String arg : args) {
      switch (arg.toLowerCase()) {
        case "--verbose":
          verbose = true;
          break;
        case "-v":
        case "--version":
        case "version":
          action = "version";
          break;
        case "-h":
        case "-?":
        case "--help":
        case "help":
          action = "help";
          break;
        default:
          System.err.println("Unknown argument: " + arg);
          System.err.println("Only version/--version/-v, help/--help/-h/-?, and --verbose are allowed");
          System.exit(1);
          return;
      }
    }
    if ("version".equals(action)) {
      emitVersion();
    }
    else {
      emitHelp(verbose);
    }
    System.exit(0);
  }

  private static void emitVersion() {
    System.out.printf("%s v%s - %s%n", NAME, VERSION, COPYRIGHT);
  }

  private static void emitHelp(boolean verbose) {
    System.out.printf("%s v%s - %s%n", NAME, VERSION, COPYRIGHT);
    System.out.println();
    System.out.println(DESCRIPTION);
    System.out.println();
    if (verbose) {
      System.out.printf("Usage: java -jar %s-%s.jar [action] [options]%n", NAME, VERSION);
      System.out.println();
      System.out.println("Actions:");
      System.out.println("  version              Show the version and exit");
      System.out.println("  help                 Show this help and exit");
      System.out.println();
      System.out.println("Options:");
      System.out.println("  -v, --version        Same as the 'version' action");
      System.out.println("  -h, -?, --help       Same as the 'help' action");
      System.out.println("  --verbose            Show this extended help instead of the short form");
    }
    else {
      System.out.printf("Usage: java -jar %s-%s.jar [version|help] [--verbose]%n", NAME, VERSION);
    }
    System.out.println();
    System.out.println("Exit codes: 0 on success (including version/help), 1 on an unrecognized argument.");
  }
}
