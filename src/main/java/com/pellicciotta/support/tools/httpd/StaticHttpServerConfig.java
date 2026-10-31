// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.tools.httpd;

import com.pellicciotta.support.app.BasicConfig;
import com.pellicciotta.support.config.*;
import com.pellicciotta.support.utils.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

class StaticHttpServerConfig extends BasicConfig
{
  public StaticHttpServerConfig() {
    super();
    List<ConfigElement> configElements = new ArrayList<>();
    // Add existing ones, from base config:
    for (Iterator<? extends ConfigElement> it = this.topLevelConfigElementsIterator(); it.hasNext(); ) {
      ConfigElement ce = it.next();
      configElements.add(ce);
    }
    // Add additional ones:
    SettingsFactory create = new SettingsFactory();
    this.baseDir = create.standaloneStringSetting("base-dir", "Base directory for files to be served", ".");
    configElements.add(this.baseDir);
    this.notFoundFile = create.standaloneStringSetting("not-found-file", "File to be served when no direct mapping can be found", "index.html");
    configElements.add(this.notFoundFile);
    this.cacheResults = create.standaloneBooleanSetting("cache-results", "Cache results once found, hence not always re-loading files when requested and remembering also 'not-found' results.", false);
    configElements.add(this.cacheResults);
    this.port = create.standaloneIntegerSetting("port", "Port at which the web server will be listening for HTTP(s) requests", 80);
    configElements.add(this.port);
    this.welcomeFiles = create.standaloneListSetting(String.class, "welcome-files", "Names of files to look for and return when a directory path is requested", Arrays.asList("index.html", "index.md"));
    this.initialize(configElements.toArray(new ConfigElement[0]));
  }

  @Override
  public void loadFrom(CommandLine cli) throws IOException {
    super.loadFrom(cli);
    if (cli.hasPropertyValue("welcome-file")) { // Also allow this list-style setting to be given on the command-line
      String welcomeFile = cli.getLastPropertyValue("welcome-file").trim();
      this.welcomeFiles.setValue(Arrays.asList(welcomeFile));
    }
  }

  private final Setting<String> baseDir;

  /**
   *  @return The configured base directory.
   */
  public String getBaseDir() {
    return this.baseDir.getValueOrDefault();
  }

  /**
   *  Configure a base directory to look up static files in.
   *
   *  @param dir The root directory for static files to be served from.
   */
  public void setBaseDir(String dir) {
    this.baseDir.setValue(dir);
  }

  private final Setting<Boolean> cacheResults;

  /**
   *  @return Whether caching is enabled or not.
   */
  public boolean getCacheResults() {
    return this.cacheResults.getValueOrDefault();
  }

  /**
   *  Configure whether to cache results.
   *
   *  @param enableCache Enable using a cache for all types of results.
   */
  public void setCacheResults(boolean enableCache) {
    this.cacheResults.setValue(enableCache);
  }

  private final ListSetting<String> welcomeFiles;

  /**
   *  @return The configured welcome files: the names of files to return when a directory path is requested.
   */
  public List<String> getWelcomeFiles() {
    return (List<String>) this.welcomeFiles.getValueOrDefault();
  }

  /**
   *  Configure the set of files to look for and return when a directory path is requested.
   *
   *  @param fileNames File names (without path) to look for (E.g <code>index.html</code>)
   */
  public void setWelcomeFiles(String... fileNames) {
    this.welcomeFiles.setValue(CollectionUtils.asList(fileNames));
  }

  private final Setting<String> notFoundFile;

  /**
   *  @return The configured file to serve when no actual file could be found that matches the request path.
   */
  public String getNotFoundFile() {
    return this.notFoundFile.getValueOrDefault();
  }

  /**
   *  Configure the file to serve when no direct mapping as requested can be found.
   *
   *  @param fileName File name (with or without path) to serve when no direct mapping can be found.
   */
  public void setNotFoundFile(String fileName) {
    this.notFoundFile.setValue(fileName);
  }

  private final Setting<Integer> port;

  /**
   *  @return The configured HTTP server port.
   */
  public int getPort() {
    return this.port.getValueOrDefault();
  }

  /**
   *  Configure an HTTP server port.
   *
   *  @param port The HTTP server port to listen for connections.
   */
  public void setPort(int port) {
    this.port.setValue(port);
  }
}
