package online.veloraplugins.base.core.redis.event

class ListenerWrapper<T : RedisEvent>(
    val clazz: Class<T>,
    val priority: RedisEventPriority,
    val ignoreCancelled: Boolean,
    val handler: (T) -> Unit
) {
    @Suppress("UNCHECKED_CAST")
    fun run(event: RedisEvent) {
        handler(event as T)
    }
}
