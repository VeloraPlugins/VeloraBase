package online.veloraplugins.base.common.time

/**
 * Extension: Formats this duration (in milliseconds) in short format.
 *
 * Example:
 *    3600000L.toDurationShort()  -> "1h"
 */
fun Long.toDurationShort(): String =
    DurationFormatter.formatShort(this)

/**
 * Extension: Formats this duration (in milliseconds) in long format.
 *
 * Example:
 *    3600000L.toDurationLong() -> "1 hour"
 */
fun Long.toDurationLong(): String =
    DurationFormatter.formatLong(this)

/**
 * Extension: Parses this string into a duration in milliseconds.
 *
 * Example:
 *    "1h30m".toDurationMillis() -> 5400000
 */
fun String.toDurationMillis(): Long =
    DurationParser.parse(this)

/**
 * Extension: Formats a java.time.Duration into short format.
 */
fun java.time.Duration.toShortString(): String =
    DurationFormatter.formatShort(this.toMillis())

/**
 * Extension: Formats a java.time.Duration into long format.
 */
fun java.time.Duration.toLongString(): String =
    DurationFormatter.formatLong(this.toMillis())
