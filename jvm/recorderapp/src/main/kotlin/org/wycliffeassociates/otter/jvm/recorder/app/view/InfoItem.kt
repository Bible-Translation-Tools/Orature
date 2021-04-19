package org.wycliffeassociates.otter.jvm.recorder.app.view

import javafx.scene.Node
import javafx.scene.layout.HBox
import tornadofx.*

class InfoItem(major: String, minor: String? = null, icon: Node) : HBox() {
    init {
        addClass("info__item")

        label(major) {
            addClass("info__item__major")
            graphic = icon.apply {
                addClass("info__item__major__icon")
            }
        }
        minor?.let {
            label(it) {
                addClass("info__item__minor")
            }
        }
    }
}