package online.veloraplugins.base.core.redis.event

enum class RedisEventPriority(val order: Int) {
    LOWEST(0),
    LOW(1),
    NORMAL(2),
    HIGH(3),
    HIGHEST(4),
    MONITOR(5);
}