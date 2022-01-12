package org.wycliffeassociates.otter.common.utils

import java.util.*

fun String.capitalizeString(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
}
