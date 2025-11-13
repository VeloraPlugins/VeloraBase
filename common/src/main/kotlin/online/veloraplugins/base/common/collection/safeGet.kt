package online.veloraplugins.base.common.collection

/**
 * Extension functions for collections and lists.
 *
 * These provide safe access, random picks, uniqueness helpers,
 * builder-style additions, and convenient set/list operations.
 */

/**
 * Safely retrieves the element at [index] or returns null.
 */
fun <T> List<T>.safeGet(index: Int): T? =
    if (index in indices) this[index] else null

/**
 * Returns the first element or null if empty.
 */
fun <T> Collection<T>.firstOrNullSafe(): T? =
    if (isEmpty()) null else first()

/**
 * Returns the last element or null if empty.
 */
fun <T> Collection<T>.lastOrNullSafe(): T? =
    if (isEmpty()) null else last()


/**
 * Returns a random element or null if empty.
 */
fun <T> Collection<T>.randomOrNullSafe(): T? =
    if (isEmpty()) null else random()

/**
 * Adds [value] only if it is not null.
 */
fun <T> MutableCollection<T>.addIfNotNull(value: T?) {
    if (value != null) add(value)
}

/**
 * Adds all non-null values from a vararg.
 */
fun <T> MutableCollection<T>.addAllIfNotNull(vararg values: T?) {
    for (v in values) {
        if (v != null) add(v)
    }
}

/**
 * Returns all elements that occur exactly once in the collection.
 */
fun <T> Collection<T>.uniqueOnly(): List<T> =
    this.groupBy { it }
        .filter { it.value.size == 1 }
        .keys
        .toList()

/**
 * Returns all elements that appear more than once.
 */
fun <T> Collection<T>.duplicates(): List<T> =
    this.groupBy { it }
        .filter { it.value.size > 1 }
        .keys
        .toList()

/**
 * Returns elements that are in this collection but not in [other].
 */
fun <T> Collection<T>.difference(other: Collection<T>): Set<T> =
    this.toSet() - other.toSet()

/**
 * Returns elements that appear in both collections.
 */
fun <T> Collection<T>.intersectWith(other: Collection<T>): Set<T> =
    this.toSet() intersect other.toSet()

/**
 * Inverts a map (swaps key/value).
 * If values are not unique, later entries overwrite earlier ones.
 */
fun <K, V> Map<K, V>.invert(): Map<V, K> =
    this.entries.associate { (k, v) -> v to k }

/**
 * Maps the collection but skips nulls automatically.
 *
 * Example:
 *    list.mapNotNullValues { parseOrNull(it) }
 */
inline fun <T, R> Iterable<T>.mapNotNullValues(transform: (T) -> R?): List<R> =
    this.mapNotNull(transform)
