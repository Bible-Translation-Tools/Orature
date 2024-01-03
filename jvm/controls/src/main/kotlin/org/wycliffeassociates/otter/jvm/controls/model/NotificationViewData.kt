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
    val actionCallback: () -> Unit = {},
)
