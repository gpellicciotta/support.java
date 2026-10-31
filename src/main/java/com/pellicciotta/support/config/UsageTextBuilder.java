// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.config;

import java.util.*;

import com.pellicciotta.support.text.TextBuilder;
import com.pellicciotta.support.utils.*;

/**
 *  A utility class for building text messages.
 *  <p>
 *  A usage text should consist of following sections, in order:</p><ol>
 *    <li>Header lines with the summarized command line syntax, e.g.<pre>
 *          java -jar diffutils.jar [general-options] diff [diff-options] file1 file2</pre>
 *        or with a description:<pre>
 *          java -jar diffutils.jar [general-options] diff [diff-options] file1 file2
 *            The 'diff' command invokes a 2-way diff between file1 and file2</pre></li>
 *    <li>Options and option groups with option descriptions, e.g.<pre>
 *         --logging.console.level=LOGLEVEL
 *             The min. log level for root logger console output
 *             Valid values: one of following log-levels: 'error', 'warning', 'info', 'debug' or 'none'
 *             Default value: 'info'</pre></li>
 *    <li>Extra information about position arguments, exit codes or where to find more info, also optionally grouped, e.g.<pre>
 *          Exit codes:
 *            0 - operation ended normally
 *            1 - configuration error
 *            2 - runtime error
 *        </pre></li></ol>
 *  <p>
 *  Methods need to be invoked in correct order.</p>
 */
public class UsageTextBuilder {
  /**
   *  Create a new usage text builder.
   */
  public UsageTextBuilder() {
    this.reset();
  }

  private final int maxLineSize = 100;
  private final TextBuilder tb = new TextBuilder(new StringBuilder(), 4);
  private final List<String> syntaxLines = new ArrayList<>();
  private final List<String> optionGroups = new ArrayList<>();
  private final Map<String, List<String>> groupedOptionLines = new HashMap<>();
  private final List<String> extraGroups = new ArrayList<>();
  private final Map<String, List<String>> groupedExtraLines = new HashMap<>();

  private final Stack<String> currentOptionGroup = new Stack<>();
  private final Stack<String> currentExtraGroup = new Stack<>();

  private String currentOptSyntax;
  private String currentOptDescription;
  private String currentOptValidValuesDescription;
  private String currentOptDefaultValueDescription;

  /**
   *  Reset completely to an empty usage text.
   *
   *  @return <code>this</code> so method chaining is possible.
   */
  public UsageTextBuilder reset() {
    this.syntaxLines.clear();
    this.optionGroups.clear();
    this.groupedOptionLines.clear();
    this.extraGroups.clear();
    this.groupedExtraLines.clear();
    // For ungrouped options
    this.optionGroups.add("");
    this.groupedOptionLines.put("", new ArrayList<>());
    this.currentOptionGroup.clear();
    this.currentOptionGroup.push("");
    // For ungrouped extra info
    this.extraGroups.add("");
    this.groupedExtraLines.put("", new ArrayList<>());
    this.currentExtraGroup.clear();
    this.currentExtraGroup.push("");
    //
    this.currentOptSyntax = null;
    this.currentOptDescription = null;
    this.currentOptValidValuesDescription = null;
    this.currentOptDefaultValueDescription = null;
    return this;
  }

  /**
   *  Add a command line syntax summary line, e.g.<pre>
   *    java -jar diffutils.jar [general-options] diff [diff-options] file1 file2</pre>
   *  or with a description:<pre>
   *    java -jar diffutils.jar [general-options] diff [diff-options] file1 file2
   *       The 'diff' command invokes a 2-way diff between file1 and file2</pre>
   *
   *  @param syntaxSummary The syntax summary. Must not be null.
   *
   *  @return <code>this</code> so method chaining is possible.
   */
  public UsageTextBuilder addCommandLineSummary(String syntaxSummary) {
    return addCommandLineSummary(syntaxSummary, new String[0]);
  }

