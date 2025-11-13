package online.veloraplugins.base.common.collection

/**
 * Utility object for common collection operations.
 *
 * Provides helper methods for safely retrieving values,
 * chunking, combining, flattening, comparing, and generating
 * random elements from lists, sets, and maps.
 */
object CollectionUtils {

    /**
     * Safely retrieves an element at [index] or returns null if out of bounds.
     */
    @JvmStatic
    fun <T> getOrNull(list: List<T>, index: Int): T? =
        if (index in list.indices) list[index] else null

    /**
     * Splits a list into chunks of the given size.
     *
     * Example:
     *   chunk(listOf(1,2,3,4,5), size = 2) -> [[1,2], [3,4], [5]]
     */
    @JvmStatic
    fun <T> chunk(list: List<T>, size: Int): List<List<T>> =
        list.chunked(size)

    /**
     * Flattens a list of lists into a single list.
     */
    @JvmStatic
    fun <T> flatten(list: List<List<T>>): List<T> =
        list.flatten()

    /**
     * Picks a random element or null if list is empty.
     */
    @JvmStatic
    fun <T> randomOrNull(list: List<T>): T? =
        if (list.isEmpty()) null else list.random()

    /**
     * Retrieves a random element, throwing if empty.
     */
    @JvmStatic
    fun <T> random(list: List<T>): T =
        list.random()

    /**
     * Returns a shuffled copy of the list.
     */
    @JvmStatic
    fun <T> shuffled(list: List<T>): List<T> =
        list.shuffled()

    /**
     * Builds an immutable list using a builder-style lambda.
     *
     * Example:
     *   val list = CollectionUtils.buildList {
     *       add("A")
     *       add("B")
     *   }
     */
    @JvmStatic
    inline fun <T> buildList(builder: MutableList<T>.() -> Unit): List<T> =
        mutableListOf<T>().apply(builder).toList()

    /**
     * Builds an immutable set via builder-style syntax.
     */
    @JvmStatic
    inline fun <T> buildSet(builder: MutableSet<T>.() -> Unit): Set<T> =
        mutableSetOf<T>().apply(builder).toSet()

    /**
     * Combines two collections into one list.
     */
    @JvmStatic
    fun <T> combine(a: Collection<T>, b: Collection<T>): List<T> =
        ArrayList<T>(a.size + b.size).apply {
            addAll(a)
            addAll(b)
        }

    /**
     * Returns elements present in [first] but not in [second].
     */
    @JvmStatic
    fun <T> difference(first: Collection<T>, second: Collection<T>): Set<T> =
        first.toSet() - second.toSet()

    /**
     * Returns elements present in both collections.
     */
    @JvmStatic
    fun <T> intersection(first: Collection<T>, second: Collection<T>): Set<T> =
        first.toSet() intersect second.toSet()

    /**
     * Returns elements that appear only once in the collection.
     *
     * Example: [1,2,2,3,3,3] -> [1]
     */
    @JvmStatic
    fun <T> uniqueOnly(collection: Collection<T>): List<T> =
        collection.groupBy { it }
            .filter { it.value.size == 1 }
            .map { it.key }
}
