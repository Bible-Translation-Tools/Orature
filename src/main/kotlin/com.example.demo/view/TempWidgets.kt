package com.example.demo.view


import javafx.scene.paint.Color
import tornadofx.*
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.scene.Cursor
import javafx.scene.effect.DropShadow
import javafx.scene.layout.VBox


class PlusWidget(myFill :String) : VBox() {

    val addUserIcon = MaterialIconView(MaterialIcon.GROUP_ADD, "40px")

     val root = button ("", addUserIcon){
         action { find(DatagridDemo::class).replaceWith(UserCreation::class)  }

        style {
            borderRadius+=box(50.0.px)
            backgroundColor+=c("#FFFFFF")
            backgroundRadius+=box(50.0.px)
            setMinWidth(75.0)
            setMinHeight(75.0)
            accentColor=Color.WHITE
            baseColor=Color.WHITE
            textFill=c("#CC4141")
            addUserIcon.fill= c(myFill)
            effect = DropShadow(10.0, Color.GRAY)
            cursor = Cursor.HAND

        }


         action {
             val userScreen = UserCreation()
             find(DatagridDemo::class).replaceWith(userScreen)
         }

    }



}