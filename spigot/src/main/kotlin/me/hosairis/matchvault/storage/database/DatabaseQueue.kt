package me.hosairis.matchvault.storage.database

import me.hosairis.matchvault.MatchVault
import me.hosairis.matchvault.storage.config.Config
import me.hosairis.matchvault.helpers.Log
import org.bukkit.Bukkit
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.Transaction
import org.jetbrains.exposed.v1.core.statements.BatchInsertStatement
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.util.concurrent.atomic.AtomicBoolean

object DatabaseQueue {

    private val operations = ArrayDeque<Operation>()
    private val isFlushing = AtomicBoolean(false)
    private val lock = Any()

    private sealed interface Operation {
        data class Insert(val table: Table?, val action: Transaction.(BatchInsertStatement?) -> Unit) : Operation
        data class Update(val action: Transaction.() -> Unit) : Operation
    }

    fun queueInsert(table: Table? = null, action: Transaction.(BatchInsertStatement?) -> Unit) {
        synchronized(lock) { operations.addLast(Operation.Insert(table, action)) }
    }

    fun queueUpdate(action: Transaction.() -> Unit) {
        synchronized(lock) { operations.addLast(Operation.Update(action)) }
    }

    fun runTransactionAsync(action: Transaction.() -> Unit) {
        Bukkit.getScheduler().runTaskAsynchronously(MatchVault.getInst()) {
            try {
                transaction { action(this) }
            } catch (e: Exception) {
                Log.warning("Async database task failed: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun flush() {
        if (!isFlushing.compareAndSet(false, true)) return

        try {
            val deadline = System.nanoTime() + Config.ADVANCED_DATABASE_FLUSH_DURATION

            while (System.nanoTime() < deadline) {
                val snapshot = synchronized(lock) {
                    if (operations.isEmpty()) null else ArrayList<Operation>(operations.size).also { buffer ->
                        while (operations.isNotEmpty()) {
                            buffer += operations.removeFirst()
                        }
                    }
                } ?: break

                Log.debug("DatabaseQueue flushing ${snapshot.size} queued operations.")

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

                            table.batchInsert(rows, shouldReturnGeneratedValues = false) { rowAction -> rowAction(this@transaction, this) }
                        }

                        var opsUntilTimeCheck = Config.ADVANCED_DATABASE_TIME_CHECK_STRIDE

                        for (index in snapshot.indices) {
                            if (--opsUntilTimeCheck <= 0) {
                                opsUntilTimeCheck = Config.ADVANCED_DATABASE_TIME_CHECK_STRIDE
                                if (System.nanoTime() >= deadline) {
                                    timedOut = true
                                    resumeIndex = index
                                    break
                                }
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
                    Log.warning("Database flush failed: ${e.message}")
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

    fun scheduleFlush() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(MatchVault.getInst(), Runnable {
            val pending = synchronized(lock) { operations.size }
            if (pending > 0) {
                Log.debug("running async flush task (queued=$pending)")
            }
            flush()
        }, 0, Config.ADVANCED_DATABASE_FLUSH_INTERVAL * 20)
    }
}
