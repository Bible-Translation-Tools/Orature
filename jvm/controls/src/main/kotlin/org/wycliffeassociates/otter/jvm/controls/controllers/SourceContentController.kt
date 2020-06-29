package org.wycliffeassociates.otter.jvm.controls.controllers

import javafx.beans.binding.Bindings
import javafx.beans.binding.BooleanBinding
import javafx.beans.binding.ObjectBinding
import javafx.beans.binding.StringBinding
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.StringProperty
import javafx.scene.Node
import org.wycliffeassociates.otter.jvm.controls.sourceformattoggle.SourceFormatToggle
import tornadofx.*
import java.util.concurrent.Callable

class SourceContentController {

    val activeSourceProperty = SimpleObjectProperty<SourceFormatToggle.SourceFormat>()

    fun sourceContentLabelBinding(
        audioLabelProperty: StringProperty,
        textLabelProperty: StringProperty
    ): StringBinding {
        return Bindings.createStringBinding(
            Callable {
                when (activeSourceProperty.value) {
                    SourceFormatToggle.SourceFormat.AUDIO -> audioLabelProperty.value
                    SourceFormatToggle.SourceFormat.TEXT -> textLabelProperty.value
                    else -> audioLabelProperty.value
                }
            },
            activeSourceProperty,
            audioLabelProperty,
            textLabelProperty
        )
    }

    fun sourceContentIconBinding(audioIcon: Node, textIcon: Node): ObjectBinding<Node> {
        return Bindings.createObjectBinding(
            Callable {
                when (activeSourceProperty.value) {
                    SourceFormatToggle.SourceFormat.AUDIO -> audioIcon
                    SourceFormatToggle.SourceFormat.TEXT -> textIcon
                    else -> audioIcon
                }
            },
            activeSourceProperty
        )
    }

    fun audioActiveBinding(): BooleanBinding {
        return activeSourceProperty.booleanBinding {
            it == SourceFormatToggle.SourceFormat.AUDIO
        }
    }

    fun textActiveBinding(): BooleanBinding {
        return activeSourceProperty.booleanBinding {
            it == SourceFormatToggle.SourceFormat.TEXT
        }
    }
}