package online.veloraplugins.base.core.service

import kotlin.reflect.KClass

/**
 * Annotation holding metadata (same as ModuleInfo)
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ServiceInfo(
    val name: String,
    val version: Double = 1.0,
    val order: LoadOrder = LoadOrder.NORMAL,
    val dependsOn: Array<KClass<out Service>> = []
)
