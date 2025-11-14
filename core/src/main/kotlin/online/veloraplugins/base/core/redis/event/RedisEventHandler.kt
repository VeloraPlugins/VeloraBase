package online.veloraplugins.base.core.redis.event

class RedisEventHandler<T : RedisEvent>(
    val clazz: Class<T>,
    val priority: RedisEventPriority,
    val ignoreCancelled: Boolean,
    val handler: (T) -> Unit
)