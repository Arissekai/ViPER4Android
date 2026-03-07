package com.llsl.viper4android.audio

data class ParamEntry(val paramId: Int, val values: IntArray) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ParamEntry) return false
        return paramId == other.paramId && values.contentEquals(other.values)
    }

    override fun hashCode(): Int = 31 * paramId + values.contentHashCode()
}
