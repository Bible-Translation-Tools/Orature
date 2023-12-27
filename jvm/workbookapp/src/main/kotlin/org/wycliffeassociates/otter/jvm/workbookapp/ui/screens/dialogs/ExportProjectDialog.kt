package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.dialogs

import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.SetChangeListener
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.ScrollPane
import javafx.scene.control.ToggleGroup
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.jvm.controls.model.ChapterDescriptor
import org.wycliffeassociates.otter.common.data.workbook.WorkbookDescriptor
import org.wycliffeassociates.otter.common.domain.project.exporter.ExportType
import org.wycliffeassociates.otter.jvm.controls.button.cardRadioButton
import org.wycliffeassociates.otter.jvm.controls.customizeScrollbarSkin
import org.wycliffeassociates.otter.jvm.controls.dialog.OtterDialog
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.tableview.ExportProjectTableView
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.tableview.exportProjectTableView
import org.wycliffeassociates.otter.jvm.workbookapp.ui.events.WorkbookExportEvent
import tornadofx.*
import java.text.MessageFormat
import kotlin.math.roundToInt

class ExportProjectDialog : OtterDialog() {

    val chapters = observableListOf<ChapterDescriptor>()
    val selectedChapters = observableSetOf<ChapterDescriptor>()
    val workbookDescriptorProperty = SimpleObjectProperty<WorkbookDescriptor>()
    val onEstimateSizeAction = SimpleObjectProperty<(WorkbookDescriptor, List<Int>, ExportType) -> Long>()

    private val exportTypeProperty = SimpleObjectProperty<ExportType>(ExportType.BACKUP)
    private val estimatedSizeProperty = SimpleDoubleProperty(0.0)
    private val onCloseActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()
    private lateinit var tableView: ExportProjectTableView

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
                                    exportTypeProperty.set(ExportType.BACKUP)
                                    onSelectExportType(ExportType.BACKUP)
                                }
                            }
                            isSelected = true
                        }
                        cardRadioButton(tg) {
                            titleProperty.set(messages["sourceAudio"])
                            selectedProperty().onChange {
                                if (it) {
                                    exportTypeProperty.set(ExportType.SOURCE_AUDIO)
                                    onSelectExportType(ExportType.SOURCE_AUDIO)
                                }
                            }
                        }
                        cardRadioButton(tg) {
                            titleProperty.set(messages["listen"])
                            selectedProperty().onChange {
                                if (it) {
                                    exportTypeProperty.set(ExportType.LISTEN)
                                    onSelectExportType(ExportType.LISTEN)
                                }
                            }
                        }
                        cardRadioButton(tg) {
                            titleProperty.set(messages["publish"])
                            selectedProperty().onChange {
                                if (it) {
                                    exportTypeProperty.set(ExportType.PUBLISH)
                                    onSelectExportType(ExportType.PUBLISH)
                                }
                            }
                        }
                    }
                }

                center = exportProjectTableView(chapters, selectedChapters) {
                    tableView = this
                }
            }
        }

        hbox {
            addClass("confirm-dialog__footer")
            label {
                addClass("h5")
                textProperty().bind(estimatedSizeProperty.stringBinding {
                    MessageFormat.format(messages["estimatedFileSize"], it)
                })
            }
            region { hgrow = Priority.ALWAYS }
            button(messages["exportProject"]) {
                addClass("btn", "btn--primary")
                disableWhen { booleanBinding(selectedChapters) { selectedChapters.isEmpty() } }
                tooltip(text)
                graphic = FontIcon(MaterialDesign.MDI_PUBLISH)

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
                        onCloseActionProperty.value?.handle(ActionEvent())
                    }
                }
            }
        }
    }

    init {
        setContent(content)
    }

    private val selectionListener = SetChangeListener<ChapterDescriptor> {
        updateEstimateSize()
    }

    override fun onDock() {
        super.onDock()
        onSelectExportType(ExportType.BACKUP) // selects default option
        tableView.customizeScrollbarSkin()

        selectedChapters.addListener(selectionListener)
    }

    override fun onUndock() {
        super.onUndock()
        selectedChapters.removeListener(selectionListener)
    }

    fun setOnCloseAction(op: () -> Unit) {
        onCloseActionProperty.set(EventHandler { op() })
    }

    private fun onSelectExportType(type: ExportType) {
        val newList = when (type) {
            ExportType.BACKUP -> chapters.map { it.copy(selectable = it.progress > 0.0) }
            else -> chapters.map { it.copy(selectable = it.progress == 1.0) }
        }
        chapters.setAll(newList)
        selectedChapters.clear()
        selectedChapters.addAll(newList.filter { it.selectable }) // select available chapters by default
    }

    private fun updateEstimateSize() {
        val sizeInBytes = onEstimateSizeAction.value?.invoke(
            workbookDescriptorProperty.value,
            selectedChapters.map { it.number },
            exportTypeProperty.value
        ) ?: 0

        val sizeInMBs = sizeInBytes.toDouble() / 1_000_000
        val roundedSize = (sizeInMBs * 10).roundToInt() / 10.0 // round to 1 decimal place
        estimatedSizeProperty.set(roundedSize)
    }
}