  /**
   *  Add a command line syntax summary line, e.g.<pre>
   *    java -jar diffutils.jar [general-options] diff [diff-options] file1 file2</pre>
   *  or with a description:<pre>
   *    java -jar diffutils.jar [general-options] diff [diff-options] file1 file2
   *       The 'diff' command invokes a 2-way diff between file1 and file2</pre>
   *
   *  @param syntaxSummary The syntax summary. Must not be null.
   *  @param shortExplanation Description of the function invoked by the syntax. If not null, each will be positioned on a new line and indented.
   *
   *  @return <code>this</code> so method chaining is possible.
   */
  public UsageTextBuilder addCommandLineSummary(String syntaxSummary, String... shortExplanation) {
    Contract.isNotNull("syntaxSummary", syntaxSummary);
    this.tb.reset();
    try (TextBuilder.Group g = this.tb.start()) {
      this.tb.appendLine(syntaxSummary);
      if (shortExplanation != null) {
        try (TextBuilder.Group s = this.tb.start()) {
          for (int i = 0; i < shortExplanation.length; i++) {
            this.tb.appendLine(shortExplanation[i]);
          }
        }
      }
    }
    this.syntaxLines.add(StringUtils.trimRight(this.tb.toString()));
    return this;
  }

  /**
   *  Start an option group description.
   *  <p>
   *  Also implicitly ends any previously started group or option description.</p>
   *
   *  @param groupTitle The title of the group. Should be capitalized and end with 'Settings', e.g. <code>Logging Settings</code>.
   *                    <p>If this title doesn't start with an uppercase letter, it will be capitalized. If it doesn't end with ':', a ':' will be added.</p>
   *  @return <code>this</code> so method chaining is possible.
   */
  public UsageTextBuilder startOptionGroup(String groupTitle) {
    Contract.isNotNull("groupTitle", groupTitle);
    if (this.currentOptSyntax != null) { // End started option description
      this.endOption();
    }
    if (!this.groupedOptionLines.containsKey(groupTitle)) {
      this.optionGroups.add(groupTitle);
      this.groupedOptionLines.put(groupTitle, new ArrayList<>());
    }
    this.currentOptionGroup.push(groupTitle);
    return this;
  }

  /**
   *  Start an option description.
   *  <p>
   *  Also implicitly ends any previously started option description.</p>
   *
   *  @param optionSyntax The option syntax which should be described as two dashes, followed by the name, and then optionally followed by an equal sign and
   *                      an all uppercase word indicating the needed values type. E.g. <code>--logging.console.level=LOGLEVEL</code>
   *
   *  @return <code>this</code> so method chaining is possible.
   */
  public UsageTextBuilder startOption(String optionSyntax) {
    Contract.isNotNull("optionSyntax", optionSyntax);
    if (this.currentOptSyntax != null) { // End started option description
      this.endOption();
    }
    this.currentOptSyntax = optionSyntax;
    return this;
  }

  /**
   *  Continue the current option description with a textual description of the meaning of the option.
   *  <p>
   *  Must follow a {@link #startOption(String)} invocation.</p>
   *
   *  @param descriptionFormat The textual, possibly multi-line, description of the meaning of this option.
   *  @param descriptionParams Any parameters to be filled into the descriptionFormat.
   *
   *  @return <code>this</code> so method chaining is possible.
   */
  public UsageTextBuilder addOptionDescription(String descriptionFormat, Object... descriptionParams) {
    if (this.currentOptSyntax == null) {
      throw new IllegalStateException("Before using this method, startOption must be invoked first");
    }
    Contract.isNotNull("descriptionFormat", descriptionFormat);
    this.currentOptDescription = String.format(descriptionFormat, descriptionParams);
    return this;
  }

  /**
   *  Continue the current option description with a textual description of the possible option values.
   *  <p>
   *  Must follow a {@link #startOption(String)} invocation.</p>
   *
   *  @param descriptionFormat The textual, possibly multi-line, description of the possible option values.
   *  @param descriptionParams Any parameters to be filled into the descriptionFormat.
   *
   *  @return <code>this</code> so method chaining is possible.
   */
  public UsageTextBuilder addOptionValidValuesDescription(String descriptionFormat, Object... descriptionParams) {
    if (this.currentOptSyntax == null) {
      throw new IllegalStateException("Before using this method, startOption must be invoked first");
    }
    Contract.isNotNull("descriptionFormat", descriptionFormat);
    this.currentOptValidValuesDescription = String.format(descriptionFormat, descriptionParams);
    return this;
  }

