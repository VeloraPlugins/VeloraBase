package online.veloraplugins.base.core.database.dao

import online.veloraplugins.base.core.database.core.DatabaseService
import org.jetbrains.exposed.sql.*

/**
 * BaseDAO provides a clean coroutine-friendly structure for DAOs using Exposed.
 *
 * Includes utility functions commonly needed by all database layers.
 */
abstract class BaseDao<T : Table>(
    val db: DatabaseService,
    val table: T
) {

    suspend fun init() {
        updateSchema()
    }

    protected suspend fun <R> query(block: Transaction.() -> R): R =
        db.query(block)

    suspend fun createTable() = query {
        SchemaUtils.createMissingTablesAndColumns(table)
    }

    suspend fun dropTable() = query {
        SchemaUtils.drop(table)
    }

    suspend fun clearTable() = query {
        table.deleteAll()
    }

    suspend fun updateSchema() = query {
        SchemaUtils.createMissingTablesAndColumns(table)
    }

    suspend fun count(): Long = query {
        table.selectAll().count()
    }

    suspend fun exists(predicate: SqlExpressionBuilder.() -> Op<Boolean>): Boolean =
        query {
            table
                .selectAll()
                .where(predicate)
                .limit(1)
                .any()
        }

    suspend fun <V> exists(column: Column<V>, value: V): Boolean =
        query {
            table
                .selectAll()
                .where { column eq value }
                .limit(1)
                .any()
        }

    suspend fun findOne(predicate: SqlExpressionBuilder.() -> Op<Boolean>): ResultRow? =
        query {
            table
                .selectAll()
                .where(predicate)
                .limit(1)
                .singleOrNull()
        }

    suspend fun findMany(predicate: SqlExpressionBuilder.() -> Op<Boolean>): List<ResultRow> =
        query {
            table
                .selectAll()
                .where(predicate)
                .toList()
        }
}

