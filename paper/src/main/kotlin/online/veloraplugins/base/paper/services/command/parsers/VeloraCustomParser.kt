package online.veloraplugins.base.paper.services.command.parsers

import io.leangen.geantyref.TypeToken
import org.incendo.cloud.context.CommandContext
import org.incendo.cloud.context.CommandInput
import org.incendo.cloud.parser.ArgumentParseResult
import org.incendo.cloud.parser.ArgumentParser
import org.incendo.cloud.parser.ParserDescriptor
import org.incendo.cloud.suggestion.Suggestion
import org.incendo.cloud.suggestion.SuggestionProvider
import java.util.concurrent.CompletableFuture

/**
 * Base class for all custom parsers in VeloraBase.
 */
abstract class VeloraCustomParser<C, T : Any>(
    private val type: Class<T>
) : ArgumentParser<C, T>,
    ParserDescriptor<C, T>,
    SuggestionProvider<C> {

    /** Parser display name */
    abstract fun name(): String

    /** Convert input → value or throw exception */
    abstract fun parseValue(input: String, ctx: CommandContext<C>): T

    /** Suggestions (optional) */
    open fun suggest(input: String, ctx: CommandContext<C>): List<String> = emptyList()

    override fun parser(): ArgumentParser<C, T> = this

    override fun valueType(): TypeToken<T> = TypeToken.get(type)

    override fun parseFuture(
        commandContext: CommandContext<C>,
        commandInput: CommandInput
    ): CompletableFuture<ArgumentParseResult<T>> {

        val raw = commandInput.readString()

        return CompletableFuture.supplyAsync {
            try {
                val value = parseValue(raw, commandContext)
                @Suppress("UNCHECKED_CAST")
                ArgumentParseResult.success(value)

            } catch (ex: Exception) {
                @Suppress("UNCHECKED_CAST")
                ArgumentParseResult.failure(ex)
            }
        }
    }

    override fun suggestionsFuture(
        context: CommandContext<C>,
        input: CommandInput
    ): CompletableFuture<out Iterable<Suggestion>> {

        val s = input.remainingInput()

        val list = suggest(s, context)
            .map { Suggestion.suggestion(it) }

        return CompletableFuture.completedFuture(list)
    }
}
