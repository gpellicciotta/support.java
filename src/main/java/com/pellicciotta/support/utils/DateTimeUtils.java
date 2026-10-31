// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.utils;

import java.text.*;
import java.time.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.pellicciotta.support.exceptions.FormatException;

/**
 *  Date and time related utility methods.
 */
public final class DateTimeUtils {
  /** Prevent instantiation */
  private DateTimeUtils() {  }

  // <editor-fold desc="Support Types">

  public enum TimespanFormat {
    /** Always hours, minutes and seconds, even when zero:
     *    [-] h:mm:ss.mmm
     */
    FIXED,

    /** As much as needed:
     *     [-] h:mm.ss.mmm
     *  or [-] mm.ss.mmm
     *  or [-] m.ss.mmm
     *  or [-] ss.mmm
     *  or [0] s.mmm
     */
    FLEX
  }

  private enum TimespanFormatPattern {
    HOURS_MINUTES_SECONDS_AND_MILLIS("([-])?(\\d+)[:](\\d+)[:](\\d+)[.](\\d\\d\\d)"),
    MINUTES_SECONDS_AND_MILLIS("([-])?(\\d+)[:](\\d+)[.](\\d\\d\\d)"),
    SECONDS_AND_MILLIS("([-])?(\\d+)[.](\\d\\d\\d)"),
    MILLIS_ONLY("([-])?(\\d+)");

    // Java 7: HOURS_MINUTES_SECONDS_AND_MILLIS("(?<negative>[-])?(?<hours>\\d+)[:](?<minutes>\\d+)[:](?<seconds>\\d+)[.](?<millis>\\d\\d\\d)"),
    // Java 7: MINUTES_SECONDS_AND_MILLIS("(?<negative>[-])?(?<minutes>\\d+)[:](?<seconds>\\d+)[.](?<millis>\\d\\d\\d)"),
    // Java 7: SECONDS_AND_MILLIS("(?<negative>[-])?(?<seconds>\\d+)[.](?<millis>\\d\\d\\d)"),
    // Java 7: MILLIS_ONLY("(?<negative>[-])?(?<millis>\\d+)");

    TimespanFormatPattern(String patternStr) {
      this.pattern = Pattern.compile("^\\s*" + patternStr + "\\s*$");
    }

    public final Pattern pattern;
  }

  // </editor-fold>
  // <editor-fold desc="Constants">

  public static final long HOURS_PER_DAY = 24L;
  public static final long MINUTES_PER_HOUR = 60L;
  public static final long MINUTES_PER_DAY = DateTimeUtils.MINUTES_PER_HOUR * DateTimeUtils.HOURS_PER_DAY;
  public static final long SECONDS_PER_MINUTE = 60L;
  public static final long SECONDS_PER_HOUR = DateTimeUtils.SECONDS_PER_MINUTE * DateTimeUtils.MINUTES_PER_HOUR;
  public static final long SECONDS_PER_DAY = DateTimeUtils.SECONDS_PER_HOUR * DateTimeUtils.HOURS_PER_DAY;
  public static final long MILLISECONDS_PER_SECOND = 1000L;
  public static final long MILLISECONDS_PER_MINUTE = DateTimeUtils.MILLISECONDS_PER_SECOND * DateTimeUtils.SECONDS_PER_MINUTE;
  public static final long MILLISECONDS_PER_HOUR = DateTimeUtils.MILLISECONDS_PER_MINUTE * DateTimeUtils.MINUTES_PER_HOUR;
  public static final long MILLISECONDS_PER_DAY = DateTimeUtils.MILLISECONDS_PER_HOUR * DateTimeUtils.HOURS_PER_DAY;

  /**
   * Default date format that is used by the formatDate methods.
   */
  public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";

  /**
   * Default time format that is used by the formatTime methods.
   */
  public static final String DEFAULT_TIME_FORMAT = "HH:mm:ss";

  /**
   * Default time format that is used by the formatTime methods.
   */
  public static final String DEFAULT_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

  /**
   * ISO 8601 standard date/time format with second precision.
   */
  public static final String ISO_8601_DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

