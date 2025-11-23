package online.veloraplugins.base.core.service

enum class LoadOrder(val priority: Int) {
    LOWEST(0),
    LOW(1),
    NORMAL(2),
    HIGH(3),
    HIGHEST(4)
}
