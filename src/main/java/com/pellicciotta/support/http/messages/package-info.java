// Copyright (c) Giovanni Pellicciotta

/**
 *  Generic REST wire-envelope DTOs shared across single-process-app consumers of this library:
 *  {@link com.pellicciotta.support.http.messages.ResultMessage} (and its {@code status}-fixed subclasses
 *  {@link com.pellicciotta.support.http.messages.ErrorMessage}, {@link com.pellicciotta.support.http.messages.OkMessage},
 *  {@link com.pellicciotta.support.http.messages.SessionIdMessage}) is a generic {@code status}/{@code message}
 *  result envelope; {@link com.pellicciotta.support.http.messages.ApiVersionMessage} and
 *  {@link com.pellicciotta.support.http.messages.ApiInfoMessage} (built from a list of
 *  {@link com.pellicciotta.support.http.messages.ApiRouteMessage}) report a running API's version and routes.
 *  These are plain field-holding DTOs with no serialization annotations, so any reflection-based JSON library
 *  (e.g. GSON) can serialize them directly.
 */
package com.pellicciotta.support.http.messages;
