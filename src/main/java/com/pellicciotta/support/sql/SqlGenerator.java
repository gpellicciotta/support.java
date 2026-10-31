// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.sql;

import java.util.List;

import com.pellicciotta.support.text.*;

/**
 *  Class capable of generating SQL text from tokens and/or AST nodes and/or generate-expressions.
 */
class SqlGenerator {
  /**
   *  Create a SQL generator.
   */
  public SqlGenerator() { }

  boolean uppercaseKeywords = true;

  public void setUppercaseKeywords(boolean uppercase) {
    this.uppercaseKeywords = uppercase;
  }

  boolean normalizeWhitespace = true;

  public void setNormalizeWhitespace(boolean normalize) {
    this.normalizeWhitespace = normalize;
  }

  boolean keepComments = true;

  public void setKeepComments(boolean keep) {
    this.keepComments = keep;
  }

  boolean keepErrors = true;

  public void setKeepErrors(boolean keep) {
    this.keepErrors = keep;
  }

  public String generate(List<SyntaxToken> tokens) {
    return this.generate(tokens, 0, tokens.size());
  }

  public String generate(List<SyntaxToken> tokens, int offset, int length) {
    StringBuilder sb = new StringBuilder();
    this.generateInto(sb, tokens, offset, length);
    return sb.toString();
  }

  public void generateInto(StringBuilder into, List<SyntaxToken> tokens) {
    this.generateInto(into, tokens, 0, tokens.size());
  }

  public void generateInto(StringBuilder into, List<SyntaxToken> tokens, int offset, int length) {
    boolean whitespaceSeen = true;
    for (int i = offset, max = Math.min(tokens.size(), offset + length); i < max; i++) {
      SyntaxToken token = tokens.get(i);
      SyntaxType type = token.getSyntaxType();
      if (!this.keepErrors && (type == SyntaxType.ERROR)) {
        continue ;
      }
      if (!this.keepComments && ((type == SqlSyntaxType.BLOCK_COMMENT) || (type == SqlSyntaxType.LINE_COMMENT))) {
        continue;
      }
      if (type == SqlSyntaxType.WHITESPACE) {
        if (this.normalizeWhitespace) {
          whitespaceSeen = false; // So we'll add a ' ' on next iteration
        }
        else {
          into.append(token.getText());
          whitespaceSeen = true;
        }
        continue;
      }
      if (!whitespaceSeen) { // And current token is also not whitespace: add at least one space
        into.append(' ');
      }
      if (this.uppercaseKeywords && ((type == SqlSyntaxType.KEYWORD) || (type == SqlSyntaxType.OPERATOR))) {
        into.append(token.getText().toUpperCase());
      }
      else {
        into.append(token.getText());
      }
      whitespaceSeen = false;
    }
  }
}
