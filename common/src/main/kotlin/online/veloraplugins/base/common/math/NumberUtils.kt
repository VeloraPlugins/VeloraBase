package online.veloraplugins.base.common.math

import java.math.BigDecimal
import java.math.BigInteger
import java.text.DecimalFormat
import java.util.NavigableMap
import java.util.TreeMap

/**
 * Utility object providing high-level numeric formatting and arithmetic helpers.
 *
 * Features:
 * - Human-readable large number formatting ("1.5 Million")
 * - File size formatting ("2.3 MB")
 * - Percentage formatting
 * - Generic addition across multiple Number types
 * - Comma formatting for thousands separators
 *
 * Fully platform-agnostic and safe for high-volume use.
 */
object NumberUtils {

    /** Names for large number scales in ascending order. */
    private val LARGE_NAMES = arrayOf("Thousand", "Million", "Billion", "Trillion")

    /** Represents 1000 as BigInteger for scale calculations. */
    private val THOUSAND = BigInteger.valueOf(1000)

    /**
     * Map of numeric thresholds → scale names.
     * Example:
     * 1000 -> "Thousand"
     * 1,000,000 -> "Million"
     */
    private val SCALE_MAP: NavigableMap<BigInteger, String> = TreeMap()

    init {
        for (i in LARGE_NAMES.indices) {
            SCALE_MAP[THOUSAND.pow(i + 1)] = LARGE_NAMES[i]
        }
    }

    /**
     * Formats a large BigInteger into human-readable notation.
     *
     * Examples:
     * - 1500 -> "1.5 Thousand"
     * - 2500000 -> "2.5 Million"
     * - 999 -> "999"
     *
     * @param number the BigInteger to format
     * @return formatted number string
     */
    @JvmStatic
    fun formatLargeNumber(number: BigInteger): String {
        val entry = SCALE_MAP.floorEntry(number) ?: return number.toString()

        val scaleValue = entry.key
        val name = entry.value

        val divisor = scaleValue.divide(THOUSAND)  // e.g., for million: 1,000,000 / 1,000 = 1,000
        val raw = number.divide(divisor).toDouble() / 1000.0

        return "%.1f %s".format(raw, name)
    }

    /**
     * Formats a file size into a human-readable string.
     *
     * Examples:
     * - 1024 -> "1.0 KB"
     * - 15360 -> "15.0 KB"
     * - 1048576 -> "1.0 MB"
     *
     * @param size file size in bytes
     * @return formatted file size string
     */
    @JvmStatic
    fun formatFileSize(size: Long): String {
        val units = arrayOf("B", "KB", "MB", "GB", "TB")

        var result = size.toDouble()
        var unitIndex = 0

        while (result >= 1024 && unitIndex < units.lastIndex) {
            result /= 1024.0
            unitIndex++
        }

        return "%.1f %s".format(result, units[unitIndex])
    }

    /**
     * Formats a value as a percentage of a maximum value.
     *
     * Example:
     * - value=25, max=200 → "12%"
     *
     * @return percentage string without decimals
     */
    @JvmStatic
    fun formatPercent(value: Double, max: Double): String {
        if (max == 0.0) return "0%"
        return "${((value / max) * 100).toInt()}%"
    }

    /**
     * Adds two numbers of the same type and returns the result.
     *
     * Supported number types:
     * - Int, Long, Double, Float, Short, Byte
     * - BigInteger, BigDecimal
     *
     * @throws IllegalArgumentException if the type is not supported.
     */
    @JvmStatic
    @Suppress("UNCHECKED_CAST")
    fun <T : Number> add(n1: T, n2: T): T {
        return when (n1) {
            is Int        -> (n1 + n2.toInt()) as T
            is Long       -> (n1 + n2.toLong()) as T
            is Double     -> (n1 + n2.toDouble()) as T
            is Float      -> (n1 + n2.toFloat()) as T
            is Short      -> (n1 + n2.toShort()).toShort() as T
            is Byte       -> (n1 + n2.toByte()).toByte() as T
            is BigInteger -> (n1 + BigInteger.valueOf(n2.toLong())) as T
            is BigDecimal -> (n1 + BigDecimal.valueOf(n2.toDouble())) as T

            else -> throw IllegalArgumentException(
                "Unsupported number type: ${n1::class.qualifiedName}"
            )
        }
    }

    /**
     * Format a long value using comma separators.
     *
     * Example:
     * - 15000 -> "15,000"
     */
    @JvmStatic
    fun formatWithCommas(value: Long): String =
        DecimalFormat("#,###").format(value)

    /**
     * Format a double value using comma separators, preserving meaningful decimals.
     *
     * Example:
     * - 15000.0  -> "15,000"
     * - 15000.25 -> "15,000.25"
     */
    @JvmStatic
    fun formatWithCommas(value: Double): String {
        val formatter = DecimalFormat("#,###.##")
        formatter.minimumFractionDigits = if (value % 1 == 0.0) 0 else 2
        return formatter.format(value)
    }
}
