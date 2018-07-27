package app.widgets.usersList

import app.ui.imageLoader
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.geometry.Pos
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import app.ui.styles.ButtonStyles.Companion.rectangleButtonDefault
import app.ui.styles.ButtonStyles.Companion.roundButtonMedium
import app.ui.styles.LayoutStyles.Companion.usersListCell
import app.ui.styles.LayoutStyles.Companion.usersListGrid
import tornadofx.*
import java.io.File

class UsersList(listOfImageFiles: List<File>) : HBox() {
    val root  = datagrid(listOfImageFiles) {
        style{
            addClass(usersListGrid)
            hgrow = Priority.ALWAYS
            vgrow = Priority.ALWAYS
        }
        cellFormat {
            graphic = vbox(16) {
                addClass(usersListCell)
                //Small user icon in each cell
                //"it" is equal the value of each iteration of datagrid parameter
                button(graphic = imageLoader(it)) {
                    addClass(roundButtonMedium)
                    graphic.scaleX = 1.5
                    graphic.scaleY = 1.5
                }
                //Home button in in each cell
                val homeIcon = MaterialIconView(MaterialIcon.HOME, "25px")
                button(graphic = homeIcon) {
                    addClass(rectangleButtonDefault)
                    style {
                        alignment = Pos.CENTER
                        homeIcon.fill = c("#CC4141")
                    }
                }
            }
        }
    }
}
