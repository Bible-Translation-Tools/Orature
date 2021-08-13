package org.wycliffeassociates.otter.common.data.primitives

enum class ImageRatio(val width: Int, val height: Int) {
    SIXTEEN_BY_NINE(16, 9),
    FOUR_BY_THREE(4, 3),
    ONE_BY_ONE(1, 1),
    DEFAULT(0, 0);

    fun getStringFormat(): String {
        return if (this != DEFAULT) {
            "${width}x${height}"
        } else ""
    }
}