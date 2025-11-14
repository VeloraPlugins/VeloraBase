package online.veloraplugins.base.core.configuration

import eu.okaeri.configs.OkaeriConfig
import eu.okaeri.configs.annotation.Comment

/**
 * Base configuration for all plugins running on VeloraBase.
 *
 * This configuration is automatically created and loaded by BasePlugin.
 * It provides global settings that every plugin can rely on, such as:
 * - Debug logging
 * - Future shared global config options
 */
class BaseConfig : OkaeriConfig() {

    /**
     * Enables detailed debugging output for the entire plugin.
     *
     * When set to true:
     * - Debug messages from BasePlugin.debug() will be printed
     * - Services may output additional debug information
     * - Useful during development or troubleshooting
     */
    var debug: Boolean = false

    /**
     * MySQL database settings.
     * Used by DatabaseService to configure the MariaDB/HikariCP pool.
     */
    @Comment("MySQL database settings")
    var mysql: MySQLConfig = MySQLConfig()
}