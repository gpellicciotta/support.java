// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.config;

import java.util.*;

import com.pellicciotta.support.text.TextBuilder;
import com.pellicciotta.support.utils.XmlUtils;
import org.w3c.dom.Element;

/**
 *  A group of {@link Setting}s and other {@link SettingsGroup}s.
 */
public class SettingsGroup<T extends SettingsGroup> extends ConfigElement
{
  // <editor-fold desc="Construction">

  protected SettingsGroup(String name, String description, boolean autoDiscoverChildElements) throws Exception {
    super(name, description);
    if (autoDiscoverChildElements) {
      this.addChildren(new SettingsFactory().discoverElements(this));
    }
  }

  protected SettingsGroup(String name, String description, ConfigElement... childElements) {
    super(name, description);
    if ((childElements != null) && (childElements.length > 0)) {
      this.addChildren(childElements);
    }
  }

  // </editor-fold>
  // <editor-fold desc="ConfigElement Overrides">

  @Override
  protected boolean isElementary() { return false; }

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

  protected void removeChild(ConfigElement newChild) {
    if (this.children.remove(newChild)) {
      newChild.setParent(null);
    }
  }

  protected void addChild(ConfigElement newChild) {
    if (!this.children.contains(newChild)) {
      this.children.add(newChild);
      newChild.setParent(this);
    }
  }

  protected void addChildren(ConfigElement... newChildren) {
    for (ConfigElement newChild : newChildren) {
      newChild.setParent(this);
    }
  }

  // </editor-fold>
  // <editor-fold desc="Loading & Saving">

  protected void reset() {
    for (ConfigElement child : this.children) {
      child.reset();
    }
  }

  protected void loadFrom(Element xmlParent) {
    Element groupEl = XmlUtils.getParentOrChildWithTagName(xmlParent, this.getName());
    if (groupEl != null) {
      for (ConfigElement child : this.children) {
        child.loadFrom(groupEl);
      }
    }
  }

  protected void saveTo(Element xmlParent, boolean saveDefaultValues) {
    Element groupEl = xmlParent.getOwnerDocument().createElement(this.getName());
    for (ConfigElement child : this.children) {
      child.saveTo(groupEl, saveDefaultValues);
    }
    xmlParent.appendChild(groupEl);
  }

  protected void loadFrom(Map<String, String> props) {
    for (ConfigElement child : this.children) {
      child.loadFrom(props);
    }
  }

  protected void saveTo(Map<String, String> props, boolean saveDefaultValues) {
    for (ConfigElement child : this.children) {
      child.saveTo(props, saveDefaultValues);
    }
  }

  // </editor-fold>
  // <editor-fold desc="Debugging Aids">

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
