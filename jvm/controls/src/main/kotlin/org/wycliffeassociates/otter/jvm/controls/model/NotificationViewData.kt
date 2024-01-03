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
package org.wycliffeassociates.otter.jvm.controls.model

import org.kordamp.ikonli.Ikon

/**
 * Defines the relevant information to be displayed in the notification.
 *
 * @param title title/header of the notification.
 * @param message detailed message about the notification. This should be already localized.
 * @param statusType status of the notification.
 * @param actionText (localized) label text of the confirmation button, if available.
 * @param actionIcon icon graphic for the confirmation button, if available.
 * @param actionCallback callback handler for the confirmation action.
 */
data class NotificationViewData(
    val title: String,
    val message: String,
    val statusType: NotificationStatusType,
    val actionText: String? = null,
    val actionIcon: Ikon? = null,
    val actionCallback: () -> Unit = {}
)