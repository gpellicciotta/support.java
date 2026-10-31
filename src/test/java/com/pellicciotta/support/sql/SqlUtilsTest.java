// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.sql;

import java.util.List;
import java.util.stream.Collectors;

import com.pellicciotta.support.text.SyntaxToken;
import com.pellicciotta.support.text.SyntaxType;
import com.pellicciotta.support.utils.CollectionUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import static com.pellicciotta.support.sql.SqlSyntaxType.*;
import static com.pellicciotta.support.sql.SqlSyntaxUtils.*;

public class SqlUtilsTest
{
  @Test
  public void testBasicClauseParsing() throws Exception {
    String sqlText = "Select count(*)FROM foo.bar  /* A\nmulti-line\nblock-comment */     WHERE A > ? and (C + D / E * F < :c-val) -- A line comment too\nORDER BY \"A\" ASC, `b` DESC, [c] ASC LIMIT 7";
    SqlTokenizer t = new SqlTokenizer();
    List<SyntaxToken> tokens = CollectionUtils.listFromIterator(t.tokenize(sqlText));
    SyntaxType[] tokenTypes = tokens.stream().filter((tok) -> tok.getSyntaxType() != WHITESPACE).map((tok) -> tok.getSyntaxType()).collect(Collectors.toList()).toArray(new SyntaxType[0]);

    Assertions.assertArrayEquals(CollectionUtils.asArray(KEYWORD /*Select*/, KEYWORD, LEFT_PAREN, OPERATOR, RIGHT_PAREN,
        KEYWORD /*FROM*/, IDENTIFIER, BLOCK_COMMENT,
        KEYWORD /*WHERE*/, IDENTIFIER, OPERATOR, POSITIONAL_PARAMETER, OPERATOR, LEFT_PAREN, IDENTIFIER, OPERATOR, IDENTIFIER, OPERATOR, IDENTIFIER, OPERATOR, IDENTIFIER, OPERATOR, NAMED_PARAMETER, RIGHT_PAREN, LINE_COMMENT,
        KEYWORD /*ORDER BY*/, IDENTIFIER, KEYWORD, COMMA, IDENTIFIER, KEYWORD, COMMA, IDENTIFIER, KEYWORD,
        KEYWORD /* LIMIT */, NUMBER_LITERAL),
        tokenTypes);

    SqlParser parser = new SqlParser(tokens);

    SqlParser.OrderByClause orderBy = parser.findOrderByClause();
    Assertions.assertNotNull(orderBy);
    Assertions.assertEquals(3, orderBy.orderBySpecs.length);
    Assertions.assertEquals("[c] ASC", orderBy.orderBySpecs[2].toString());

    SqlParser.SelectClause select = parser.findSelectClause();
    Assertions.assertNotNull(select);
    Assertions.assertEquals("SELECT COUNT ( * )", select.toString());

    SqlParser.FromClause from = parser.findFromClause();
    Assertions.assertNotNull(from);
    Assertions.assertEquals("FROM foo.bar", from.toString());

    SqlParser.WhereClause where = parser.findWhereClause();
    Assertions.assertNotNull(where);
    Assertions.assertEquals("WHERE A > ? AND ( C + D / E * F < :c-val )", where.toString());
  }

  @Test
  public void testNormalize() throws Exception {
    String sqlText = "Select count(*)FROM foo.bar  /* A\nmulti-line\nblock-comment */     WHERE A > ? and (C + D / E * F < :c-val) -- A line comment too\nORDER BY \"A\" ASC, `b` DESC, [c] ASC LIMIT 7";
    Assertions.assertEquals("SELECT COUNT ( * ) FROM foo.bar WHERE A > ? AND ( C + D / E * F < :c-val ) ORDER BY \"A\" ASC , `b` DESC , [c] ASC LIMIT 7", normalizeText(sqlText));
  }

  @Test
  public void testAddingWhereCondition1() throws Exception {
    String sqlText = normalizeText("SELECT A FROM B WHERE C > ? ORDER BY D ASC, E DESC");
    Assertions.assertEquals("SELECT A FROM B WHERE C > ? ORDER BY D ASC , E DESC", sqlText);

    String updatedSqlText = addWhereCondition(sqlText, "(F IS NOT NULL)");
    Assertions.assertEquals("SELECT A FROM B WHERE ( C > ? ) AND ( F IS NOT NULL ) ORDER BY D ASC , E DESC", updatedSqlText);
  }