  /**
   * ISO 8601 standard date/time format with microsecond precision and timezone indication .
   */
  public static final String ISO_8601_EXTENDED_DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS000X";

  /**
   * The ordered list of date/time formats that will be used by parseTime.
   */
  public static final DateFormat[] DATETIME_FORMATS_FOR_PARSE;

  static {
    List<DateFormat> formats = new ArrayList<>();
    formats.add(new SimpleDateFormat(DEFAULT_DATETIME_FORMAT));
    formats.add(new SimpleDateFormat(ISO_8601_DATETIME_FORMAT));
    formats.add(new SimpleDateFormat(ISO_8601_EXTENDED_DATETIME_FORMAT));
    formats.add(new SimpleDateFormat(DEFAULT_DATE_FORMAT));
    formats.add(new SimpleDateFormat(DEFAULT_TIME_FORMAT));
    formats.add(SimpleDateFormat.getTimeInstance());
    formats.add(SimpleDateFormat.getDateInstance());
    formats.add(SimpleDateFormat.getDateTimeInstance());
    DATETIME_FORMATS_FOR_PARSE = formats.toArray(new DateFormat[0]);
  }

  // </editor-fold>
  // <editor-fold desc="Operations">

  /**
   *  An approx. sleep method that will never throw an exception.
   *
   *  @param millis The number of milliseconds to sleep.
   */
  public static void sleep(long millis) {
    try {
      Thread.sleep(millis);
    }
    catch (Exception e) {
      /* Do nothing */
    }
  }

  /**
   *  Run an arbitrary action after an initial delay.
   *
   *  @param delayInMillis The delay after which to run the action.
   *  @param action The action to run.
   */
  public static void after(long delayInMillis, Runnable action) {
    Contract.isPositive("delayInMillis", delayInMillis);
    Contract.isNotNull("action", action);
    final java.util.Timer t = new java.util.Timer();
    t.schedule(new TimerTask() {
      @Override
      public void run() {
        action.run();
        t.cancel();
      }
    }, delayInMillis);
  }


  public static LocalDateTime utcNowAsLocalDateTime() {
    return LocalDateTime.now(ZoneOffset.UTC);
  }

  public static LocalDateTime fromUtcDateToLocalDateTime(Date d) {
    return LocalDateTime.ofInstant(d.toInstant(), ZoneOffset.UTC);
  }

  public static Date fromLocalDateTimeToUtcDate(LocalDateTime d) {
    return Date.from(d.atZone(ZoneOffset.UTC).toInstant());
  }

  public static Date fromLocalDateTimeToLocalDate(LocalDateTime d) {
    return Date.from(d.atZone(ZoneId.systemDefault()).toInstant());
  }

  public static Date fromZonedDateTimeToLocalDate(ZonedDateTime d) {
    return fromLocalDateTimeToLocalDate(d.toLocalDateTime());
  }

