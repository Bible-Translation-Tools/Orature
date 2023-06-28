package org.wycliffeassociates.otter.jvm.controls.model

import org.kordamp.ikonli.Ikon

data class NotificationViewData(
    val title: String,
    val message: String,
    val statusType: NotificationStatusType,
    val actionText: String? = null,
    val actionIcon: Ikon? = null,
    val actionCallback: () -> Unit = {}
)