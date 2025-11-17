package online.veloraplugins.base.core.redis

import io.lettuce.core.*
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.sync.RedisCommands
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection
import online.veloraplugins.base.core.BasePlugin
import online.veloraplugins.base.core.configuration.RedisConfig
import online.veloraplugins.base.core.service.AbstractService

class RedisService(
    private val app: BasePlugin,
) : AbstractService(app) {

    private val config: RedisConfig = app.pluginConfig.redis

    private lateinit var client: RedisClient
    private lateinit var connection: StatefulRedisConnection<String, String>
    private lateinit var sync: RedisCommands<String, String>
    private lateinit var pubSub: StatefulRedisPubSubConnection<String, String>

    private val channelListeners = mutableMapOf<String, MutableList<(String) -> Unit>>()

    override suspend fun onEnable() {
        if (!config.enabled) {
            log("Redis is disabled in config.")
            return
        }

        setupClient()
        setupPubSub()

        log("Connected to Redis → ${config.host}:${config.port}/${config.database}")
        super.onEnable()
    }

    override suspend fun onDisable() {
        if (!config.enabled) return

        runCatching { pubSub.close() }
        runCatching { connection.close() }
        runCatching { client.shutdown() }

        log("Redis connections closed")
    }

    private fun setupClient() {
        val uri = RedisURI.Builder
            .redis(config.host, config.port)
            .apply {
                if (config.password.isNotEmpty())
                    withPassword(config.password.toCharArray())
                withDatabase(config.database)
            }
            .build()

        client = RedisClient.create(uri)

        connection = client.connect()
        sync = connection.sync()
    }

    private fun setupPubSub() {
        pubSub = client.connectPubSub()

        pubSub.addListener(object : io.lettuce.core.pubsub.RedisPubSubListener<String, String> {

            override fun message(channel: String, message: String) {
                channelListeners[channel]?.forEach { it(message) }
            }

            override fun message(pattern: String, channel: String, message: String) {}
            override fun subscribed(channel: String, count: Long) {}
            override fun psubscribed(pattern: String, count: Long) {}
            override fun unsubscribed(channel: String, count: Long) {}
            override fun punsubscribed(pattern: String, count: Long) {}
        })
    }

    fun get(key: String): String? =
        sync.get(key)

    fun set(key: String, value: String) {
        sync.set(key, value)
    }

    fun setEx(key: String, ttlSeconds: Long, value: String) {
        sync.setex(key, ttlSeconds, value)
    }

    fun del(key: String) {
        sync.del(key)
    }

    /** Returns true if the key exists */
    fun exists(key: String): Boolean =
        sync.exists(key) > 0

    /** SCAN pattern (fully sync) */
    fun scanKeys(pattern: String): List<String> {
        val keys = mutableListOf<String>()
        var cursor: ScanCursor = ScanCursor.INITIAL

        do {
            val scan = sync.scan(cursor, ScanArgs.Builder.matches(pattern))
            keys += scan.keys
            cursor = scan
        } while (!cursor.isFinished)

        return keys
    }

    fun publish(channel: String, message: String) {
        sync.publish(channel, message)
    }

    fun subscribe(channel: String, listener: (String) -> Unit) {
        val list = channelListeners.computeIfAbsent(channel) { mutableListOf() }
        list += listener

        pubSub.sync().subscribe(channel)
    }
}
