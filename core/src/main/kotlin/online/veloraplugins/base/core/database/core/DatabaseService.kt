package online.veloraplugins.base.core.database.core

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import online.veloraplugins.base.core.BasePlugin
import online.veloraplugins.base.core.configuration.MySQLConfig
import online.veloraplugins.base.core.service.LoadOrder
import online.veloraplugins.base.core.service.Service
import online.veloraplugins.base.core.service.ServiceInfo
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.sql.Connection

@ServiceInfo("Database", order = LoadOrder.LOWEST)
class DatabaseService(
    private val app: BasePlugin,
) : Service(app) {

    private val config: MySQLConfig = app.pluginConfig.mysql

    private var hikari: HikariDataSource? = null
    lateinit var db: Database
        private set

    override fun onInitialize() {
        super.onInitialize()
        if (config.useSQLite) {
            connectSQLite()
            log("SQLite connected → ${config.sqliteFile}")
        } else {
            setupPool()
            connectExposed()
            runBlocking { testConnection() }
            log("MySQL connected → ${config.host}:${config.database}")
        }
    }


    override suspend fun onDisable() {
        hikari?.close()
        log("Closed database connection")
    }

    /**
     * -------------------------
     * SQLite
     * -------------------------
     */
    private fun connectSQLite() {
        val file = File(app.dataFolder, config.sqliteFile)
        file.parentFile.mkdirs()

        db = Database.connect(
            url = "jdbc:sqlite:${file.absolutePath}",
            driver = "org.sqlite.JDBC"
        )

        TransactionManager.manager.defaultIsolationLevel =
            Connection.TRANSACTION_SERIALIZABLE
    }

    private fun setupPool() {
        val cfg = HikariConfig().apply {
            jdbcUrl = "jdbc:mariadb://${config.host}:${config.port}/${config.database}"

            username = config.user
            password = config.password

            maximumPoolSize = config.maximumPoolSize
            minimumIdle = config.minimumIdle
            maxLifetime = config.maximumLifetime
            connectionTimeout = config.connectionTimeout

            driverClassName = "org.mariadb.jdbc.Driver"

            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"

            validate()
        }

        hikari = HikariDataSource(cfg)
    }

    private fun connectExposed() {
        db = Database.connect(hikari!!)
        TransactionManager.manager.defaultIsolationLevel =
            Connection.TRANSACTION_REPEATABLE_READ
    }

    private suspend fun testConnection() {
        query {
            val ok = exec("SELECT 1") { rs -> rs.next() }
            if (ok != true)
                error("Database test query failed (SELECT 1 returned null)")
        }
    }

    /**
     * Coroutine-friendly query wrapper.
     */
    suspend fun <T> query(block: Transaction.() -> T): T =
        withContext(Dispatchers.IO) {
            transaction(db) { block() }
        }
}
