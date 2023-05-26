package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.home

import javafx.collections.ObservableList
import javafx.geometry.Pos
import javafx.scene.layout.Priority
import javafx.scene.layout.StackPane
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.common.data.workbook.WorkbookDescriptor
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.tableview.workbookTableView
import tornadofx.addClass
import tornadofx.booleanBinding
import tornadofx.hgrow
import tornadofx.label
import tornadofx.managedWhen
import tornadofx.vbox
import tornadofx.visibleWhen

class BookTableSection(books: ObservableList<WorkbookDescriptor>) : StackPane() {
    init {
        workbookTableView(books) {
            hgrow = Priority.ALWAYS
            visibleWhen(books.booleanBinding { it.isNotEmpty() })
            managedWhen(visibleProperty())
        }
        // empty project view
        vbox {
            alignment = Pos.CENTER

            visibleWhen(books.booleanBinding { it.isEmpty() })
            managedWhen(visibleProperty())

            label {
                addClass("icon-xl")
                graphic = FontIcon(MaterialDesign.MDI_LIBRARY_BOOKS)
            }
            label("Create a Project to Get Started") {
                addClass("h4--80")
            }
        }
    }
}