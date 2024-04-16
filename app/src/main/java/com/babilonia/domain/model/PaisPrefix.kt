package com.babilonia.domain.model


data class PaisPrefix(
    var name: String,
    var prefix: String,
    var mask: String,
    var isoCode: String
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PaisPrefix

        if (name != other.name) return false
        if (prefix != other.prefix) return false
        if (mask != other.mask) return false
        if (isoCode != other.isoCode) return false
        return true
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

}

