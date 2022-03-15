package org.wycliffeassociates.otter.jvm.workbookapp.ui.components

import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.ListCell
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.material.Material
import org.wycliffeassociates.otter.common.data.primitives.Contributor
import tornadofx.*

class ContributorListCell : ListCell<Contributor>() {
    private val cellGraphic = ContributorCell()

    val onRemoveContributorActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()

    override fun updateItem(item: Contributor?, empty: Boolean) {
        super.updateItem(item, empty)

        if (empty || item == null) {
            graphic = null
            return
        }

        graphic = cellGraphic.apply {
            nameProperty.set(item.name)
            indexProperty.set(index)
            onRemoveContributorActionProperty.bind(this@ContributorListCell.onRemoveContributorActionProperty)
        }
    }
}

class ContributorCell : HBox() {
    val indexProperty = SimpleIntegerProperty(-1)
    val nameProperty = SimpleStringProperty()
    val onRemoveContributorActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>(null)

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
                onRemoveContributorActionProperty.value?.handle(
                    ActionEvent(indexProperty.value, this@ContributorCell)
                )
            }
        }
    }
}