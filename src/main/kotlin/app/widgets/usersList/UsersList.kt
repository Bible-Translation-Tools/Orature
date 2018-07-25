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

class UsersList : HBox() {

    val data =listOf(
            File("C:\\Users\\fucat\\Documents\\repositories\\8woc2018-jvm\\src\\main\\resources\\userIcons\\userIcon1.svg"),
            File("C:\\Users\\fucat\\Documents\\repositories\\8woc2018-jvm\\src\\main\\resources\\userIcons\\userIcon2.svg"),
            File("C:\\Users\\fucat\\Documents\\repositories\\8woc2018-jvm\\src\\main\\resources\\userIcons\\userIcon3.svg"),
            File("C:\\Users\\fucat\\Documents\\repositories\\8woc2018-jvm\\src\\main\\resources\\userIcons\\userIcon4.svg"),
            File("C:\\Users\\fucat\\Documents\\repositories\\8woc2018-jvm\\src\\main\\resources\\userIcons\\userIcon5.svg"),
            File("C:\\Users\\fucat\\Documents\\repositories\\8woc2018-jvm\\src\\main\\resources\\userIcons\\userIcon6.svg"),
            File("C:\\Users\\fucat\\Documents\\repositories\\8woc2018-jvm\\src\\main\\resources\\userIcons\\userIcon7.svg"),
            File("C:\\Users\\fucat\\Documents\\repositories\\8woc2018-jvm\\src\\main\\resources\\userIcons\\userIcon8.svg"),
            File("C:\\Users\\fucat\\Documents\\repositories\\8woc2018-jvm\\src\\main\\resources\\userIcons\\userIcon9.svg"),
            File("C:\\Users\\fucat\\Documents\\repositories\\8woc2018-jvm\\src\\main\\resources\\userIcons\\userIcon10.svg"),
            File("C:\\Users\\fucat\\Documents\\repositories\\8woc2018-jvm\\src\\main\\resources\\userIcons\\userIcon11.svg"),
            File("C:\\Users\\fucat\\Documents\\repositories\\8woc2018-jvm\\src\\main\\resources\\userIcons\\userIcon12.svg"),
            File("C:\\Users\\fucat\\Documents\\repositories\\8woc2018-jvm\\src\\main\\resources\\userIcons\\userIcon13.svg")
            )

    val root  = datagrid(data) {
        style{
            backgroundColor += Color.valueOf("#DFDEE3")
            hgrow = Priority.ALWAYS
            vgrow = Priority.ALWAYS
//            padding = box((width/40).px)
            prefHeight = 900.0.px
            cellHeight = 170.0.px
            verticalCellSpacing = 15.0.px
            horizontalCellSpacing = 30.0.px
        }
        //formats each cell; if not called, cells are just empty white squares
        //the "it" inside is an item from data.numbers
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
