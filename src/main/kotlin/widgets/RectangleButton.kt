package widgets

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.geometry.Pos
import javafx.scene.Cursor

import javafx.scene.effect.DropShadow
import javafx.scene.layout.HBox
import javafx.scene.paint.Color
import tornadofx.*


class RectangleButton(color: String, width: Double, myFill : String, icon: MaterialIcon, var iconSize: String = "25px"): HBox() {


    val mIcon = MaterialIconView(icon, iconSize)

    val root = button("",mIcon) {
        style {
            backgroundColor+= c(color)
            setMinWidth(width)
            alignment = Pos.CENTER
            mIcon.fill = c(myFill)
            effect = DropShadow(10.0, Color.GRAY)
            cursor = Cursor.HAND



        }
    }

    init {
        alignment = Pos.CENTER
    }


}