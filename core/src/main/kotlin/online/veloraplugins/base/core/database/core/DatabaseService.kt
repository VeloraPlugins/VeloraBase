package online.veloraplugins.base.core.database.core

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import online.veloraplugins.base.core.BasePlugin
import online.veloraplugins.base.core.configuration.MySQLConfig
import online.veloraplugins.base.core.service.AbstractService
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection

class DatabaseService(
    private val app: BasePlugin,
) : AbstractService(app) {

    private val config: MySQLConfig = app.pluginConfig.mysql

    private lateinit var hikari: HikariDataSource
    lateinit var db: Database
        private set

    override suspend fun onEnable() {
        setupPool()
        connectExposed()
        testConnection()

        log("Database connected → ${config.host}:${config.database}")
    }

    override suspend fun onDisable() {
        if (::hikari.isInitialized) {
            hikari.close()
        }
        log("Closed HikariCP pool")
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
        db = Database.connect(hikari)

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
     * Ensures SQL runs on Dispatchers.IO.
     */
    suspend fun <T> query(block: Transaction.() -> T): T =
        withContext(Dispatchers.IO) {
            transaction(db) { block() }
        }
}
