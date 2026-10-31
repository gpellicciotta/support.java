// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.tools.httpd;

import com.pellicciotta.support.Info;
import com.pellicciotta.support.annotations.CommandLineAction;
import com.pellicciotta.support.app.*;
import com.pellicciotta.support.config.*;
import com.pellicciotta.support.http.*;
import com.pellicciotta.support.logging.Log;
import com.pellicciotta.support.logging.LogService;

import java.io.File;
import java.util.concurrent.CountDownLatch;

/**
 * Command-line tool that serves static files over HTTP from a base directory until the process is stopped.
 */
public class StaticHttpServer extends BasicApp<StaticHttpServerConfig> {
  // <editor-fold desc="Command-line Entry-Point">

  /**
   *  Start a web server using only a {@link com.pellicciotta.support.http.StaticFileHandler}.
   *
   *  @param args Command-line arguments can be:
   *              <pre>
   *              Options:
   *                 --config-file=FILE_PATH          Config file to use.
   *                 --base-dir=FILE_PATH             Base directory to serve static files from.
   *                 --not-found-file=FILE_PATH       File to serve when no direct mapping (nor via welcome file) can be found.
   *                 --welcome-file=FILE_NAME         A file name to look for and serve when a directory is requested.
   *                 --port=INTEGER                   Port at which to listen for requests.
   *
   *              Actions:
   *                 version                          Show version info and exit
   *                 help                             Show help info and exit
   *                 list-config                      List active configuration
   *                 list-config-options              List all possible configuration settings
   *              </pre>
   */
  public static void main(String[] args) {
    System.exit(new StaticHttpServer().run(args));
  }

  // </editor-fold>

  private static final String NAME = "httpd";
  private static final String PRETTY_NAME = "Static HTTP Server";
  private static final String DESCRIPTION = "A simple static HTTP server";
  private static final Version VERSION = new Version(Info.VERSION);

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public Version getVersion() {
    return VERSION;
  }

  @Override
  public String getPrettyName() {
    return PRETTY_NAME;
  }

  @Override
  public String getDescription() {
    return DESCRIPTION;
  }

  @Override
  protected StaticHttpServerConfig getInitialConfiguration() {
    return new StaticHttpServerConfig();
  }

  @Override
  protected String getCommandLinePrefix() {
    return String.format("java -cp [CLASSPATH] tools %s", this.getName());
  }

  @Override
  protected Log getAppLog() {
    return LogService.getBackEnd().getLog(NAME);
  }

  /**
   * Starts the web server and blocks the calling thread indefinitely.
   *
   * @param cli The parsed command line.
   * @param config The tool configuration (port, base directory, welcome and not-found files, caching).
   * @param positionalArguments The action arguments, unused.
   * @return {@link ExitCode#INTERNAL_ERROR} if the server fails; otherwise this method does not return.
   */
  @CommandLineAction(name = "start", description = "Start the web server")
  protected int runStartAction(CommandLine cli, StaticHttpServerConfig config, String[] positionalArguments) {
    Log log = this.getAppLog();
    try {
      WebServer server = new JdkHttpServerWebServer(config.getPort());
      server.add(new StaticFileHandler(config.getBaseDir(), config.getWelcomeFiles(), config.getNotFoundFile(), config.getCacheResults()));
      server.start();
      String filesDir = new File(config.getBaseDir()).getCanonicalFile().getAbsolutePath();
      String serverAddress = "http://localhost" + ((config.getPort() != 80) ? ":" + config.getPort() : "") + "/";
      log.info("Serving files in %s at %s", filesDir, serverAddress);
      // JdkHttpServerWebServer.start() is non-blocking (its request handling runs on its own
      // executor threads), so block the main thread here to keep the process alive - mirrors
      // Jetty's blocking server.join().
      new CountDownLatch(1).await();
      return ExitCode.NORMAL;
    }
    catch (Exception e) {
      log.error("Server stopped due to fatal error: %s", e);
      return ExitCode.INTERNAL_ERROR;
    }
  }
}
