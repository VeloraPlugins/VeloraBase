package online.veloraplugins.base.core.redis.event

open class RedisEvent(
    val async: Boolean = false,
    val cancellable: Boolean = false
) {
    val type: String = this.javaClass.name
    var cancelled: Boolean = false
    val creationTime: Long = System.currentTimeMillis()
}