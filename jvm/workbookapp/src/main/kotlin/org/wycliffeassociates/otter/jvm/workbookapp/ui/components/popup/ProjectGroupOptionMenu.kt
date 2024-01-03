package org.wycliffeassociates.otter.jvm.workbookapp.ui.components.popup

import javafx.scene.control.ContextMenu
import javafx.scene.control.Label
import javafx.scene.control.MenuItem
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.common.data.workbook.WorkbookDescriptor
import org.wycliffeassociates.otter.jvm.controls.event.ProjectContributorsEvent
import org.wycliffeassociates.otter.jvm.controls.event.ProjectGroupDeleteEvent
import tornadofx.*
import tornadofx.FX.Companion.messages

class ProjectGroupOptionMenu : ContextMenu() {
    val books = observableListOf<WorkbookDescriptor>()

    init {
        val editContributorOption =
            MenuItem().apply {
                graphic =
                    Label(messages["modifyContributors"]).apply {
                        this.graphic = FontIcon(MaterialDesign.MDI_ACCOUNT_MULTIPLE)
                        tooltip(this.text)
                    }
                action {
                    FX.eventbus.fire(ProjectContributorsEvent(books))
                }
            }
        val deleteOption =
            MenuItem().apply {
                addClass("danger")
                graphic =
                    Label(messages["deleteProject"]).apply {
                        this.graphic = FontIcon(MaterialDesign.MDI_DELETE)
                        tooltip(this.text)
                    }
                action {
                    FX.eventbus.fire(ProjectGroupDeleteEvent(books))
                }
                disableWhen {
                    books.booleanBinding { list -> list.any { it.progress > 0 } }
                }
            }
        addClass("wa-context-menu")
        isAutoHide = true
        items.setAll(editContributorOption, deleteOption)
    }
}
