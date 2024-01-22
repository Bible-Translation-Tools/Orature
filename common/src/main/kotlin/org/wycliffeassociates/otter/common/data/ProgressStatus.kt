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
package org.wycliffeassociates.otter.common.data

/**
 * Status of the current stage of the background task to be shown in the UI.
 *
 * @param titleKey the localized key for the title text
 * @param titleMessage the message to be formatted with the localized title
 * @param subTitleKey the localized key for the subtitle text
 * @param subTitleMessage the message to be formatted with the localized subtitle
 */
data class ProgressStatus(
    val titleKey: String? = null,
    val titleMessage: String? = null,
    val subTitleKey: String? = null,
    val subTitleMessage: String? = null,
    val percent: Double? = null
)