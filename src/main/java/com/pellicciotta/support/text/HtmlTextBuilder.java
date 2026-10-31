// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.text;

import java.util.*;

import com.pellicciotta.support.logging.LogService;
import com.pellicciotta.support.utils.*;

/**
 *  Class for building HTML text.
 */
public final class HtmlTextBuilder {
  // <editor-fold desc="Support Types">

  /** An open HTML tag that closes itself when used in a try-with-resources block. */
  public interface Tag extends AutoCloseable {
    @Override
    void close();
  }

  // </editor-fold>
  // <editor-fold desc="Construction">

  /** Create a builder that writes into a new, empty {@link StringBuilder}. */
  public HtmlTextBuilder() {
    this.sb = new StringBuilder();
  }

  /**
   *  Create a builder that appends to the given {@link StringBuilder}.
   *
   *  @param sb The target buffer; must not be null.
   */
  public HtmlTextBuilder(StringBuilder sb) {
    Contract.isNotNull(sb);
    this.sb = sb;
  }

  // </editor-fold>
  // <editor-fold desc="Build API">

  private final StringBuilder sb;
  private boolean lastOpenTagIsClosed = true;
  private final Deque<String> tagsToBeClosed = new ArrayDeque<>();
  private static final Set<String> tagsToBeIndented = CollectionUtils.asSet("html", "head", "body",
      /* <head> elements: */ "title", "style", "meta", "link", "script", "base",
      /* <body> elements: */ "h1", "h2", "h3", "h4", "h5", "h6", "p", "div", "form", "table", "th", "tr");
  private static final Set<String> endTagsToBeOnSeparateLines = CollectionUtils.asSet("html", "head", "script", "style", "body", "form", "div", "table");


  /**
   *  Finalize the building, after which this builder will get reset to its initial state.
   *
   *  @return The HTML text built so far.
   */
  public String build() {
    while (!this.tagsToBeClosed.isEmpty()) {
      this.endTag();
    }
    String txt = this.sb.toString();
    this.reset();
    return txt;
  }

  /**
   *  Reset this builder to its initial state.
   *
   *  @return This builder.
   */
  public HtmlTextBuilder reset() {
    this.sb.setLength(0);
    this.lastOpenTagIsClosed = true;
    this.tagsToBeClosed.clear();
    return this;
  }

  private String escapedText(String format, Object... args) {
    String rawText;
    if (args.length == 0) {
      rawText = format;
    }
    else {
      rawText = String.format(format, args);
    }
    return XmlUtils.escape(rawText);
  }

  /**
   *  Write a complete element: start tag, escaped formatted text, then end tag.
   *
   *  @param tag The tag name.
   *  @param format The {@link String#format} pattern for the text; the result is HTML-escaped.
   *  @param args The format arguments.
   *  @return This builder.
   */
  public HtmlTextBuilder inlineTag(String tag, String format, Object... args) {
    this.startTag(tag);
    this.text(format, args);
    this.endTag();
    return this;
  }

  private class AutoCloseableTag implements Tag {
    AutoCloseableTag(int level, String name) {
      this.level = level;
      this.name = name;
    }

    private final int level;
    private final String name;

    @Override
    public void close() {
      Contract.isTrue("There is an unclosed '" + this.name + "' tag at level " + this.level, (HtmlTextBuilder.this.tagsToBeClosed.size() == this.level) && this.name.equalsIgnoreCase(HtmlTextBuilder.this.tagsToBeClosed.peekLast()));
      HtmlTextBuilder.this.endTag(this.name);
    }
  }

  /**
   *  Start a tag and return a handle whose {@code close()} ends it.
   *
   *  @param name The tag name.
   *  @return The handle; closing it fails with a contract violation if the tag is no longer the innermost open tag.
   */
  public Tag startAutoCloseableTag(String name) {
    this.startTag(name);
    int level = this.tagsToBeClosed.size();
    return new AutoCloseableTag(level, name);
  }

  private void closeOpenTag() {
    if (!this.lastOpenTagIsClosed) {
      this.sb.append(">");
    }
    this.lastOpenTagIsClosed = true;
  }

  /**
   *  Start a tag and leave it open for {@code attr} calls; tags such as {@code html}, {@code body} and {@code h1}
   *  are placed on a new line.
   *
   *  @param name The tag name.
   *  @return This builder.
   */
  public HtmlTextBuilder startTag(String name) {
    this.closeOpenTag();
    if (tagsToBeIndented.contains(name)) {
      this.sb.append("\n");
      // TODO: actual indent
    }
    this.tagsToBeClosed.addLast(name);
    this.sb.append("<");
    this.sb.append(name);
    this.lastOpenTagIsClosed = false;
    return this;
  }

