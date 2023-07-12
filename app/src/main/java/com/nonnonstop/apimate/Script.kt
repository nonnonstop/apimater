package com.nonnonstop.apimate

data class Script(val filename: String, val presets: Array<ScriptPreset>) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Script

        if (filename != other.filename) return false
        if (!presets.contentEquals(other.presets)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = filename.hashCode()
        result = 31 * result + presets.contentHashCode()
        return result
    }
}
