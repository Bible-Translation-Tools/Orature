package org.wycliffeassociates.otter.jvm.controls.toggle

class ToggleButtonData(
    val title: String,
    val isDefaultSelected: Boolean = false,
    val onAction: () -> Unit = { }
)
