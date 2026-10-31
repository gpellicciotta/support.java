// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.utils;

import java.io.*;
import java.util.*;

import com.pellicciotta.support.io.TextWriter;
import com.pellicciotta.support.text.TextBuilder;

/**
 *  A generic tree data structure.
 *  <p>
 *  A tree is really a tree node with the possibility of adding/removing newChild nodes. Child nodes
 *  know their parent and index within their parent.</p>
 */
public abstract class Tree<T extends Tree<T>> {
  // <editor-fold desc="Construction">

  /**
   * Create a new one-node tree.
   */
  public Tree() {
    this.parent = null;
    this.children = new ArrayList<>();
  }

  /**
   * Create a new tree as a copy of the given template.
   *
   * @param template Tree structure to copy
   */
  public Tree(T template) {
    Contract.isNotNull("template", template);
    this.parent = null;
    this.children = new ArrayList<>();
    for (T child : template.getChildren()) {
      this.addChild(child.copyOfTree());
    }
  }

  /**
   * Copy the current node, returning it as a standalone one-node tree. Attentiion: the actual tree structure (i.e. node's children)
   * is not copied. Use copyOfTree for that.
   *
   * @return A copy of the current node.
   */
  public abstract T copyOfNode();

  /**
   * Copy the tree starting at the current node, returning it as a standalone tree.
   *
   * @return A copy of the current tree.
   */
  public T copyOfTree() {
    T newTree = this.copyOfNode();
    for (T child : this.children) {
      newTree.addChild(child.copyOfTree());
    }
    return newTree;
  }

  // </editor-fold>
  // <editor-fold desc="Tree Structure">

  /** The parent node, or {@code null} for a root node. */
  protected T parent;
  /** The child nodes of this node, in insertion order. */
  protected List<T> children;

  /**
   * @return true if this tree node has no parent.
   */
  public boolean isRoot() {
    return (this.parent == null);
  }

  /**
   * @return true if this tree node has no children.
   */
  public boolean isLeaf() {
    return (this.children.isEmpty());
  }

  /**
   * @return The parent node, if any.
   */
  public T getParent() {
    return this.parent;
  }

  /**
   * @return This node's child index within the parent or -1 if there is no parent.
   */
  public int getChildIndex() {
    if (this.parent == null) {
      return -1;
    }
    return this.parent.children.indexOf(this);
  }

  /**
   * @return The current number of child nodes.
   */
  public int getChildCount() {
    return this.children.size();
  }

  /**
   * @return true When this node is not the only child of its parent. False if it has no parent.
   */
  public boolean hasSiblings() {
    if (this.parent == null) {
      return false;
    }
    return (this.parent.getChildCount() > 1);
  }

  /**
   * @return The number of siblings this node has. Will be zero if either it has no parent node or its parent node only has a single
   *         child.
   */
  public int getSiblingCount() {
    if (this.parent == null) {
      return 0;
    }
    return (this.parent.getChildCount() - 1);
  }

  /**
   * @param node The node to check against.
   * @return True if node is a sibling of the current node.
   */
  public boolean isSiblingOf(T node) {
    if (this.parent == null) {
      return false;
    }
    return this.parent.children.contains(node);
  }

  /**
   * @return The sibling at the previous index when viewed as children of their mutual parent, or null if there is no such sibling.
   */
  public T getPreviousSibling() {
    int childIdx = this.getChildIndex();
    if (childIdx > 0) {
      return this.parent.children.get(childIdx - 1);
    }
    return null;
  }

  /**
   * @return The sibling at the next index when viewed as children of their mutual parent, or null if there is no such sibling.
   */
  public T getNextSibling() {
    int childIdx = this.getChildIndex();
    if ((childIdx >= 0) && ((childIdx + 1) < this.parent.getChildCount())) {
      return this.parent.children.get(childIdx + 1);
    }
    return null;
  }

  /**
   * @param node The node to check against.
   * @return True if node is the parent or further ancestor node of the current node. False if node == this.
   */
  public boolean isDescendantOf(T node) {
    if (this == node) {
      return false;
    }
    if (this.parent == null) {
      return false;
    }
    if (this.parent.equals(node)) {
      return true;
    }
    return this.parent.isDescendantOf(node);
  }

  /**
   * @return The number of descendants (children, children's children, ...) of this node. This node itself is not counted.
   */
  public int getDescendantCount() {
    int cnt = 0;
    for (T c : this.children) {
      cnt += 1 /* the child itself */ + c.getDescendantCount();
    }
    return cnt;
  }

  /**
   * @param node The node to check against.
   * @return True if node is a descendant (meaning direct or indirect child) of the current node.
   */
  public boolean isAncestorOf(T node) {
    if (this == node) {
      return false;
    }
    if (this.children.contains(node)) {
      return true;
    }
    for (T c : this.children) {
      if (c.isAncestorOf(node)) {
        return true;
      }
    }
    return false;
  }

