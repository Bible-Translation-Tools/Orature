package org.wycliffeassociates.otter.jvm.controls.dialog

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import javafx.scene.layout.Priority
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.jvm.controls.media.SourceContent
import tornadofx.*

class PluginOpenedPage : Fragment() {

    val dialogTitleProperty = SimpleStringProperty()
    val dialogTextProperty = SimpleStringProperty()
    val playerProperty = SimpleObjectProperty<IAudioPlayer>()
    val audioAvailableProperty = SimpleBooleanProperty(false)
    val sourceTextProperty = SimpleStringProperty()
    val sourceContentTitleProperty = SimpleStringProperty()

    init {
        importStylesheet(resources["/css/plugin-opened-page.css"])
    }

    override val root = vbox {
        alignment = Pos.CENTER
        addClass("plugin-opened-page")
        label(dialogTitleProperty) {
            addClass("plugin-opened-page__title", "plugin-opened-page__label")
            visibleWhen(textProperty().isNotEmpty)
            managedProperty().bind(visibleProperty())
        }
        label(dialogTextProperty) {
            alignment = Pos.CENTER
            addClass("plugin-opened-page__label", "plugin-opened-page__label--message")
            visibleWhen(textProperty().isNotEmpty)
            managedWhen(visibleProperty())
        }
        add(
            SourceContent().apply {
                vgrow = Priority.ALWAYS
                sourceTextProperty.bind(this@PluginOpenedPage.sourceTextProperty)
                audioPlayerProperty.bind(playerProperty)

                audioNotAvailableTextProperty.set(messages["audioNotAvailable"])
                textNotAvailableTextProperty.set(messages["textNotAvailable"])
                playLabelProperty.set(messages["playSource"])
                pauseLabelProperty.set(messages["pauseSource"])

                contentTitleProperty.bind(sourceContentTitleProperty)
                isMinimizableProperty.set(false)
            }
        )
    }

    override fun onUndock() {
        playerProperty.value?.stop()
        super.onUndock()
    }
}
