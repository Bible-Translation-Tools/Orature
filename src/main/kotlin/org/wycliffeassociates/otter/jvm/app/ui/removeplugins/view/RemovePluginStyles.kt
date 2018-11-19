package org.wycliffeassociates.otter.jvm.app.ui.removeplugins.view

import javafx.geometry.Pos
import javafx.scene.paint.Color
import javafx.scene.text.FontWeight
import org.wycliffeassociates.otter.jvm.app.UIColorsObject.Colors
import tornadofx.*

class RemovePluginStyles : Stylesheet() {
    companion object {
        val noPluginLabel by cssclass()
        val deleteButton by cssclass()
        val pluginList by cssclass()
        val pluginListCell by cssclass()
    }

    init {
        noPluginLabel {
            fontSize = 16.px
            fontWeight = FontWeight.BOLD
        }

        pluginList {
            focusColor = Color.TRANSPARENT
            faintFocusColor = Color.TRANSPARENT
            listCell {
                backgroundColor += c(Colors["base"])
            }
        }

        pluginListCell {
            backgroundColor += c(Colors["base"])
            alignment = Pos.CENTER_LEFT
            padding = box(5.px)
            label {
                fontWeight = FontWeight.BOLD
            }
            button {
                child("*") {
                    fill = Color.RED
                }
            }
        }


    }
}