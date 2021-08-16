package org.wycliffeassociates.otter.common.data.primitives

enum class ImageRatio(val width: Int, val height: Int) {
    SIXTEEN_BY_NINE(16, 9),
    FOUR_BY_THREE(4, 3),
    FOUR_BY_ONE(4, 1),
    TWO_BY_ONE(2, 1),
    ONE_BY_ONE(1, 1),
    DEFAULT(0, 0);

    override fun toString(): String {
        return if (this != DEFAULT) {
            "${width}x${height}"
        } else ""
    }

    fun getImageSuffix(): String {
        return if (this != DEFAULT) {
            "_${toString()}"
        } else ""
    }
}