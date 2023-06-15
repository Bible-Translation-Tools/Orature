package org.wycliffeassociates.otter.jvm.workbookapp.ui.components.popup

import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuItem
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.common.data.workbook.WorkbookDescriptor
import tornadofx.FX.Companion.messages
import tornadofx.*

class ProjectGroupOptionMenu : ContextMenu() {
    val books = observableListOf<WorkbookDescriptor>()
    init {
        val editContributorOption = MenuItem(messages["modifyContributors"]).apply {
            graphic = FontIcon(MaterialDesign.MDI_ACCOUNT_MULTIPLE)
            action {
                // TODO
            }
        }
        val deleteOption = MenuItem(messages["deleteProject"]).apply {
            addClass("danger")
            graphic = FontIcon(MaterialDesign.MDI_DELETE)
            action {
                // TODO
            }
        }
        addClass("wa-context-menu")
        isAutoHide = true
        items.setAll(editContributorOption, deleteOption)
    }
}