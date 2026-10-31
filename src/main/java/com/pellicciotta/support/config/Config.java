// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.config;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;

import com.pellicciotta.support.text.TextBuilder;
import com.pellicciotta.support.utils.StringUtils;
import com.pellicciotta.support.utils.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *  A configuration represents a collection of semantically related {@link Setting}s, potentially organized into {@link SettingsGroup}s.
 *  It provides operations for loading and storing the settings' data from external configuration sources.
 *  <p>
 *  It offers easy access to all individual {@link Setting}s, while also providing <code>load</code> and <code>save</code>
 *  operations that operate on all {@link Setting}s together.</p>
 */
public class Config {
  // <editor-fold desc="Construction">

  public Config(ConfigElement... elements) {
    this.initialize(elements);
  }

  public Config(List<ConfigElement> elements) {
    this.initialize(elements.toArray(new ConfigElement[0]));
  }

  private final List<ConfigElement> topLevelConfigElements = new ArrayList<>(); // All direct (= top-level) configuration elements contained in this configuration
  private final List<ConfigElement> configElementsInPreOrder = new ArrayList<>();
  private final Map<String, ConfigElement> fqnToConfigElements = new HashMap<>(); // FQN to setting, for all config elements
  private final Map<String, Setting> fqnToSettings = new HashMap<>(); // FQN to setting, for all elementary settings
  private final Map<String, SettingsGroup> fqnToGroups = new HashMap<>(); // FQN to setting, for all setting groups
  private final Map<String, String> partialToFQNs = new HashMap<>(); // Any unique partially qualified name that can be mapped to a FQN

  /**
   *  (Re)Initialize with the provided set of {@link ConfigElement}s.
   *
   *  @param children The {@link ConfigElement}s to be considered part of this {@link Config}.
   */
  protected void initialize(ConfigElement... children) {
    this.topLevelConfigElements.clear();
    this.fqnToConfigElements.clear();
    this.configElementsInPreOrder.clear();
    this.fqnToGroups.clear();
    this.fqnToSettings.clear();
    this.partialToFQNs.clear();
    this.addTopLevelConfigurationElements(children);
    this.determinePartialNames();
  }

  private void addTopLevelConfigurationElements(ConfigElement... children) {
    for (ConfigElement e : children) {
      if (e.getParent() != null) {
        String msg = String.format("Cannot add setting or settings group '%s' that is not a top-level element (i.e. it must not already be part of a group)", e.getFullName());
        throw new IllegalArgumentException(msg);
      }
      this.topLevelConfigElements.add(e);
      this.addConfigurationElement(e);
    }
  }

  private void addConfigurationElement(ConfigElement e) {
    String fqn = e.getFullName();
    if (this.fqnToConfigElements.containsKey(fqn)) {
      String msg = String.format("Cannot have a setting with FQN '%s' while another setting with that same FQN is used too", fqn);
      throw new IllegalArgumentException(msg);
    }
    this.fqnToConfigElements.put(fqn, e);
    e.config = this;
    if (e instanceof SettingsGroup) {
      SettingsGroup grp = (SettingsGroup) e;
      this.fqnToGroups.put(fqn, grp);
      this.configElementsInPreOrder.add(grp);
      for (Object cd : grp.getChildren()) {
        this.addConfigurationElement((ConfigElement) cd);
      }
    }
    else {
      assert (e instanceof Setting);
      this.fqnToSettings.put(fqn, (Setting) e);
      this.configElementsInPreOrder.add(e);
    }
  }

