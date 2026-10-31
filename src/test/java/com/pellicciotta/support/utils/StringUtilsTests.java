// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.utils;

import java.util.ArrayList;
import java.util.List;

import com.pellicciotta.support.logging.LogService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StringUtilsTests
{
  @Test
  public void testMakePairTableLinesDefault() {
    List<Pair<String, String>> vals = new ArrayList<>();
    vals.add(Pair.of("setting-1", "a-very-long-value-for-setting-1"));
    vals.add(Pair.of("setting-2-with-long-name", "value-2"));

    String[] expectedLines = new String[] {
        "setting-1                : a-very-long-value-for-setting-1",
        "setting-2-with-long-name : value-2"
    };
    Assertions.assertArrayEquals(expectedLines, StringUtils.makePairTableLines(vals));
  }

  @Test
  public void testMakePairTableLinesWithPrefix() {
    List<Pair<String, String>> vals = new ArrayList<>();
    vals.add(Pair.of("setting-1", "a-very-long-value-for-setting-1"));
    vals.add(Pair.of("setting-2-with-long-name", "value-2"));

    String[] expectedLines = new String[] {
        "setting: setting-1                : a-very-long-value-for-setting-1",
        "setting: setting-2-with-long-name : value-2"
    };
    Assertions.assertArrayEquals(expectedLines, StringUtils.makePairTableLines(vals,"setting: "));
  }

  @Test
  public void testMakePairTableLinesWithPrefixAndSeparator() {
    List<Pair<String, String>> vals = new ArrayList<>();
    vals.add(Pair.of("setting-1", "a-very-long-value-for-setting-1"));
    vals.add(Pair.of("setting-2-with-long-name", "value-2"));

    String[] expectedLines = new String[] {
        "(setting-1               )=> a-very-long-value-for-setting-1",
        "(setting-2-with-long-name)=> value-2"
    };
    Assertions.assertArrayEquals(expectedLines, StringUtils.makePairTableLines(vals, "(", ")=> "));
  }

  @Test
  public void testMakePairTableLines() {
    List<Pair<String, String>> vals = new ArrayList<>();
    vals.add(Pair.of("setting-1", "a-very-long-value-for-setting-1"));
    vals.add(Pair.of("setting-2-with-long-name", "value-2"));

    String[] expectedLines = new String[] {
        "<tr><td>setting-1               </td><td>a-very-long-value-for-setting-1</td></tr>",
        "<tr><td>setting-2-with-long-name</td><td>value-2                        </td></tr>"
    };
    Assertions.assertArrayEquals(expectedLines, StringUtils.makePairTableLines(vals, "<tr><td>", "</td><td>", "</td></tr>"));
  }

  @Test
  public void testMakeTableLines() {
    List<String[]> rows = new ArrayList<>();
    rows.add(CollectionUtils.asArray("Letter", "Description", "Count", "Remarks"));
    rows.add(CollectionUtils.asArray("a", "The letter 'a'", "123"));
    rows.add(CollectionUtils.asArray("b", "The letter 'b'", "456", "No remarks"));
    rows.add(CollectionUtils.asArray("c", "The third letter", "7890", ""));

    String[] expectedLines = new String[] {
        "<tr><td>Letter</td><td>Description     </td><td>Count</td><td>Remarks   </td></tr>",
        "<tr><td>a     </td><td>The letter 'a'  </td><td>123  </td><td>          </td></tr>",
        "<tr><td>b     </td><td>The letter 'b'  </td><td>456  </td><td>No remarks</td></tr>",
        "<tr><td>c     </td><td>The third letter</td><td>7890 </td><td>          </td></tr>"
    };
    Assertions.assertArrayEquals(expectedLines, StringUtils.makeTableLines(rows, "<tr><td>", "</td><td>", "</td></tr>"));
  }

  @Test
  public void testMakeTableLinesForColumnsWithMultipleLines() {
    List<String[]> rows = new ArrayList<>();
    rows.add(CollectionUtils.asArray("Letter", "Description", "Count", "Remarks"));
    rows.add(CollectionUtils.asArray("a", "The letter 'a'", "123"));
    rows.add(CollectionUtils.asArray("b", "The letter 'b'", "456", "Remark #1\nRemark #2\nRemark #3"));
    rows.add(CollectionUtils.asArray("c", "The third letter", "7890", ""));

    String[] expectedLines = new String[] {
        "<tr><td>Letter</td><td>Description     </td><td>Count</td><td>Remarks  </td></tr>",
        "<tr><td>a     </td><td>The letter 'a'  </td><td>123  </td><td>         </td></tr>",
        "<tr><td>b     </td><td>The letter 'b'  </td><td>456  </td><td>Remark #1</td></tr>",
        "<tr><td>      </td><td>                </td><td>     </td><td>Remark #2</td></tr>",
        "<tr><td>      </td><td>                </td><td>     </td><td>Remark #3</td></tr>",
        "<tr><td>c     </td><td>The third letter</td><td>7890 </td><td>         </td></tr>"
    };
    Assertions.assertArrayEquals(expectedLines, StringUtils.makeTableLines(rows, "<tr><td>", "</td><td>", "</td></tr>"));
  }
}
