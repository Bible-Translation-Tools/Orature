package org.wycliffeassociates.otter.jvm.controls.model

data class NotificationViewData(
    val titleKey: String,
    val subtitleKey: String,
    val statusType: NotificationStatusType,
    val mainAction: (() -> Unit)? = null
)