  private void determinePartialNames() {
    Set<String> duplicateNames = new HashSet<>();
    for (String fqn : this.fqnToConfigElements.keySet()) {
      for (String pqn : this.getPartiallyQualifiedNames(fqn)) {
        if (duplicateNames.contains(pqn)) { // Already a duplicate: so skip
          continue;
        }
        if (this.fqnToConfigElements.containsKey(pqn)) { // Overlaps a FQN: so skip
          continue;
        }
        if (this.partialToFQNs.containsKey(pqn)) { // Already a PQN: since there is a duplicate, remove as PQN
          duplicateNames.add(pqn);
          this.partialToFQNs.remove(pqn);
          continue;
        }
        // If we get here, we should keep and remember it
        this.partialToFQNs.put(pqn, fqn);
      }
    }
  }

  private String[] getPartiallyQualifiedNames(String fullyQualifiedName) {
    String[] parts = fullyQualifiedName.split("[.]");
    ArrayList<String> names = new ArrayList<>();
    for (int i = 1; i < parts.length; i++) { // Starting from 1 since we don't want to return the fully qualified name
      // From i'the part to last part
      StringBuilder nb = new StringBuilder();
      nb.append(parts[i]);
      for (int j = i + 1; j < parts.length; j++) {
        nb.append('.');
        nb.append(parts[j]);
      }
      names.add(nb.toString());
    }
    return names.toArray(new String[0]);
  }

  // </editor-fold>
  // <editor-fold desc="Finding/enumerating Settings" >

  /**
   * Get the names of all elementary {@link Setting}s.
   *
   * @return The set of names of elementary {@link Setting}s that are part of this {@link Config}.
   */
  public Set<String> elementarySettingNames() {
    return this.fqnToSettings.keySet();
  }

  /**
   * Get all {@link Setting}s that are contained (directly or indirectly) in this {@link Config}.
   *
   * @return An {@link Iterator} that will iterate over all such config elements; which can be none.
   */
  public Iterator<? extends Setting> elementarySettingsIterator() {
    return this.fqnToSettings.values().iterator();
  }

  /**
   * Get the {@link Setting} with the provided FQN.
   *
   * @param fqn The fully qualified setting name.
   *
   * @return Either the setting with the FQN or <code>null</code> if no such setting exists.
   */
  public Setting getSetting(String fqn) {
    return this.fqnToSettings.get(fqn);
  }

  /**
   * Get the names of all {@link SettingsGroup}s.
   *
   * @return The set of names of {@link SettingsGroup}s that are part of this {@link Config}.
   */
  public Set<String> settingsGroupNames() {
    return this.fqnToGroups.keySet();
  }

  /**
   * Get all {@link SettingsGroup}s that are contained (directly or indirectly) in this {@link Config}.
   *
   * @return An {@link Iterator} that will iterate over all such group elements; which can be none.
   */
  public Iterator<? extends SettingsGroup> settingsGroupsIterator() {
    return this.fqnToGroups.values().iterator();
  }

  /**
   * Get the {@link SettingsGroup} with the provided FQN.
   *
   * @param fqn The fully qualified setting group name.
   *
   * @return Either the settings group with the FQN or <code>null</code> if no such group exists.
   */
  public SettingsGroup getSettingsGroup(String fqn) {
    return this.fqnToGroups.get(fqn);
  }

  /**
   * Get all {@link ConfigElement}s that are contained (directly or indirectly) in this {@link Config},
   * in pre-order, which means: from top to bottom (i.e. first visiting groups, than their children)
   * and from left to right (i.e. in order of children).
   *
   * @return An {@link Iterator} that will iterate over all such config elements; which can be none.
   */
  public Iterator<? extends ConfigElement> configElementsIterator() {
    return this.configElementsInPreOrder.iterator();
  }

  /**
   * Get the {@link ConfigElement} with the provided FQN.
   *
   * @param fqn The fully qualified config element name.
   *
   * @return Either the setting or settings group with the FQN or <code>null</code> if no such element exists.
   */
  public ConfigElement getConfigElement(String fqn) {
    return this.fqnToConfigElements.get(fqn);
  }

