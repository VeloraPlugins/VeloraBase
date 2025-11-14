package online.veloraplugins.base.core.redis.event

data class EventHolder<T : RedisEvent>(
    val event: T,
    val createdAt: Long = System.currentTimeMillis()
)