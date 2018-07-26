package app.widgets.usersList

import app.ui.imageLoader
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.geometry.Pos
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import app.ui.styles.ButtonStyles.Companion.rectangleButtonDefault
import app.ui.styles.ButtonStyles.Companion.roundButtonMedium
import tornadofx.*
import java.io.File

class UsersList(listOfImageFiles: List<File>) : HBox() {

    val root  = datagrid(listOfImageFiles) {

        style{
            backgroundColor += Color.valueOf("#DFDEE3")
            hgrow = Priority.ALWAYS
            vgrow = Priority.ALWAYS
            prefHeight = 900.0.px
            cellHeight = 170.0.px
            verticalCellSpacing = 15.0.px
            horizontalCellSpacing = 20.0.px
        }

        cellFormat {

            graphic = vbox(16) {

                style{
                    backgroundColor += Color.valueOf("#DFDEE3")
                    alignment = Pos.CENTER
                }

                //Small user icon in each cell
                //"it" is equal the value of each iteration of datagrid parameter
                button(graphic = imageLoader(it)) {
                    addClass(roundButtonMedium)
                    graphic.scaleX = 1.5
                    graphic.scaleY = 1.5
                }

                //Home button in in each cell
                val homeIcon = MaterialIconView(MaterialIcon.HOME, "25px")
                button("", homeIcon) {
                    addClass(rectangleButtonDefault)
                    alignment = Pos.CENTER
                    homeIcon.fill = c("#CC4141")
                }

            }

        }
    }

}