  /**
   * @return The number of ancestor (parent, parent's parent, ...) of this node. This node itself is not counted.
   */
  public int getAncestorCount() {
    if (this.parent != null) {
      return 1 + this.parent.getAncestorCount();
    }
    else {
      return 0;
    }
  }

  /**
   * @return The immutable list of child nodes. This list may be empty.
   */
  public List<T> getChildren() {
    return Collections.unmodifiableList(this.children);
  }

  /**
   * @param index The child-index.
   * @return The child node at the specified index.
   *
   * @throws IllegalArgumentException When index is invalid
   */
  public T getChild(int index) {
    Contract.isInRange("index", index, 0, this.children.size() - 1);
    return this.children.get(index);
  }

  /**
   * @return The first child node or null if there are no children.
   */
  public T getFirstChild() {
    if (this.children.size() > 0) {
      return this.children.get(0);
    }
    return null;
  }

  /**
   * @return The last child node or null if there are no children.
   */
  public T getLastChild() {
    if (this.children.size() > 0) {
      return this.children.get(this.children.size() - 1);
    }
    return null;
  }

  /**
   * Insert newChild at the specified index. Note that the newChild to be added must not already have a parent.
   *
   * @param index The index at which the child will be added.
   *              The child at the specified index and subsequent children will be shifted.
   * @param newChild The new to be added newChild.
   *
   * @throws IllegalArgumentException If newChild is null or already has a parent node or is equal to the current node.
   */
  @SuppressWarnings("unchecked")
  public void addChild(int index, T newChild) {
    Contract.isInRange("index", index, 0, this.children.size());
    Contract.isNotNull("newChild", newChild);
    Contract.isTrue("newChild.isRoot()", newChild.isRoot());
    Contract.isTrue("newChild != this", newChild != this);

    this.children.add(index, newChild);
    newChild.parent = (T) this;
  }

  /**
   * Add newChild as the last (or first if there were none before) child node. Note that the newChild to be added must not already
   * have a parent and not be equal to the current node.
   *
   * @param newChild The new to be added newChild.
   *
   * @throws IllegalArgumentException If newChild is null or already has a parent node.
   */
  public void addChild(T newChild) {
    this.addChild(this.children.size(), newChild);
  }

  /**
   * Insert newChild at the specified index. Note that the newChild to be added must not already have a parent.
   *
   * @param index The index at which the child will be added.
   *              The child at the specified index and subsequent children will be shifted.
   * @param newChildren The to be added child nodes.
   *
   * @throws IllegalArgumentException If any of newChildren is null or already has a parent node or is equal to the current node
   */
  @SuppressWarnings("unchecked")
  public void addChildren(int index, List<T> newChildren) {
    Contract.isInRange("index", index, 0, this.children.size());
    Contract.isNotNull("newChildren", newChildren);
    for (T newChild : newChildren) {
      Contract.isTrue("newChild.isRoot()", newChild.isRoot());
      Contract.isTrue("newChild != this", newChild != this);
    }

    for (int i = newChildren.size() - 1; i >= 0; i--) {
      T newChild = newChildren.get(i);
      this.children.add(index, newChild);
      newChild.parent = (T) this;
    }
  }

  /**
   * Insert newChildren at the end of the current sequence of children.
   *
   * @param newChildren The to be added child nodes.
   *
   * @throws IllegalArgumentException If any of newChildren is null or already has a parent node or is equal to the current node
   */
  public void addChildren(List<T> newChildren) {
    this.addChildren(this.children.size(), newChildren);
  }

  /**
   * Remove the child at the specified index, making it parent-less.
   *
   * @param index Index of child to be removed.
   *
   * @return The removed child.
   *
   * @throws IllegalArgumentException When index is invalid.
   */
  public T deleteChild(int index) {
    Contract.isInRange("index", index, 0, this.children.size() - 1);

    T removedChild = this.children.remove(index);
    removedChild.parent = null;
    return removedChild;
  }

  /**
   * Remove all children, making them parent-less.
   *
   * @return The removed children.
   */
  public List<T> deleteChildren() {
    List<T> removedChildren = new ArrayList<>();
    for (int i = this.children.size() - 1; i >= 0; i--) {
      T removedChild = this.children.remove(i);
      removedChild.parent = null;
      removedChildren.add(0, removedChild);
    }
    return removedChildren;
  }

