package online.veloraplugins.base.core.configuration

import eu.okaeri.configs.OkaeriConfig
import eu.okaeri.configs.annotation.Comment

class RedisConfig : OkaeriConfig() {

    @Comment("Enable or disable Redis support")
    var enabled: Boolean = true

    @Comment("The Redis host")
    var host: String = "127.0.0.1"

    @Comment("The Redis port (default 6379)")
    var port: Int = 6379

    @Comment("Redis database index")
    var database: Int = 0

    @Comment("Redis password (empty = no password)")
    var password: String = ""

    @Comment("Channel name used for Redis cross-server events")
    var eventChannel: String = "velora:events"
}