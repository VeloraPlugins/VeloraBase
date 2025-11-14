package online.veloraplugins.base.common.enums

enum class McLanguage(val code: String) {
    EN_US("en_us"),
    EN_GB("en_gb"),
    NL_NL("nl_nl"),
    DE_DE("de_de"),
    FR_FR("fr_fr"),
    ES_ES("es_es"),
    ES_MX("es_mx"),
    PT_BR("pt_br"),
    RU_RU("ru_ru"),
    JA_JP("ja_jp"),
    ZH_CN("zh_cn"),
    ZH_TW("zh_tw");

    companion object {
        fun fromCode(code: String) =
            entries.find { it.code.equals(code, ignoreCase = true) }
    }
}
