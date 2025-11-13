package online.veloraplugins.base.common.enums

/**
 * Utility functions for working with enums.
 * Useful for cyclic toggles (like next/previous menu states or settings).
 *
 * Example:
 * var mode = VisibilityOption.ALL
 * mode = EnumUtils.next(mode) // FRIENDS
 * mode = EnumUtils.previous(mode) // NONE
 */
object EnumUtils {

    /**
     * Get the next enum constant, looping back to the first if at the end.
     */
    inline fun <reified T : Enum<T>> next(current: T): T {
        val values = enumValues<T>()
        val nextIndex = (current.ordinal + 1) % values.size
        return values[nextIndex]
    }

    /**
     * Get the previous enum constant, looping back to the last if at the beginning.
     */
    inline fun <reified T : Enum<T>> previous(current: T): T {
        val values = enumValues<T>()
        val prevIndex = (current.ordinal - 1 + values.size) % values.size
        return values[prevIndex]
    }

    /**
     * Returns the next enum constant, or null if there is no next one (non-cyclic).
     */
    inline fun <reified T : Enum<T>> nextOrNull(current: T): T? {
        val values = enumValues<T>()
        val nextIndex = current.ordinal + 1
        return values.getOrNull(nextIndex)
    }

    /**
     * Returns the previous enum constant, or null if there is no previous one (non-cyclic).
     */
    inline fun <reified T : Enum<T>> previousOrNull(current: T): T? {
        val values = enumValues<T>()
        val prevIndex = current.ordinal - 1
        return values.getOrNull(prevIndex)
    }

    /** Runtime-variant: werkt als je alleen Class<T> hebt i.p.v. reified T. */
    fun <T : Enum<T>> next(current: T, enumClass: Class<T>): T {
        val values = enumClass.enumConstants
        val nextIndex = (current.ordinal + 1) % values.size
        return values[nextIndex]
    }

    fun <T : Enum<T>> previous(current: T, enumClass: Class<T>): T {
        val values = enumClass.enumConstants
        val prevIndex = (current.ordinal - 1 + values.size) % values.size
        return values[prevIndex]
    }

    /** Runtime variant zonder generic T (lost CapturedType-mismatch op). */
    fun nextAny(current: Enum<*>, enumClass: Class<out Enum<*>>): Enum<*> {
        val values = enumClass.enumConstants ?: return current
        val nextIndex = (current.ordinal + 1) % values.size
        return values[nextIndex]
    }

    fun previousAny(current: Enum<*>, enumClass: Class<out Enum<*>>): Enum<*> {
        val values = enumClass.enumConstants ?: return current
        val prevIndex = (current.ordinal - 1 + values.size) % values.size
        return values[prevIndex]
    }
}
