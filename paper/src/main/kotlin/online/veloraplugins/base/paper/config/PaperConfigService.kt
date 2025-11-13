package online.veloraplugins.base.paper.config

import eu.okaeri.configs.serdes.commons.SerdesCommons
import eu.okaeri.configs.yaml.bukkit.YamlBukkitConfigurer
import eu.okaeri.configs.yaml.bukkit.serdes.SerdesBukkit
import online.veloraplugins.base.core.configuration.AbstractConfigService
import org.bukkit.plugin.java.JavaPlugin

class PaperConfigService(plugin: JavaPlugin) : AbstractConfigService(
    dataFolder = plugin.dataFolder,
    configurer = YamlBukkitConfigurer(),
    serdes = listOf(
        SerdesCommons(),
        SerdesBukkit()
    )
)