package app.widgets.usersList

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.geometry.Pos
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import app.widgets.WidgetsStyles
import tornadofx.*
import app.widgets.profileIcon.ProfileIcon

class UsersList : HBox() {

    val data = listOf("11234567890", "12234567890", "12334567890", "12344567890", "12345567890", "02345667896", "12345677890", "02345678990", "12345678990")
    val gridWidth = 400.0

    val root = datagrid(data) {
        addStylesheet(WidgetsStyles::class)
        addClass(WidgetsStyles.UsersListGrid).style {
            hgrow = Priority.ALWAYS                        // this styling cannot be added in the styles sheet
            vgrow = Priority.ALWAYS                        // because it is a node property
            padding = box((width / 40).px)
        }

        cellFormat {
            //each cell is a borderpane
            graphic = vbox(16) {
                style {
                    backgroundColor += c("#DFDEE3")
                }
                //make a small icon
                val randomNumber = Math.floor(Math.random() * 9_000_000_0000L).toLong() + 1_000_000_0000L     // use for demo, replace with DB hash
                val currentSmallUserIcon = ProfileIcon(randomNumber.toString(), 100.0)
                //set its alignment to center so it shows up in the middle of the cell
                //(otherwise shows up in left)
                currentSmallUserIcon.alignment = Pos.CENTER
                add(currentSmallUserIcon)
                val homeIcon = MaterialIconView(MaterialIcon.HOME, "25px")
                hbox {
                    alignment = Pos.CENTER
                    button("", homeIcon) {
                        alignment = Pos.CENTER
                        importStylesheet(WidgetsStyles::class)
                        addClass(WidgetsStyles.rectangleButtonDefault)
                        style {
                            homeIcon.fill = c("#CC4141")
                        }
                    }
                }
            }

        }
    }

}
