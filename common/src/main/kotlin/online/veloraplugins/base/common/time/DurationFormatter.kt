package online.veloraplugins.base.common.time

/**
 * Formats durations (in milliseconds) into readable strings.
 *
 * Supports short format (e.g. "1y 2mo 3d 4h 5m 6s")
 * and long format (e.g. "1 year, 2 months, 3 days, 4 hours").
 */
object DurationFormatter {

    private data class DurationParts(
        val years: Long,
        val months: Long,
        val weeks: Long,
        val days: Long,
        val hours: Long,
        val minutes: Long,
        val seconds: Long
    )

    /** Converts milliseconds into structured duration parts. */
    private fun split(millis: Long): DurationParts {
        var seconds = millis / 1000

        val years = seconds / (365 * 24 * 60 * 60); seconds %= 365 * 24 * 60 * 60
        val months = seconds / (30 * 24 * 60 * 60); seconds %= 30 * 24 * 60 * 60
        val weeks = seconds / (7 * 24 * 60 * 60);   seconds %= 7 * 24 * 60 * 60
        val days = seconds / (24 * 60 * 60);        seconds %= 24 * 60 * 60
        val hours = seconds / 3600;                 seconds %= 3600
        val minutes = seconds / 60;                 seconds %= 60

        return DurationParts(years, months, weeks, days, hours, minutes, seconds)
    }

    /**
     * Formats duration into short form: "1y 2mo 3w 4d 5h 6m 7s".
     */
    @JvmStatic
    fun formatShort(millis: Long): String {
        val d = split(millis)
        val list = mutableListOf<String>()

        if (d.years > 0) list.add("${d.years}y")
        if (d.months > 0) list.add("${d.months}mo")
        if (d.weeks > 0) list.add("${d.weeks}w")
        if (d.days > 0) list.add("${d.days}d")
        if (d.hours > 0) list.add("${d.hours}h")
        if (d.minutes > 0) list.add("${d.minutes}m")
        if (d.seconds > 0 || list.isEmpty()) list.add("${d.seconds}s")

        return list.joinToString(" ")
    }

    /**
     * Formats duration into long form:
     * "1 year, 2 months, 3 days, 4 hours, 5 minutes".
     */
    @JvmStatic
    fun formatLong(millis: Long): String {
        val d = split(millis)
        val list = mutableListOf<String>()

        fun add(value: Long, name: String) {
            if (value > 0) list.add("$value $name${if (value != 1L) "s" else ""}")
        }

        add(d.years, "year")
        add(d.months, "month")
        add(d.weeks, "week")
        add(d.days, "day")
        add(d.hours, "hour")
        add(d.minutes, "minute")
        add(d.seconds, "second")

        return list.joinToString(", ").ifEmpty { "0 seconds" }
    }
}
