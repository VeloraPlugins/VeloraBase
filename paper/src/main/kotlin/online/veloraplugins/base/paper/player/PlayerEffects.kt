package online.veloraplugins.base.paper.player

import online.veloraplugins.base.paper.world.TpsUtil
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Player

object PlayerEffects {

    @JvmStatic
    fun spawnEffect(player: Player) {
        if (TpsUtil.isLowerThan(18.0)) return
        val location = player.location

        player.world.spawnParticle(Particle.PORTAL, location, 50, 0.5, 1.0, 0.5, 0.1)
        player.world.spawnParticle(Particle.FIREWORK, location, 30, 0.3, 0.6, 0.3)

        player.playSound(location, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.2f)
    }

}