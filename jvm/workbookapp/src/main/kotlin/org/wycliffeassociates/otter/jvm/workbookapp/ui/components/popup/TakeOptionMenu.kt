package org.wycliffeassociates.otter.jvm.workbookapp.ui.components.popup

import javafx.scene.control.ContextMenu
import javafx.scene.control.Label
import javafx.scene.control.MenuItem
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.jvm.controls.event.ChunkTakeEvent
import org.wycliffeassociates.otter.jvm.controls.event.TakeAction
import tornadofx.*
import tornadofx.FX.Companion.messages

class TakeOptionMenu(take: Take) : ContextMenu() {
    init {
        addClass("wa-context-menu")
        isAutoHide = true

        val editOption = MenuItem().apply {
            graphic = Label(messages["open_in_something"]).apply {
                this.graphic = FontIcon(MaterialDesign.MDI_OPEN_IN_NEW)
            }
            action {
                FX.eventbus.fire(ChunkTakeEvent(take, TakeAction.EDIT))
            }
        }
        val deleteOption = MenuItem().apply {
            addClass("danger")
            graphic = Label(messages["delete"]).apply {
                this.graphic = FontIcon(MaterialDesign.MDI_DELETE)
            }
            action {
                FX.eventbus.fire(ChunkTakeEvent(take, TakeAction.DELETE))
            }
        }

        items.setAll(editOption, deleteOption)
    }
}