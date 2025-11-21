package online.veloraplugins.base.paper.player

import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.entity.Damageable
import org.bukkit.entity.Player
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.plugin.Plugin
import org.jetbrains.annotations.ApiStatus

object PlayerUtils {
    /**
     * Reset the given [Player] to its original state
     *
     * @param player the player to reset
     * @param effects whether to clear the player's effects
     * @param inventory whether to clear the player's inventory
     * @param gameMode the [GameMode] to put the player in
     */
    @JvmStatic
    fun reset(player: Player, effects: Boolean, inventory: Boolean = false, gameMode: GameMode? = null) {
        if (effects) {
            for (potionEffect in player.activePotionEffects) player.removePotionEffect(potionEffect.type)
            player.activePotionEffects.clear()
        }
        if (gameMode != null) player.gameMode = gameMode
        player.allowFlight = false
        player.isSprinting = false
        player.foodLevel = 20
        player.saturation = 3.0f
        player.exhaustion = 0.0f
        player.maxHealth = 20.0
        player.health = player.maxHealth
        player.fireTicks = 0
        player.fallDistance = 0.0f
        player.level = 0
        player.exp = 0.0f
        player.walkSpeed = 0.2f
        player.flySpeed = 0.1f
        if (inventory) {
            player.inventory.clear()
            player.inventory.armorContents = arrayOfNulls(4)
            player.updateInventory()
        }
    }

    /**
     * @param entity the entity
     * @return the formatted health
     */
    @JvmStatic
    fun getPrettyHealth(entity: Damageable): String {
        return String.format("%.1f", entity.health / 2) + " &c❤&r"
    }

    @JvmStatic
    fun isGameMode(player: Player, gameMode: GameMode): Boolean {
        return player.gameMode == gameMode
    }

    @Deprecated("Moved to UserUtils.kt in paper module")
    @JvmStatic
    fun isVanished(player: Player): Boolean {
        return player.getMetadata("vanished").firstOrNull()?.asBoolean() == true
    }

    @Deprecated("Moved to UserUtils.kt in paper module")
    @JvmStatic
    fun isStaffMode(player: Player): Boolean {
        return player.getMetadata("staffmode").firstOrNull()?.asBoolean() == true
    }

    @ApiStatus.Obsolete
    @JvmStatic
    fun setVisibility(player: Player, hidden: Boolean, plugin: Plugin) {
        for (other in Bukkit.getOnlinePlayers()) {
            if (other == player) continue

            if (hidden) {
                if (!other.hasPermission("essentials.vanish.see")) {
                    other.hidePlayer(plugin, player)
                }
            } else {
                other.showPlayer(plugin, player)
            }

            /*if (shouldSeePlayer(other, player)) {
                other.showPlayer(plugin, player)
            } else {
                other.hidePlayer(plugin, player)
            }*/
        }
    }

    //fun shouldSeePlayer(viewer: Player, target: Player): Boolean {
    //    return !(isVanished(target) && !viewer.hasPermission("essentials.vanish.see"))

        //coming soon!
        // 2. Respecteer visibility setting (hub toggle, etc)
        /*if (inHubServer(viewer)) {
            val setting = getPlayerVisibilitySetting(viewer) // e.g. SHOW_NONE / SHOW_FRIENDS / SHOW_ALL
            return when (setting) {
                VisibilitySetting.SHOW_NONE -> false
                VisibilitySetting.SHOW_FRIENDS -> isFriend(viewer, target)
                VisibilitySetting.SHOW_ALL -> true
            }
        }

        // 3. Default = true
    }*/


    @Deprecated("Moved to UserUtils.kt in paper module")
    @JvmStatic
    fun setStaffMode(plugin: Plugin, player: Player, staffMode: Boolean) {
        player.setMetadata("staffmode", FixedMetadataValue(plugin, staffMode))
    }

    @Deprecated("Moved to UserUtils.kt in paper module")
    @JvmStatic
    fun getVanishedPlayers(): List<Player> {
        return Bukkit.getOnlinePlayers().stream()
            .filter(this::isVanished)
            .toList()
    }

    @Deprecated("Moved to UserUtils.kt in paper module")
    @JvmStatic
    fun getStaffModePlayers(): List<Player> {
        return Bukkit.getOnlinePlayers().stream()
            .filter(this::isStaffMode)
            .toList()
    }

    /*fun updateVisibility(plugin: Plugin, target: Player) {
        for (other in Bukkit.getOnlinePlayers()) {
            if (target == other) continue
            if (isVanished(target)) {
                other.hidePlayer(plugin, target)
            } else {
                other.showPlayer(plugin, target)
            }
        }
    }*/
}