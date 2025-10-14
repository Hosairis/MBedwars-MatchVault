package me.hosairis.matchvault.storage.database

import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.Transaction
import org.jetbrains.exposed.v1.core.statements.BatchInsertStatement
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

object DatabaseQueue {

    private sealed interface Operation {
        data class Insert(val table: Table?, val action: Transaction.(BatchInsertStatement?) -> Unit) : Operation
        data class Update(val action: Transaction.() -> Unit) : Operation
    }

    private val operations = mutableListOf<Operation>()
    private val lock = Any()

    fun queueInsert(table: Table? = null, action: Transaction.(BatchInsertStatement?) -> Unit) {
        synchronized(lock) { operations += Operation.Insert(table, action) }
    }

    fun queueUpdate(action: Transaction.() -> Unit) {
        synchronized(lock) { operations += Operation.Update(action) }
    }

    fun flush() {
        val snapshot: List<Operation>
        synchronized(lock) {
            if (operations.isEmpty()) return
            snapshot = operations.toList()
            operations.clear()
        }

        try {
            transaction {
                var currentTable: Table? = null
                val currentBatch = mutableListOf<Transaction.(BatchInsertStatement) -> Unit>()

                fun flushBatch() {
                    val table = currentTable
                    if (table == null || currentBatch.isEmpty()) {
                        currentBatch.clear()
                        currentTable = null
                        return
                    }

                    val rows = currentBatch.toList()
                    currentBatch.clear()
                    currentTable = null

                    table.batchInsert(rows) { action -> this@transaction.action(this) }
                }

                snapshot.forEach { op ->
                    when (op) {
                        is Operation.Insert -> {
                            if (op.table == null) {
                                flushBatch()
                                op.action(this, null)
                            } else {
                                if (currentTable != op.table) {
                                    flushBatch()
                                    currentTable = op.table
                                }
                                currentBatch += { statement -> op.action(this, statement) }
                            }
                        }
                        is Operation.Update -> {
                            flushBatch()
                            op.action(this)
                        }
                    }
                }

                flushBatch()
            }
        } catch (e: Exception) {
            Bukkit.getLogger().warning("Database flush failed: ${e.message}")
            e.printStackTrace()
            synchronized(lock) { operations.addAll(snapshot) }
        }
    }

    fun scheduleFlush(plugin: Plugin, intervalTicks: Long = 20L * 5) {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, { flush() }, intervalTicks, intervalTicks)
    }
}
