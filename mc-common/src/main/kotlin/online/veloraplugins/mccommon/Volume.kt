package online.veloraplugins.mccommon

enum class Volume(val value: Double) {

    V0(0.0),
    V1(0.1),
    V2(0.2),
    V3(0.3),
    V4(0.4),
    V5(0.5),
    V6(0.6),
    V7(0.7),
    V8(0.8),
    V9(0.9),
    V10(1.0);

    companion object {
        /**
         * Convert raw double (0.0 - 1.0) to closest Volume enum.
         */
        fun fromDouble(v: Double): Volume {
            val clamped = v.coerceIn(0.0, 1.0)
            val index = (clamped * 10).toInt()
            return entries[index]
        }
    }
}
