package org.wycliffeassociates.otter.jvm.controls.utils

import javafx.scene.image.ImageView
import javafx.scene.layout.Region

fun ImageView.fitToParentHeight() {
    val parent = this.parent
    if (parent != null && parent is Region) {
        fitToHeight(parent)
    }
}

fun ImageView.fitToParentWidth() {
    val parent = this.parent
    if (parent != null && parent is Region) {
        fitToWidth(parent)
    }
}

fun ImageView.fitToParentSize() {
    fitToParentHeight()
    fitToParentWidth()
}

fun ImageView.fitToHeight(region: Region) {
    fitHeightProperty().bind(region.heightProperty())
}

fun ImageView.fitToWidth(region: Region) {
    fitWidthProperty().bind(region.widthProperty())
}

fun ImageView.fitToSize(region: Region) {
    fitToHeight(region)
    fitToWidth(region)
}