  /**
   *  Continue the current option description with a textual description of the option's default value.
   *  <p>
   *  Must follow a {@link #startOption(String)} invocation.</p>
   *
   *  @param descriptionFormat The textual, possibly multi-line, description of the possible option values.
   *                           If simply a literal value, it should be surrounded with single quotes, e.g. 'info'.
   *  @param descriptionParams Any parameters to be filled into the descriptionFormat.
   *
   *  @return <code>this</code> so method chaining is possible.
   */
  public UsageTextBuilder addOptionDefaultValueDescription(String descriptionFormat, Object... descriptionParams) {
    if (this.currentOptSyntax == null) {
      throw new IllegalStateException("Before using this method, startOption must be invoked first");
    }
    Contract.isNotNull("descriptionFormat", descriptionFormat);
    this.currentOptDefaultValueDescription = String.format(descriptionFormat, descriptionParams);
    return this;
  }

  /**
   *  End the current option description.
   *  <p>
   *  Must follow a {@link #startOption(String)} invocation.</p>
   *
   *  @return <code>this</code> so method chaining is possible.
   */
  public UsageTextBuilder endOption() {
    if (this.currentOptSyntax == null) {
      throw new IllegalStateException("Before using this method, startOption must be invoked first");
    }
    String optSyntax = this.currentOptSyntax;
    this.currentOptSyntax = null;
    this.addOptionInfo(optSyntax, this.currentOptDescription, this.currentOptValidValuesDescription, this.currentOptDefaultValueDescription);
    this.currentOptDefaultValueDescription = null;
    this.currentOptDescription = null;
    this.currentOptValidValuesDescription = null;
    return this;
  }

  /**
   *  Add a complete option description, including its syntax, semantic description, possible values and default value.
   *  <p>
   *  Also implicitly ends any previously started option description.</p>
   *
   *  @param syntax The option syntax which should be described as two dashes, followed by the name, and then optionally followed by an equal sign and
   *                an all uppercase word indicating the needed values type. E.g. <code>--logging.console.level=LOGLEVEL</code>
   *  @param meaning The textual, possibly multi-line, description of the meaning of this option.
   *  @param validValues The textual, possibly multi-line, description of the possible option values.
   *  @param defaultValue The textual, possibly multi-line, description of the possible option values.
   *                      If simply a literal value, it should be surrounded with single quotes, e.g. 'info'.
   *
   *  @return <code>this</code> so method chaining is possible.
   */
  public UsageTextBuilder addOptionInfo(String syntax, String meaning, String validValues, String defaultValue) {
    if (this.currentOptSyntax != null) { // End started option description
      this.endOption();
    }
    this.tb.reset();
    this.tb.appendLine(syntax);
    try (TextBuilder.Group tg = this.tb.start()) {
      if (meaning != null) {
        this.tb.appendLine(meaning);
      }
      if (validValues != null) {
        final String prefix = "Valid values: ";
        if (validValues.length() < (this.maxLineSize - prefix.length())) {
          this.tb.appendLine("%s%s", prefix, validValues);
        }
        else {
          this.tb.appendLine(prefix);
          try (TextBuilder.Group g = this.tb.start()) {
            this.tb.appendLine(validValues);
          }
        }
      }
      {
        final String prefix = "Default value: ";
        if (defaultValue == null) {
          this.tb.appendLine("%s<none>", prefix);
        }
        else if (defaultValue.length() < (this.maxLineSize - prefix.length())) {
          this.tb.appendLine("%s%s", prefix, defaultValue);
        }
        else {
          this.tb.appendLine(prefix);
          try (TextBuilder.Group g = this.tb.start()) {
            this.tb.appendLine(defaultValue);
          }
        }
      }
    }
    String group = this.currentOptionGroup.peek();
    String val = StringUtils.trimRight(this.tb.toString());
    this.groupedOptionLines.get(group).add(val);
    return this;
  }

  /**
   *  End the current option group, hereby re-enabling the last (or the default) option group and also ending any start option description and
   *  still adding it to the current option group.
   *
   *  @return <code>this</code> so method chaining is possible.
   */
  public UsageTextBuilder endOptionGroup() {
    if (this.currentOptSyntax != null) { // End started option description
      this.endOption();
    }
    if (this.currentOptionGroup.size() > 1) { // Keep empty group available
      this.currentOptionGroup.pop();
    }
    return this;
  }

