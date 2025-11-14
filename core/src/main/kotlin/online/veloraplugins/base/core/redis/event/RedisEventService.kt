package online.veloraplugins.base.core.redis.event

import online.veloraplugins.base.common.gson.GsonUtils
import online.veloraplugins.base.core.BasePlugin
import online.veloraplugins.base.core.redis.RedisService
import online.veloraplugins.base.core.service.AbstractService
import kotlin.reflect.KClass

/**
 * RedisEventService enables cross-server event communication using Redis Pub/Sub.
 *
 * Features:
 *  - Publish any RedisEvent to other servers
 *  - Listen to events using subscribe(EventClass) { ... }
 *  - Supports async/sync dispatch
 *  - Supports cancellable events with listener priorities
 *  - Supports local-only event dispatch (no Redis involved)
 *
 * Architecture:
 *  - Events are wrapped inside EventHolder for polymorphic Gson deserialization
 *  - Remote events received through Redis are dispatched locally
 *  - Local events (localOnly = true) bypass Redis entirely
 *
 * Requirements:
 *  - Redis must be enabled in config
 *  - RedisService must be enabled first (dependsOn ensures order)
 */
class RedisEventService(
    val app: BasePlugin,
) : AbstractService(app) {

    override val dependsOn = setOf(RedisService::class)

    private val redis = app.serviceManager.require(RedisService::class)

    /** Shared Redis PubSub channel for all Velora cross-server events */
    private val channel = app.pluginConfig.redis.eventChannel

    /**
     * Registered event listeners.
     *
     * Key   = Event class (e.g., UserVanishUpdatedEvent::class.java)
     * Value = List of handlers (sorted by priority)
     */
    private val subscribers =
        mutableMapOf<Class<out RedisEvent>, MutableList<RedisEventHandler<out RedisEvent>>>()


    /**
     * Initializes the service:
     *  - Subscribes to Redis
     *  - Logs activation
     */
    override suspend fun onEnable() {
        if (!app.pluginConfig.redis.enabled) {
            log("Redis disabled → RedisEventService inactive")
            return
        }

        redis.subscribe(channel) { message ->
            handleIncoming(message)
        }

        log("RedisEventService ready on '$channel'")
    }

    /**
     * Subscribes a listener to the given RedisEvent type.
     *
     * Example:
     *    redisEventService.subscribe(UserUpdateEvent::class) { evt ->
     *        println("User updated: ${evt.userId}")
     *    }
     *
     * @param clazz           Event class to listen for
     * @param priority        Determines execution order of listeners
     * @param ignoreCancelled If true, ignore cancelled events
     * @param handler         Callback receiving the event instance
     */
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

        // Ensure correct priority order
        list.sortBy { it.priority.order }
    }

    /**
     * Publishes an event to Redis OR locally (if localOnly = true).
     *
     * localOnly = true:
     *     - Event is dispatched locally only
     *     - Redis is NOT used
     *
     * localOnly = false:
     *     - Event is serialized and published to Redis
     *     - All servers (including this one) receive & dispatch the event
     *
     * @param event     The event instance to publish
     * @param localOnly Whether to skip Redis and dispatch only locally
     */
    fun publish(event: RedisEvent, localOnly: Boolean = false) {
        if (localOnly) {
            dispatchToLocalListeners(event)
            return
        }

        val json = GsonUtils.gson.toJson(EventHolder(event))
        redis.publish(channel, json)
    }

    /**
     * Called whenever a Redis message is received.
     * Automatically deserializes, unwraps, and dispatches the event.
     */
    private fun handleIncoming(json: String) {
        runCatching {
            val holder = GsonUtils.gson.fromJson(json, EventHolder::class.java)
            val event = holder.event

            dispatchToLocalListeners(event)

        }.onFailure { it.printStackTrace() }
    }

    /**
     * Dispatches the event to all local subscribers.
     * Respects:
     *  - async/sync dispatch (event.async flag)
     *  - cancellation
     *  - listener priorities
     */
    @Suppress("UNCHECKED_CAST")
    private fun dispatchToLocalListeners(event: RedisEvent) {
        val list = subscribers[event.javaClass] ?: return

        for (handler in list) {

            // Skip cancelled events if necessary
            if (event.cancellable && event.cancelled && handler.ignoreCancelled)
                continue

            // Safe cast because we track type in subscribe()
            val run = { (handler.handler as (RedisEvent) -> Unit)(event) }

            // async or sync execution
            if (event.async)
                dispatchAsync(run)
            else
                dispatchSync(run)

            // Cancel propagation
            if (event.cancellable && event.cancelled)
                break
        }
    }

    /** Execute on async scheduler thread */
    private fun dispatchAsync(block: () -> Unit) {
        app.scheduler.runAsync { block() }
    }

    /** Execute on main thread */
    private fun dispatchSync(block: () -> Unit) {
        app.scheduler.runSync { block() }
    }
}