  @Test
  public void testAddingWhereCondition2() throws Exception {
    String sqlText = normalizeText("SELECT A FROM B WHERE C > ? GROUP BY E, F");
    Assertions.assertEquals("SELECT A FROM B WHERE C > ? GROUP BY E , F", sqlText);

    String updatedSqlText = addWhereCondition(sqlText, "(F IS NOT NULL)");
    Assertions.assertEquals("SELECT A FROM B WHERE ( C > ? ) AND ( F IS NOT NULL ) GROUP BY E , F", updatedSqlText);
  }

  @Test
  public void testAddingWhereCondition3() throws Exception {
    String sqlText = normalizeText("SELECT A FROM B WHERE C > ? LIMIT 7");
    Assertions.assertEquals("SELECT A FROM B WHERE C > ? LIMIT 7", sqlText);

    String updatedSqlText = addWhereCondition(sqlText, "(F IS NOT NULL)");
    Assertions.assertEquals("SELECT A FROM B WHERE ( C > ? ) AND ( F IS NOT NULL ) LIMIT 7", updatedSqlText);
  }

  @Test
  public void testAddingWhereCondition4() throws Exception {
    String sqlText = normalizeText("SELECT A FROM B WHERE C > ?");
    Assertions.assertEquals("SELECT A FROM B WHERE C > ?", sqlText);

    String updatedSqlText = addWhereCondition(sqlText, "(F IS NOT NULL)");
    Assertions.assertEquals("SELECT A FROM B WHERE ( C > ? ) AND ( F IS NOT NULL )", updatedSqlText);
  }

  @Test
  public void testAddingWhereCondition5() throws Exception {
    String sqlText = normalizeText("SELECT A FROM B ORDER BY E ASC");
    Assertions.assertEquals("SELECT A FROM B ORDER BY E ASC", sqlText);

    String updatedSqlText = addWhereCondition(sqlText, "(F IS NOT NULL)");
    Assertions.assertEquals("SELECT A FROM B WHERE ( F IS NOT NULL ) ORDER BY E ASC", updatedSqlText);
  }

  @Test
  public void testAddingWhereCondition6() throws Exception {
    String sqlText = normalizeText("SELECT A FROM B GROUP BY F");
    Assertions.assertEquals("SELECT A FROM B GROUP BY F", sqlText);

    String updatedSqlText = addWhereCondition(sqlText, "(F IS NOT NULL)");
    Assertions.assertEquals("SELECT A FROM B WHERE ( F IS NOT NULL ) GROUP BY F", updatedSqlText);
  }

  @Test
  public void testAddingWhereCondition7() throws Exception {
    String sqlText = normalizeText("SELECT A GROUP BY F");
    Assertions.assertEquals("SELECT A GROUP BY F", sqlText);

    String updatedSqlText = addWhereCondition(sqlText, "(F IS NOT NULL)");
    Assertions.assertEquals("SELECT A WHERE ( F IS NOT NULL ) GROUP BY F", updatedSqlText);
  }

  @Test
  public void testAddingWhereCondition8() throws Exception {
    String sqlText = normalizeText("SELECT A");
    Assertions.assertEquals("SELECT A", sqlText);

    String updatedSqlText = addWhereCondition(sqlText, "(F IS NOT NULL)");
    Assertions.assertEquals("SELECT A WHERE ( F IS NOT NULL )", updatedSqlText);
  }

  @Test
  public void testAddingWhereCondition9() throws Exception {
    String sqlText = normalizeText("UPDATE A SET FOO = BAR");
    Assertions.assertEquals("UPDATE A SET FOO = BAR", sqlText);

    String updatedSqlText = addWhereCondition(sqlText, "(FOO = 1)");
    Assertions.assertEquals("UPDATE A SET FOO = BAR WHERE ( FOO = 1 )", updatedSqlText);
  }

  @Test
  public void testAddingWhereCondition10() throws Exception {
    String sqlText = normalizeText("DELETE FROM A");
    Assertions.assertEquals("DELETE FROM A", sqlText);

    String updatedSqlText = addWhereCondition(sqlText, "(F IS NOT NULL)");
    Assertions.assertEquals("DELETE FROM A WHERE ( F IS NOT NULL )", updatedSqlText);
  }

  @Test
  public void testChangingOrderBy1() throws Exception {
    String sqlText = normalizeText("ORDER BY A DESC, B ASC, C DESC");
    Assertions.assertEquals("ORDER BY A DESC , B ASC , C DESC", sqlText);

    String updatedSqlText = reverseOrderBy(sqlText);
    Assertions.assertEquals("ORDER BY A ASC , B DESC , C ASC", updatedSqlText);
  }