  /**
   * Get the {@link SettingsGroup}s and {@link Setting}s that have a FQN that consists of
   * a single name.
   *
   * @return An {@link Iterable} that will iterate over all such config elements; which can be none.
   */
  public Iterator<? extends ConfigElement> topLevelConfigElementsIterator() {
    return this.topLevelConfigElements.iterator();
  }

  // </editor-fold>
  // <editor-fold desc="Event Listeners" >

  private final Set<ConfigEventListener> eventListeners = new HashSet<>();

  /**
   * Register the specified listener. If it was already registered, it will remain so and this operation has no effect.
   *
   * @param listener The listener to be registered with this {@link Config} object.
   */
  public void register(ConfigEventListener listener) {
    this.eventListeners.add(listener);
  }

  /**
   * Unregister the specified listener or do nothing if it was not registered.
   *
   * @param listener The listener to be unregistered from this {@link Config} object.
   */
  public void unregister(ConfigEventListener listener) {
    this.eventListeners.remove(listener);
  }

  private int loadOpsInProgress = 0;

  protected <T> void fireLoadStarted() {
    this.loadOpsInProgress += 1;
    if (this.loadOpsInProgress == 1) { // Was zero
      this.eventListeners.forEach(ConfigEventListener::onLoadStarted);
    }
  }

  protected <T> void fireLoadEnded() {
    this.loadOpsInProgress -= 1;
    if (this.loadOpsInProgress == 0) { // Was one
      this.eventListeners.forEach(ConfigEventListener::onLoadEnded);
    }
  }

  private int saveOpsInProgress = 0;

  protected <T> void fireSaveStarted() {
    this.saveOpsInProgress += 1;
    if (this.saveOpsInProgress == 1) { // Was zero
      this.eventListeners.forEach(ConfigEventListener::onSaveStarted);
    }
  }

  protected <T> void fireSaveEnded() {
    this.saveOpsInProgress -= 1;
    if (this.saveOpsInProgress == 0) { // Was one
      this.eventListeners.forEach(ConfigEventListener::onSaveEnded);
    }
  }

  protected <T> void fireValueChanged(Setting<T> setting, T oldValue, T newValue) {
    this.eventListeners.forEach(l -> l.onValueChanged(setting, oldValue, newValue));
  }

  // </editor-fold>
  // <editor-fold desc="Loading & Saving Setting Values" >

  /**
   * Reset to the built-in, default configuration where all settings have their default values.
   */
  public void reset() {
    for (ConfigElement e : this.topLevelConfigElements) {
      e.reset();
    }
  }

  /**
   *  Load and merge (i.e. no prior reset) settings from, in this order:<ol>
   *    <li>If the command line contains a <code>config-file</code> property: load all settings from that file first.</li>
   *    <li>Override any settings that are directly set on the command-line with properties or flags.
   *        For flags, the corresponding setting should have a {@link Boolean} type and the value will be set to true.</li>
   *    <li>If the command line also contains a <code>save-config-file</code> property, the current values of all settings (after the previous steps)
   *        will also get save to that file. This is useful to ensure we can replicate the exact same configuration for
   *        later runs, or also for debugging purposes.</li>
   *  </ol>
   *
   *  @param cli The command line to use for loading settings values.
   */
  public void loadFrom(CommandLine cli) throws IOException {
    try {
      this.fireLoadStarted();
      // First load config file:
      if (cli.hasPropertyValue("config-file")) {
        String configFilePath = cli.getLastPropertyValue("config-file");
        this.loadFrom(new File(configFilePath));
      }
      // Override with direct command line props:
      this.loadFrom(cli.getCombinedPropertyValues(true));
    }
    finally {
      this.fireLoadEnded();
    }
    if (cli.hasPropertyValue("save-config-file")) {
      String saveFilePath = cli.getLastPropertyValue("save-config-file");
      this.saveTo(new File(saveFilePath), true);
    }
  }

