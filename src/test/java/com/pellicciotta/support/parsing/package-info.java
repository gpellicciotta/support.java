// Copyright (c) Giovanni Pellicciotta

/**
 *  Parsing package offering building blocks for:<ul>
 *    <li>tokenizing text into tokens</li>
 *    <li>parsing text into abstract syntax trees</li>
 *    <li>building symbol tables</li>
 *    <li>incremental re-parsing sub-ASTs</li>
 *    <li>incremental re-tokenizing sub-tokenstream-sequences</li>
 *  </ul>
 *  Parsing is a multi-layered process consisting of:<ol>
 *    <li>Splitting an input text in lines and keeping track of line-boundaries</li>
 *    <li>Turning input text into tokens</li>
 *    <li>Turning tokens into ASTs</li>
 *  </ol>
 *  The reverse is also possible:<ol>
 *    <li>Turning ASTs back into tokens</li>
 *    <li>Turning token-streams back into text</li>
 *  </ol>
 */
package com.pellicciotta.support.parsing;