  /**
   *  Start an extra info group description.
   *  <p>
   *  Also implicitly ends any previously started group or option.</p>
   *
   *  @param groupTitle The title of the group. Should be capitalized and end with 'Settings', e.g. <code>Logging Settings</code>.
   *                    <p>If this title doesn't start with an uppercase letter, it will be capitalized. If it doesn't end with ':', a ':' will be added.</p>
   *  @return <code>this</code> so method chaining is possible.
   */
  public UsageTextBuilder startExtraInfoGroup(String groupTitle) {
    Contract.isNotNull("groupTitle", groupTitle);
    this.endOptionGroup();
    if (!this.groupedExtraLines.containsKey(groupTitle)) {
      this.extraGroups.add(groupTitle);
      this.groupedExtraLines.put(groupTitle, new ArrayList<>());
    }
    this.currentExtraGroup.push(groupTitle);
    return this;
  }

  /**
   *  Add a additional information to the current 'extra info' group or an implicit and unnamed 'extra info' group.
   *
   *  @param descriptionFormat The textual, possibly multi-line, description to.
   *  @param descriptionParams Any parameters to be filled into the descriptionFormat.
   *
   *  @return <code>this</code> so method chaining is possible.
   */
  public UsageTextBuilder addExtraInfo(String descriptionFormat, Object... descriptionParams) {
    Contract.isNotNull("descriptionFormat", descriptionFormat);
    this.tb.reset();
    TextBuilder.Group g = null;
    if (!"".equals(this.currentExtraGroup.peek())) {
      g = this.tb.start();
    }
    this.tb.appendLine(descriptionFormat, descriptionParams);
    if (g != null) {
      g.close();
    }
    String group = this.currentExtraGroup.peek();
    String val = StringUtils.trimRight(this.tb.toString());
    this.groupedExtraLines.get(group).add(val);
    return this;
  }

  /**
   *  End the current extra info group.
   *
   *  @return <code>this</code> so method chaining is possible.
   */
  public UsageTextBuilder endExtraInfoGroup() {
    if (this.currentExtraGroup.size() > 1) { // Keep empty group available
      this.currentExtraGroup.pop();
    }
    return this;
  }

  private static String makeSettingsTitle(String text) {
    if ((text.length() > 0) && Character.isUpperCase(text.charAt(0))) {
      if (text.charAt(text.length() - 1) == ':') { // Nothing to be done
        return text;
      }
      return text + ":"; // Only have to add :
    }
    // Capitalize all words
    StringBuilder tb = new StringBuilder();
    String[] titleWords = text.split("\\s+", 0);
    for (int i = 0; i < titleWords.length; i++) {
      String tw = titleWords[i];
      if (i > 0) {
        tb.append(" ");
      }
      tb.append(StringUtils.capitalize(tw));
    }
    if (!text.endsWith(":")) {
      tb.append(":");
    }
    return tb.toString();
  }

  /**
   *  Build and return the full usage text.
   *  <p>
   *  Following processing will happen:</p><ul>
   *    <li>If there are syntax lines, they will be preprended with <code>Usage:</code>.</li>
   *    <li>Option group names will get capitalized and suffixed with a colon</li>
   *  </ul>
   *
   *  @return The full usage text as currently built.
   */
  @Override
  public String toString() {
    this.endOptionGroup();
    this.endExtraInfoGroup();

    final String NL = EnvironmentUtils.getLineSeparator();
    StringBuilder sb = new StringBuilder();
    // Summary syntax
    if (!this.syntaxLines.isEmpty()) {
      sb.append("Usage:");
      sb.append(NL);
      for (int i = 0; i < this.syntaxLines.size(); i++) {
        sb.append(this.syntaxLines.get(i));
        sb.append(NL);
      }
      sb.append(NL);
    }
    // Option groups
    if (!this.optionGroups.isEmpty()) {
      for (String g : this.optionGroups) {
        List<String> gl = this.groupedOptionLines.get(g);
        if (!gl.isEmpty()) {
          if (!g.isEmpty()) {
            sb.append(makeSettingsTitle(g));
            sb.append(NL);
          }
          for (String l : gl) {
            sb.append(l);
            sb.append(NL);
          }
          sb.append(NL);
        }
      }
    }
    // Extra info groups
    if (!this.extraGroups.isEmpty()) {
      for (String g : this.extraGroups) {
        List<String> gl = this.groupedExtraLines.get(g);
        if (!gl.isEmpty()) {
          if (!g.isEmpty()) {
            sb.append(makeSettingsTitle(g));
            sb.append(NL);
          }
          for (String l : gl) {
            sb.append(l);
            sb.append(NL);
          }
          sb.append(NL);
        }
      }
    }
    return StringUtils.trimRight(sb.toString());
  }
}