  /**
   *  Load and merge (i.e. no prior reset) settings from, in this order:<ol>
   *    <li>If the command line contains a <code>config-file</code> property: load all settings from that file first.</li>
   *    <li>Override any settings that are directly set on the command-line with properties or flags.
   *        For flags, the corresponding setting should have a {@link Boolean} type and the value will be set to true.</li>
   *    <li>If the command line also contains a <code>save-config-file</code> property, the current values of all settings (after the previous steps)
   *        will also get save to that file. This is useful to ensure we can replicate the exact same configuration for
   *        later runs, or also for debugging purposes.</li>
   *  </ol>
   *  <p>
   *  Any properties or flags provided on the command-line that don't correspond to a setting, will lead to
   *  a {@link java.text.ParseException} being thrown.</p>
   *
   *  @param cliArgs Command line arguments array.
   *  @return The positional arguments that were found on the command-line.
   */
  public String[] loadFrom(String[] cliArgs) throws IOException, ParseException {
    CommandLineParser clip = new CommandLineParser();
    CommandLine cli = clip.parse(cliArgs);
    this.loadFrom(cli);
    return cli.getArguments();
  }

  /**
   *  Load and merge (i.e. no prior reset) from the set of name/value pairs.
   *
   *  @param props The name/value pairs to use. Only names that correspond to this component's settings FQN will be used.
   *               Values may either be single Strings or Lists of strings.
   */
  public void loadFrom(Map<String, String> props) {
    try {
      this.fireLoadStarted();
      for (ConfigElement e : this.topLevelConfigElements) {
        e.loadFrom(props);
      }
    }
    finally {
      this.fireLoadEnded();
    }
  }

  /**
   *  Load configuration data from the provided XML element.
   *
   *  @param xmlParent The parent element of all setting data.
   */
  public void loadFrom(Element xmlParent) {
    try {
      this.fireLoadStarted();
      for (ConfigElement e : this.topLevelConfigElements) {
        e.loadFrom(xmlParent);
      }
    }
    finally {
      this.fireLoadEnded();
    }
  }

  /**
   *  Load configuration data from the provided XML document.
   *
   *  @param xmlDoc The XML document to load setting data from.
   */
  public void loadFrom(Document xmlDoc) {
    this.loadFrom(xmlDoc.getDocumentElement());
  }

  /**
   *  Load configuration data from the provided XML file.
   *
   *  @param xmlFile The file path to load data from.
   */
  public void loadFrom(File xmlFile) throws IOException {
    Document xmlDoc = XmlUtils.createDocument(xmlFile);
    this.loadFrom(xmlDoc);
  }

  /**
   *  Save the current setting values as a name/value pairs.
   *  <p>
   *  This is equivalent to <code>saveTo(props, true)</code>.</p>
   *
   *  @param props The map to add setting values too. Any added settings will have the setting's FQN as key.
   */
  public void saveTo(Map<String, String> props) {
    this.saveTo(props, true);
  }

  /**
   *  Save the current setting values as a name/value pairs.
   *
   *  @param props The map to add setting values too. Any added settings will have the setting's FQN as key.
   *  @param saveDefaultValues Whether to also save settings that still have their default value.
   */
  public void saveTo(Map<String, String> props, boolean saveDefaultValues) {
    try {
      this.fireSaveStarted();
      for (ConfigElement e : this.topLevelConfigElements) {
        e.saveTo(props, saveDefaultValues);
      }
    }
    finally {
      this.fireSaveEnded();
    }
  }

  /**
   *  Save the current setting values as a child element under the provided XML node.
   *  <p>
   *  This is equivalent to <code>saveTo(props, true)</code>.</p>
   *
   *  @param xmlParent The parent XML element to store settings under.
   *                   Any existing child elements that correspond to settings will either get updated or replaced.
   */
  public void saveTo(Element xmlParent) {
    this.saveTo(xmlParent, true);
  }

