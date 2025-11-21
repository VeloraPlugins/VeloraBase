package online.veloraplugins.base.core.redis

import io.lettuce.core.*
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.async.RedisAsyncCommands
import io.lettuce.core.api.sync.RedisCommands
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withContext
import online.veloraplugins.base.core.BasePlugin
import online.veloraplugins.base.core.configuration.RedisConfig
import online.veloraplugins.base.core.service.AbstractService

/**
 * RedisService (Velora Base)
 *
 * - Sync + Async API
 * - PubSub support
 * - Hash, Set, String helpers
 * - SCAN (sync & async)
 */
class RedisService(
    private val app: BasePlugin,
) : AbstractService(app) {

    private val config: RedisConfig = app.pluginConfig.redis

    private lateinit var client: RedisClient
    private lateinit var connection: StatefulRedisConnection<String, String>
    private lateinit var sync: RedisCommands<String, String>
    private lateinit var async: RedisAsyncCommands<String, String>
    private lateinit var pubSub: StatefulRedisPubSubConnection<String, String>

    private val channelListeners = mutableMapOf<String, MutableList<(String) -> Unit>>()

    override suspend fun onLoad() {
        if (!config.enabled) {
            log("Redis disabled in config.")
            return
        }

        setupClient()
        setupPubSub()
        log("Redis connected → ${config.host}:${config.port}/${config.database}")
    }

    override suspend fun onDisable() {
        if (!config.enabled) return

        runCatching { pubSub.close() }
        runCatching { connection.close() }
        runCatching { client.shutdown() }

        log("Redis shutdown.")
    }

    private fun setupClient() {
        val uri = RedisURI.Builder
            .redis(config.host, config.port)
            .apply {
                if (config.password.isNotEmpty())
                    withPassword(config.password.toCharArray())
                withDatabase(config.database)
                withClientName("velora-base")
            }
            .build()

        client = RedisClient.create(uri)

        connection = client.connect()
        sync = connection.sync()
        async = connection.async()
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

    fun sync(): RedisCommands<String, String> = sync
    fun async(): RedisAsyncCommands<String, String> = async

    fun get(key: String): String? = sync.get(key)
    fun set(key: String, value: String) = sync.set(key, value)
    fun setEx(key: String, ttlSeconds: Long, value: String) = sync.setex(key, ttlSeconds, value)
    fun del(key: String) = sync.del(key)
    fun exists(key: String): Boolean = sync.exists(key) > 0

    fun hset(key: String, field: String, value: String) = sync.hset(key, field, value)
    fun hmset(key: String, map: Map<String, String>) = sync.hset(key, map)

    fun sadd(key: String, value: String) = sync.sadd(key, value)
    fun srem(key: String, value: String) = sync.srem(key, value)
    fun expire(key: String, seconds: Long) = sync.expire(key, seconds)

    fun publish(channel: String, message: String) = sync.publish(channel, message)

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

    suspend fun getAsync(key: String): String? =
        withContext(Dispatchers.IO) { async.get(key).await() }

    suspend fun setAsync(key: String, value: String) =
        withContext(Dispatchers.IO) { async.set(key, value).await() }

    suspend fun setExAsync(key: String, ttlSeconds: Long, value: String) =
        withContext(Dispatchers.IO) { async.setex(key, ttlSeconds, value).await() }

    suspend fun deleteAsync(key: String) =
        withContext(Dispatchers.IO) { async.del(key).await() }

    suspend fun expireAsync(key: String, seconds: Long) =
        withContext(Dispatchers.IO) { async.expire(key, seconds).await() }

    suspend fun existsAsync(key: String): Boolean =
        withContext(Dispatchers.IO) { async.exists(key).await() > 0 }

    suspend fun hsetAsync(key: String, field: String, value: String) =
        withContext(Dispatchers.IO) { async.hset(key, field, value).await() }

    suspend fun hmsetAsync(key: String, map: Map<String, String>) =
        withContext(Dispatchers.IO) { async.hset(key, map).await() }

    suspend fun saddAsync(key: String, value: String) =
        withContext(Dispatchers.IO) { async.sadd(key, value).await() }

    suspend fun sremAsync(key: String, value: String) =
        withContext(Dispatchers.IO) { async.srem(key, value).await() }

    suspend fun publishAsync(channel: String, message: String) =
        withContext(Dispatchers.IO) { async.publish(channel, message).await() }

    suspend fun scanKeysAsync(pattern: String, limit: Int = 1000): List<String> =
        withContext(Dispatchers.IO) {
            val out = mutableListOf<String>()
            var cursor = ScanCursor.INITIAL
            val args = ScanArgs.Builder.matches(pattern).limit(limit.toLong())

            do {
                val scan = async.scan(cursor, args).await()
                out += scan.keys
                cursor = ScanCursor.of(scan.cursor)
            } while (!cursor.isFinished)

            out
        }

    fun subscribe(channel: String, listener: (String) -> Unit) {
        channelListeners.computeIfAbsent(channel) { mutableListOf() }.add(listener)
        pubSub.sync().subscribe(channel)
    }
}