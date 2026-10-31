// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.sql;

import java.util.*;

import com.pellicciotta.support.text.SyntaxToken;
import com.pellicciotta.support.utils.CollectionUtils;
import com.pellicciotta.support.utils.Contract;
import static com.pellicciotta.support.sql.SqlSyntaxType.*;

/**
 *  The syntax for a single SQL statement with capabilities to recognize and normalize both named and unnamed parameters.
 */
class SqlStatementSyntax
{
  /** Create a new SQL statement syntax, while normalizing it and checking for named and unnamed parameters. */
  SqlStatementSyntax(String rawSyntax) {
    Contract.isNotNull("rawSyntax", rawSyntax);
    this.originalSyntax = rawSyntax;

    // 1. Tokenize
    SqlTokenizer tokenizer = new SqlTokenizer();
    this.tokens = CollectionUtils.listFromIterator(tokenizer.tokenize(rawSyntax));
    // 2. Find all named parameter references and replace by ? while keeping track of the position
    //    Also normalize all whitespace and the case of keywords
    StringBuilder updatedSqlText = new StringBuilder();
    int paramPos = 0;
    Map<String, List<Integer>> paramPositions = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    for (SyntaxToken token : this.tokens) {
      String tokenText = token.getText();
      if (NAMED_PARAMETER.equals(token.getSyntaxType())) {
        paramPos += 1;
        String paramName = tokenText.substring(1);
        CollectionUtils.getOrAddDefault(paramPositions, paramName, new ArrayList<>())
                       .add(paramPos);
        updatedSqlText.append("?");
      } else if (POSITIONAL_PARAMETER.equals(token.getSyntaxType())) {
        paramPos += 1;
        updatedSqlText.append("?");
      } else if (WHITESPACE.equals(token.getSyntaxType())) {
        updatedSqlText.append(" ");
      } else if (KEYWORD.equals(token.getSyntaxType())) {
        updatedSqlText.append(tokenText.toUpperCase());
      } else {
        updatedSqlText.append(tokenText);
      }
    }
    this.parameterCount = paramPos;
    this.normalizedSyntax = updatedSqlText.toString();
    // 3. Finalize positions
    for (String paramName : paramPositions.keySet()) {
      List<Integer> positions = paramPositions.get(paramName);
      if ((positions != null) && !positions.isEmpty()) {
        this.namedParameters.put(paramName, positions.stream().mapToInt(i->i).toArray());
      }
    }
  }

  final String originalSyntax;
  final List<SyntaxToken> tokens;
  final Map<String, int[]> namedParameters = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
  final String normalizedSyntax;
  final int parameterCount;

  /** @return The normalized SQL syntax. */
  @Override
  public String toString() {
    return this.normalizedSyntax;
  }
}