  /**
   * Parse a timespan, when provided a format that is created by formatTimespan, as a number of milliseconds.
   *
   * @param timespanStr The timespan representation, which must be in one of the following formats:
   *   <pre>
   *   1. (-)?[h:mm:ss.mmm]
   *   2. (-)?[m:ss.mmm]
   *   3. (-)?[s.mmm]
   *   4. (-)?0.[mmm]
   *   </pre>
   *   Note that each all time units (hours, minutes, seconds) can contain multiple digits and don't have to
   *   constitute a correct 'hour of day' indication. E.g. 100:200.300 is valid and indicates 100 minutes, 200
   *   seconds and 300 millis.
   *
   * @return The number of milliseconds represented by the timespan representation.
   *
   * @throws FormatException if timespan cannot be interpreted as a correct timespan format.
   */
  public static long parseTimespan(String timespanStr) {
    Contract.isNotNull("timespan", timespanStr);
    timespanStr = timespanStr.trim();

    for (TimespanFormatPattern format : TimespanFormatPattern.values()) {
      Matcher m = format.pattern.matcher(timespanStr);
      try {
        if (m.matches()) {
          int groupCount = m.groupCount();
          String millis = m.group(groupCount - 0); // Java 7: m.group("millis");
          long totalMillis = Long.parseLong(millis);

          if (groupCount > 2) {
            String seconds = m.group(groupCount - 1); // Java 7: m.group("seconds");
            if ((seconds != null) && !seconds.isEmpty()) {
              long extraMillis = Long.parseLong(seconds) * DateTimeUtils.MILLISECONDS_PER_SECOND;
              totalMillis += extraMillis;
            }
          }

          if (groupCount > 3) {
            String minutes = m.group(groupCount - 2); // Java 7: m.group("minutes");
            if ((minutes != null) && !minutes.isEmpty()) {
              long extraMillis = Long.parseLong(minutes) * DateTimeUtils.MILLISECONDS_PER_MINUTE;
              totalMillis += extraMillis;
            }
          }

          if (groupCount > 4) {
            String hours = m.group(groupCount - 3); // Java 7: m.group("hours");
            if ((hours != null) && !hours.isEmpty()) {
              long extraMillis = Long.parseLong(hours) * DateTimeUtils.MILLISECONDS_PER_HOUR;
              totalMillis += extraMillis;
            }
          }

          String negative = m.group(1); // Java 7: m.group("negative");
          if ((negative != null) && !negative.isEmpty()) {
            totalMillis = 0 - totalMillis;
          }

          return totalMillis;
        }
      }
      catch (NumberFormatException nfe) {
        // continue trying the next pattern - should never happen though
      }
    }

    throw new FormatException(timespanStr, "The value '" + timespanStr + "' cannot be interpreted as a timespan");
  }

  /**
   * Format a timespan as (-)?[h:mm:ss.mmm]
   *
   * @param millis The timespan, expressed in milliseconds, that needs to be formatted.
   *
   * @return A string representation of the specified timespan.
   */
  public static String formatTimespan(long millis) {
    return formatTimespan(millis, TimespanFormat.FIXED);
  }

  /**
   * Format a timespan. The timespan will be formatted as either:
   *
   * <pre>
   *   1. (-)?[h:mm:ss.mmm] if hours &gt; 0 or flexFormat == false
   *   2. (-)?[m:ss.mmm]    if minutes &gt; 0 and hours = 0
   *   3. (-)?[s.mmm]       if seconds &gt; 0 and minutes = 0 and hours = 0
   *   4. (-)?0.[mmm]       if seconds = 0 and minutes = 0 and hours = 0
   * </pre>
   *
   * All formats can be preceded with a minus sign when the timespan is negative.
   *
   * @param millis The timespan, expressed in milliseconds, that needs to be formatted.
   * @param format Whether to format the timespan according to the above 4 rules (FLEX)
   *               or whether rule 1 will always be used (FIXED).
   *
   * @return A string representation of the specified timespan.
   */
  public static String formatTimespan(long millis, TimespanFormat format) {
    boolean negative = (millis < 0);
    millis = Math.abs(millis);
    long hours = millis / DateTimeUtils.MILLISECONDS_PER_HOUR;
    millis -= hours * DateTimeUtils.MILLISECONDS_PER_HOUR;
    long minutes = millis / DateTimeUtils.MILLISECONDS_PER_MINUTE;
    millis -= minutes * DateTimeUtils.MILLISECONDS_PER_MINUTE;
    long seconds = millis / 1000L;
    millis -= seconds * 1000L;
    long milliseconds = millis;
    StringBuilder buf = new StringBuilder();
    boolean flexFormat = (format == TimespanFormat.FLEX);
    if (!flexFormat || (hours > 0L)) {
      buf.append(hours);
      buf.append(':');
    }
    if (!flexFormat || ((hours > 0L) || (minutes > 9L))) { // First digit of minutes
      buf.append((char) (int) ((minutes / 10L) + 48L));
    }
    if (!flexFormat || ((hours > 0L) || (minutes > 0L))) { // Second digit of minutes
      buf.append((char) (int) ((minutes % 10L) + 48L));
      buf.append(':');
    }
    if (!flexFormat || ((hours > 0L) || (minutes > 0L) || (seconds > 9L))) { // First digit of seconds
      buf.append((char) (int) ((seconds / 10L) + 48L));
    }
    buf.append((char) (int) ((seconds % 10L) + 48L)); // Second digit of seconds
    buf.append('.');
    if (milliseconds < 10L) {
      buf.append('0').append('0');
    }
    else if (milliseconds < 100L) {
      buf.append('0');
    }
    buf.append(milliseconds);
    if (negative) {
      buf.insert(0, '-');
    }
    return buf.toString();
  }

