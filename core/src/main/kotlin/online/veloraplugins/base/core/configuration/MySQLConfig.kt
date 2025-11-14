package online.veloraplugins.base.core.configuration

import eu.okaeri.configs.OkaeriConfig
import eu.okaeri.configs.annotation.Comment

/**
 * MySQL configuration used by VeloraBase.
 *
 * This class maps directly to the `mysql:` section in the YAML config.
 * All fields include descriptive comments for the generated config file.
 */
class MySQLConfig : OkaeriConfig() {


    @Comment("If true, SQLite will be used instead of MySQL/MariaDB.")
    var useSQLite: Boolean = true

    @Comment("The path to the SQLite database file (only used when useSQLite = true).")
    var sqliteFile: String = "database.db"

    @Comment("The address of the MySQL/MariaDB server")
    var host: String = "127.0.0.1"

    @Comment("The port of the database server (Default: 3306)")
    var port: Int = 3306

    @Comment("The name of the database/schema to connect to")
    var database: String = "velora"

    @Comment("Username for authentication")
    var user: String = "root"

    @Comment("Password for authentication")
    var password: String = ""

    @Comment("The maximum size of the HikariCP connection pool")
    var maximumPoolSize: Int = 10

    @Comment("The minimum number of idle connections to maintain")
    var minimumIdle: Int = 2

    @Comment("Maximum lifetime of a connection (in ms). Recommended: 1800000 (30 min)")
    var maximumLifetime: Long = 1800000

    @Comment("Maximum time (in ms) to wait for a connection before timing out")
    var connectionTimeout: Long = 10000
}
