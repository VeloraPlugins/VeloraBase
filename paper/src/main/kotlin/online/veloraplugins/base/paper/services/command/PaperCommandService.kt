package online.veloraplugins.base.paper.services.command

import online.veloraplugins.base.core.BasePlugin
import online.veloraplugins.base.core.service.ServiceInfo
import online.veloraplugins.base.paper.plugin.PaperBasePlugin
import online.veloraplugins.base.paper.services.PaperService
import org.bukkit.command.CommandSender
import org.incendo.cloud.Command
import org.incendo.cloud.SenderMapper
import org.incendo.cloud.annotations.AnnotationParser
import org.incendo.cloud.brigadier.BrigadierSetting
import org.incendo.cloud.bukkit.CloudBukkitCapabilities
import org.incendo.cloud.exception.*
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.minecraft.extras.MinecraftExceptionHandler
import org.incendo.cloud.minecraft.extras.MinecraftHelp
import org.incendo.cloud.minecraft.extras.caption.ComponentCaptionFormatter
import org.incendo.cloud.paper.LegacyPaperCommandManager
import org.incendo.cloud.parser.standard.*
import online.veloraplugins.mccommon.ComponentUtil
import org.bukkit.entity.Player
import org.incendo.cloud.bukkit.parser.*
import org.incendo.cloud.minecraft.extras.AudienceProvider
import org.incendo.cloud.minecraft.extras.ImmutableMinecraftHelp
import org.incendo.cloud.parser.ArgumentParser

