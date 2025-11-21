package online.veloraplugins.base.paper.player

import com.cryptomorin.xseries.XSound
import me.clip.placeholderapi.PlaceholderAPI
import net.kyori.adventure.title.Title
import online.veloraplugins.base.paper.plugin.PluginUtils
import online.veloraplugins.mccommon.ComponentUtil
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitRunnable
import java.util.*
import java.util.logging.Logger


object PlayerActionUtils {
    private val logger = Logger.getLogger("PlayerActionUtils")

    @JvmStatic
    fun execute(plugin: Plugin, actions: List<String>) {
        actions.forEach { formattedAction: String ->
            if (formattedAction.isEmpty()) return@forEach

            val delay = getActionDelay(formattedAction)
            val actionWithoutDelay = removeDelayTag(formattedAction)

            val task = Runnable {
                val type = getActionType(actionWithoutDelay)
                val content = getActionContent(actionWithoutDelay)
                when (type) {
                    "broadcast" -> Bukkit.broadcast(ComponentUtil.parse(content))
                    "console" -> Bukkit.dispatchCommand(
                        Bukkit.getConsoleSender(),
                        content
                    )
                    "title" -> Bukkit.getOnlinePlayers().forEach { showTitle(it, content) }
                    "actionbar" -> Bukkit.getOnlinePlayers().forEach { it.sendActionBar(ComponentUtil.parse(content)) }
                    "message" -> Bukkit.getOnlinePlayers().forEach { it.sendMessage(ComponentUtil.parse(content)) }
                    "sound" -> Bukkit.getOnlinePlayers().forEach { playSound(it, content) }
                    "player" -> Bukkit.getOnlinePlayers().forEach { it.performCommand(content) }
                    "close" -> Bukkit.getOnlinePlayers().forEach { it.closeInventory() }
                    "echest" -> Bukkit.getOnlinePlayers().forEach { it.openInventory(it.enderChest) }
                    "asplayer" -> Bukkit.getOnlinePlayers().forEach { executeAction(plugin, it, listOf(content)) }

                    else -> {
                        if (content.isNotBlank()) {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), content)
                        }
                    }
                }
            }
            if (delay > 0) {
                object : BukkitRunnable() {
                    override fun run() {
                        task.run()
                    }
                }.runTaskLater(plugin, delay * 20L)
            } else {
                task.run()
            }
        }
    }

    @JvmStatic
    fun executeAction(plugin: Plugin, player: Player, actions: List<String>) {
        actions.forEach { action: String ->
            if (action.isEmpty()) return@forEach
            val formattedAction = if (PluginUtils.isEnabled("PlaceholderAPI"))
                PlaceholderAPI.setPlaceholders(player, action)
            else action

            val delay = getActionDelay(formattedAction)
            val actionWithoutDelay = removeDelayTag(formattedAction)

            val task = Runnable {
                val type = getActionType(actionWithoutDelay)
                val content = getActionContent(actionWithoutDelay).replace("%player%", player.name).replace("{player}", player.name).replace("{p}", player.name)
                when (type) {
                    "title" -> showTitle(player, content)
                    "actionbar" -> player.sendActionBar(ComponentUtil.parse(content))
                    "broadcast" -> Bukkit.broadcast(ComponentUtil.parse(content))
                    "message" -> player.sendMessage(ComponentUtil.parse(content))
                    "sound" -> playSound(player, content)
                    "console" -> Bukkit.dispatchCommand(
                        Bukkit.getConsoleSender(),
                        content
                    )
                    "player" -> player.performCommand(content)
                    "close" -> player.closeInventory()
                    "echest" -> player.openInventory(player.enderChest)
                    else -> {
                        if (content.isNotBlank()) {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), content)
                        }
                    }
                }
            }
            if (delay > 0) {
                object : BukkitRunnable() {
                    override fun run() {
                        task.run()
                    }
                }.runTaskLater(plugin, delay * 20L)
            } else {
                task.run()
            }
        }
    }

    private fun getActionType(action: String): String {
        val endIndex = action.indexOf("]")
        return if (action.startsWith("[") && endIndex > 0) action.substring(1, endIndex)
            .lowercase(Locale.getDefault()) else ""
    }

    private fun getActionContent(action: String): String {
        return if (action.contains("]")) action.substring(action.indexOf("]") + 1).trim() else ""
    }

    private fun showTitle(player: Player, content: String) {
        val parts = content.split("\\|".toRegex(), limit = 2).toTypedArray()
        val title = if (parts.isNotEmpty()) parts[0] else ""
        val subtitle = if (parts.size > 1) parts[1] else ""
        player.showTitle(Title.title(ComponentUtil.parse(title), ComponentUtil.parse(subtitle), Title.DEFAULT_TIMES))
    }

    private fun playSound(player: Player, content: String) {
        val parts = content.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        try {
            val sound = parts[0]
            val volume = if (parts.size > 1) parts[1].toFloat() else 1.0f
            val pitch = if (parts.size > 2) parts[2].toFloat() else 1.0f
            XSound.of(sound).ifPresent { it.play(player, volume, pitch) }
        } catch (e: IllegalArgumentException) {
            logger.warning("Invalid sound action: $content")
        } catch (e: ArrayIndexOutOfBoundsException) {
            logger.warning("Invalid sound action: $content")
        }
    }

    private fun getActionDelay(action: String): Int {
        if (action.contains("[delay:")) {
            try {
                val start = action.indexOf("[delay:") + 7
                val end = action.indexOf("]", start)
                return action.substring(start, end).toInt()
            } catch (e: NumberFormatException) {
                logger.warning("Invalid delay format in action: $action")
            } catch (e: IndexOutOfBoundsException) {
                logger.warning("Invalid delay format in action: $action")
            }
        }
        return 0
    }

    private fun removeDelayTag(action: String): String {
        return action.replace("\\[delay:\\d+]".toRegex(), "").trim()
    }
}
