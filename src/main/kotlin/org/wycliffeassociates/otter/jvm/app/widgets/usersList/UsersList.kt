package org.wycliffeassociates.otter.jvm.app.widgets.usersList

import org.wycliffeassociates.otter.jvm.app.ui.imageLoader
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.geometry.Pos
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import org.wycliffeassociates.otter.jvm.app.widgets.WidgetsStyles
import tornadofx.*
import java.io.File

class UsersList(listOfImageFiles: List<File>) : HBox() {

    val root  = datagrid(listOfImageFiles) {
        addStylesheet(WidgetsStyles::class)

        addClass(WidgetsStyles.UsersListGrid).style {
            hgrow = Priority.ALWAYS                        // this styling cannot be added in the styles sheet
            vgrow = Priority.ALWAYS                        // because it is a node property
            padding = box((width / 40).px)
        }

        cellFormat {
            graphic = vbox(16) {
                addClass(WidgetsStyles.usersListCell)
                //Small user icon in each cell
                //"it" is equal the value of each iteration of datagrid parameter
                button(graphic = imageLoader(it)) {
                    addClass(WidgetsStyles.roundButtonMedium)
                    graphic.scaleX = 1.5
                    graphic.scaleY = 1.5
                }
                //Home button in in each cell
                val homeIcon = MaterialIconView(MaterialIcon.HOME, "25px")
                button(graphic = homeIcon) {
                    style {
                        alignment = Pos.CENTER
                        addClass(WidgetsStyles.rectangleButtonDefault)
                        homeIcon.fill = c("#CC4141")

                    }
                }
            }
        }
    }
}
