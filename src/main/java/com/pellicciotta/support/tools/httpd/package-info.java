// Copyright (c) Giovanni Pellicciotta

/**
 *  The <code>httpd</code> command-line tool.
 *  {@link com.pellicciotta.support.tools.httpd.StaticHttpServer} starts a web server that serves files from a base
 *  directory through a {@link com.pellicciotta.support.http.StaticFileHandler}, running until the process is stopped.
 *  Its settings (port, base directory, welcome and not-found files, caching) live in
 *  {@code StaticHttpServerConfig}.
 */
package com.pellicciotta.support.tools.httpd;
