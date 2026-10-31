// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.sql;

import java.sql.*;
import java.util.*;
import java.util.Date;

import com.pellicciotta.support.logging.LogService;
import com.pellicciotta.support.utils.*;
import com.pellicciotta.support.utils.Random;
import org.junit.jupiter.api.*;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mariadb.MariaDBContainer;
import static com.pellicciotta.support.utils.DateTimeUtils.*;

@Testcontainers(disabledWithoutDocker = true)
public class SqlPagingTest
{
  @Container
  static final MariaDBContainer MARIADB = new MariaDBContainer("mariadb:11.4")
      .withDatabaseName("test")
      .withUsername("test")
      .withPassword("test");

  @BeforeAll
  public static void createDB() throws Exception {
    Connection conn = DriverManager.getConnection(MARIADB.getJdbcUrl(), MARIADB.getUsername(), MARIADB.getPassword());
    conn.setAutoCommit(false);
    SqlScriptFileReader sqlReader = new SqlScriptFileReader();
    try (SqlExecutor sql = new SqlExecutor(conn)) {
      sql.executeFromResourceFile("/sql/mysql/setup-db.sql", null, false);
      long beginDate = date(2009, 9, 13).getTime();
      long endDate = now().getTime();
      try (ParameterizedSqlStatement sqlStmt = sql.createStatement("INSERT INTO `score`(`game-name`, `player-name`, `score`, `achievement-time`) VALUES (:game, :player, :score, :date)")) {
        for (int i = 0; i < 100; i++) {
          String game = "game-" + Random.intInRange(1, 4);
          String player = "player-" + Random.intInRange(1, 9);
          int score = Random.intInRange(100, 1000);
          long achievementDate = Random.longInRange(beginDate, endDate);
          sqlStmt.setString("game", game);
          sqlStmt.setString("player", player);
          sqlStmt.setInt("score", score);
          sqlStmt.setTimestamp("date", new Timestamp(achievementDate));
          sqlStmt.addBatch();
        }
        sqlStmt.executeBatch();
        sql.commit();
      }
    }
  }

  @BeforeEach
  public void connect() throws Exception {
    Connection conn = DriverManager.getConnection(MARIADB.getJdbcUrl(), MARIADB.getUsername(), MARIADB.getPassword());
    conn.setAutoCommit(false);
    SqlScriptFileReader sqlReader = new SqlScriptFileReader();
    this.sql = new SqlExecutor(conn);
    this.sql.executeUpdate("USE `test`");
  }

  @AfterEach
  public void disconnect() throws Exception {
    this.sql.close();
  }

  private SqlExecutor sql;

  public static class Score
  {
    public Score(long id, String game, String player, long score, Date achievementTime) {
      this.id = id;
      this.game = game;
      this.player = player;
      this.score = score;
      this.achievementTime = achievementTime;
    }

    private final long id;
    private final String game;
    private final String player;
    private final long score;
    private final Date achievementTime;

    @Override
    public String toString() {
      return String.format("score { id: %d, game: \"%s\", player: \"%s\", score: %d, achievement-time: %s }", this.id, this.game, this.player, this.score, DateTimeUtils.formatDateTime(this.achievementTime));
    }

    public static Score fromRow(SqlRow row) {
      long id = row.getLong("id");
      String playerName = row.getString("player-name");
      String gameName = row.getString("game-name");
      long score = row.getLong("score");
      Date achievementTime = new Date (row.getTimestamp("achievement-time").getTime());
      return new Score(id, gameName, playerName, score, achievementTime);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || this.getClass() != o.getClass()) return false;
      Score score1 = (Score) o;
      return this.id == score1.id &&
          this.score == score1.score &&
          Objects.equals(this.game, score1.game) &&
          Objects.equals(this.player, score1.player) &&
          Objects.equals(this.achievementTime, score1.achievementTime);
    }

