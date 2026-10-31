// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.config;

import java.util.Map;

import com.pellicciotta.support.text.TextBuilder;
import org.w3c.dom.Element;

/**
 *  A configuration element is a node in a hierarchy of configuration elements, where
 *  the leaf nodes are individual {@link Setting}s and all non-leaf nodes are groups of settings
 *  represented as {@link SettingsGroup}s.
 *  <p>
 *  Each element has a name, a description and relations to its parent and/or children.</p>
 */
public abstract class ConfigElement {
  // <editor-fold desc="Construction">

  /**
   * Creates an element with the given name and description.
   *
   * @param name The simple name of the element.
   * @param description A human-readable description.
   */
  protected ConfigElement(String name, String description) {
    this.name = name;
    this.description = description;
  }

  // </editor-fold>
  // <editor-fold desc="Properties">

  private final String name;

  /**
   * @return The (simple) name of the current config element.
   */
  public String getName() {
    return this.name;
  }

  private String canonicalName;

  /**
   * @return The full name of the current element, which will be the dot-separated path starting with
   *         the ultimate ancestor and ending with the current element's name. If the element has no parent,
   *         the canonical name will be the same as its name.
   */
  public String getFullName() {
    if (this.canonicalName == null) {
      if (this.parent != null) {
        this.canonicalName = this.parent.getFullName() + "." + this.name;
      }
      else {
        this.canonicalName = this.name;
      }
    }
    return this.canonicalName;
  }

  private String description;

  /**
   * @return A human-readable description of the current configuration element.
   *         This can be a multi-line description detailing valid values representations and any defaults that apply.
   */
  public String getDescription() {
    return this.description;
  }

  /**
   * Replaces this element's description.
   *
   * @param newDescription The new description.
   */
  protected void setDescription(String newDescription) {
    this.description = newDescription;
  }

  private SettingsGroup parent;

  /**
   * @return The parent group, which always will be a {@link SettingsGroup}. Will be <code>null</code> for the root of a configuration.
   */
  public SettingsGroup getParent() {
    return this.parent;
  }

  /**
   * Moves this element under a new parent group. The element is removed from its previous parent, added to the new one and its cached full name is reset. Does nothing if the parent is unchanged.
   *
   * @param newParent The new parent, or <code>null</code> to detach the element.
   */
  protected void setParent(SettingsGroup newParent) {
    if (newParent == this.parent) {
      return ;
    }
    if (this.parent != null) {
      this.parent.removeChild(this);
    }
    this.parent = newParent;
    if (newParent != null) {
      newParent.addChild(this);
    }
    this.canonicalName = null;
  }

  /** @return Whether this element is a single setting (a leaf) rather than a group. */
  protected abstract boolean isElementary();

  /** @return Whether this element is a group, i.e. not elementary. */
  protected boolean isGroup() {
    return !this.isElementary();
  }

  // </editor-fold>
  // <editor-fold desc="Context">

  /** The configuration this element belongs to, if any. */
  protected Config config;

  // </editor-fold>
  // <editor-fold desc="Loading & Saving">

  /** Resets this element, and any children, to the default values. */
  protected abstract void reset();

  /**
   * Loads this element's value(s) from the given XML element.
   *
   * @param xmlParent The XML element holding the data.
   */
  protected abstract void loadFrom(Element xmlParent);

  /**
   * Saves this element's value(s) as child nodes of the given XML element.
   *
   * @param xmlParent The XML element to add nodes to.
   * @param saveDefaultValues Whether settings still at their default value are saved as well.
   */
  protected abstract void saveTo(Element xmlParent, boolean saveDefaultValues);

  /**
   * Loads this element's value(s) from the given map of properties.
   *
   * @param props Property values keyed by full name.
   */
  protected abstract void loadFrom(Map<String, String> props);

  /**
   * Saves this element's value(s) into the given map of properties.
   *
   * @param props The map to store values in, keyed by full name.
   * @param saveDefaultValues Whether settings still at their default value are saved as well.
   */
  protected abstract void saveTo(Map<String, String> props, boolean saveDefaultValues);

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
   * Appends a human-readable description of this element to the given builder.
   *
   * @param tb The builder to append to.
   */
  public abstract void addTo(TextBuilder tb);

  // </editor-fold>
}
