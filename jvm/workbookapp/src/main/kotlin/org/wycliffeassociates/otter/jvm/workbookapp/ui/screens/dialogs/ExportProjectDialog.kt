package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.dialogs

import javafx.beans.property.SimpleObjectProperty
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.ScrollPane
import javafx.scene.control.ToggleGroup
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.common.data.workbook.ChapterSummary
import org.wycliffeassociates.otter.common.data.workbook.WorkbookDescriptor
import org.wycliffeassociates.otter.common.domain.project.exporter.ExportType
import org.wycliffeassociates.otter.jvm.controls.button.cardRadioButton
import org.wycliffeassociates.otter.jvm.controls.dialog.OtterDialog
import org.wycliffeassociates.otter.jvm.workbookapp.ui.events.WorkbookExportEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.tableview.exportProjectTableView
import tornadofx.*
import java.text.MessageFormat

class ExportProjectDialog : OtterDialog() {

    val availableChapters = observableListOf<ChapterSummary>()
    val workbookDescriptorProperty = SimpleObjectProperty<WorkbookDescriptor>()

    private val exportTypeProperty = SimpleObjectProperty<ExportType>(ExportType.BACKUP)
    private val selectedChapters = observableSetOf<ChapterSummary>()
    private val onCloseActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()

    private val content = VBox().apply {
        addClass("confirm-dialog", "export-project-dialog")

        hbox {
            addClass("confirm-dialog__header")
            label {
                textProperty().bind(workbookDescriptorProperty.stringBinding {
                    MessageFormat.format(messages["bookNameExportTitle"], it?.title)
                })
                addClass("h3")
            }
            region { hgrow = Priority.ALWAYS }
            button {
                addClass("btn", "btn--icon", "btn--borderless")
                graphic = FontIcon(MaterialDesign.MDI_CLOSE_CIRCLE)
                tooltip(messages["close"])
                onActionProperty().bind(onCloseActionProperty)
            }
        }

        vbox {
            addClass("confirm-dialog__body", "export-project-dialog__body")
            vgrow = Priority.ALWAYS

            borderpane {
                left = scrollpane {
                    addClass("left-pane")
                    isFitToWidth = true
                    hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER

                    vbox {
                        val tg = ToggleGroup()

                        cardRadioButton(tg) {
                            titleProperty.set(messages["backup"])
                            selectedProperty().onChange {
                                if (it) {
                                    subTitleProperty.set(messages["exportBackupDescription"])
                                    exportTypeProperty.set(ExportType.BACKUP)
                                } else {
                                    subTitleProperty.set("")
                                }
                            }
                            isSelected = true
                        }
                        cardRadioButton(tg) {
                            titleProperty.set(messages["sourceAudio"])
                            selectedProperty().onChange {
                                if (it) {
                                    subTitleProperty.set(messages["exportSourceAudioDescription"])
                                    exportTypeProperty.set(ExportType.SOURCE_AUDIO)
                                } else {
                                    subTitleProperty.set("")
                                }
                            }
                        }
                        cardRadioButton(tg) {
                            titleProperty.set(messages["listen"])
                            selectedProperty().onChange {
                                if (it) {
                                    subTitleProperty.set(messages["exportListenDescription"])
                                    exportTypeProperty.set(ExportType.LISTEN)
                                } else {
                                    subTitleProperty.set("")
                                }
                            }
                        }
                        cardRadioButton(tg) {
                            titleProperty.set(messages["publish"])
                            selectedProperty().onChange {
                                if (it) {
                                    subTitleProperty.set(messages["exportPublishDescription"])
                                    exportTypeProperty.set(ExportType.PUBLISH)
                                } else {
                                    subTitleProperty.set("")
                                }
                            }
                        }
                    }
                }

                center = exportProjectTableView(availableChapters, selectedChapters)
            }
        }

        hbox {
            addClass("confirm-dialog__footer")
            label {
                addClass("h5")
                textProperty().bind(exportTypeProperty.stringBinding {
                    val estimatedSize = when(it) {
                        ExportType.BACKUP -> messages["large"]
                        ExportType.LISTEN -> messages["small"]
                        ExportType.SOURCE_AUDIO, ExportType.PUBLISH -> messages["normal"]
                        else -> { "" }
                    }
                    MessageFormat.format(messages["estimatedFileSize"], estimatedSize)
                })
            }
            region { hgrow = Priority.ALWAYS }
            button(messages["Export"]) {
                addClass("btn", "btn--primary")
                disableWhen { booleanBinding(selectedChapters) { selectedChapters.isEmpty() } }

                action {
                    val directory = chooseDirectory(FX.messages["exportProject"], owner = currentWindow)
                    directory?.let { dir ->
                        FX.eventbus.fire(
                            WorkbookExportEvent(
                                workbookDescriptorProperty.value,
                                exportTypeProperty.value,
                                dir,
                                selectedChapters.map { it.number }
                            )
                        )
                        close()
                    }
                }
            }
        }
    }

    init {
        setContent(content)
    }

    override fun onDock() {
        super.onDock()
        selectedChapters.addAll(availableChapters) // select all by default
    }

    fun setOnCloseAction(op: () -> Unit) {
        onCloseActionProperty.set(EventHandler { op() })
    }
}