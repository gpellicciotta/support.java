// Copyright (c) Giovanni Pellicciotta

/**
 *  A minimal, server-agnostic HTTP layer: {@link com.pellicciotta.support.http.Request}/
 *  {@link com.pellicciotta.support.http.Response} are the API consuming code writes against;
 *  {@link com.pellicciotta.support.http.HttpRequestHandler}/{@link com.pellicciotta.support.http.HttpRoute} route
 *  requests to code; {@link com.pellicciotta.support.http.WebServer} is the server abstraction, with
 *  {@link com.pellicciotta.support.http.JdkHttpServerWebServer} as its only backing implementation today, on top
 *  of the JDK's built-in {@code com.sun.net.httpserver.HttpServer} — no servlet container, no external HTTP
 *  server dependency. {@link com.pellicciotta.support.http.StaticFileHandler} serves files from the file system or
 *  classpath. {@link com.pellicciotta.support.http.WebServerConfig} is a ready-made host/port/base-url settings
 *  group for apps embedding a {@link com.pellicciotta.support.http.WebServer}.
 */
package com.pellicciotta.support.http;
