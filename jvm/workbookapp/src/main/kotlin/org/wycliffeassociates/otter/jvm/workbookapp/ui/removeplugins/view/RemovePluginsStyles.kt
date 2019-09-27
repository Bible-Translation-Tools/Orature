package org.wycliffeassociates.otter.jvm.workbookapp.ui.removeplugins.view

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.geometry.Pos
import javafx.scene.paint.Color
import javafx.scene.text.FontWeight
import org.wycliffeassociates.otter.jvm.workbookapp.theme.AppTheme
import tornadofx.Stylesheet
import tornadofx.box
import tornadofx.cssclass
import tornadofx.px

class RemovePluginsStyles : Stylesheet() {
    companion object {
        val removePluginsRoot by cssclass()
        val noPluginLabel by cssclass()
        val deleteButton by cssclass()
        val pluginList by cssclass()
        val pluginListCell by cssclass()

        fun deleteIcon(size: String) = MaterialIconView(MaterialIcon.DELETE, size)
    }

    init {
        removePluginsRoot {
            prefWidth = 300.px
            prefHeight = 200.px
        }
        noPluginLabel {
            fontSize = 16.px
            fontWeight = FontWeight.BOLD
        }

        pluginList {
            focusColor = Color.TRANSPARENT
            faintFocusColor = Color.TRANSPARENT
            listCell {
                backgroundColor += AppTheme.colors.defaultBackground
            }
        }

        pluginListCell {
            backgroundColor += AppTheme.colors.defaultBackground
            alignment = Pos.CENTER_LEFT
            padding = box(5.px)
            spacing = 10.px
            label {
                fontWeight = FontWeight.BOLD
                textFill = AppTheme.colors.defaultText
            }
            button {
                child("*") {
                    fill = AppTheme.colors.appRed
                }
            }
        }
    }
}