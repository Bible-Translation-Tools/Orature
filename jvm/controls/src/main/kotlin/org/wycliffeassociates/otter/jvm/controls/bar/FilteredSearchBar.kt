package org.wycliffeassociates.otter.jvm.controls.bar

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ObservableList
import javafx.scene.Node
import javafx.scene.control.Control
import javafx.scene.control.MenuItem
import javafx.scene.control.Skin
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.jvm.controls.skins.bar.FilteredSearchBarSkin
import tornadofx.*

class FilteredSearchBar : Control() {

    val textProperty = SimpleStringProperty()
    val leftIconProperty = SimpleObjectProperty<Node>()
    val rightIconProperty = SimpleObjectProperty<Node>(FontIcon(MaterialDesign.MDI_MAGNIFY))
    val promptTextProperty = SimpleStringProperty()
    val filterItems: ObservableList<MenuItem> = observableListOf()

    init {
        styleClass.setAll("filtered-search-bar")
    }

    override fun createDefaultSkin(): Skin<*> {
        return FilteredSearchBarSkin(this)
    }
}
