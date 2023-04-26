package org.wycliffeassociates.otter.jvm.controls.demo.ui.components

import javafx.geometry.Pos
import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuItem
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import org.controlsfx.control.textfield.CustomTextField
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import tornadofx.*
import tornadofx.FX.Companion.messages

class HeaderWithSearchBar : HBox() {
    private val popupMenu = createPopupMenu()

    init {
        spacing = 5.0
        alignment = Pos.CENTER_LEFT
        /* Page title*/
        button {
            addClass("btn", "btn--icon", "btn--borderless")
            graphic = FontIcon(MaterialDesign.MDI_DOTS_HORIZONTAL).apply {
                addClass("table-view__action-icon")
            }
            action {
                val bound = this.boundsInLocal
                val screenBound = this.localToScreen(bound)
                popupMenu.show(
                    FX.primaryStage
                )
                popupMenu.x = screenBound.centerX - this.width
                popupMenu.y = screenBound.maxY
            }
        }
        label("Your English Translations") {
            addClass("home-page__main-header")
        }
        region { hgrow = Priority.ALWAYS }
        hbox {
            /* Search bar */
            alignment = Pos.CENTER_LEFT
            add(
                CustomTextField().apply {
                    addClass("txt-input", "filtered-search-bar__input", "home-page__search-bar")
                    promptText = "Search..."
                    right = FontIcon(MaterialDesign.MDI_MAGNIFY)
                }
            )
        }
    }

    private fun createPopupMenu(): ContextMenu {
        val editContributorOption = MenuItem(messages["modifyContributor"]).apply {
            graphic = FontIcon(MaterialDesign.MDI_ACCOUNT_MULTIPLE)
        }
        val deleteOption = MenuItem(messages["deleteProject"]).apply {
            addClass("danger")
            graphic = FontIcon(MaterialDesign.MDI_DELETE)
        }

        return ContextMenu(editContributorOption, deleteOption).apply {
            addClass("wa-context-menu")
            isAutoHide = true
        }
    }
}