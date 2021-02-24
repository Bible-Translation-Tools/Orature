package org.wycliffeassociates.otter.common.data.primitives

import java.io.File
import java.time.LocalDate

data class ResourceMetadata(
    val conformsTo: String,
    val creator: String,
    val description: String,
    val format: String,
    val identifier: String,
    val issued: LocalDate,
    val language: Language,
    val modified: LocalDate,
    val publisher: String,
    val subject: String,
    val type: ContainerType,
    val title: String,
    val version: String,
    val path: File,
    val id: Int = 0
)
