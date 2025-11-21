package online.veloraplugins.paper.example.service

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import online.veloraplugins.base.core.BasePlugin
import online.veloraplugins.base.core.service.Service
import online.veloraplugins.base.core.service.ServiceInfo
import kotlin.reflect.KClass

/**
 * Example service demonstrating how to build a service on top of VeloraBase.
 *
 * This service:
 * - Starts a repeating coroutine timer
 * - Logs a message every X seconds
 * - Shows how dependencies can be defined
 * - Cleans up jobs when disabled
 */
@ServiceInfo("Example")
class ExampleService(
    private val app: BasePlugin
) : Service(app) {

    private var repeatingJob: Job? = null

    override suspend fun onEnable() {
        this.log("Starting repeating task...")

        this.repeatingJob = this.app.scope.launch {
            while (this.isActive) {
                this@ExampleService.log("Hello from ExampleService!")
                delay(5000) // 5 seconds
            }
        }
    }

    override suspend fun onDisable() {
        this.log("Stopping repeating task...")

        this.repeatingJob?.cancel()
        this.repeatingJob = null
    }
}