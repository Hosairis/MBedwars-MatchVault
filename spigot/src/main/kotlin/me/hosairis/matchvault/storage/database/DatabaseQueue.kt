package me.hosairis.matchvault.storage.database

import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.Transaction
import org.jetbrains.exposed.v1.core.statements.BatchInsertStatement
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.util.concurrent.atomic.AtomicBoolean

object DatabaseQueue {

    private sealed interface Operation {
        data class Insert(val table: Table?, val action: Transaction.(BatchInsertStatement?) -> Unit) : Operation
        data class Update(val action: Transaction.() -> Unit) : Operation
    }

    private val operations = ArrayDeque<Operation>()
    private val isFlushing = AtomicBoolean(false)
    private const val MAX_FLUSH_DURATION_NANOS = 50_000_000L // ~50ms guard against async thread starvation.
    private val lock = Any()

    fun queueInsert(table: Table? = null, action: Transaction.(BatchInsertStatement?) -> Unit) {
        synchronized(lock) { operations.addLast(Operation.Insert(table, action)) }
    }

    fun queueUpdate(action: Transaction.() -> Unit) {
        synchronized(lock) { operations.addLast(Operation.Update(action)) }
    }

    fun flush() {
        if (!isFlushing.compareAndSet(false, true)) return

        try {
            val deadline = System.nanoTime() + MAX_FLUSH_DURATION_NANOS

            while (System.nanoTime() < deadline) {
                val snapshot = synchronized(lock) {
                    if (operations.isEmpty()) null else ArrayList<Operation>(operations.size).also { buffer ->
                        while (operations.isNotEmpty()) {
                            buffer += operations.removeFirst()
                        }
                    }
                } ?: break

                var timedOut = false
                var resumeIndex = snapshot.size

                try {
                    transaction {
                        var currentTable: Table? = null
                        val currentBatch = mutableListOf<Transaction.(BatchInsertStatement) -> Unit>()

                        fun flushBatch() {
                            val table = currentTable
                            if (table == null || currentBatch.isEmpty()) {
                                currentTable = null
                                return
                            }

                            val rows = currentBatch.toList()
                            currentBatch.clear()
                            currentTable = null

                            table.batchInsert(rows) { action -> this@transaction.action(this) }
                        }

                        for (index in snapshot.indices) {
                            if (System.nanoTime() >= deadline) {
                                timedOut = true
                                resumeIndex = index
                                break
                            }

                            when (val op = snapshot[index]) {
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
                    synchronized(lock) {
                        for (op in snapshot.asReversed()) {
                            operations.addFirst(op)
                        }
                    }
                    break
                }

                if (timedOut) {
                    if (resumeIndex < snapshot.size) {
                        synchronized(lock) {
                            for (i in snapshot.size - 1 downTo resumeIndex) {
                                operations.addFirst(snapshot[i])
                            }
                        }
                    }
                    break
                }
            }
        } finally {
            isFlushing.set(false)
        }
    }

    fun scheduleFlush(plugin: Plugin, intervalTicks: Long = 20L * 5) {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, { flush() }, intervalTicks, intervalTicks)
    }
}
