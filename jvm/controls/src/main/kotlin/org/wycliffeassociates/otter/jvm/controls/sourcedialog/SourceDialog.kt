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

    val shouldOpenDialogProperty = SimpleBooleanProperty()

    init {
        println(this)
        importStylesheet(javaClass.getResource("/css/source-dialog.css").toExternalForm())

        shouldOpenDialogProperty.onChange {
            Platform.runLater {
                if (it) open() else close()
            }
        }
    }

    override val root = borderpane {
        addClass("source-dialog")

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
            vbox {
                alignment = Pos.CENTER
                label(dialogTextProperty) {
                    addClass("source-dialog__label", "source-dialog__label--message")
                    visibleWhen(textProperty().isNotEmpty)
                    managedWhen { visibleProperty() }
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
}

fun sourcedialog(setup: SourceDialog.() -> Unit = {}): SourceDialog {
    val sourceDialog = SourceDialog()
    sourceDialog.setup()
    return sourceDialog
}