  /**
   *  Save the current setting values as a child element under the provided XML node.
   *
   *  @param xmlParent The parent XML element to store settings under.
   *                   Any existing child elements that correspond to settings will either get updated or replaced.
   *  @param saveDefaultValues Whether to also save settings that still have their default value.
   */
  public void saveTo(Element xmlParent, boolean saveDefaultValues) {
    try {
      this.fireSaveStarted();
      for (ConfigElement e : this.topLevelConfigElements) {
        e.saveTo(xmlParent, saveDefaultValues);
      }
    }
    finally {
      this.fireSaveEnded();
    }
  }

  /**
   *  Save the current setting values as an XML config file.
   *
   *  @param xmlFile The file path to use. Any existing file will be overwritten.
   *  @param saveDefaultValues Whether to also save settings that still have their default value.
   */
  public void saveTo(File xmlFile, boolean saveDefaultValues) throws IOException {
    try {
      this.fireSaveStarted();
      Document xmlDoc;
      xmlDoc = XmlUtils.createDocument("<configuration />");
      Element xmlParent = xmlDoc.getDocumentElement();
      for (ConfigElement e : this.topLevelConfigElements) {
        e.saveTo(xmlParent, saveDefaultValues);
      }
      Transformer transformer = TransformerFactory.newInstance().newTransformer();
      Result output = new StreamResult(xmlFile);
      Source input = new DOMSource(xmlDoc);
      transformer.setOutputProperty(OutputKeys.METHOD, "xml");
      transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
      transformer.transform(input, output);
    }
    catch (Exception e) {
      throw new IOException("Failed to write XML config file '" + xmlFile + "'", e);
    }
    finally {
      this.fireSaveEnded();
    }
  }

  // </editor-fold>
  // <editor-fold desc="Debugging Aids">

  /** {@inheritDoc} */
  @Override
  public String toString() {
    TextBuilder tb = new TextBuilder();
    this.addTo(tb);
    return tb.toString();
  }

  /**
   *  Provide a tabular overview of all settings.
   *
   *  @return The multi-line table representation.
   */
  public String toTableString() {
    SortedSet<String> sortedSettingNames = new TreeSet<>(this.elementarySettingNames());
    List<String[]> tableRows = new ArrayList<>();
    tableRows.add(new String[] { "Name", "Type", "State", "Value" });
    for (String settingName : sortedSettingNames) {
      String[] row = new String[4];
      row[0] = settingName;
      Setting setting = this.getSetting(settingName);
      String valueDefaultStr = "(default)";
      String valueStr = "(none)";
      if (setting instanceof ListSetting) {
        row[1] = "list of " + ((ListSetting)setting).getElementType().getSimpleName();
      }
      else {
        row[1] = setting.getType().getSimpleName();
      }
      Object value = setting.getValueOrDefault();
      if (setting.isValueSet()) {
        valueDefaultStr = "(set)";
      }
      if (value != null) {
        //noinspection unchecked
        valueStr = setting.toString(value);
      }
      row[2] = valueDefaultStr;
      row[3] = valueStr;
      tableRows.add(row);
    }
    StringBuilder sb = new StringBuilder();
    for (String line : StringUtils.makeTableLines(tableRows)) {
      sb.append(line);
      sb.append("\n");
    }
    return sb.toString();
  }

  public void addTo(TextBuilder tb) {
    if (this.topLevelConfigElements.isEmpty()) {
      tb.append("configuration is empty");
      return ;
    }
    tb.append("configuration contains %d groups and %d elementary settings:", this.fqnToGroups.size(), this.fqnToSettings.size());
    try (TextBuilder.Group g = tb.start()) {
      for (Iterator<? extends ConfigElement> it = this.topLevelConfigElementsIterator(); it.hasNext(); ) {
        ConfigElement el = it.next();
        tb.appendLine();
        if (el != null) {
          el.addTo(tb);
        }
        else {
          tb.append("%s", el);
        }
      }
    }
  }

  // </editor-fold>
}