  private String formatNumber(long value) {
    return "" + value;
  }

  /**
   *  Add an integer attribute to the tag that was just started.
   *
   *  @param name The attribute name.
   *  @param value The attribute value.
   *  @return This builder.
   */
  public HtmlTextBuilder attr(String name, int value) {
    return attr(name, formatNumber(value));
  }

  /**
   *  Add a long attribute to the tag that was just started.
   *
   *  @param name The attribute name.
   *  @param value The attribute value.
   *  @return This builder.
   */
  public HtmlTextBuilder attr(String name, long value) {
    return attr(name, formatNumber(value));
  }

  /**
   *  Add a value-less attribute to the tag that was just started.
   *
   *  @param name The attribute name.
   *  @return This builder.
   *  @throws IllegalArgumentException If there is no start tag still open for attributes.
   */
  public HtmlTextBuilder attr(String name) {
    Contract.isTrue("There is an unclosed open tag", !this.tagsToBeClosed.isEmpty() && !this.lastOpenTagIsClosed);
    this.sb.append(" ");
    this.sb.append(name);
    return this;
  }

  /**
   *  Add an attribute with an HTML-escaped, double-quoted value to the tag that was just started.
   *
   *  @param name The attribute name.
   *  @param value The attribute value.
   *  @return This builder.
   *  @throws IllegalArgumentException If there is no start tag still open for attributes.
   */
  public HtmlTextBuilder attr(String name, String value) {
    Contract.isTrue("There is an unclosed open tag", !this.tagsToBeClosed.isEmpty() && !this.lastOpenTagIsClosed);
    this.sb.append(" ");
    this.sb.append(name);
    this.sb.append("=\"");
    this.sb.append(this.escapedText(value));
    this.sb.append("\"");
    return this;
  }

  /**
   *  Add an attribute whose value is formatted with {@link String#format}, HTML-escaped and double-quoted.
   *
   *  @param name The attribute name.
   *  @param valueFormat The format pattern for the value.
   *  @param args The format arguments.
   *  @return This builder.
   *  @throws IllegalArgumentException If there is no start tag still open for attributes.
   */
  public HtmlTextBuilder attr(String name, String valueFormat, Object... args) {
    Contract.isTrue("There is an unclosed open tag", !this.tagsToBeClosed.isEmpty() && !this.lastOpenTagIsClosed);
    this.sb.append(" ");
    this.sb.append(name);
    this.sb.append("=\"");
    this.sb.append(this.escapedText(valueFormat, args));
    this.sb.append("\"");
    return this;
  }

  /**
   *  Append HTML-escaped text, first closing any start tag that is still open for attributes.
   *
   *  @param format The {@link String#format} pattern; used as-is (but still escaped) when there are no arguments.
   *  @param args The format arguments.
   *  @return This builder.
   */
  public HtmlTextBuilder text(String format, Object... args) {
    if (!this.lastOpenTagIsClosed) {
      this.sb.append(">");
      this.lastOpenTagIsClosed = true;
    }
    this.sb.append(this.escapedText(format, args));
    return this;
  }

  /**
   *  End the innermost open tag, checking that it has the given name (case-insensitive).
   *
   *  @param name The expected tag name.
   *  @return This builder.
   *  @throws IllegalArgumentException If no tag is open or the innermost open tag has a different name.
   */
  public HtmlTextBuilder endTag(String name) {
    Contract.isTrue("There is an unclosed tag", !this.tagsToBeClosed.isEmpty());
    Contract.isTrue("There is an unclosed '" + name + "' tag", name.equalsIgnoreCase(this.tagsToBeClosed.peekLast()));
    if (!this.lastOpenTagIsClosed) {
      this.sb.append(">");
      this.lastOpenTagIsClosed = true;
    }
    String tag = this.tagsToBeClosed.removeLast();
    if (endTagsToBeOnSeparateLines.contains(tag)) {
      this.sb.append("\n");
      // TODO: actual indentation
    }
    this.sb.append("</");
    this.sb.append(tag);
    this.sb.append(">");
    return this;
  }

  /**
   *  End the innermost open tag.
   *
   *  @return This builder.
   *  @throws IllegalArgumentException If no tag is open.
   */
  public HtmlTextBuilder endTag() {
    Contract.isTrue("There is an unclosed tag", !this.tagsToBeClosed.isEmpty());
    if (!this.lastOpenTagIsClosed) {
      this.sb.append(">");
      this.lastOpenTagIsClosed = true;
    }
    String tag = this.tagsToBeClosed.removeLast();
    if (endTagsToBeOnSeparateLines.contains(tag)) {
      this.sb.append("\n");
      // TODO: actual indentation
    }
    this.sb.append("</");
    this.sb.append(tag);
    this.sb.append(">");
    return this;
  }

