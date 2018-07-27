package app.widgets.usersList

import app.MyApp.Companion.Colors
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.geometry.Pos
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import app.ui.styles.ButtonStyles
import tornadofx.*
import app.widgets.profileIcon.ProfileIcon

class UsersList : HBox() {

    val data =listOf("11234567890", "12234567890", "12334567890", "12344567890", "12345567890", "02345667896", "12345677890", "02345678990", "12345678990")
    val gridWidth = 400.0

    val root  = datagrid(data) {
        verticalCellSpacing = 25.0


        style{
            backgroundColor += c(Colors["mediumGray"])
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
                    backgroundColor +=c(Colors["mediumGray"])
                }

                //make a small icon
                val randomNumber = Math.floor(Math.random() * 9_000_000_0000L).toLong() + 1_000_000_0000L     // use for demo, replace with DB hash
                val currentSmallUserIcon = ProfileIcon(randomNumber.toString(), 100.0)


                //set its alignment to center so it shows up in the middle of the cell
                //(otherwise shows up in left)
                currentSmallUserIcon.alignment = Pos.CENTER;

                add(currentSmallUserIcon)


                val homeIcon = MaterialIconView(MaterialIcon.HOME, "25px")
                hbox {
                    alignment = Pos.CENTER
                    button("", homeIcon) {
                        alignment = Pos.CENTER
                        importStylesheet(ButtonStyles::class)
                        addClass(ButtonStyles.rectangleButtonDefault)
                        style {
                            homeIcon.fill = c(Colors["accent"])
                        }
                    }
                }
            }

        }
    }

}