@ServiceInfo("Cloud Commands")
class PaperCommandService(
    private val paperPlugin: PaperBasePlugin
) : PaperService(paperPlugin) {

    private lateinit var manager: LegacyPaperCommandManager<CommandSender>
    private lateinit var parser: AnnotationParser<CommandSender>
    private lateinit var help: MinecraftHelp<CommandSender>

    override suspend fun onEnable() {
        super.onEnable()
        setupManager()
        setupParsers()
        setupExceptionHandlers()
        setupHelp()
    }

    private fun setupManager() {
        this.manager = LegacyPaperCommandManager(
            paperPlugin,
            ExecutionCoordinator.asyncCoordinator(),
            SenderMapper.identity()
        )

        if (this.manager.hasCapability(CloudBukkitCapabilities.NATIVE_BRIGADIER)) {
            this.manager.registerBrigadier()
        } else if (this.manager.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
            this.manager.registerAsynchronousCompletions()
        }
    }

    private fun setupParsers() {
        this.parser = AnnotationParser(this.manager, CommandSender::class.java)
        this.parser.defaultValueRegistry()

        val reg = this.manager.parserRegistry()

        reg.registerNamedParserSupplier("boolean") {
            BooleanParser.booleanParser<CommandSender>().parser()
        }
        reg.registerNamedParserSupplier("byte") {
            ByteParser.byteParser<CommandSender>().parser()
        }
        reg.registerNamedParserSupplier("char") {
            CharacterParser.characterParser<CommandSender>().parser()
        }
        reg.registerNamedParserSupplier("double") {
            DoubleParser.doubleParser<CommandSender>().parser()
        }
        reg.registerNamedParserSupplier("duration") {
            DurationParser.durationParser<CommandSender>().parser()
        }
        reg.registerNamedParserSupplier("float") {
            FloatParser.floatParser<CommandSender>().parser()
        }
        reg.registerNamedParserSupplier("int") {
            IntegerParser.integerParser<CommandSender>().parser()
        }
        reg.registerNamedParserSupplier("long") {
            LongParser.longParser<CommandSender>().parser()
        }
        reg.registerNamedParserSupplier("short") {
            ShortParser.shortParser<CommandSender>().parser()
        }
        reg.registerNamedParserSupplier("string") {
            StringParser.stringParser<CommandSender>().parser()
        }
        reg.registerNamedParserSupplier("string_array") {
            StringArrayParser.stringArrayParser<CommandSender>().parser()
        }
        reg.registerNamedParserSupplier("uuid") {
            UUIDParser.uuidParser<CommandSender>().parser()
        }
        reg.registerNamedParserSupplier("greedy_string") {
            StringParser.greedyStringParser<CommandSender>().parser()
        }
        reg.registerNamedParserSupplier("quoted_string") {
            StringParser.quotedStringParser<CommandSender>().parser()
        }
        reg.registerNamedParserSupplier("long_positive") {
            LongParser.longParser<CommandSender>(0, Long.MAX_VALUE).parser()
        }
        reg.registerNamedParserSupplier("double_positive") {
            DoubleParser.doubleParser<CommandSender>(0.0, Double.MAX_VALUE).parser()
        }
        reg.registerNamedParserSupplier("player") {
            PlayerParser.playerParser<CommandSender>().parser()
        }
        reg.registerNamedParserSupplier("offline_player") {
            OfflinePlayerParser.offlinePlayerParser<CommandSender>().parser()
        }
        reg.registerNamedParserSupplier("material") {
            MaterialParser.materialParser<CommandSender>().parser()
        }
        reg.registerNamedParserSupplier("world") {
            WorldParser.worldParser<CommandSender>().parser()
        }
        reg.registerNamedParserSupplier("item") {
            ItemStackParser.itemStackParser<CommandSender>().parser()
        }
        reg.registerNamedParserSupplier("item_predicate") {
            ItemStackPredicateParser.itemStackPredicateParser<CommandSender>().parser()
        }
        reg.registerNamedParserSupplier("enchantment") {
            EnchantmentParser.enchantmentParser<CommandSender>().parser()
        }
        reg.registerNamedParserSupplier("block_predicate") {
            BlockPredicateParser.blockPredicateParser<CommandSender>().parser()
        }
        reg.registerNamedParserSupplier("namespacedkey") {
            NamespacedKeyParser.namespacedKeyParser<CommandSender>().parser()
        }

    }

    /**
     * Registers a parser under a named identifier for annotation usage.
     */
    fun <T> registerParser(
        name: String,
        supplier: () -> ArgumentParser<CommandSender, T>
    ) {
        manager.parserRegistry().registerNamedParserSupplier(name) {
            supplier()
        }
    }

    private fun setupExceptionHandlers() {
        MinecraftExceptionHandler.createNative<CommandSender>()
            .handler(InvalidSyntaxException::class.java) { _, ctx ->
                val sender = ctx.context().sender()
                val input = ctx.context().rawInput().input()

                if (sender is Player) {
                    help.queryCommands(input, sender)
                } else {
                    sender.sendMessage(
                        ComponentUtil.parse(
                            "<red>Invalid syntax. Use <yellow>/cms help ${ctx.exception().correctSyntax()}</yellow>"
                        )
                    )
                    this.help.queryCommands(input, sender)
                }
                null
            }
        MinecraftExceptionHandler.createNative<CommandSender>()
            .handler(NoPermissionException::class.java) { _, ctx ->
                ComponentUtil.parse(
                    "You do not have permission to do this. <dark_gray>(<red>${ctx.exception().missingPermission()}</red>)"
                )
            }
            .handler(InvalidCommandSenderException::class.java) { _, _ ->
                ComponentUtil.parse("This command can only be used by a player.")
            }
            .handler(ArgumentParseException::class.java) { _, ex ->
                ComponentUtil.parse(
                    "<dark_red><bold>ARGS</bold></dark_red> <dark_gray>» <red>${ex.exception().cause?.message ?: "Invalid command argument!"}"
                )
            }
            .handler(CommandExecutionException::class.java) { _, ctx ->
                val cause = ctx.exception().cause?.message ?: "Unknown internal error"
                ComponentUtil.parse(
                    "An error occurred while executing the command: <red>$cause"
                )
            }
            .handler(Exception::class.java) { _, ctx ->
                ComponentUtil.parse(
                    "Something went wrong: <red>${ctx.exception().message ?: "Unknown exception"}"
                )
            }
            .captionFormatter(ComponentCaptionFormatter.miniMessage())
            .registerTo(this.manager)
    }


    private fun setupHelp() {
        val baseCommand = paperPlugin.pluginMeta.name

        this.help = ImmutableMinecraftHelp.builder<CommandSender>()
            .commandManager(manager)
            .audienceProvider(AudienceProvider.nativeAudience())
            .commandPrefix("/$baseCommand help")
            .build()

        // Brigadier instelling (aanbevolen)
        manager.brigadierManager().settings()[BrigadierSetting.FORCE_EXECUTABLE] = true
    }

    fun register(vararg commands: Any): List<Command<CommandSender>> {
        return this.parser.parse(*commands).toList()
    }

    fun help(): MinecraftHelp<CommandSender> = this.help
}