  /**
   * Remove the children between the specified start and end indexes, making them parent-less.
   *
   * @param startIndex Index of first child to be removed.
   * @param endIndex Index of last child to be removed.
   *
   * @return The removed children.
   *
   * @throws IllegalArgumentException When startIndex or endIndex is invalid.
   */
  public List<T> deleteChildren(int startIndex, int endIndex) {
    Contract.isInRange("startIndex", startIndex, 0, endIndex);
    Contract.isInRange("endIndex", endIndex, 0, this.children.size() - 1);

    List<T> removedChildren = new ArrayList<>();
    for (int i = endIndex; i >= startIndex; i--) {
      T removedChild = this.children.remove(i);
      removedChild.parent = null;
      removedChildren.add(0, removedChild);
    }
    return removedChildren;
  }

  /**
   * Replaces the node at the specified index in the list of children with newChild. The replaced child will become parent-less.
   *
   * @param index Index of the child to replaced.
   * @param newChild New child node to be used as replacement.
   *
   * @return The replaced, and now parent-less, tree node.
   *
   * @throws IllegalArgumentException When index is invalid, or newChild is null or not parent-less.
   */
  @SuppressWarnings("unchecked")
  public T replaceChild(int index, T newChild) {
    Contract.isInRange("index", index, 0, this.children.size() - 1);
    Contract.isNotNull("newChild", newChild);
    Contract.isTrue("newChild.isRoot()", newChild.isRoot());

    T removedChild = this.children.set(index, newChild);
    removedChild.parent = null;
    newChild.parent = (T) this;
    return removedChild;
  }

  /**
   * Replaces the nodes between startIndex and endIndex with the newChildren nodes. The replaced children will become parent-less.
   * The newChildren must initially be parent-less.
   *
   * @param startIndex Index of first child to replaced.
   * @param endIndex Index of the last child to be replaced.
   * @param newChildren New child nodes to be used as replacement.
   *
   * @return The replaced, and now parent-less, tree nodes.
   *
   * @throws IllegalArgumentException When startIndex or endIndex is invalid, or newChildren is null or not all are parent-less.
   */
  public List<T> replaceChildren(int startIndex, int endIndex, List<T> newChildren) {
    List<T> removedChildren = this.deleteChildren(startIndex, endIndex);
    this.addChildren(startIndex, newChildren);
    return removedChildren;
  }

  /**
   * Copy the tree starting at node to the newParent, becoming newParent's child at the specified newIndex.
   *
   * @param <T> The type of tree nodes to take a copy of.
   *
   * @param node The tree to be copied
   * @param newIndex The child index within the newParent.
   * @param newParent The to-become parent-node.
   *
   * @throws IllegalArgumentException When index is invalid, node or newParent are null.
   */
  public static <T extends Tree<T>> void copyTree(T node, int newIndex, T newParent) {
    Contract.isNotNull("node", node);
    Contract.isNotNull("newParent", newParent);
    Contract.isInRange("newIndex", newIndex, 0, newParent.getChildCount());

    newParent.addChild(newIndex, node.copyOfTree());
  }

  /**
   * Move the tree starting at node to the newParent, becoming newParent's child at the specified newIndex. The node being moved
   * will be removed first from its parent, if it has a parent.
   *
   * @param <T> The type of tree to move in.
   *
   * @param node The node to be moved
   * @param newIndex The child index within the newParent.
   * @param newParent The to-become parent-node. This must not be a node that is a child of the current node.
   *
   * @throws IllegalArgumentException When index is invalid, node or newParent are null or newParent is a descendent of this node.
   */
  public static <T extends Tree<T>> void moveTree(T node, int newIndex, T newParent) {
    Contract.isNotNull("node", node);
    Contract.isNotNull("newParent", newParent);
    Contract.isInRange("newIndex", newIndex, 0, newParent.getChildCount());
    Contract.isTrue("!newParent.isDescendantOf(this)", !newParent.isDescendantOf(node));

    if (node.parent != null) {
      node.parent.deleteChild(node.getChildIndex());
    }
    newParent.addChild(newIndex, node);
  }

  /**
   * Move the tree starting at node to the newParent, becoming newParent's last child. The node being moved will be removed first
   * from its parent, if it has a parent.
   *
   * @param <T> The type of tree nodes to move in.
   *
   * @param node The node to be moved
   * @param newParent The to-become parent-node. This must not be a node that is a child of the current node.
   *
   * @throws IllegalArgumentException When node or newParent are null or newParent is a descendent of this node.
   */
  public static <T extends Tree<T>> void moveTree(T node, T newParent) {
    Contract.isNotNull("node", node);
    Contract.isNotNull("newParent", newParent);
    Contract.isTrue("!newParent.isDescendantOf(this)", !newParent.isDescendantOf(node));

    if (node.parent != null) {
      node.parent.deleteChild(node.getChildIndex());
    }
    newParent.addChild(node);
  }

  // </editor-fold>
  // <editor-fold desc="Search and Iteration">