  @Test
  public void testChangingOrderBy2() throws Exception {
    String sqlText = normalizeText("SELECT FROM G WHERE FOO > BAR ORDER BY A DESC, B ASC, C DESC LIMIT 3 4");
    Assertions.assertEquals("SELECT FROM G WHERE FOO > BAR ORDER BY A DESC , B ASC , C DESC LIMIT 3 4", sqlText);

    String updatedSqlText = reverseOrderBy(sqlText);
    Assertions.assertEquals("SELECT FROM G WHERE FOO > BAR ORDER BY A ASC , B DESC , C ASC LIMIT 3 4", updatedSqlText);
  }

  @Test
  public void testCreatingPagingCondition1() throws Exception {
    String sqlText = normalizeText("ORDER BY A DESC");
    Assertions.assertEquals("ORDER BY A DESC", sqlText);

    String updatedSqlText = createPagingWhereCondition(sqlText, false, "bookmark.val");
    Assertions.assertEquals("( A < bookmark.val1 )", updatedSqlText);
  }

  @Test
  public void testCreatingPagingCondition2() throws Exception {
    String sqlText = normalizeText("ORDER BY A ASC, B DESC, C ASC");
    Assertions.assertEquals("ORDER BY A ASC , B DESC , C ASC", sqlText);

    String updatedSqlText = createPagingWhereCondition(sqlText, false, "bookmark.val");
    Assertions.assertEquals("( A > bookmark.val1 ) OR ( ( A = bookmark.val1 ) AND ( B < bookmark.val2 ) ) OR ( ( A = bookmark.val1 ) AND ( B = bookmark.val2 ) AND ( C > bookmark.val3 ) )", updatedSqlText);
  }

  @Test
  public void testCreatePagingWhereCondition() throws Exception {
    String sqlText = normalizeText("ORDER BY A DESC, B ASC, C DESC");
    Assertions.assertEquals("ORDER BY A DESC , B ASC , C DESC", sqlText);

    String whereCondition = createPagingWhereCondition(sqlText, false, "bookmark.val");
    Assertions.assertEquals("( A < bookmark.val1 ) OR ( ( A = bookmark.val1 ) AND ( B > bookmark.val2 ) ) OR ( ( A = bookmark.val1 ) AND ( B = bookmark.val2 ) AND ( C < bookmark.val3 ) )", whereCondition);

    String reverseWhereCondition = createPagingWhereCondition(sqlText, true, "bookmark.val");
    Assertions.assertEquals("( A > bookmark.val1 ) OR ( ( A = bookmark.val1 ) AND ( B < bookmark.val2 ) ) OR ( ( A = bookmark.val1 ) AND ( B = bookmark.val2 ) AND ( C > bookmark.val3 ) )", reverseWhereCondition);
  }

  @Test
  public void testCreatePagingStatement1() throws Exception {
    String pagingStmt = createIdColumnBasedPagingStatement("SELECT * FROM D ORDER BY A DESC, B ASC", "ID", false);
    Assertions.assertEquals("WITH bookmark ( val1 , val2 ) AS ( SELECT A , B FROM D WHERE ID = :bookmark-id-val ) SELECT * FROM D , bookmark WHERE ( A < bookmark.val1 ) OR ( ( A = bookmark.val1 ) AND ( B > bookmark.val2 ) ) ORDER BY A DESC , B ASC", pagingStmt);
  }

  @Test
  public void testCreatePagingStatement2() throws Exception {
    String pagingStmt = createIdColumnBasedPagingStatement("SELECT * FROM D WHERE A BETWEEN 1 AND 2 ORDER BY A DESC, B ASC", "ID", false);
    Assertions.assertEquals("WITH bookmark ( val1 , val2 ) AS ( SELECT A , B FROM D WHERE ID = :bookmark-id-val ) SELECT * FROM D , bookmark WHERE ( A BETWEEN 1 AND 2 ) AND ( ( A < bookmark.val1 ) OR ( ( A = bookmark.val1 ) AND ( B > bookmark.val2 ) ) ) ORDER BY A DESC , B ASC", pagingStmt);
  }

  @Test
  public void testCreatePagingStatement3() throws Exception {
    String pagingStmt = createIdColumnBasedPagingStatement("SELECT a.v1, b.v2 FROM a, b inner join a.id = b.id WHERE a.v1 > 33 ORDER BY b.v2 DESC", "ID", false);
    Assertions.assertEquals("WITH bookmark ( val1 ) AS ( SELECT b.v2 FROM a , b inner join a.id = b.id WHERE ID = :bookmark-id-val ) SELECT a.v1 , b.v2 FROM a , b inner join a.id = b.id , bookmark WHERE ( a.v1 > 33 ) AND ( ( b.v2 < bookmark.val1 ) ) ORDER BY b.v2 DESC", pagingStmt);
  }
}
