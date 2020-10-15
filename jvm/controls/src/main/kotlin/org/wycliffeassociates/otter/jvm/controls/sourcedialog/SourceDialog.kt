package org.wycliffeassociates.otter.jvm.controls.sourcedialog

import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import javafx.scene.Cursor
import javafx.scene.Node
import javafx.scene.layout.Priority
import javafx.stage.Modality
import javafx.stage.Stage
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
    val sourceContentTitleProperty = SimpleStringProperty()
    val showDialogProperty = SimpleBooleanProperty()

    var dialogStage: Stage? = null

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
                addClass("source-dialog__title")

                label(dialogTitleProperty) {
                    addClass("source-dialog__label")
                    visibleWhen(textProperty().isNotEmpty)
                    managedProperty().bind(visibleProperty())
                }

                setOnDrag(this)
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
                    playLabelProperty.set(messages["playSource"])
                    pauseLabelProperty.set(messages["pauseSource"])

                    contentTitleProperty.bind(sourceContentTitleProperty)
                }
            )
        }
    }

    fun open() {
        dialogStage = openModal(StageStyle.UNDECORATED, Modality.APPLICATION_MODAL, false)
    }

    private fun setOnDrag(node: Node) {
        node.onHover {
            node.cursor = if (it) Cursor.OPEN_HAND else Cursor.DEFAULT
        }

        node.setOnMousePressed { pressEvent ->
            node.cursor = Cursor.CLOSED_HAND
            node.setOnMouseDragged { dragEvent ->
                dialogStage?.let {
                    it.x = dragEvent.screenX - pressEvent.sceneX
                    it.y = dragEvent.screenY - pressEvent.sceneY
                }
            }
        }

        node.setOnMouseReleased {
            node.cursor = Cursor.OPEN_HAND
        }
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
