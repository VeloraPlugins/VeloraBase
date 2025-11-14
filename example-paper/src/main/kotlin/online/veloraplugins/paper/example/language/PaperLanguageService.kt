package online.veloraplugins.paper.example.language

import com.cryptomorin.xseries.XSound
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import online.veloraplugins.base.core.BasePlugin
import online.veloraplugins.base.core.database.dao.language.LanguageEntry
import online.veloraplugins.base.core.language.BaseMessage
import online.veloraplugins.base.core.language.LanguageService
import online.veloraplugins.base.core.language.MessageType
import online.veloraplugins.mccommon.ComponentUtil
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class PaperLanguageService(
    private val plugin: BasePlugin
) : LanguageService(plugin) {

    private fun componentFromString(input: String): Component =
        ComponentUtil.parse(input)

    private fun componentOf(entry: LanguageEntry, vararg placeholders: Pair<String, String>): Component {
        val text = format(entry.value, *placeholders)
        return componentFromString(text)
    }

    fun send(sender: CommandSender, message: BaseMessage, vararg placeholders: Pair<String, String>) {
        val entry = getEntry(message)
        sendByType(sender, entry, *placeholders)
        playSound(sender, entry)
    }

    private fun sendByType(sender: CommandSender, entry: LanguageEntry, vararg placeholders: Pair<String, String>) {
        when (entry.type) {
            MessageType.CHAT      -> sendChat(sender, entry, *placeholders)
            MessageType.ACTION_BAR -> sendActionBar(sender, entry, *placeholders)
            MessageType.TITLE     -> sendTitle(sender, entry, *placeholders)
        }
    }

    private fun sendChat(sender: CommandSender, entry: LanguageEntry, vararg placeholders: Pair<String, String>) {
        sender.sendMessage(componentOf(entry, *placeholders))
    }

    private fun sendActionBar(sender: CommandSender, entry: LanguageEntry, vararg placeholders: Pair<String, String>) {
        if (sender !is Player) {
            sendChat(sender, entry, *placeholders)
            return
        }
        sender.sendActionBar(componentOf(entry, *placeholders))
    }

    private fun sendTitle(sender: CommandSender, entry: LanguageEntry, vararg placeholders: Pair<String, String>) {
        if (sender !is Player) {
            sendChat(sender, entry, *placeholders)
            return
        }

        val formatted = format(entry.value, *placeholders)
        val parts = formatted.split("\\|", limit = 2)

        val title = componentFromString(parts.getOrNull(0) ?: "")
        val subtitle = componentFromString(parts.getOrNull(1) ?: "")

        sender.showTitle(Title.title(title, subtitle))
    }

    private fun playSound(sender: CommandSender, entry: LanguageEntry) {
        if (sender !is Player) return
        val soundName = entry.soundName ?: return

        val optional = XSound.of(soundName)
        if (optional.isEmpty) {
            plugin.logger.warning("Invalid sound configured: '$soundName'")
            return
        }

        optional.get().play(sender)
    }

}
