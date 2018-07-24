package app.widgets.usersList

import app.ui.imageLoader
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.geometry.Pos
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import app.ui.styles.ButtonStyles
import tornadofx.*
import app.widgets.profileIcon.ProfileIcon
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
    val gridWidth = 400.0

    val root  = datagrid(data) {

        verticalCellSpacing = 25.0


        style{
            backgroundColor += Color.valueOf("#DFDEE3")
            hgrow = Priority.ALWAYS
            vgrow = Priority.ALWAYS
            padding = box((width/40).px)
            prefHeight = 800.0.px
        }

        //formats each cell; if not called, cells are just empty white squares
        //the "it" inside is an item from data.numbers
        verticalCellSpacing = 24.0
        maxCellsInRow = 3
        horizontalCellSpacing = 32.0


        cellFormat {

            //each cell is a borderpane
            graphic = vbox(16) {

                style{
                    backgroundColor += Color.valueOf("#DFDEE3")
                }

                //make a small icon
//                val randomNumber = Math.floor(Math.random() * 9_000_000_0000L).toLong() + 1_000_000_0000L     // use for demo, replace with DB hash
//                val currentSmallUserIcon = ProfileIcon(randomNumber.toString(), 100.0)


                //set its alignment to center so it shows up in the middle of the cell
                //(otherwise shows up in left)
//                currentSmallUserIcon.alignment = Pos.CENTER;
                button {
                    graphic = imageLoader(data.get())
                }
//                add(currentSmallUserIcon)


                val homeIcon = MaterialIconView(MaterialIcon.HOME, "25px")
                hbox {
                    alignment = Pos.CENTER
                    button("", homeIcon) {
                        alignment = Pos.CENTER
                        importStylesheet(ButtonStyles::class)
                        addClass(ButtonStyles.rectangleButtonDefault)
                        style {
                            homeIcon.fill = c("#CC4141")
                        }
                    }
                }
            }

        }
    }

}
