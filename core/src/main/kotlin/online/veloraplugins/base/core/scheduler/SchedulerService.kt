package online.veloraplugins.base.core.scheduler

import kotlinx.coroutines.*
import online.veloraplugins.base.core.BasePlugin
import online.veloraplugins.base.core.service.LoadOrder
import online.veloraplugins.base.core.service.Service
import online.veloraplugins.base.core.service.ServiceInfo
import java.util.concurrent.TimeUnit

/**
 * Platform-agnostic coroutine scheduler.
 *
 * Uses the platform-provided plugin.scope, allowing each platform
 * to define its threading model (Paper: MCCoroutine scope,
 * Velocity: default coroutine scope, etc.)
 */
@ServiceInfo("Scheduler", order = LoadOrder.LOWEST)
class SchedulerService(
    override val plugin: BasePlugin
) : Service(plugin) {

    private val scope: CoroutineScope
        get() = plugin.scope

    override suspend fun onEnable() {
        log("SchedulerService enabled")
    }

    override suspend fun onDisable() {
        log("SchedulerService disabled")
    }

    /** Runs a task immediately. */
    fun run(task: suspend () -> Unit): Job =
        scope.launch { task() }

    /** Runs a task synchronously on the main thread (via Dispatchers.Main). */
    fun runSync(task: suspend () -> Unit): Job =
        scope.launch(Dispatchers.Main) { task() }

    /** Runs a task asynchronously (Dispatchers.Default). */
    fun runAsync(task: suspend () -> Unit): Job =
        scope.launch(Dispatchers.Default) { task() }

    /** Runs a task after a delay. */
    fun runLater(delay: Long, unit: TimeUnit, task: suspend () -> Unit): Job =
        scope.launch {
            delay(unit.toMillis(delay))
            task()
        }

    /** Runs a repeating scheduled task. */
    fun runRepeating(
        initialDelay: Long,
        repeat: Long,
        unit: TimeUnit,
        task: suspend () -> Unit
    ): Job = scope.launch {
        delay(unit.toMillis(initialDelay))
        while (isActive) {
            task()
            delay(unit.toMillis(repeat))
        }
    }
}
