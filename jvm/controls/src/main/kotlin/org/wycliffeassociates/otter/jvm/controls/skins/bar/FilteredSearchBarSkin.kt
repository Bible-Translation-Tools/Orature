package org.wycliffeassociates.otter.jvm.controls.skins.bar

import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.control.MenuButton
import javafx.scene.control.SkinBase
import org.controlsfx.control.textfield.CustomTextField
import org.wycliffeassociates.otter.jvm.controls.bar.FilteredSearchBar
import tornadofx.*

class FilteredSearchBarSkin(private val bar: FilteredSearchBar) : SkinBase<FilteredSearchBar>(bar) {

    @FXML
    lateinit var searchField: CustomTextField

    @FXML
    lateinit var filterMenu: MenuButton

    init {
        importStylesheet(javaClass.getResource("/css/filtered-search-bar.css").toExternalForm())

        loadFXML()
        initializeControl()
    }

    private fun initializeControl() {
        searchField.leftProperty().bind(bar.leftIconProperty)
        searchField.rightProperty().bind(bar.rightIconProperty)
        searchField.promptTextProperty().bind(bar.promptTextProperty)

        filterMenu.items.bind(bar.filterItems) { it }

        bar.textProperty.bindBidirectional(searchField.textProperty())
    }

    private fun loadFXML() {
        val loader = FXMLLoader(javaClass.getResource("FilteredSearchBar.fxml"))
        loader.setController(this)
        val root: Node = loader.load()
        children.add(root)
    }
}
