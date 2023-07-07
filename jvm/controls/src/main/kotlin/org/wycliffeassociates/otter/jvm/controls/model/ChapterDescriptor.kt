package org.wycliffeassociates.otter.jvm.controls.model

data class ChapterDescriptor(val number: Int, val progress: Double) {
    val available: Boolean = progress > 0.0
}
