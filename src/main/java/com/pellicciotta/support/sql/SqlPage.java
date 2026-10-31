// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.sql;

import java.util.*;

import com.pellicciotta.support.utils.PagingResult;

/**
 *  Class representing a page of typed query results.
 */
public class SqlPage<T> implements PagingResult<T> {
  // <editor-fold desc="Construction">

  protected SqlPage(List<T> elements, int size, String startBookmark, String endBookmark) {
    this.elements = new ArrayList<>(elements);
    this.pageSize = size;
    this.startBookmark = startBookmark;
    this.endBookmark = endBookmark;
  }

  private final List<T> elements;
  private final int pageSize;
  private final String startBookmark;
  private final String endBookmark;

  // </editor-fold>
  // <editor-fold desc="Paging API">

  /**
   *  @return The current page's typed elements which can be less than the page size.
   */
  public List<T> getElements() {
    return this.elements;
  }

  /**
   *  @return The requested page size.
   */
  public int getPageSize() {
    return this.pageSize;
  }

  /**
   * @return The start bookmark that can be used in {@link SqlPager#moveToPageBefore(String)}.
   *         Can be <c>null</c> if this is the first page according to the base SQL query's order.
   */
  public String getStartBookmark() {
    return this.startBookmark;
  }

  /**
   * @return The end bookmark that can be used in {@link SqlPager#moveToPageAfter(String)}.
   *         Can be <c>null</c> if this is the last page according to the base SQL query's order.
   */
  public String getEndBookmark() {
    return this.endBookmark;
  }

  // </editor-fold>
  // <editor-fold desc="Debugging Support">

  @Override
  public String toString() {
    return String.format("Page with %d/%d elements [%s, %s]", this.elements.size(), this.pageSize, this.startBookmark, this.endBookmark);
  }

  // </editor-fold>
}
