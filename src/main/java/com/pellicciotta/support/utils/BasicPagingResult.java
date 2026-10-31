// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.utils;

import java.util.List;

/**
 * Simple immutable {@link PagingResult} implementation.
 */
public class BasicPagingResult<T> implements PagingResult<T>
{
  /**
   *  Create a {@link PagingResult} giving the provided start and end bookmarks.
   *
   *  @param results The current page of results.
   *  @param pageSize The page size, which can be larger than {@code results.size()}, in which case the end book mark will be <c>null</c>.
   *  @param start Bookmark for the first result. Can be <c>null</c>.
   *  @param end Bookmark for the last result. Can be <c>null</c>.
   */
  public BasicPagingResult(String start, String end, List<T> results, int pageSize) {
    Contract.isNotNull("results", results);
    Contract.isTrue(pageSize >= results.size());
    this.pageSize = pageSize;
    this.results = results;
    this.startBookmark = start;
    this.endBookmark = end;
  }

  private final String startBookmark;

  /** {@inheritDoc} */
  @Override
  public String getStartBookmark() {
    return this.startBookmark;
  }

  private final String endBookmark;

  /** {@inheritDoc} */
  @Override
  public String getEndBookmark() {
    return this.endBookmark;
  }

  private final int pageSize;

  /** {@inheritDoc} */
  @Override
  public int getPageSize() {
    return this.pageSize;
  }

  private final List<T> results;

  /** {@inheritDoc} */
  @Override
  public List<T> getElements() {
    return this.results;
  }
}
