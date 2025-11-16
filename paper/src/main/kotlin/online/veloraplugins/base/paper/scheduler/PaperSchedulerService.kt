package online.veloraplugins.base.paper.scheduler

import com.github.shynixn.mccoroutine.bukkit.scope
import kotlinx.coroutines.*
import online.veloraplugins.base.core.scheduler.SchedulerService
import online.veloraplugins.base.paper.plugin.PaperBasePlugin
import java.util.concurrent.TimeUnit

class PaperSchedulerService(plugin: PaperBasePlugin) : SchedulerService(plugin.base()) {

    private val scope = plugin.scope

    override fun run(task: suspend () -> Unit) =
        scope.launch { task() }

    override fun runSync(task: suspend () -> Unit) =
        scope.launch(Dispatchers.Main) { task() }

    override fun runAsync(task: suspend () -> Unit) =
        scope.launch(Dispatchers.IO) { task() }

    override fun runLater(delay: Long, unit: TimeUnit, task: suspend () -> Unit) =
        scope.launch {
            delay(unit.toMillis(delay))
            task()
        }

    override fun runRepeating(initialDelay: Long, repeat: Long, unit: TimeUnit, task: suspend () -> Unit) =
        scope.launch {
            delay(unit.toMillis(initialDelay))
            while (isActive) {
                task()
                delay(unit.toMillis(repeat))
            }
        }
}
