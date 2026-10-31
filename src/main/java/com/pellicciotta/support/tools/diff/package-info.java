// Copyright (c) Giovanni Pellicciotta

/**
 *  The <code>diff</code> command-line tool.
 *  {@link com.pellicciotta.support.tools.diff.DiffTool} compares two text files using one of the algorithms in
 *  {@code com.pellicciotta.support.diff} and prints the result in a
 *  {@link com.pellicciotta.support.tools.diff.DiffOutputFormat}
 *  (unified diff, side-by-side or JSON). Its options are held by
 *  {@code DiffToolConfig}; file loading and ignore-pattern filtering are handled
 *  by the package-private {@code DiffFile}.
 */
package com.pellicciotta.support.tools.diff;
