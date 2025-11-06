package me.hosairis.matchvault.helpers

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import me.hosairis.matchvault.MatchVault
import org.bukkit.Bukkit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object CoroutineHelper {
    private val job = SupervisorJob()
    private val ioScope = CoroutineScope(Dispatchers.IO + job)

    // Launch a coroutine on the IO dispatcher (background thread)
    fun runAsync(block: suspend CoroutineScope.() -> Unit) {
        ioScope.launch(block = block)
    }

    // Run code safely on the Minecraft main thread
    suspend fun runSync(block: () -> Unit) {
        if (Bukkit.isPrimaryThread()) {
            block()
            return
        }

        return suspendCancellableCoroutine { cont ->
            val task = Bukkit.getScheduler().runTask(MatchVault.getInst()) {
                try {
                    block()
                    if (cont.isActive) cont.resume(Unit)
                } catch (ex: Throwable) {
                    if (cont.isActive) cont.resumeWithException(ex)
                }
            }

            cont.invokeOnCancellation { task.cancel() }
        }
    }

    // Cancel all active coroutines
    fun cancelAll() {
        job.cancel()
    }
}
