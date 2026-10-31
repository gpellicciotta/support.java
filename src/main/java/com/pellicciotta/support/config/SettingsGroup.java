// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.config;

import java.util.*;

import com.pellicciotta.support.text.TextBuilder;
import com.pellicciotta.support.utils.XmlUtils;
import org.w3c.dom.Element;

/**
 *  A group of {@link Setting}s and other {@link SettingsGroup}s.
 */
public class SettingsGroup<T extends SettingsGroup> extends ConfigElement {
  // <editor-fold desc="Construction">

  /**
   * Creates a group, optionally filling it with the settings and groups discovered by reflection on this object.
   *
   * @param name The simple name of the group.
   * @param description A human-readable description.
   * @param autoDiscoverChildElements Whether to discover child elements by reflection.
   * @throws Exception If discovery fails.
   */
  protected SettingsGroup(String name, String description, boolean autoDiscoverChildElements) throws Exception {
    super(name, description);
    if (autoDiscoverChildElements) {
      this.addChildren(new SettingsFactory().discoverElements(this));
    }
  }

  /**
   * Creates a group containing the given child elements.
   *
   * @param name The simple name of the group.
   * @param description A human-readable description.
   * @param childElements The initial children, may be <code>null</code> or empty.
   */
  protected SettingsGroup(String name, String description, ConfigElement... childElements) {
    super(name, description);
    if ((childElements != null) && (childElements.length > 0)) {
      this.addChildren(childElements);
    }
  }

  // </editor-fold>
  // <editor-fold desc="ConfigElement Overrides">

  @Override
  protected boolean isElementary() {
    return false;
  }

  // </editor-fold>
  // <editor-fold desc="SettingsGroup Interface">

  private final List<ConfigElement> children = new ArrayList<>();

  /**
   * @return The ordered list of child elements, which themselves can be individual {@link Setting}s or further groups,
   *         represented as {@link SettingsGroup}s.
   */
  public List<? extends ConfigElement> getChildren() {
    return this.children;
  }

  /**
   * @param childName The name of the child to retrieve.
   * @return The child with the provided name or <code>null</code> if no such child exists.
   */
  public ConfigElement getChild(String childName) {
    for (ConfigElement child : this.children) {
      if (child.getName().equals(childName)) {
        return child;
      }
    }
    return null;
  }

  /**
   * Removes the given child from this group and detaches it. Does nothing if it is not a child.
   *
   * @param newChild The child to remove.
   */
  protected void removeChild(ConfigElement newChild) {
    if (this.children.remove(newChild)) {
      newChild.setParent(null);
    }
  }

  /**
   * Adds the given child at the end of this group and makes this group its parent. Does nothing if it is already a child.
   *
   * @param newChild The child to add.
   */
  protected void addChild(ConfigElement newChild) {
    if (!this.children.contains(newChild)) {
      this.children.add(newChild);
      newChild.setParent(this);
    }
  }

  /**
   * Makes this group the parent of each of the given elements, adding them in order.
   *
   * @param newChildren The children to add.
   */
  protected void addChildren(ConfigElement... newChildren) {
    for (ConfigElement newChild : newChildren) {
      newChild.setParent(this);
    }
  }

  // </editor-fold>
  // <editor-fold desc="Loading & Saving">

  /** Resets all children to their default values. */
  protected void reset() {
    for (ConfigElement child : this.children) {
      child.reset();
    }
  }

  /**
   * Loads all children from the XML element named like this group, found in or directly below the given element. Does nothing if there is none.
   *
   * @param xmlParent The XML element holding the group's element.
   */
  protected void loadFrom(Element xmlParent) {
    Element groupEl = XmlUtils.getParentOrChildWithTagName(xmlParent, this.getName());
    if (groupEl != null) {
      for (ConfigElement child : this.children) {
        child.loadFrom(groupEl);
      }
    }
  }

  /**
   * Saves all children into a new XML element named like this group, appended to the given element.
   *
   * @param xmlParent The XML element to append to.
   * @param saveDefaultValues Whether settings still at their default value are saved as well.
   */
  protected void saveTo(Element xmlParent, boolean saveDefaultValues) {
    Element groupEl = xmlParent.getOwnerDocument().createElement(this.getName());
    for (ConfigElement child : this.children) {
      child.saveTo(groupEl, saveDefaultValues);
    }
    xmlParent.appendChild(groupEl);
  }

  /**
   * Loads all children from the given map of properties.
   *
   * @param props Property values keyed by full name.
   */
  protected void loadFrom(Map<String, String> props) {
    for (ConfigElement child : this.children) {
      child.loadFrom(props);
    }
  }

  /**
   * Saves all children into the given map of properties.
   *
   * @param props The map to store values in, keyed by full name.
   * @param saveDefaultValues Whether settings still at their default value are saved as well.
   */
  protected void saveTo(Map<String, String> props, boolean saveDefaultValues) {
    for (ConfigElement child : this.children) {
      child.saveTo(props, saveDefaultValues);
    }
  }

  // </editor-fold>
  // <editor-fold desc="Debugging Aids">

  /**
   * Appends a human-readable description of this group and its children to the given builder.
   *
   * @param tb The builder to append to.
   */
  public void addTo(TextBuilder tb) {
    if (this.children.isEmpty()) {
      tb.append("'%s' group is empty", this.getName());
      return ;
    }
    tb.append("'%s' group contains %d items:", this.getName(), this.children.size());
    try (TextBuilder.Group group = tb.start()) {
      for (ConfigElement child : this.children) {
        tb.appendLine();
        child.addTo(tb);
      }
    }
  }

  // </editor-fold>
}
