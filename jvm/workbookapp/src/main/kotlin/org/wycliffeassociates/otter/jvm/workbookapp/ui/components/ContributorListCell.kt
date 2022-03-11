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

    init {
        useMaxWidth = true
        hgrow = Priority.ALWAYS
        addClass("contributor__list-cell")

        textfield(nameProperty) {
            hgrow = Priority.ALWAYS
            addClass("txt-input", "contributor__text-input")
        }
        button {
            addClass("btn", "btn--icon", "contributor__list-cell__delete-btn")
            graphic = FontIcon(Material.DELETE)
            setOnAction {

            }
        }
    }
}