  /**
   * Parse a date/time by trying multiple possible date/time formats in succession.
   *
   * Following time formats will be tried in succession (note that this list may grow in upcoming versions):
   *   yyyy-MM-dd HH:mm:ss (== DEFAULT_TIME_FORMAT)
   *   yyyy-MM-dd'T'HH:mm:ss (== ISO_8601_TIME_FORMAT)
   *   yyyy-MM-dd'T'HH:mm:ss.SSS000X (== ISO_8601_EXTENDED_TIME_FORMAT).
   *   yyyy-MM-dd
   *   HH:mm:ss
   *   default locale time only
   *   default locale date only
   *   default locale date/time
   *
   * @param timeStr The date/time representation to be parsed.
   *
   * @return The correctly parsed date/time.
   *
   * @throws FormatException if timeStr cannot be interpreted as a correct date/time.
   */
  public static Date parseDateTime(String timeStr) {
    Contract.isNotNull("timeStr", timeStr);

    for (DateFormat format : DATETIME_FORMATS_FOR_PARSE) {
      try {
        // DateFormat/SimpleDateFormat is not thread-safe; clone the shared template before use
        // so concurrent callers never mutate the same instance.
        return parseDateTimeExact(timeStr, (DateFormat)format.clone());
      }
      catch (ParseException pe) {
        // continue trying the next pattern
      }
    }

    throw new FormatException(timeStr, "The value '" + timeStr + "' cannot be interpreted as a date/time value");
  }

  /**
   * Parse a date/time representation according to the given format.
   *
   * Following time formats will be tried in succession:
   * <pre>
   *   DEFAULT_DATETIME_FORMAT
   *   ISO_8601_DATETIME_FORMAT
   *   ISO_8601_EXTENDED_DATETIME_FORMAT
   * </pre>
   *
   * @param timeStr The date/time representation to be parsed.
   * @param timeFormat The format to be used for interpreting the string representation.
   *
   * @return The correctly parsed date/time.
   *
   * @throws ParseException if timeStr cannot be interpreted as a correct date/time according to the given timeFormat.
   */
  public static Date parseDateTimeExact(String timeStr, DateFormat timeFormat) throws ParseException {
    return timeFormat.parse(timeStr);
  }

  /**
   * Format a time instant in the default time format.
   *
   * @param time The local time to be formatted.
   *
   * @return A string representation formatted as described above.
   *
   * @throws IllegalArgumentException When time is null.
   */
  public static String formatDateTime(Instant time) throws IllegalArgumentException {
    return formatDateTime(time, DateTimeUtils.DEFAULT_DATETIME_FORMAT, false);
  }

  /**
   * Format a time instant as according to the specified format.
   *
   * @param time The time to be formatted.
   * @param format The format mask to be used. See {@link SimpleDateFormat} for details about this mask.
   *
   * @return A string representation formatted as described above.
   *
   * @throws IllegalArgumentException When time or format is null.
   */
  public static String formatDateTime(Instant time, String format) throws IllegalArgumentException {
    return formatDateTime(time, format, false);
  }

