package online.veloraplugins.base.paper.world

import org.bukkit.Bukkit

object TpsUtil {

    /**
     * Returns the 1-minute TPS average.
     */
    @JvmStatic
    fun getServerTPS(): Double {
        return Bukkit.getServer().tps[0]
    }

    /**
     * Checks if the current 1-minute TPS is lower than the specified threshold.
     */
    @JvmStatic
    fun isLowerThan(threshold: Double): Boolean {
        return getServerTPS() < threshold
    }

    /**
     * Returns a MiniMessage color tag based on TPS value.
     * Example: <green>, <yellow>, <gold>, <red>
     */
    @JvmStatic
    fun getColorForTps(tps: Double): String {
        return when {
            tps >= 19.5 -> "<green>"
            tps >= 17.0 -> "<yellow>"
            tps >= 14.0 -> "<gold>"
            else -> "<red>"
        }
    }
}
