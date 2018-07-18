package widgets.UsersList

import com.example.demo.controller.DataService
import com.example.demo.styles.Styles
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.geometry.Pos
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import javafx.stage.Screen
import tornadofx.*
import widgets.profileIcon.view.ProfileIcon

class UsersList : HBox() {

    val data =listOf("one", "two", "three", "four", "five", "six", "seven", "eight", "nine")
    val gridWidth = 400.0
    val root  = datagrid(data) {
        verticalCellSpacing = 25.0



        style{
            backgroundColor += Color.valueOf("#DFDEE3")
            setMinWidth(Screen.getPrimary().bounds.width/2)
            hgrow = Priority.ALWAYS
            padding = box(56.0.px)
        }

        //formats each cell; if not called, cells are just empty white squares
        //the "it" inside is an item from data.numbers
        verticalCellSpacing = 24.0
        maxCellsInRow = 3


        cellFormat {

            //each cell is a borderpane
            graphic = vbox(16) {

                style{
                    backgroundColor += Color.valueOf("#DFDEE3")
                }

                //make a small icon
                val randomNumber = Math.floor(Math.random() * 9_000_000_0000L).toLong() + 1_000_000_0000L     // use for demo, replace with DB hash
                val currentSmallUserIcon = ProfileIcon(randomNumber.toString(), 100.0)

                //set its alignment to center so it shows up in the middle of the cell
                //(otherwise shows up in left)
                currentSmallUserIcon.alignment = Pos.CENTER;
                //set it to an area of the borderpane

                add(currentSmallUserIcon)

                //puts a user's number instead of their icon; in the real thing use icon


                //val currentBottomWidget = RectangleButton("#FFFF", 100.0, "#CC4141", MaterialIcon.HOME, operation = :: println);

                //currentBottomWidget.alignment = Pos.CENTER;
                //add(currentBottomWidget);
                val homeIcon = MaterialIconView(MaterialIcon.HOME, "25px")
                hbox {
                    alignment = Pos.CENTER
                    button("", homeIcon) {
                        alignment = Pos.CENTER
                        addClass(Styles.rectangleButtonDefault)
                        style {
                            homeIcon.fill = c("#CC4141")

                        }
                    }
                }
            }

        }
    }

}
