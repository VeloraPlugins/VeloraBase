package online.veloraplugins.base.core.redis.event

import online.veloraplugins.base.common.gson.GsonUtils
import online.veloraplugins.base.core.BasePlugin
import online.veloraplugins.base.core.database.core.DatabaseService
import online.veloraplugins.base.core.redis.RedisService
import online.veloraplugins.base.core.service.LoadOrder
import online.veloraplugins.base.core.service.Service
import online.veloraplugins.base.core.service.ServiceInfo
import kotlin.reflect.KClass

/**
 * RedisEventService
 *
 * Cross-server event communication using Redis Pub/Sub.
 */
@ServiceInfo("Events", order = LoadOrder.LOW, dependsOn = [RedisService::class])
class RedisEventService(
    private val app: BasePlugin,
) : Service(app) {

    /** Lazy getter — haalt altijd de bestaande DatabaseService */
    private val redisService: RedisService
        get() = plugin.serviceManager.require(RedisService::class)

    /** Shared PubSub channel defined in base-settings.yml */
    private val channel = app.pluginConfig.redis.eventChannel

    /** Subscribers map: EventClass -> Handlers */
    private val subscribers =
        mutableMapOf<Class<out RedisEvent>, MutableList<RedisEventHandler<out RedisEvent>>>()

    override fun onInitialize() {
        // Redis disabled → skip initialization
        if (!app.pluginConfig.redis.enabled) {
            log("Redis disabled → RedisEventService inactive")
            return
        }

        // Subscribe directly during initialization
        redisService.subscribe(channel) { message ->
            handleIncoming(message)
        }

        log("Subscribed to redis event channel '$channel'")
    }

    override suspend fun onEnable() {
        log("RedisEventService enabled")
    }

    override suspend fun onDisable() {
        log("RedisEventService disabled")
    }

    fun <T : RedisEvent> subscribe(
        clazz: KClass<T>,
        priority: RedisEventPriority = RedisEventPriority.NORMAL,
        ignoreCancelled: Boolean = true,
        handler: (T) -> Unit
    ) {
        val list = subscribers.computeIfAbsent(clazz.java) { mutableListOf() }

        list += RedisEventHandler(
            clazz = clazz.java,
            priority = priority,
            ignoreCancelled = ignoreCancelled,
            handler = handler
        )

        list.sortBy { it.priority.order }
    }

    fun publish(event: RedisEvent, localOnly: Boolean = false) {
        if (localOnly) {
            dispatchToLocal(event)
            return
        }

        val json = GsonUtils.gson.toJson(EventHolder(event))
        redisService.publish(channel, json)
    }

    private fun handleIncoming(json: String) {
        runCatching {
            val holder = GsonUtils.gson.fromJson(json, EventHolder::class.java)
            dispatchToLocal(holder.event)
        }.onFailure { it.printStackTrace() }
    }

    @Suppress("UNCHECKED_CAST")
    private fun dispatchToLocal(event: RedisEvent) {
        val handlers = subscribers[event.javaClass] ?: return

        for (handler in handlers) {

            // ignore if cancelled
            if (event.cancellable && event.cancelled && handler.ignoreCancelled)
                continue

            val task = { (handler.handler as (RedisEvent) -> Unit)(event) }

            if (event.async)
                app.scheduler.runAsync { task() }
            else
                app.scheduler.runSync { task() }

            // stop propagation
            if (event.cancellable && event.cancelled)
                break
        }
    }
}
