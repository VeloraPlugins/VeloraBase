package online.veloraplugins.base.paper.player

import eu.okaeri.configs.OkaeriConfig
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.UUID

class PlayerData() : OkaeriConfig() {

    lateinit var uuid: UUID
    var contents: Array<ItemStack?> = arrayOf()
    var armorContents: Array<ItemStack?> = arrayOf()
    var offhand: ItemStack? = null
    var gameMode: GameMode = GameMode.SURVIVAL
    var health = 20.0
    var food = 20
    var level = 0
    var walkSpeed = 0.2f
    var flySpeed = 0.1f
    var exp = 0f
    var allowFlight = false
    var isFlying = false
    var location: Location? = null
    var potionEffects: MutableCollection<PotionEffect> = mutableListOf()

    constructor(player: Player) : this() {
        uuid = player.uniqueId
        contents = player.inventory.contents
        armorContents = player.inventory.armorContents
        offhand = player.inventory.itemInOffHand
        gameMode = player.gameMode
        health = player.health
        food = player.foodLevel
        level = player.level
        walkSpeed = player.walkSpeed
        flySpeed = player.flySpeed
        exp = player.exp
        allowFlight = player.allowFlight
        isFlying = player.isFlying
        location = player.location
        potionEffects = player.activePotionEffects
    }

    fun restore(player: Player) {
        player.inventory.contents = contents
        player.inventory.armorContents = armorContents
        player.inventory.setItemInOffHand(offhand)
        player.gameMode = gameMode
        player.health = health
        player.foodLevel = food
        player.level = level
        player.exp = exp
        for (effect in player.activePotionEffects) {
            @Suppress("DEPRECATION")
            if (PlayerUtils.isVanished(player)) {
                if (effect.type == PotionEffectType.NIGHT_VISION || effect.type == PotionEffectType.INVISIBILITY) continue
            }
            player.removePotionEffect(effect.type)
        }
        for (effect in potionEffects) {
            player.addPotionEffect(effect)
        }
        player.allowFlight = allowFlight
        player.isFlying = isFlying
        player.isFlying = allowFlight
        player.flySpeed = flySpeed
        player.walkSpeed = walkSpeed
        location?.apply { player.teleport(this) }
    }
}