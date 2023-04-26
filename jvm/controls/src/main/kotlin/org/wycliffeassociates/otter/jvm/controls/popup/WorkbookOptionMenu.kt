package org.wycliffeassociates.otter.jvm.controls.popup

import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuItem
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.common.data.workbook.WorkbookInfo
import org.wycliffeassociates.otter.jvm.controls.event.WorkbookDeleteEvent
import org.wycliffeassociates.otter.jvm.controls.event.WorkbookExportEvent
import org.wycliffeassociates.otter.jvm.controls.event.WorkbookOpenEvent
import tornadofx.FX
import tornadofx.action
import tornadofx.addClass
import tornadofx.get

class WorkbookOptionMenu : ContextMenu() {

    val workbookInfoProperty = SimpleObjectProperty<WorkbookInfo>(null)

    init {
        val openOption = MenuItem(FX.messages["openBook"]).apply {
            graphic = FontIcon(MaterialDesign.MDI_ARROW_RIGHT)
            action {
                workbookInfoProperty.value?.let {
                    FX.eventbus.fire(WorkbookOpenEvent(it))
                }
            }
        }
        val exportOption = MenuItem(FX.messages["exportProject"]).apply {
            graphic = FontIcon(MaterialDesign.MDI_OPEN_IN_NEW)
            action {
                workbookInfoProperty.value?.let {
                    FX.eventbus.fire(WorkbookExportEvent(it))
                }
            }
        }
        val deleteOption = MenuItem(FX.messages["deleteBook"]).apply {
            addClass("danger")
            graphic = FontIcon(MaterialDesign.MDI_DELETE)
            action {
                workbookInfoProperty.value?.let {
                    FX.eventbus.fire(WorkbookDeleteEvent(it))
                }
            }
        }
        addClass("wa-context-menu")
        isAutoHide = true
        items.setAll(openOption, exportOption, deleteOption)
    }
}