/**
 * Copyright (C) 2020-2024 Wycliffe Associates
 *
 * This file is part of Orature.
 *
 * Orature is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Orature is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Orature.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.wycliffeassociates.otter.common.data.workbook

import org.wycliffeassociates.otter.common.data.primitives.Anthology
import org.wycliffeassociates.otter.common.data.primitives.Collection
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.data.primitives.ProjectMode
import org.wycliffeassociates.otter.common.data.primitives.bookAnthology
import java.time.LocalDateTime

data class WorkbookDescriptor(
    val id: Int,
    val sourceCollection: Collection,
    val targetCollection: Collection,
    val mode: ProjectMode,
    val progress: Double = 0.0,
    val hasSourceAudio: Boolean = false
) {
    val slug: String = targetCollection.slug
    val title: String = targetCollection.titleKey
    val label: String = targetCollection.labelKey
    val sort: Int = sourceCollection.sort
    val lastModified: LocalDateTime? = targetCollection.modifiedTs

    val sourceLanguage: Language
        get() = sourceCollection.resourceContainer?.language
            ?: throw NullPointerException("Source metadata must not be null")

    val targetLanguage: Language
        get() = targetCollection.resourceContainer?.language
            ?: throw NullPointerException("Target metadata must not be null")

    val anthology = bookAnthology.getOrDefault(slug, Anthology.OTHER)
}