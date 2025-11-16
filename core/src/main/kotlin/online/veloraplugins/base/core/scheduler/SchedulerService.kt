package online.veloraplugins.base.core.scheduler

import kotlinx.coroutines.Job
import online.veloraplugins.base.core.BasePlugin
import online.veloraplugins.base.core.service.AbstractService
import java.util.concurrent.TimeUnit

/**
 * Abstract scheduler service.
 *
 * Concrete implementations must provide actual scheduling logic
 * for the specific platform (Velocity, Paper, etc.).
 */
abstract class SchedulerService(
    plugin: BasePlugin
) : AbstractService(plugin) {

    override suspend fun onEnable() {
        log("SchedulerService enabled (abstract)")
    }

    override suspend fun onDisable() {
        log("SchedulerService disabled (abstract)")
    }

    /**
     * Runs a task immediately.
     */
    abstract fun run(task: suspend () -> Unit): Job

    /**
     * Runs a task synchronously on main thread.
     */
    abstract fun runSync(task: suspend () -> Unit): Job

    /**
     * Runs a task asynchronously.
     */
    abstract fun runAsync(task: suspend () -> Unit): Job

    /**
     * Runs a task after a delay.
     */
    abstract fun runLater(delay: Long, unit: TimeUnit, task: suspend () -> Unit): Job

    /**
     * Runs a repeating task with an initial delay.
     */
    abstract fun runRepeating(
        initialDelay: Long,
        repeat: Long,
        unit: TimeUnit,
        task: suspend () -> Unit
    ): Job
}
