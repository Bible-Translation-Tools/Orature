package org.wycliffeassociates.otter.jvm.workbookapp.ui.components.drawer

import io.reactivex.SingleEmitter
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.ObservableList
import javafx.collections.ObservableSet
import javafx.geometry.Pos
import javafx.scene.control.CheckBox
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import org.wycliffeassociates.otter.common.domain.project.importer.ImportOptions
import org.wycliffeassociates.otter.jvm.utils.overrideDefaultKeyEventHandler
import tornadofx.*
import tornadofx.FX.Companion.defaultScope
import tornadofx.FX.Companion.messages
import java.text.MessageFormat

class ImportProjectFilterSection(
    private val availableChapters: ObservableList<Int>
) : VBox() {

    lateinit var lv: ListView<Int>
    lateinit var selectAllCheckbox: CheckBox
    lateinit var result: SingleEmitter<ImportOptions>
    private val selectionMode = SimpleObjectProperty<ListSelectionMode>(ListSelectionMode.ALL)
    private val selectionModes = ListSelectionMode.values().asList().asObservable()
    private val selectedChapters = observableSetOf<Int>()
    private val showCustomSelectProperty = SimpleBooleanProperty(false)

    private var onImportAction: (List<Int>) -> Unit = {}
    private var onCancelAction: () -> Unit = {}

    init {
        addClass("import-project-filter__container")
        vgrow = Priority.ALWAYS

        label("We found a matching project in Orature. Please select the chapters you want to import from this file.") {
            addClass("import-project-filter__sub-text")
            minHeight = Region.USE_PREF_SIZE
        }

        hbox {
            addClass("import-project-filter__list-action-group")

            stackpane {
                alignment = Pos.CENTER_LEFT

                combobox(selectionMode, selectionModes) {
                    addClass("wa-combobox", "import-project-filter__dropdown")

                    cellFormat(defaultScope) {
                        graphic = label(messages[item.titleKey]) {
                            addClass("import-project-filter__dropdown__item")
                        }
                    }

                    overrideDefaultKeyEventHandler {
                        when(it) {
                            ListSelectionMode.ALL -> {
                                selectAllCheckbox.isSelected = true
                                showCustomSelectProperty.set(false)
                                selectedChapters.addAll(availableChapters)
                                lv.refresh()
                            }
                            ListSelectionMode.NONE -> {
                                selectAllCheckbox.isSelected = false
                                selectedChapters.clear()
                                lv.refresh()
                            }
                            else -> {
                                selectAllCheckbox.isSelected = false
                                showCustomSelectProperty.set(true)
                            }
                        }
                    }
                }
                checkbox {
                    addClass("import-project-filter__select-all-checkbox")
                    selectAllCheckbox = this

                    setOnAction {
                        if (this.isSelected) {
                            selectedChapters.addAll(availableChapters)
                        } else {
                            selectedChapters.clear()
                        }
                        lv.refresh()
                    }
                }
            }

            label(messages["select"]) {
                addClass("import-project-filter__sub-text")
                textProperty().bind(selectionMode.stringBinding { mode ->
                    if (mode == ListSelectionMode.CUSTOM) {
                        messages[mode.titleKey]
                    } else {
                        messages["select"]
                    }
                })
                isMouseTransparent = true
            }
        }

        textfield {
            promptText = "e.g. 1-5, 8, 10"
            visibleWhen(showCustomSelectProperty)
            managedWhen(visibleProperty())
        }

        listview(availableChapters) {
//            addClass("wa-list-view")
            lv = this
            multiSelect(true)

            setCellFactory {
                ImportFilterSelectionCell(selectedChapters)
            }
        }
        hbox {
            button("Import") {
                addClass("btn", "btn--primary")
                tooltip {
                    textProperty().bind(this@button.textProperty())
                }
                setOnAction {
                    onImportAction(selectedChapters.toList())
                    selectedChapters.clear()
                }
            }
            button("Cancel") {
                addClass("btn", "btn--secondary")
                tooltip {
                    textProperty().bind(this@button.textProperty())
                }
                setOnAction {
                    selectedChapters.clear()
                    onCancelAction()
                }
            }
        }
    }

    fun setOnImportAction(action: (List<Int>) -> Unit) {
        onImportAction = action
    }

    fun setOnCancelAction(action: () -> Unit) {
        onCancelAction = action
    }
}

class ImportFilterSelectionCell(
    private val selectedChapters: ObservableSet<Int>
) : ListCell<Int>() {

    val checkBox = CheckBox().apply {
        addClass("import-project-filter__list__check-box")

        selectedProperty().onChange {
            if (it) selectedChapters.add(item)
            else selectedChapters.remove(item)
        }
    }

    init {
        setOnMouseClicked {
            if (graphic != null) {
                checkBox.isSelected = !checkBox.isSelected
            }
        }
    }

    override fun updateItem(item: Int?, empty: Boolean) {
        super.updateItem(item, empty)

        if (item == null || empty) {
            graphic = null
            return
        }

        graphic = checkBox.apply {
            text = MessageFormat.format(
                messages["chapterTitle"],
                messages["chapter"],
                item
            )
            isSelected = item in selectedChapters
        }
    }
}

enum class ListSelectionMode(val titleKey: String) {
    ALL("all"),
    NONE("none"),
    CUSTOM("custom")
}