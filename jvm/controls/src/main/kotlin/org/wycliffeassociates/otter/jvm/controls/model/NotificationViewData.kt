package org.wycliffeassociates.otter.jvm.controls.model

import org.kordamp.ikonli.Ikon

data class NotificationViewData(
    val titleKey: String,
    val subtitleKey: String,
    val statusType: NotificationStatusType,
    val actionIcon: Ikon,
    val actionText: String? = null,
    val actionCallback: () -> Unit = {}
)