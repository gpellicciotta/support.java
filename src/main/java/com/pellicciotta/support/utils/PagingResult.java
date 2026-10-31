// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.utils;

import java.util.List;

import com.pellicciotta.support.sql.SqlPager;

/**
 *  The result of a paging query where a particular page of results is returned.
 *
 *  @param <T> The type of query results.
 */
public interface PagingResult<T>
{
  /**
   * @return The start bookmark that can be used in {@link SqlPager#moveToPageBefore(String)}.
   *         Can be <c>null</c> if this is the first page according to the base SQL query's order.
   */
  String getStartBookmark();

  /**
   * @return The end bookmark that can be used in {@link SqlPager#moveToPageAfter(String)}.
   *         Can be <c>null</c> if this is the last page according to the base SQL query's order.
   */
  String getEndBookmark();

  /**
   * @return Number of query results per logical page.
   */
  int getPageSize();

  /**
   * @return The current page's typed elements which can be less than the page size.
   */
  List<T> getElements();
}
