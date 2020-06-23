package org.wycliffeassociates.otter.jvm.controls.sourcedialog

import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import javafx.stage.Modality
import javafx.stage.StageStyle
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.jvm.controls.SourceContent
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

    val sourceTextProperty = SimpleStringProperty()
    var sourceText by sourceTextProperty

    val showDialogProperty = SimpleBooleanProperty()

    private val sourceContent = SourceContent().apply {
        visibleWhen { audioAvailableProperty }
        managedWhen { visibleProperty() }

        audioPlayerProperty.bind(playerProperty)
        sourceTextWidthProperty.bind(primaryStage.widthProperty().divide(1.4))

        sourceAudioLabelProperty.set(messages["sourceAudio"])
        sourceTextLabelProperty.set(messages["sourceText"])

        sourceTextProperty.bind(this@SourceDialog.sourceTextProperty)
    }

    init {
        importStylesheet(javaClass.getResource("/css/source-dialog.css").toExternalForm())

        showDialogProperty.onChangeAndDoNow {
            it?.let {
                Platform.runLater {
                    if (it) open() else close()
                }
            }
        }

        sourceContent.sourceFormatChangedProperty.onChange {
            currentWindow?.sizeToScene()
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
                paddingBottom = 20
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
            add(sourceContent)
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
