package com.example.demo.view

import javafx.scene.Parent
import tornadofx.*

class RecordUser: View() {

    companion object {
        val button = " String"
    }

    override val root = hbox {
        style{
            backgroundColor += c("#000000")
        }
    }
}