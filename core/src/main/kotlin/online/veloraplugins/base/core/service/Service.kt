package online.veloraplugins.base.core.service

import online.veloraplugins.base.core.BasePlugin

abstract class Service(
    open val plugin: BasePlugin
) {

    private lateinit var serviceInfo: ServiceInfo
    private lateinit var loggerName: String

    private var _loaded = false
    private var _enabled = false

    val loaded get() = _loaded
    val enabled get() = _enabled

    val loadOrder: LoadOrder
        get() = serviceInfo.order

    /** No INIT here, only constructor */
    init {
        if (!javaClass.isAnnotationPresent(ServiceInfo::class.java)) {
            throw IllegalStateException("Missing @ServiceInfo on ${javaClass.name}")
        }
    }

    /** Called by ServiceManager.register() */
    internal fun initializeInternal() {
        val start = System.currentTimeMillis()

        this.serviceInfo = javaClass.getAnnotation(ServiceInfo::class.java)
        this.loggerName = "Service:${serviceInfo.name}"

        log("Initializing service...")

        onInitialize()

        log("Initialized in ${System.currentTimeMillis() - start}ms")
    }

    protected open fun onInitialize() {}

    open suspend fun onLoad() {
        _loaded = true
        log("Loaded")
    }

    open suspend fun onEnable() {
        _enabled = true
        log("Enabled")
    }

    open suspend fun onDisable() {
        _enabled = false
        log("Disabled")
    }

    open suspend fun onReload() {
        log("Reloaded")
    }

    protected fun log(msg: String) = plugin.info("[$loggerName] $msg")
    protected fun debug(msg: String) = plugin.debug("[$loggerName] $msg")
}
