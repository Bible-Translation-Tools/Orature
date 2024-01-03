/**
 * Copyright (C) 2020-2022 Wycliffe Associates
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
package org.wycliffeassociates.otter.jvm.controls.demo.ui.viewmodels

import com.jthemedetecor.OsThemeDetector
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.event.ActionEvent
import javafx.event.EventHandler
import org.wycliffeassociates.otter.common.data.ColorTheme
import org.wycliffeassociates.otter.jvm.controls.demo.ui.models.ChunkData
import tornadofx.*

internal class DemoViewModel : ViewModel() {
    val supportedThemes = observableListOf<ColorTheme>()
    val selectedThemeProperty = SimpleObjectProperty<ColorTheme>()

    val currentVerseLabelProperty = SimpleStringProperty()
    val floatingCardVisibleProperty = SimpleBooleanProperty()
    val onCurrentVerseActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()

    val currentChapterProperty = SimpleIntegerProperty(2)

    val appColorMode = SimpleObjectProperty<ColorTheme>()
    private val osThemeDetector = OsThemeDetector.getDetector()
    private val isOSDarkMode = SimpleBooleanProperty(osThemeDetector.isDark)

    val shownFragment = SimpleObjectProperty<UIComponent>()

    init {
        osThemeDetector.registerListener {
            runLater { isOSDarkMode.set(it) }
        }
    }

    fun bind() {
        supportedThemes.setAll(ColorTheme.values().asList())
        selectedThemeProperty.set(ColorTheme.DARK)
    }

    fun updateTheme(selectedTheme: ColorTheme) {
        if (selectedTheme == ColorTheme.SYSTEM) {
            bindSystemTheme()
        } else {
            appColorMode.unbind()
            appColorMode.set(selectedTheme)
        }
    }

    fun onChunkOpenIn(chunk: ChunkData) {
        println("Opening verse ${chunk.title} in external app...")
    }

    fun onRecordChunkAgain(chunk: ChunkData) {
        println("Recording verse ${chunk.title} again")
    }

    fun onRecord(chunk: ChunkData) {
        println("Recording verse ${chunk.title}")
    }

    inline fun <reified T : UIComponent> showContent() {
        val fragment = find<T>()
        shownFragment.set(fragment)
    }

    private fun bindSystemTheme() {
        appColorMode.bind(
            isOSDarkMode.objectBinding {
                if (it == true) {
                    ColorTheme.DARK
                } else {
                    ColorTheme.LIGHT
                }
            },
        )
    }
}
