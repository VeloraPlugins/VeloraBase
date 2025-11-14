package online.veloraplugins.base.common.time

import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Year
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.*

/**
 * General-purpose utilities for working with dates and timestamps.
 *
 * This class intentionally does NOT include duration parsing/formatting.
 * Those are now handled by:
 * - DurationFormatter
 * - DurationParser
 * - DurationExtensions
 */
object TimeUtils {

    /** Default system zone used for formatting. */
    @JvmStatic
    val zoneId: ZoneId = ZoneId.of("Europe/Berlin")

    /** Commonly used date-time formatter (MMM dd, yyyy HH:mm:ss). */
    @JvmStatic
    val STANDARD_DATE_TIME_FORMATTER: DateTimeFormatter =
        DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm:ss").withZone(zoneId)

    /** Returns the current LocalDateTime using the configured zone. */
    @JvmStatic
    val localDateTime: LocalDateTime
        get() = LocalDateTime.now(zoneId)

    /**
     * Returns a daily key in format: yyyy-MM-dd
     * Example: "2025-03-14"
     */
    @JvmStatic
    fun getCurrentDayKey(): String =
        LocalDate.now().toString()

    /**
     * Returns a weekly key in format: yyyy-WW
     * Example: "2025-06"
     */
    @JvmStatic
    fun getCurrentWeekKey(): String {
        val now = LocalDate.now()
        val week = WeekFields.ISO.weekOfWeekBasedYear().getFrom(now)
        return "${now.year}-${String.format("%02d", week)}"
    }

    /**
     * Returns a monthly key in format: yyyy-MM
     * Example: "2025-03"
     */
    @JvmStatic
    fun getCurrentMonthKey(): String {
        val now = LocalDate.now()
        return "${now.year}-${String.format("%02d", now.monthValue)}"
    }

    /** Returns the current year as a string (yyyy). */
    @JvmStatic
    fun getCurrentYearKey(): String =
        Year.now().toString()

    /**
     * Formats a timestamp into "MMM dd, yyyy (HH:mm)"
     * Example: 1700000000000 -> "Nov 14, 2023 (19:33)"
     */
    @JvmStatic
    fun formatDateTime(millis: Long): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy (HH:mm)", Locale.ENGLISH)
        return sdf.format(Date(millis))
    }

    /**
     * Short time format HH:mm
     * Example: 1700000000000 -> "19:33"
     */
    @JvmStatic
    fun formatShort(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm")
        sdf.timeZone = TimeZone.getTimeZone("Europe/Amsterdam")
        return sdf.format(Date(timestamp))
    }

    /**
     * Short date+time format dd-MM HH:mm
     * Example: 1700000000000 -> "14-11 19:33"
     */
    @JvmStatic
    fun formatShortWithDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd-MM HH:mm")
        sdf.timeZone = TimeZone.getTimeZone("Europe/Amsterdam")
        return sdf.format(Date(timestamp))
    }

    /**
     * Formats the time until expiration.
     *
     * @param expires Expiration timestamp in millis.
     * @param now Current time (default = system time).
     * @param short Whether to use short units ("1h 23m") or long ("1 hour 23 minutes").
     */
    @JvmStatic
    fun formatRemainingTime(
        expires: Long,
        now: Long = System.currentTimeMillis(),
        short: Boolean = false
    ): String {
        if (expires == -1L) return if (short) "Perm" else "Permanent"
        if (expires <= now) return "Expired"

        val remaining = expires - now
        return if (short)
            DurationFormatter.formatShort(remaining)
        else
            DurationFormatter.formatLong(remaining)
    }
}