  /**
   * Format a time instant as according to the specified format.
   *
   * @param time The time to be formatted.
   * @param format The format mask to be used. See {@link SimpleDateFormat} for details about this mask.
   * @param transformToGMT Whether the time should be converted to GMT time.
   *
   * @return A string representation formatted as described above.
   *
   * @throws IllegalArgumentException When time or format is null.
   */
  public static String formatDateTime(Instant time, String format, boolean transformToGMT) throws IllegalArgumentException {
    Contract.isNotNull("time", time);
    Contract.isNotNull("format", format);
    DateFormat df = new SimpleDateFormat(format);
    if (transformToGMT) {
      df.setTimeZone(TimeZone.getTimeZone("GMT"));
    }
    Date date = Date.from(time);
    return df.format(date);
  }

  /**
   * Format a time in the default time format.
   *
   * @param time The local time to be formatted.
   *
   * @return A string representation formatted as described above.
   *
   * @throws IllegalArgumentException When time is null.
   */
  public static String formatDateTime(Date time) throws IllegalArgumentException {
    return formatDateTime(time, DateTimeUtils.DEFAULT_DATETIME_FORMAT, false);
  }

  /**
   * Format a time in the default time format.
   *
   * @param time The local time to be formatted.
   * @param transformToGMT Whether the time should be converted to GMT time.
   *
   * @return A string representation formatted as described above.
   *
   * @throws IllegalArgumentException When time is null.
   */
  public static String formatDateTime(Date time, boolean transformToGMT) throws IllegalArgumentException {
    return formatDateTime(time, DateTimeUtils.DEFAULT_DATETIME_FORMAT, transformToGMT);
  }

  /**
   * Format a time as according to the specified format.
   *
   * @param time The local time to be formatted.
   * @param format The format mask to be used. See SimpleDateFormat for details about this mask.
   *
   * @return A string representation formatted as described above.
   *
   * @throws IllegalArgumentException When time or format is null.
   */
  public static String formatDateTime(Date time, String format) throws IllegalArgumentException {
    return formatDateTime(time, format, false);
  }

  /**
   * Format a local time as according to the specified format.
   *
   * @param time The time to be formatted.
   * @param format The format mask to be used. See SimpleDateFormat for details about this mask.
   * @param transformToGMT Whether the time should be converted to GMT time.
   *
   * @return A string representation formatted as described above.
   *
   * @throws IllegalArgumentException When time or format is null.
   */
  public static String formatDateTime(Date time, String format, boolean transformToGMT) throws IllegalArgumentException {
    Contract.isNotNull("time", time);
    Contract.isNotNull("format", format);
    DateFormat df = new SimpleDateFormat(format);
    Calendar cal = Calendar.getInstance();
    cal.setTime(time);
    cal.setTimeZone(TimeZone.getDefault());
    if (transformToGMT) {
      df.setTimeZone(TimeZone.getTimeZone("GMT"));
    }
    return df.format(cal.getTime());
  }

  /**
   * Format a time as according to the specified format.
   *
   * @param time The time to be formatted.
   * @param format The format mask to be used. See SimpleDateFormat for details about this mask.
   * @param timeZone The timezone the time should be assumed to refer to.
   *
   * @return A string representation formatted as described above.
   *
   * @throws IllegalArgumentException When time or format is null.
   */
  public static String formatDateTime(Date time, String format, TimeZone timeZone) throws IllegalArgumentException {
    Contract.isNotNull("time", time);
    Contract.isNotNull("format", format);
    DateFormat df = new SimpleDateFormat(format);
    Calendar cal = Calendar.getInstance();
    cal.setTime(time);
    cal.setTimeZone(TimeZone.getDefault());
    df.setTimeZone(timeZone);
    return df.format(cal.getTime());
  }

  /**
   * Format a time as according to the specified format.
   *
   * @param time The time to be formatted.
   * @param format The format mask to be used. See SimpleDateFormat for details about this mask.
   * @param timeZone The timezone the time should be assumed to refer to.
   *
   * @return A string representation formatted as described above.
   *
   * @throws IllegalArgumentException When time or format is null.
   */
  public static String formatDateTime(LocalDateTime time, String format, TimeZone timeZone) throws IllegalArgumentException {
    Contract.isNotNull("time", time);
    Contract.isNotNull("format", format);
    DateFormat df = new SimpleDateFormat(format);
    Calendar cal = Calendar.getInstance();
    Date t = Date.from(time.atZone(ZoneId.systemDefault()).toInstant());
    cal.setTime(t);
    cal.setTimeZone(TimeZone.getDefault());
    df.setTimeZone(timeZone);
    return df.format(cal.getTime());
  }