  /**
   *  End all open tags, innermost first.
   *
   *  @return This builder.
   */
  public HtmlTextBuilder endAllOpenTags() {
    while (!this.tagsToBeClosed.isEmpty()) {
      this.endTag();
    }
    return this;
  }

  /**
   *  Write a {@code pre} element containing the escaped formatted text.
   *
   *  @param format The {@link String#format} pattern.
   *  @param args The format arguments.
   *  @return This builder.
   */
  public HtmlTextBuilder pre(String format, Object... args) {
    this.startTag("pre");
    this.text(format, args);
    this.endTag();
    return this;
  }

  /**
   *  Write an {@code h1} element containing the escaped formatted text.
   *
   *  @param format The {@link String#format} pattern.
   *  @param args The format arguments.
   *  @return This builder.
   */
  public HtmlTextBuilder h1(String format, Object... args) {
    this.startTag("h1");
    this.text(format, args);
    this.endTag();
    return this;
  }

  /**
   *  Write an {@code h2} element containing the escaped formatted text.
   *
   *  @param format The {@link String#format} pattern.
   *  @param args The format arguments.
   *  @return This builder.
   */
  public HtmlTextBuilder h2(String format, Object... args) {
    this.startTag("h2");
    this.text(format, args);
    this.endTag();
    return this;
  }

  /**
   *  Write an {@code h3} element containing the escaped formatted text.
   *
   *  @param format The {@link String#format} pattern.
   *  @param args The format arguments.
   *  @return This builder.
   */
  public HtmlTextBuilder h3(String format, Object... args) {
    this.startTag("h3");
    this.text(format, args);
    this.endTag();
    return this;
  }

  /**
   *  Write an {@code h4} element containing the escaped formatted text.
   *
   *  @param format The {@link String#format} pattern.
   *  @param args The format arguments.
   *  @return This builder.
   */
  public HtmlTextBuilder h4(String format, Object... args) {
    this.startTag("h4");
    this.text(format, args);
    this.endTag();
    return this;
  }

  /**
   *  Write an {@code h5} element containing the escaped formatted text.
   *
   *  @param format The {@link String#format} pattern.
   *  @param args The format arguments.
   *  @return This builder.
   */
  public HtmlTextBuilder h5(String format, Object... args) {
    this.startTag("h5");
    this.text(format, args);
    this.endTag();
    return this;
  }

  /**
   *  Write an {@code h6} element containing the escaped formatted text.
   *
   *  @param format The {@link String#format} pattern.
   *  @param args The format arguments.
   *  @return This builder.
   */
  public HtmlTextBuilder h6(String format, Object... args) {
    this.startTag("h6");
    this.text(format, args);
    this.endTag();
    return this;
  }

  /**
   *  Write a raw HTML fragment followed by a newline; the format pattern is not escaped, only the arguments are.
   *
   *  @param format The {@link String#format} pattern containing markup.
   *  @param args The arguments, each converted with {@code toString} and HTML-escaped.
   *  @return This builder.
   */
  public HtmlTextBuilder custom(String format, Object... args) {
    this.closeOpenTag();
    Object[] escapedArgs = new Object[args.length];
    for (int i = 0; i < args.length; i++) {
      String arg = "" + args[i];
      escapedArgs[i] = this.escapedText(arg);
    }
    this.sb.append(String.format(format, escapedArgs));
    this.sb.append("\n");
    return this;
  }

  // </editor-fold>
  // <editor-fold desc="Human-readable representation">

  /** @return The HTML-text built so far. */
  @Override
  public String toString() {
    return this.sb.toString();
  }

  // </editor-fold>

  /**
   *  Demo entry point that builds a small HTML page and logs it.
   *
   *  @param args Ignored.
   */
  public static void main(String[] args) {
    HtmlTextBuilder html = new HtmlTextBuilder();
    html.startTag("html")
        .startTag("head")
        .inlineTag("title", "Hiscore Dashboard")
        .startTag("style")
        .attr("language", "css")
        .text("form { display: inline-block; }")
        .endTag()
        .endTag()
        .startTag("body")
        .h1("Hiscore Dashboard")
        .startTag("p")
        .text("A paragraph of text")
        .endTag()
        .endTag()
        .endTag();

    String htmlText = html.build();
    LogService.info("HTML text: %s", htmlText);
  }
}
