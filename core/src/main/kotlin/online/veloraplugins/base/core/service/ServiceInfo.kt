package online.veloraplugins.base.core.service

/**
 * Annotation holding metadata (same as ModuleInfo)
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ServiceInfo(
    val name: String,
    val version: Double = 1.0
)