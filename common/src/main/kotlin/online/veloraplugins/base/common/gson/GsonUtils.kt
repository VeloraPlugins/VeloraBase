package online.veloraplugins.base.common.gson

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

/**
 * Centralized Gson utility used across VeloraBase.
 *
 * - Provides a single shared Gson instance
 * - Helper functions for serialization and deserialization
 * - Supports generic types via TypeToken
 * - Allows registration of custom type adapters
 *
 * Useful for:
 * - Redis events
 * - Config serialization
 * - JSON debugging logs
 * - Any data transfer between services
 */
object GsonUtils {

    /** Builder so plugins can add custom adapters */
    private val builder = GsonBuilder()
        .disableHtmlEscaping()
        .serializeNulls()
        .setPrettyPrinting()

    private var built = false

    /** Lazy Gson instance */
    val gson: Gson by lazy {
        built = true
        builder.create()
    }

    /**
     * Registers a custom type adapter BEFORE gson is created.
     *
     * @throws IllegalStateException if called after gson instance is initialized.
     */
    fun registerAdapter(type: Type, adapter: Any) {
        if (built) {
            throw IllegalStateException("Cannot register Gson adapter: Gson is already initialized!")
        }
        builder.registerTypeAdapter(type, adapter)
    }

    /**
     * Returns true if the Gson instance has already been created.
     */
    fun isBuilt(): Boolean = built

    /** Converts any object into JSON string */
    fun toJson(obj: Any): String = gson.toJson(obj)

    /** Converts any object into a JsonObject */
    fun toJsonObject(obj: Any): JsonObject =
        gson.toJsonTree(obj).asJsonObject

    /** Deserialize JSON to type */
    fun <T> fromJson(json: String, clazz: Class<T>): T =
        gson.fromJson(json, clazz)

    /** Deserialize JSON into a generic type using TypeToken */
    fun <T> fromJson(json: String, type: TypeToken<T>): T =
        gson.fromJson(json, type.type)

    /** Parse raw JSON into JsonObject */
    fun parseObject(json: String): JsonObject =
        JsonParser.parseString(json).asJsonObject

    /** Parse raw JSON into JsonElement */
    fun parse(json: String): JsonElement =
        JsonParser.parseString(json)
}
