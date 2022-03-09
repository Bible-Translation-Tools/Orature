package org.wycliffeassociates.otter.jvm.workbookapp.ui.components

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import javafx.scene.control.ListCell
import javafx.scene.control.TextField
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.material.Material
import org.wycliffeassociates.otter.common.data.primitives.Contributor
import tornadofx.*

class ContributorListCell : ListCell<Contributor>() {
    private val cellGraphic = ContributorCell()

    override fun updateItem(item: Contributor?, empty: Boolean) {
        super.updateItem(item, empty)

        if (empty || item == null) {
            graphic = null
            return
        }

        graphic = cellGraphic.apply {
            nameProperty.set(item.name)
        }
    }
}

class ContributorCell : HBox() {
    val nameProperty = SimpleStringProperty()
    private val newNameProperty = SimpleStringProperty()
    private val isEditingProperty = SimpleBooleanProperty()
    private lateinit var nameTextField: TextField

    init {
        hbox{
            hgrow = Priority.ALWAYS
            spacing = 10.0
            hbox {
                useMaxWidth = true
                hgrow = Priority.ALWAYS
                addClass("contributor__list-cell")

                setOnMouseClicked {
                    newNameProperty.set(nameProperty.value)
                    isEditingProperty.set(true)
                    nameTextField.requestFocus()
                }

                label(nameProperty) {
                    addClass("contributor__list-cell__title")

                    visibleProperty().bind(isEditingProperty.not())
                    managedProperty().bind(isEditingProperty.not())

                }
                hbox {
                    addClass("contributor__list-cell__editor")
                    visibleProperty().bind(isEditingProperty)
                    managedProperty().bind(isEditingProperty)

                    textfield(newNameProperty) {
                        nameTextField = this
                        addClass("txt-input", "contributor__text-input")
                    }
                    button {
                        addClass("btn", "btn--icon", "contributor__list-cell__save-btn")
                        graphic = FontIcon(Material.CHECK)
                        setOnAction {
                            nameProperty.set(newNameProperty.value)
                            isEditingProperty.set(false)
                        }
                    }
                    button {
                        addClass("btn", "btn--icon", "contributor__list-cell__discard-btn")
                        graphic = FontIcon(Material.CANCEL)
                        setOnAction {
                            isEditingProperty.set(false)
                        }
                    }
                }
            }
            hbox {
                alignment = Pos.CENTER
                hgrow = Priority.NEVER
                button {
                    addClass("btn", "btn--icon", "contributor__list-cell__delete-btn")
                    graphic = FontIcon(Material.DELETE)
                    setOnAction {

                    }
                }
            }
        }
    }
}