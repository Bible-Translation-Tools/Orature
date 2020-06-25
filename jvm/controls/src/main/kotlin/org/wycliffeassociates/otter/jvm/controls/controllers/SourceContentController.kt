package org.wycliffeassociates.otter.jvm.controls.controllers

import javafx.beans.binding.Bindings
import javafx.beans.binding.ObjectBinding
import javafx.beans.binding.StringBinding
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.StringProperty
import javafx.scene.Node
import java.util.concurrent.Callable

class SourceContentController {

    val displayPlayerProperty = SimpleBooleanProperty(true)

    fun toggleSource() {
        displayPlayerProperty.set(!displayPlayerProperty.value)
    }

    fun sourceContentLabelBinding(
        audioLabelProperty: StringProperty,
        textLabelProperty: StringProperty
    ): StringBinding {
        return Bindings.createStringBinding(
            Callable {
                if (displayPlayerProperty.value) {
                    audioLabelProperty.value
                } else {
                    textLabelProperty.value
                }
            },
            displayPlayerProperty,
            audioLabelProperty,
            textLabelProperty
        )
    }

    fun sourceContentIconBinding(audioIcon: Node, textIcon: Node): ObjectBinding<Node> {
        return Bindings.createObjectBinding(
            Callable {
                if (displayPlayerProperty.value) {
                    audioIcon
                } else {
                    textIcon
                }
            },
            displayPlayerProperty
        )
    }
}