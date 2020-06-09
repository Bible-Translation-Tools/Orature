package org.wycliffeassociates.otter.jvm.controls.sourcedialog

import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import javafx.stage.Modality
import javafx.stage.StageStyle
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.jvm.controls.AudioPlayerNode
import org.wycliffeassociates.otter.jvm.controls.skins.media.SourceAudioSkin
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import tornadofx.*

class SourceDialog : Fragment() {

    val dialogTitleProperty = SimpleStringProperty()
    var dialogTitle by dialogTitleProperty

    val dialogTextProperty = SimpleStringProperty()
    var dialogText by dialogTextProperty

    val playerProperty = SimpleObjectProperty<IAudioPlayer>()
    var player by playerProperty

    val audioAvailableProperty = SimpleBooleanProperty(false)
    var audioAvailable by audioAvailableProperty

    val showDialogProperty = SimpleBooleanProperty()

    init {
        importStylesheet(javaClass.getResource("/css/source-dialog.css").toExternalForm())

        showDialogProperty.onChangeAndDoNow {
            it?.let {
                Platform.runLater {
                    if (it) open() else close()
                }
            }
        }
    }

    override val root = borderpane {
        addClass("source-dialog")

        prefWidthProperty().bind(primaryStage.widthProperty())

        top {
            vbox {
                alignment = Pos.CENTER
                label(dialogTitleProperty) {
                    addClass("source-dialog__label")
                    visibleWhen(textProperty().isNotEmpty)
                    managedProperty().bind(visibleProperty())
                }
            }
        }
        center {
            hbox {
                alignment = Pos.CENTER
                text(dialogTextProperty) {
                    addClass("source-dialog__text")
                    visibleWhen(textProperty().isNotEmpty)
                    managedWhen { visibleProperty() }
                    wrappingWidthProperty().bind(primaryStage.widthProperty().divide(1.4))
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
                        paddingTop = 10
                    }
                    audioPlayerProperty.bind(playerProperty)
                    sourceTextWidthProperty.bind(primaryStage.widthProperty().divide(1.4))

                    refreshParentProperty.set(true)
                    sourceAudioLabelProperty.set(messages["sourceAudio"])
                }
            )
        }
    }

    fun open() {
        openModal(StageStyle.UNDECORATED, Modality.APPLICATION_MODAL, false)
    }

    override fun onUndock() {
        player?.stop()
        super.onUndock()
    }
}

fun sourcedialog(setup: SourceDialog.() -> Unit = {}): SourceDialog {
    val sourceDialog = SourceDialog()
    sourceDialog.setup()
    return sourceDialog
}
