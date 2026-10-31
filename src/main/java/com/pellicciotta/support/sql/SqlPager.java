// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.sql;

/**
 *  Interface for paging through a typed resultset.
 *
 *  @param <T> The type of elements to page through.
 */
public interface SqlPager<T>
{
  /**
   *  @return The currently used page-size.
   */
  int getPageSize();

  /**
   *  Set the page size to be used from now on.
   *
   *  @param newSize New page size. Must be &gt;= 1.
   */
  void setPageSize(int newSize);

  /**
   *  The page this pager is currently positioned at.
   *  Initially the pager will not be positioned.
   *
   *  @return The current page or <c>null</c> if not positioned.
   */
  SqlPage<T> getCurrentPage();

  /**
   *  @return Whether there is a current page. Alternative for checking <c>getCurrentPage != null</c>.
   */
  boolean isPositioned();

  /**
   *  @return Whether a {@link #moveToNextPage()} will succeed.
   */
  boolean hasNextPage();

  /**
   *  @return Whether a {@link #moveToPreviousPage()} will succeed.
   */
  boolean hasPreviousPage();

  /**
   *  Move to the first page.
   *
   *  @return Whether the move succeeded and a new {@link #getCurrentPage()} is available.
   */
  boolean moveToFirstPage();

  /**
   *  Move to the next page, or to the first page if this pager is not positioned yet.
   *
   *  @return Whether the move succeeded and a new {@link #getCurrentPage()} is available.
   */
  boolean moveToNextPage();

  /**
   *  Move to the page following the provided bookmark.
   *
   *  @param prevEndBookmark The bookmark to use for positioning. The row with this bookmark will not be part of the resulting page but be positioned just before it the first result row.
   *  @return Whether the move succeeded and a new {@link #getCurrentPage()} is available.
   */
  boolean moveToPageAfter(String prevEndBookmark);

  /**
   *  Move to the first page or to the last page if this pager is not positioned yet.
   *
   *  @return Whether the move succeeded and a new {@link #getCurrentPage()} is available.
   */
  boolean moveToPreviousPage();

  /**
   *  Move to the page preceding the provided bookmark.
   *
   *  @param prevStartBookmark The bookmark to use for positioning. The row with this bookmark will not be part of the resulting page but be positioned just after the first result row.
   *
   *  @return Whether the move succeeded and a new {@link #getCurrentPage()} is available.
   */
  boolean moveToPageBefore(String prevStartBookmark);

  /**
   *  Move to the last page.
   *
   *  @return Whether the move succeeded and a new {@link #getCurrentPage()} is available.
   */
  boolean moveToLastPage();
}
