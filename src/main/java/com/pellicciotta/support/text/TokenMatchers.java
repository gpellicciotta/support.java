// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.text;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.pellicciotta.support.utils.Contract;
import com.pellicciotta.support.utils.Pair;

/**
 *  A factory for commonly needed {@link TokenMatcher} implementations.
 */
public final class TokenMatchers
{
  private TokenMatchers() { }

  /**
   *  Create a matcher that will use the provided regex.
   *
   *  @param regex The pattern to match. It will get options {@link Pattern#CASE_INSENSITIVE}.
   *  @param tokenType The token type to use when a match succeeds.
   *
   *  @return A {@link TokenMatcher} that will try to match with the provided regex pattern.
   */
  public static TokenMatcher forPattern(String regex, SyntaxType tokenType) {
    Contract.isNotNull("regex", regex);
    Contract.isNotNull("tokenType", tokenType);
    final Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    return (source, startOffset, length) -> {
      Matcher m = pattern.matcher(source);
      if (m.find(startOffset) && (m.start() == startOffset) && (m.end() - startOffset <= length)) {
        return new SyntaxToken(tokenType, source, startOffset, m.end() - startOffset);
      }
      return null;
    };
  }

  /**
   *  Create a matcher that will use the provided regex.
   *
   *  @param pattern The pattern to match.
   *  @param tokenType The token type to use when a match succeeds.
   *
   *  @return A {@link TokenMatcher} that will try to match with the provided regex pattern.
   */
  public static TokenMatcher forPattern(Pattern pattern, SyntaxType tokenType) {
    Contract.isNotNull("pattern", pattern);
    Contract.isNotNull("tokenType", tokenType);
    return (source, startOffset, length) -> {
      Matcher m = pattern.matcher(source);
      if (m.find(startOffset) && (m.start() == startOffset) && (m.end() - startOffset <= length)) {
        return new SyntaxToken(tokenType, source, startOffset, m.end() - startOffset);
      }
      return null;
    };
  }

  /**
   *  Create a matcher that will use the provided series of pairs of regexes and token types to use.
   *
   *  @param patternAndTokenTypePairs The ordered sequence of pairs. Each pair's regex will be tried in turn and the first one that matches, will lead to the final result.
   *
   *  @return A {@link TokenMatcher} that will try to match based on the provided sequence of regex patterns and associated token types.
   */
  public static TokenMatcher forPatterns(Pair<Pattern, SyntaxType>... patternAndTokenTypePairs) {
    Contract.isNotNull("patternAndTokenTypePairs", patternAndTokenTypePairs);
    return (source, startOffset, length) -> {
      for (Pair<Pattern, SyntaxType> pair : patternAndTokenTypePairs) {
        Matcher m = pair.getFirst().matcher(source);
        if (m.find(startOffset) && (m.start() == startOffset) && (m.end() - startOffset <= length)) {
          return new SyntaxToken(pair.getSecond(), source, startOffset, m.end() - startOffset);
        }
      }
      return null;
    };
  }

  /**
   *  Create a matcher that will try to match a start quote, then as many characters as possible, followed by an end quote.
   *  Any doubled or backslash-preceded quotes will potentially also not end the match.
   *
   *  @param tokenType The tokenType to use upon a match.
   *  @param quoteChar The start and end-quote character.
   *  @param allowDoubledQuotes Whether two consecutive quote characters should be allowed and not end the match.
   *  @param allowBackslashEscapedQuotes Whether a quote character preceded by a backslash should be allowed and not end the match.
   *
   *  @return A {@link TokenMatcher} that will try to match based on the quote character and the additional options.
   */
  public static TokenMatcher forQuotedText(SyntaxType tokenType, char quoteChar, boolean allowDoubledQuotes, boolean allowBackslashEscapedQuotes) {
    Contract.isNotNull("tokenType", tokenType);
    return (source, startOffset, length) -> {
      char ch = source.charAt(startOffset);
      if (ch != quoteChar) {
        return null;
      }
      char prevChar = ' ';
      int offset = startOffset + 1;
      for (int i = offset, max = offset + length; i < max; i++, prevChar = ch) {
        ch = source.charAt(i);
        if (ch == quoteChar) {
          if (   (allowDoubledQuotes && (prevChar == quoteChar))
              || (allowBackslashEscapedQuotes && (prevChar == '\\'))) {
            ch = ' '; // Just pretend the current char is a space, so that on the next iteration the prev characters is not treated especially
            continue ;
          }
          // If we get here, we've reached the end
          return new SyntaxToken(tokenType, source, startOffset, i - startOffset + 1);
        }
      }
      return null;
    };
  }

  /**
   *  Create a matcher that will try to match a start character sequence until the end of line.
   *
   *  @param tokenType The tokenType to use upon a match.
   *  @param startChars The comment start character(s).
    *
   *  @return A {@link TokenMatcher} that will try to match based a single-line comment.
   */
  public static TokenMatcher forLineComment(String startChars, SyntaxType tokenType) {
    Contract.isNotNull("startChars", startChars);
    Contract.isNotNull("tokenType", tokenType);
    return (source, startOffset, length) -> {
      Pattern regex = Pattern.compile(Pattern.quote(startChars) + ".*");
      Matcher m = regex.matcher(source);
      if (m.find(startOffset) && (m.start() == startOffset) && (m.end() - startOffset <= length)) {
        return new SyntaxToken(tokenType, source, startOffset, m.end() - startOffset);
      }
      return null;
    };
  }

