package online.veloraplugins.base.core.scheduler

import kotlinx.coroutines.*
import online.veloraplugins.base.core.BasePlugin
import online.veloraplugins.base.core.service.AbstractService
import java.util.concurrent.TimeUnit

/**
 * A coroutine-based scheduling service for VeloraBase.
 *
 * This service provides platform-agnostic delayed tasks, repeating tasks,
 * and async execution using the BasePlugin's CoroutineScope.
 *
 * The service is automatically enabled/disabled by the ServiceManager.
 */
class SchedulerService(
    private val plugin: BasePlugin
) : AbstractService(plugin) {

    private lateinit var schedulerScope: CoroutineScope

    override suspend fun onEnable() {
        // child scope so stopping scheduler does not kill entire BasePlugin scope
        this.schedulerScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        log("Scheduler initialized")
    }

    override suspend fun onDisable() {
        this.schedulerScope.cancel("SchedulerService shutting down")
        log("Scheduler stopped")
    }

    /**
     * Runs a task immediately.
     */
    fun run(task: suspend () -> Unit): Job =
        this.schedulerScope.launch { task() }

    /**
     * Runs a task asynchronously using Dispatchers.Default.
     */
    fun runAsync(task: suspend () -> Unit): Job =
        this.schedulerScope.launch(Dispatchers.Default) { task() }

    /**
     * Runs a task after a delay.
     */
    fun runLater(delay: Long, unit: TimeUnit, task: suspend () -> Unit): Job =
        this.schedulerScope.launch {
            delay(unit.toMillis(delay))
            task()
        }

    /**
     * Runs a repeating task with an initial delay.
     */
    fun runRepeating(
        initialDelay: Long,
        repeat: Long,
        unit: TimeUnit,
        task: suspend () -> Unit
    ): Job = this.schedulerScope.launch {
        delay(unit.toMillis(initialDelay))
        while (isActive) {
            task()
            delay(unit.toMillis(repeat))
        }
    }
}
