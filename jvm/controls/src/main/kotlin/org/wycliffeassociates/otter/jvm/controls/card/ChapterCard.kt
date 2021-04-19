package org.wycliffeassociates.otter.jvm.controls.card

import javafx.beans.binding.Bindings
import javafx.beans.binding.DoubleBinding
import javafx.beans.binding.StringBinding
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.Control
import javafx.scene.control.Skin
import org.wycliffeassociates.otter.jvm.controls.skins.cards.ChapterCardSkin
import java.io.File
import java.util.concurrent.Callable

class ChapterCard : Control() {

    val coverArtProperty = SimpleObjectProperty<File>()
    val titleProperty = SimpleStringProperty()
    val notStartedTextProperty = SimpleStringProperty("Not Started")
    val totalChunksProperty = SimpleIntegerProperty(0)
    val recordedChunksProperty = SimpleIntegerProperty(0)
    val selectedChunksProperty = SimpleIntegerProperty(0)
    val userHasChunkedProperty = SimpleBooleanProperty(true)

    init {
        styleClass.setAll("chapter-card")
    }

    fun recordedChunksBinding(): StringBinding {
        return Bindings.createStringBinding(
            Callable {
                "${recordedChunksProperty.value}/${totalChunksProperty.value}"
            },
            recordedChunksProperty,
            totalChunksProperty
        )
    }

    fun recordedProgressBinding(): DoubleBinding {
        return Bindings.createDoubleBinding(
            Callable {
                if (totalChunksProperty.value > 0) {
                    val recorded = recordedChunksProperty.value.toDouble()
                    val total = totalChunksProperty.value.toDouble()
                    recorded / total
                } else {
                    0.0
                }
            },
            recordedChunksProperty,
            totalChunksProperty
        )
    }

    fun selectedChunksBinding(): StringBinding {
        return Bindings.createStringBinding(
            Callable {
                "${selectedChunksProperty.value}/${totalChunksProperty.value}"
            },
            selectedChunksProperty,
            totalChunksProperty
        )
    }

    fun selectedProgressBinding(): DoubleBinding {
        return Bindings.createDoubleBinding(
            Callable {
                if (totalChunksProperty.value > 0) {
                    val selected = selectedChunksProperty.value.toDouble()
                    val total = totalChunksProperty.value.toDouble()
                    selected / total
                } else {
                    0.0
                }
            },
            selectedChunksProperty,
            totalChunksProperty
        )
    }

    override fun createDefaultSkin(): Skin<*> {
        return ChapterCardSkin(this)
    }
}
