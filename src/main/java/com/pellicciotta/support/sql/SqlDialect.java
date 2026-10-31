// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.sql;

import com.pellicciotta.support.utils.Contract;

/**
 *  SQL dialect specifics
 */
public abstract class SqlDialect {
  /**
   *  Quote an identifier.
   *
   *  @param id The (unquoted) identifier.
   *  @return A quoted form of identifier.
   */
  public String quoteIdentifier(String id) {
    return String.format("\"%s\"", id.replace("\"", "\"\"")); // Double all double-quotes
  }

  /**
   *  Unquote a previously quoted identifier.
   *
   *  @param quotedId An identifier previously quoted via {@link #quoteIdentifier(String)}.
   *  @return The unquoted form of the identifier.
   */
  public String unquoteIdentifier(String quotedId) {
    Contract.isNotNull("quotedId", quotedId);
    Contract.isTrue("must start and end with \"", (quotedId.length() > 1) && (quotedId.charAt(0) == quotedId.charAt(quotedId.length() - 1)) && (quotedId.charAt(0) == '"'));

    String id = quotedId.substring(1, quotedId.length() - 2); // Strip quotes
    return id.replace("\"\"", "\""); // Un-double all double-quotes
  }
}
