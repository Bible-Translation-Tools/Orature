package org.wycliffeassociates.otter.jvm.controls.sourcedialog

import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import javafx.stage.Modality
import javafx.stage.StageStyle
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.jvm.controls.sourcecontent.SourceContent
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import tornadofx.*

class SourceDialog : Fragment() {

    val dialogTitleProperty = SimpleStringProperty()
    val dialogTextProperty = SimpleStringProperty()
    val playerProperty = SimpleObjectProperty<IAudioPlayer>()
    val audioAvailableProperty = SimpleBooleanProperty(false)
    val sourceTextProperty = SimpleStringProperty()

    val bookTitleProperty = SimpleStringProperty()
    val chapterTitleProperty = SimpleStringProperty()
    val chunkTitleProperty = SimpleStringProperty()

    val playLabelProperty = SimpleStringProperty()
    val pauseLabelProperty = SimpleStringProperty()

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
                    managedWhen(visibleProperty())
                }
            }
        }
        bottom {
            add(
                SourceContent().apply {
                    sourceTextProperty.bind(this@SourceDialog.sourceTextProperty)
                    audioPlayerProperty.bind(playerProperty)

                    audioNotAvailableTextProperty.set(messages["audioNotAvailable"])
                    textNotAvailableTextProperty.set(messages["textNotAvailable"])

                    bookTitleProperty.bind(this@SourceDialog.bookTitleProperty)
                    chapterTitleProperty.bind(this@SourceDialog.chapterTitleProperty)
                    chunkTitleProperty.bind(this@SourceDialog.chunkTitleProperty)

                    playLabelProperty.bind(this@SourceDialog.playLabelProperty)
                    pauseLabelProperty.bind(this@SourceDialog.pauseLabelProperty)
                }
            )
        }
    }

    fun open() {
        openModal(StageStyle.UNDECORATED, Modality.APPLICATION_MODAL, false)
    }

    override fun onUndock() {
        playerProperty.value?.stop()
        super.onUndock()
    }
}

fun sourcedialog(setup: SourceDialog.() -> Unit = {}): SourceDialog {
    val sourceDialog = SourceDialog()
    sourceDialog.setup()
    return sourceDialog
}