  /**
   *  Create a matcher that will try to match a start character sequence, followed by arbitrary text, including
   *  newlines, until and end character sequence is found.
   *
   *  @param tokenType The tokenType to use upon a match.
   *  @param startChars The comment start character(s).
   *  @param endChars The comment end character(s).
   *
   *  @return A {@link TokenMatcher} that will try to match a multi-line comment.
   */
  public static TokenMatcher forBlockComment(String startChars, String endChars, SyntaxType tokenType) {
    Contract.isNotNull("startChars", startChars);
    Contract.isNotNull("endChars", endChars);
    Contract.isNotNull("tokenType", tokenType);
    return (source, startOffset, length) -> {
      Pattern regex = Pattern.compile(Pattern.quote(startChars) + ".*" + Pattern.quote(endChars), Pattern.DOTALL);
      Matcher m = regex.matcher(source);
      if (m.find(startOffset) && (m.start() == startOffset) && (m.end() - startOffset <= length)) {
        return new SyntaxToken(tokenType, source, startOffset, m.end() - startOffset);
      }
      return null;
    };
  }

  /**
   *  Create a matcher that will try to match a specific string.
   *
   *  @param string The text to match.
   *  @param tokenType The tokenType to use upon a match.
   *
   *  @return A {@link TokenMatcher} that will try to match an exact text.
   */
  public static TokenMatcher forString(String string, SyntaxType tokenType) {
    Contract.isNotNull("string", string);
    Contract.isNotNull("tokenType", tokenType);
    return (source, startOffset, length) -> {
      int len = string.length();
      if (len > length) {
        return null;
      }
      for (int offset = startOffset, i = 0; i < len; i++, offset++) {
        if (string.charAt(i) != source.charAt(offset)) {
          return null;
        }
      }
      return new SyntaxToken(tokenType, source, startOffset, string.length());
    };
  }

  /**
   *  Create a matcher that will try to match a specific string case-insensitively
   *
   *  @param string The text to match.
   *  @param tokenType The tokenType to use upon a match.
   *
   *  @return A {@link TokenMatcher} that will try to match an exact text.
   */
  public static TokenMatcher forCaseInsensitiveString(boolean caseInsensitive, String string, SyntaxType tokenType) {
    Contract.isNotNull("string", string);
    Contract.isNotNull("tokenType", tokenType);
    return (source, startOffset, length) -> {
      int len = string.length();
      if (len > length) {
        return null;
      }
      for (int offset = startOffset, i = 0; i < len; i++, offset++) {
        if (Character.toUpperCase(string.charAt(i)) != Character.toUpperCase(source.charAt(offset))) {
          return null;
        }
      }
      return new SyntaxToken(tokenType, source, startOffset, string.length());
    };
  }

  /**
   *  Create a matcher that will try to match one of a series of strings.
   *
   *  @param stringAndTokenTypes The tokenType to use upon a match.
   *
   *  @return A {@link TokenMatcher} that will try to match an exact text.
   */
  public static TokenMatcher forStrings(Map<String, SyntaxType> stringAndTokenTypes) {
    Contract.isNotNull("stringAndTokenTypes", stringAndTokenTypes);
    return (source, startOffset, length) -> {
      stringLoop:
      for (String str : stringAndTokenTypes.keySet()) {
        int len = str.length();
        if (len > length) {
          continue ;
        }
        for (int offset = startOffset, i = 0; i < len; i++, offset++) {
          // Compare case-insensitively:
          if (Character.toUpperCase(str.charAt(i)) != Character.toUpperCase(source.charAt(offset))) {
            continue stringLoop;
          }
        }
        // Now check with case:
        SyntaxType tokenType = stringAndTokenTypes.get(source.subSequence(startOffset, startOffset + len));
        if (tokenType != null) {
          return new SyntaxToken(tokenType, source, startOffset, len);
        }
      }
      return null;
    };
  }

  /**
   *  Create a matcher that will try to match a series of strings.
   *
   *  @param stringAndTokenTypePairs The pairs of texts to match with their corresponding token types.
   *
   *  @return A {@link TokenMatcher} that will try to match an exact text.
   */
  public static TokenMatcher forStrings(boolean caseInsensitive, Pair<String, SyntaxType>... stringAndTokenTypePairs) {
    Contract.isNotNull("stringAndTokenTypePairs", stringAndTokenTypePairs);
    return (source, startOffset, length) -> {
      stringLoop:
      for (Pair<String, SyntaxType> e : stringAndTokenTypePairs) {
        String str = e.getFirst();
        int len = str.length();
        if (len > length) {
          continue;
        }
        for (int offset = startOffset, i = 0; i < len; i++, offset++) {
          if (str.charAt(i) != source.charAt(offset)) {
            continue stringLoop;
          }
        }
        return new SyntaxToken(e.getSecond(), source, startOffset, len);
      }
      return null;
    };
  }

  /**
   *  Create a matcher that will try to match a series of strings, case-insensitively.
   *
   *  @param stringAndTokenTypePairs The pairs of texts to match with their corresponding token types.
   *
   *  @return A {@link TokenMatcher} that will try to match an exact text.
   */
  public static TokenMatcher forCaseInsensitiveStrings(boolean caseInsensitive, Pair<String, SyntaxType>... stringAndTokenTypePairs) {
    Contract.isNotNull("stringAndTokenTypePairs", stringAndTokenTypePairs);
    return (source, startOffset, length) -> {
      stringLoop:
      for (Pair<String, SyntaxType> e : stringAndTokenTypePairs) {
        String str = e.getFirst();
        int len = str.length();
        if (len > length) {
          continue ;
        }
        for (int offset = startOffset, i = 0; i < len; i++, offset++) {
          if (Character.toUpperCase(str.charAt(i)) != Character.toUpperCase(source.charAt(offset))) {
            continue stringLoop;
          }
        }
        return new SyntaxToken(e.getSecond(), source, startOffset, len);
      }
      return null;
    };
  }
}
