package org.wycliffeassociates.otter.common.utils

import java.io.File

fun filePathWithSuffix(path: String, suffix: String): String {
    val file = File(path)
    return file
        .parentFile
        .resolve(file.nameWithoutExtension + suffix + ".${file.extension}")
        .invariantSeparatorsPath
}
