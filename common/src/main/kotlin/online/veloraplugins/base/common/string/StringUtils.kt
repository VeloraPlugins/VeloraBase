package online.veloraplugins.base.common.string

/**
 * Utility class for common string operations.
 *
 * Provides helpers for:
 * - Generating short alphanumeric IDs
 * - Replacing spacers such as spaces or hyphens
 * - Capitalizing text
 * - Formatting name/value pairs inside square brackets
 */
object StringUtils {

    private val ID_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"

    /**
     * Generates a short random uppercase ID.
     *
     * Example:
     * - "F4K92D"
     *
     * @param length Length of the ID to generate.
     */
    @JvmStatic
    fun generateShortId(length: Int = 6): String {
        return (1..length)
            .map { ID_CHARS.random() }
            .joinToString("")
    }

    /**
     * Replaces the given spacers in the input string with underscores.
     *
     * Example:
     * replaceSpacers("a-b c.d", "-", " ", ".") → "a_b_c_d"
     *
     * @param input The original string.
     * @param spacers List of substrings to replace with underscores.
     */
    @JvmStatic
    fun replaceSpacers(input: String, vararg spacers: String): String {
        var output = input
        for (spacer in spacers) {
            output = output.replace(spacer, "_")
        }
        return output
    }

    /**
     * Capitalizes the first character of the string.
     */
    @JvmStatic
    fun capitalize(s: String): String {
        return s.replaceFirstChar { it.uppercase() }
    }

    /**
     * Capitalizes the first character of every word.
     */
    @JvmStatic
    fun capitalizeWords(s: String): String {
        return s.split(" ")
            .filter { it.isNotEmpty() }
            .joinToString(" ") { capitalize(it) }
    }
}