  /**
   * Format a date-time's time component.
   *
   * @param dateTime The date/time to be formatted as HH:mm:ss
   * @return A formatted representation of only the time component.
   *
   * @throws IllegalArgumentException When dateTime is null.
   */
  public static String formatTime(Date dateTime) {
    return formatDateTime(dateTime, "HH:mm:ss");
  }

  /**
   * Format a date-time's date component.
   *
   * @param dateTime The date/time to be formatted as yyyy-MM-dd
   * @return A formatted representation of only the date component.
   *
   * @throws IllegalArgumentException When dateTime is null.
   */
  public static String formatDate(Date dateTime) {
    return formatDateTime(dateTime, "yyyy-MM-dd");
  }

  /**
   * Give a human-friendly indication of how far in the past a certain date-time lays.
   *
   * @param dateTime The date/time to be formatted.
   * @return The first that applies:<ol>
   *            <li>The form 'now' when less than 2s ago/in the future.</li>
   *            <li>The form 'in s seconds' when less than 51s in the future</li>
   *            <li>The form 's seconds ago' when less than 51s ago</li>
   *            <li>The form 'one minute ago' when less than 121s ago</li>
   *            <li>The form 'in one minute' when less than 121s in the future</li>
   *            <li>The form 'in m minutes' when less than 1h in the future</li>
   *            <li>The form 'm minutes ago' when less than 1h ago</li>
   *            <li>The time in the form <code>hh:mm</code> if today</li>
   *            <li>The date in the form <code>yyyy-mm-dd hh:mm</code></li>
   *          </ol>
   *
   * @throws IllegalArgumentException When time null.
   */
  @SuppressWarnings("deprecation")
  public static String formatRelativeDateTime(Date dateTime) {
    Contract.isNotNull("dateTime", dateTime);
    Date now = new Date();
    long nowMillis = now.getTime();
    long actualMillis = dateTime.getTime();
    long diffSeconds = (long) Math.floor(Math.abs(nowMillis - actualMillis) / 1000);
    long diffDays = (long)Math.floor(diffSeconds / SECONDS_PER_DAY);
    if (nowMillis >= actualMillis) { // In past
      if (diffSeconds < 2) {
        return "now";
      }
      else if (diffSeconds < 51) {
        return String.format("%d seconds ago", diffSeconds);
      }
      else if (diffSeconds < 121) {
        return "1 minute ago";
      }
      else if (diffSeconds < SECONDS_PER_HOUR) {
        long diffMinutes = (long)Math.floor(diffSeconds / 60);
        return String.format("%d minutes ago", diffMinutes);
      }
      else if ((diffSeconds < SECONDS_PER_DAY) && (dateTime.getDate() == now.getDate())) {
        return formatTime(dateTime);
      }
      else if ((diffSeconds < 2 * SECONDS_PER_DAY) && (diffDays < 2)) {
        return "yesterday";
      }
      else {
        return "on " + formatDate(dateTime);
      }
    }
    else { // In future
      if (diffSeconds < 2) {
        return "now";
      }
      else if (diffSeconds < 51) {
        return String.format("in %d seconds", diffSeconds);
      }
      else if (diffSeconds < 121) {
        return "in 1 minute";
      }
      else if (diffSeconds < SECONDS_PER_HOUR) {
        long diffMinutes = (long)Math.floor(diffSeconds / 60);
        return String.format("in %d minutes", diffMinutes);
      }
      else if ((diffSeconds < SECONDS_PER_DAY) && (dateTime.getDate() == now.getDate())) {
        return formatTime(dateTime);
      }
      else if ((diffSeconds < 2 * SECONDS_PER_DAY) && (diffDays < 2)) {
        return "tomorrow";
      }
      else {
        return "on " + formatDate(dateTime);
      }
    }
  }