    @Override
    public int hashCode() {
      return Objects.hash(this.id, this.game, this.player, this.score, this.achievementTime);
    }
  }

  @Test
  public void testFetchAll()  {
    ParameterizedSqlStatement stmt = this.sql.createStatement("SELECT * FROM score WHERE `game-name` = 'game-2' ORDER BY `game-name` ASC, `score` DESC, `achievement-time` ASC");
    List<Score> scores = this.sql.fetchAllWithParams(Score::fromRow, stmt);
    //Assertions.assertEquals(100, scores.size());
    Score prevScore = null;
    for (Score score : scores) {
      if ((prevScore != null) && (prevScore.game.equals(score.game))) {
        Assertions.assertTrue(prevScore.score >= score.score);
      }
      prevScore = score;
    }
  }

  @Test
  public void testIdColumnBasedPaging()  {
    final int pageSize = 5;
    final String baseSqlQuery = "SELECT * FROM `score` WHERE `game-name` = 'game-2' ORDER BY `game-name` ASC, `score` DESC, `achievement-time` ASC";
    SqlPager<Score> pager = this.sql.createPager(Score::fromRow, "id", Types.BIGINT, baseSqlQuery);
    pager.setPageSize(pageSize);

    Assertions.assertFalse(pager.isPositioned());
    Assertions.assertFalse(pager.hasNextPage());
    Assertions.assertFalse(pager.hasPreviousPage());
    pager.moveToFirstPage();
    Assertions.assertTrue(pager.isPositioned());
    Deque<SqlPage<Score>> pages = new ArrayDeque<>();
    showPage(pager.getCurrentPage());
    pages.addLast(pager.getCurrentPage());
    String lastPageStartBookmark = null;
    while (pager.hasNextPage()) {
      LogService.info("Going forward");
      pager.moveToNextPage();
      showPage(pager.getCurrentPage());
      String nextBookmark = pager.getCurrentPage().getEndBookmark();
      if (nextBookmark != null) {
        lastPageStartBookmark = nextBookmark;
      }
      pages.addLast(pager.getCurrentPage());
    }
    SqlPage<Score> lastPage = pages.removeLast();
    LogService.info("Last page bookmark: %s", lastPageStartBookmark);
    while (pager.hasPreviousPage()) {
      LogService.info("Going backward");
      pager.moveToPreviousPage();
      showPage(pager.getCurrentPage());
      SqlPage<Score> old = pages.removeLast();
      comparePages(old, pager.getCurrentPage(), true);
    }
    // Start back on last
    LogService.info("Going to last page");
    pager.moveToPageAfter(lastPageStartBookmark);
    showPage(pager.getCurrentPage());
    comparePages(lastPage, pager.getCurrentPage(), false);
  }

  @Test
  public void testColumnValuesBasedPaging()  {
    final int pageSize = 5;
    final String baseSqlQuery = "SELECT * FROM `score` WHERE `game-name` = 'game-2' ORDER BY `game-name` ASC, `score` DESC, `achievement-time` ASC";
    SqlPager<Score> pager = this.sql.createPager(Score::fromRow, "id", Types.BIGINT, baseSqlQuery);
    pager.setPageSize(pageSize);

    Assertions.assertFalse(pager.isPositioned());
    Assertions.assertFalse(pager.hasNextPage());
    Assertions.assertFalse(pager.hasPreviousPage());
    pager.moveToFirstPage();
    Assertions.assertTrue(pager.isPositioned());
    Deque<SqlPage<Score>> pages = new ArrayDeque<>();
    showPage(pager.getCurrentPage());
    pages.addLast(pager.getCurrentPage());
    String lastPageStartBookmark = null;
    while (pager.hasNextPage()) {
      LogService.info("Going forward");
      pager.moveToNextPage();
      showPage(pager.getCurrentPage());
      String nextBookmark = pager.getCurrentPage().getEndBookmark();
      if (nextBookmark != null) {
        lastPageStartBookmark = nextBookmark;
      }
      pages.addLast(pager.getCurrentPage());
    }
    SqlPage<Score> lastPage = pages.removeLast();
    LogService.info("Last page bookmark: %s", lastPageStartBookmark);
    while (pager.hasPreviousPage()) {
      LogService.info("Going backward");
      pager.moveToPreviousPage();
      showPage(pager.getCurrentPage());
      SqlPage<Score> old = pages.removeLast();
      comparePages(old, pager.getCurrentPage(), true);
    }
    // Start back on last
    LogService.info("Going to last page");
    pager.moveToPageAfter(lastPageStartBookmark);
    showPage(pager.getCurrentPage());
    comparePages(lastPage, pager.getCurrentPage(), false);
  }

  private void comparePages(SqlPage<Score> expected, SqlPage<Score> actual, boolean reverse) {
    Assertions.assertEquals(expected.getPageSize(), actual.getPageSize());
    Assertions.assertEquals(expected.getStartBookmark(), actual.getStartBookmark());
    Assertions.assertEquals(expected.getEndBookmark(), actual.getEndBookmark());
    List<Score> expectedResults = expected.getElements();
    List<Score> actualResults = actual.getElements();
    Assertions.assertEquals(expectedResults.size(), actualResults.size());
    Score[] expectedScores = expectedResults.toArray(new Score[0]);
    if (reverse) {
      expectedScores = CollectionUtils.reverse(expectedScores);
    }
    Assertions.assertArrayEquals(expectedScores, actualResults.toArray(new Score[0]));
  }

  private void showPage(SqlPage<Score> scorePage) {
    LogService.info("%s:", scorePage);
    for (Score sc : scorePage.getElements()) {
      LogService.info("  %s", sc);
    }
  }
}
