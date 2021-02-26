package org.wycliffeassociates.otter.jvm.controls.dialog

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
import org.wycliffeassociates.otter.jvm.controls.media.SourceContent
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
                    if (it) app.workspace.dock(this) else app.workspace.navigateBack()
                }
            }
        }
    }

    override val root = vbox {
        addClass("source-dialog")
        vbox {
            addClass("source-dialog__title")
            label(dialogTitleProperty) {
                addClass("source-dialog__label")
                visibleWhen(textProperty().isNotEmpty)
                managedProperty().bind(visibleProperty())
            }
        }

        vbox {
            alignment = Pos.CENTER
            label(dialogTextProperty) {
                addClass("source-dialog__label", "source-dialog__label--message")
                visibleWhen(textProperty().isNotEmpty)
                managedWhen(visibleProperty())
            }
        }
        add(
            SourceContent().apply {
                vgrow = Priority.ALWAYS
                sourceTextProperty.bind(this@SourceDialog.sourceTextProperty)
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

fun sourcedialog(setup: SourceDialog.() -> Unit = {}): SourceDialog {
    val sourceDialog = SourceDialog()
    sourceDialog.setup()
    return sourceDialog
}
