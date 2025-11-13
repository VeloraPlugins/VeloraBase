package online.veloraplugins.base.common.string

object CaseConverter {
    private const val SMALL_CAPS_MAP = "ᴀʙᴄᴅᴇꜰɢʜɪᴊᴋʟᴍɴᴏᴘǫʀꜱᴛᴜᴠᴡxʏᴢ"

    @JvmStatic
    fun toSmallCaps(text: String): String {
        val result = StringBuilder()
        for (c in text.toCharArray()) {
            when (c) {
                in 'a'..'z' -> result.append(SMALL_CAPS_MAP[c.code - 'a'.code])
                in 'A'..'Z' -> result.append(SMALL_CAPS_MAP[c.code - 'A'.code])
                else -> result.append(c)
            }
        }
        return result.toString()
    }
}