package org.wycliffeassociates.otter.jvm.controls.sourcedialog

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.stage.Modality
import javafx.stage.StageStyle
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.jvm.controls.AudioPlayerNode
import org.wycliffeassociates.otter.jvm.controls.skins.media.SourceAudioSkin
import org.wycliffeassociates.otter.jvm.controls.styles.AppStyles
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import tornadofx.*

class SourceDialog : Fragment() {

    val dialogTitleProperty = SimpleStringProperty()
    var dialogTitle by dialogTitleProperty

    val textProperty = SimpleStringProperty()
    var text by textProperty

    val playerProperty = SimpleObjectProperty<IAudioPlayer>()
    var player by playerProperty

    val closeTextProperty = SimpleStringProperty("Back")
    var closeText by closeTextProperty

    val audioAvailableProperty = SimpleBooleanProperty(false)
    var audioAvailable by audioAvailableProperty

    val onCloseActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()

    init {
        importStylesheet<SourceDialogStyles>()
    }

    override val root = borderpane {
        addClass(SourceDialogStyles.defaultSourceDialog)

        top {
            label(dialogTitleProperty) {
                visibleProperty().bind(textProperty().isNotEmpty)
                managedProperty().bind(visibleProperty())
            }
        }
        center {
            vbox {
                alignment = Pos.CENTER
                label(textProperty) {
                    addClass(SourceDialogStyles.message)
                    visibleWhen(textProperty().isNotEmpty)
                    managedWhen { visibleProperty() }
                }
                button(closeTextProperty, AppStyles.closeIcon("20px")) {
                    addClass(SourceDialogStyles.closeButton)
                    onActionProperty().bind(onCloseActionProperty)
                }
            }
        }
        bottom {
            add(
                AudioPlayerNode(null).apply {
                    visibleWhen { audioAvailableProperty }
                    managedWhen { visibleProperty() }
                    style {
                        skin = SourceAudioSkin::class
                    }
                    playerProperty.onChangeAndDoNow {
                        it?.let {
                            load(it)
                        }
                    }
                }
            )
        }
    }

    fun open() {
        openModal(StageStyle.UNDECORATED, Modality.APPLICATION_MODAL, false)
    }

    fun onCloseAction(op: () -> Unit) {
        onCloseActionProperty.set(EventHandler { op.invoke() })
    }
}

fun sourcedialog(setup: SourceDialog.() -> Unit = {}): SourceDialog {
    val sourceDialog = SourceDialog()
    sourceDialog.setup()
    return sourceDialog
}
