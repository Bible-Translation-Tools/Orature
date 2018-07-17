package com.example.demo.view

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.paint.Color
import tornadofx.*
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox

//a temporary substitute for the users' icons, just a circle
//class UserIconWidget(rad: Double): Fragment() {
//    override val root = hbox {
//        circle {
//            radius = rad;
//            fill = Color.TRANSPARENT;
//        }
//    }
//}
//

class PlusWidget(myFill :String) : VBox() {

    val addUserIcon = MaterialIconView(MaterialIcon.GROUP_ADD, "40px")

     val root = button ("", addUserIcon){
        style {
            borderRadius+=box(50.0.px)
            backgroundColor+=c("#FFFF")
            backgroundRadius+=box(50.0.px)
            setMinWidth(75.0)
            setMinHeight(75.0)
            accentColor=Color.WHITE
            baseColor=Color.WHITE
            textFill=c("#CC4141")
            addUserIcon.fill= c(myFill)

        }

         action {
             val userScreen = RecordUser()
             find(DatagridDemo::class).replaceWith(userScreen)
         }
    }
}