  /**
   *  Determine the current local time.
   *
   *  @return The current time.
   */
  public static Date now() {
    Calendar cal = Calendar.getInstance();
    cal.setTime(new Date());
    cal.setTimeZone(TimeZone.getDefault());
    return cal.getTime();
  }

  /**
   *  @return Current local year.
   */
  public static int year() {
    return Calendar.getInstance().get(Calendar.YEAR);
  }

  /**
   *  @return Current local month. Results in a number in range [1, 12].
   */
  public static int month() {
    return Calendar.getInstance().get(Calendar.MONTH) + 1;
  }

  /**
   *  @return Current local day (of month). Results in a number in range [1, 31].
   */
  public static int dayOfMonth() {
    return Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
  }

  /**
   *  @return Current local hour (of day), in 24-hour format.
   */
  public static int hourOfDay() {
    return Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
  }

  /**
   *  @return Current local minute (of current hour).
   */
  public static int minute() {
    return Calendar.getInstance().get(Calendar.MINUTE);
  }

  /**
   *  @param date The local date to get the year for.
   *  @return Year of the provided date.
   */
  public static int yearIn(Date date) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    return cal.get(Calendar.YEAR);
  }

  /**
   *  @param date The local date to get the month for.
   *  @return Month of the provided date.
   */
  public static int monthIn(Date date) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    return cal.get(Calendar.MONTH) + 1;
  }

  /**
   *  @param date The local date to get the day of month for.
   *  @return Day (of month) of the provided date.
   */
  public static int dayOfMonthIn(Date date) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    return cal.get(Calendar.DAY_OF_MONTH);
  }

  /**
   *  @param time The local time to get the hour of day for.
   *  @return Hour (of day), in 24-hour format, of the provided time.
   */
  public static int hourOfDayIn(Date time) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(time);
    return Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
  }

  /**
   *  @param time The local time to get the minute for.
   *  @return Minute (of current hour) of the provided time
   */
  public static int minuteIn(Date time) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(time);
    return Calendar.getInstance().get(Calendar.MINUTE);
  }

  /**
   *  Get a {@link Date} object representing the local date/time when counting the milliseconds since epoch (1970-01-01T00:00:00Z).
   *
   *  @param millisSinceEpoch The number of milliseconds since epoch.
   *
   *  @return The requested date/time.
   */
  public static Date dateTime(long millisSinceEpoch) {
    Calendar cal = Calendar.getInstance();
    cal.setTimeInMillis(millisSinceEpoch);
    cal.setTimeZone(TimeZone.getDefault());
    return cal.getTime();
  }

  /**
   *  Create a {@link Date} with the given parts.
   *
   *  @param year A full year value.
   *  @param month The (1-based) month. 1 = January, 2 = February, ... 12 = December.
   *  @param day The (1-based) day of month, in range [1, 31].
   *
   *  @return A {@link Date} instance representing the given date and time.
   */
  public static Date date(int year, int month, int day) {
    return dateTime(year, month, day, 0, 0, 0, 0);
  }

  /**
   *  Create a {@link Date} with the given parts.
   *
   *  @param year A full year value.
   *  @param month The (1-based) month. 1 = January, 2 = February, ... 12 = December.
   *  @param day The (1-based) day of month, in range [1, 31].
   *  @param hour The hour of day, in range [0, 24].
   *
   *  @return A {@link Date} instance representing the given date and time.
   */
  public static Date dateTime(int year, int month, int day, int hour) {
    return dateTime(year, month, day, hour, 0, 0, 0);
  }

  /**
   *  Create a {@link Date} with the given parts.
   *
   *  @param year A full year value.
   *  @param month The (1-based) month. 1 = January, 2 = February, ... 12 = December.
   *  @param day The (1-based) day of month, in range [1, 31].
   *  @param hour The hour of day, in range [0, 24].
   *  @param minute The minutes within the hour, in range [0, 59].
   *
   *  @return A {@link Date} instance representing the given date and time.
   */
  public static Date dateTime(int year, int month, int day, int hour, int minute) {
    return dateTime(year, month, day, hour, minute, 0, 0);
  }

  /**
   *  Create a {@link Date} with the given parts.
   *
   *  @param year A full year value.
   *  @param month The (1-based) month. 1 = January, 2 = February, ... 12 = December.
   *  @param day The (1-based) day of month, in range [1, 31].
   *  @param hour The hour of day, in range [0, 24].
   *  @param minute The minutes within the hour, in range [0, 59].
   *  @param second The seconds within the minute, in range [0, 59].
   *
   *  @return A {@link Date} instance representing the given date and time.
   */
  public static Date dateTime(int year, int month, int day, int hour, int minute, int second) {
    return dateTime(year, month, day, hour, minute, second, 0);
  }

  /**
   *  Create a {@link Date} with the given parts.
   *
   *  @param year A full year value.
   *  @param month The (1-based) month. 1 = January, 2 = February, ... 12 = December.
   *  @param day The (1-based) day of month, in range [1, 31].
   *  @param hour The hour of day, in range [0, 24].
   *  @param minute The minutes within the hour, in range [0, 59].
   *  @param second The seconds within the minute, in range [0, 59].
   *  @param millisecond The milliseconds within the second, in range [0, 999].
   *
   *  @return A {@link Date} instance representing the given date and time.
   */
  public static Date dateTime(int year, int month, int day, int hour, int minute, int second, int millisecond) {
    Contract.isInRange("month",  month,1, 12);
    Contract.isInRange("day",  day,1, 31);
    Contract.isInRange("hour",  hour,0, 24);
    Contract.isInRange("minute",  minute,0, 59);
    Contract.isInRange("second",  second,0, 59);
    Contract.isInRange("millisecond",  millisecond,0, 999);

    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.YEAR, year);
    cal.set(Calendar.MONTH, month - 1);
    cal.set(Calendar.DAY_OF_MONTH, day);
    cal.set(Calendar.HOUR_OF_DAY, hour);
    cal.set(Calendar.MINUTE, minute);
    cal.set(Calendar.SECOND, second);
    cal.set(Calendar.MILLISECOND, millisecond);
    return cal.getTime();
  }

  /**
   *  Create a {@link Date} representing the specified local time, on the current day.
   *
   *  @param hour The hour of day, in range [0, 24].
   *  @param minute The minutes within the hour, in range [0, 59].
   *
   *  @return The requested time.
   */
  public static Date time(int hour, int minute) {
    return time(hour, minute, 0, 0);
  }

  /**
   *  Create a {@link Date} representing the specified local time, on the current day.
   *
   *  @param hour The hour of day, in range [0, 24].
   *  @param minute The minutes within the hour, in range [0, 59].
   *  @param second The seconds within the minute, in range [0, 59].
   *
   *  @return The requested time.
   */
  public static Date time(int hour, int minute, int second) {
    return time(hour, minute, second, 0);
  }

  /**
   *  Create a {@link Date} representing the specified local time, on the current day.
   *
   *  @param hour The hour of day, in range [0, 24].
   *  @param minute The minutes within the hour, in range [0, 59].
   *  @param second The seconds within the minute, in range [0, 59].
   *  @param millisecond The milliseconds within the second, in range [0, 999].
   *
   *  @return The requested time.
   */
  public static Date time(int hour, int minute, int second, int millisecond) {
    Contract.isInRange("hour", hour, 0, 24);
    Contract.isInRange("minute", minute, 0, 59);
    Contract.isInRange("second", second, 0, 59);
    Contract.isInRange("millisecond", millisecond, 0, 999);
    Calendar cal = Calendar.getInstance();
    cal.setTime(new Date());
    cal.set(Calendar.HOUR_OF_DAY, hour);
    cal.set(Calendar.MINUTE, minute);
    cal.set(Calendar.SECOND, second);
    cal.set(Calendar.MILLISECOND, millisecond);
    cal.setTimeZone(TimeZone.getDefault());
    return cal.getTime();
  }

  // </editor-fold>
}