  /**
   * Filter the tree of nodes starting at the current one, and return all accepted nodes as a list.
   *
   * @param <T> The type of tree to filter in.
   *
   * @param root The root node to start filtering at.
   * @param filter The filter function testing for acceptance.
   * @param childrenFirst Whether to first try to accept children or not. If true, a post-order tree traversal will be done where
   *          all nodes will be tested before their parent, and hence will also appear before their parent in the resulting list.
   * @param excludeChildrenOfAcceptedParent This parameter is only valid if childrenFirst==false and then it will not try to accept
   *          any children of an already accepted parent node.
   *
   * @return The list of accepted nodes, which is taken from the tree starting at the current node. No ancestors or siblings of the
   *         current node can ever be part of this list.
   */
  public static <T extends Tree<T>> List<T> find(T root, Filter<T> filter, boolean childrenFirst,
                                                 boolean excludeChildrenOfAcceptedParent) {
    List<T> accepted = new ArrayList<>();

    // Pre-order traversal:
    if (!childrenFirst) {
      boolean parentAccepted = filter.accept(root);
      if (parentAccepted) {
        accepted.add(root);
      }
      if (!parentAccepted || !excludeChildrenOfAcceptedParent) {
        for (T child : root.getChildren()) {
          accepted.addAll(find(child, filter, childrenFirst, excludeChildrenOfAcceptedParent));
        }
      }
    }

    // Post-order traversal:
    if (childrenFirst) {
      for (T child : root.getChildren()) {
        accepted.addAll(find(child, filter, childrenFirst, excludeChildrenOfAcceptedParent));
      }

      if (filter.accept(root)) {
        accepted.add(root);
      }
    }

    return accepted;
  }

  // </editor-fold>
  // <editor-fold desc="Comparison">

  /**
   *  @param otherNode The other node to compare against.
   *  @return Whether this node equal the otherNode.
   */
  public boolean nodeEquals(T otherNode) {
    return super.equals(otherNode);
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean equals(Object other) {
    if (other == null) {
      return false;
    }
    if (this == other) {
      return true;
    }
    if (!(other instanceof Tree)) {
      return false;
    }
    T otherTree = (T) other;
    return (this.parent == otherTree.parent) && this.nodeEquals(otherTree) && Checks.areEqual(this.children, otherTree.children);
  }

  /**
   *  @return The hash code for the current node.
   */
  public int nodeHashCode() {
    return super.hashCode();
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = (37 * hash) + this.nodeHashCode();
    hash = (37 * hash) + (this.children != null ? this.children.hashCode() : 0);
    return hash;
  }

  // </editor-fold>
  // <editor-fold desc="Text Representations">

  /**
   * @return A human-readable representation of the current node only. Will be used as part of toTreeString.
   */
  public String toNodeString() {
    return "";
  }

  /**
   * @return A human-readable representation of the tree starting at the current node.
   */
  public String toTreeString() {
    try {
      StringWriter sw = new StringWriter();
      TextWriter tw = new TextWriter(sw);
      tw.setPrefix("");
      this.toTreeString(tw);
      tw.close();
      return sw.toString();
    }
    catch (IOException e) {
      assert false; // We don't expect to ever get here
      return ""; //
    }
  }

  /**
   * Write a human-readable representation of the tree starting at the current node to the provided writer.
   *
   * @param w The writer to write to.
   *
   * @throws IOException When writing fails.
   */
  public void toTreeString(Writer w) throws IOException {
    if (w instanceof TextWriter) {
      this.toTreeString((TextWriter) w);
    }
    else {
      TextWriter tw = new TextWriter(w);
      this.toTreeString(tw);
      tw.flush();
    }
  }

  /**
   * Write a human-readable representation of the tree starting at the current node to the provided writer.
   *
   * @param tw The writer to write to.
   *
   * @throws IOException When writing fails.
   */
  public void toTreeString(TextWriter tw) throws IOException {
    String oldPrefix = tw.getPrefix();
    boolean oldIndentPrefix = tw.getIndentPrefix();
    String oldPrefixWithoutArrows = oldPrefix.replaceAll("[|][-][>]", "   ");
    tw.setIndentPrefix(true);
    tw.setPrefix(oldPrefixWithoutArrows + "|-> ");
    tw.setRepeatPrefix(oldPrefixWithoutArrows + "... ");
    tw.writeln(this.toNodeString());
    for (T c : this.children) {
      c.toTreeString(tw);
    }
    tw.setPrefix(oldPrefix);
    tw.setIndentPrefix(oldIndentPrefix);
  }

  /**
   * @return A human-readable representation.
   */
  @Override
  public String toString() {
    String rep = "";
    if (this.children.size() > 0) {
      rep += "(";
    }
    rep += this.toNodeString();
    for (T t : this.children) {
      rep += " " + t;
    }
    if (this.children.size() > 0) {
      rep += ")";
    }
    return rep;
  }

  // </editor-fold>
}
