/**
 * Copyright (C) 2020, 2021 Wycliffe Associates
 *
 * This file is part of Orature.
 *
 * Orature is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Orature is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Orature.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.wycliffeassociates.otter.jvm.workbookapp.ui.styles

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.geometry.Pos
import javafx.scene.paint.Color
import javafx.scene.text.FontWeight
import org.wycliffeassociates.otter.jvm.workbookapp.theme.AppTheme
import tornadofx.*

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
