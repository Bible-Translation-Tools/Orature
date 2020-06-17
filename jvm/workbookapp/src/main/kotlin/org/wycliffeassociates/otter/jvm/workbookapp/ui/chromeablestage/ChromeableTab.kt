package org.wycliffeassociates.otter.jvm.workbookapp.ui.chromeablestage

import javafx.beans.property.SimpleObjectProperty
import javafx.scene.Node
import javafx.scene.control.Tab
import tornadofx.*

abstract class ChromeableTab : Tab() {
    val animatedContentProperty = SimpleObjectProperty<Node>()
    var animatedContent: Node by animatedContentProperty

    init {
        contentProperty().onChange {
            setAnimatedContent()
        }
    }

    open fun setAnimatedContent() {
        animatedContent = content
    }
}