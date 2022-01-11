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
package org.wycliffeassociates.otter.jvm.controls.skins.media

import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.control.SkinBase
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import org.kordamp.ikonli.javafx.Icon
import org.wycliffeassociates.otter.jvm.controls.toggle.SourceFormatToggle
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import tornadofx.*

class SourceFormatToggleSkin(private val toggle: SourceFormatToggle) : SkinBase<SourceFormatToggle>(toggle) {

    private val TOGGLE_SOURCE_ACTIVE = "source-format-toggle__mode--active"
    private val TOGGLE_SOURCE_ICON_ACTIVE = "source-format-toggle__icon--active"

    @FXML
    lateinit var root: HBox

    @FXML
    lateinit var textBox: VBox

    @FXML
    lateinit var textIcon: Icon

    @FXML
    lateinit var audioBox: VBox

    @FXML
    lateinit var audioIcon: Icon

    init {
        loadFXML()
        initializeControl()
    }

    private fun initializeControl() {
        root.apply {
            setOnMouseClicked {
                toggle.activeSourceProperty.set(
                    when (toggle.activeSourceProperty.value) {
                        SourceFormatToggle.SourceFormat.AUDIO -> SourceFormatToggle.SourceFormat.TEXT
                        SourceFormatToggle.SourceFormat.TEXT -> SourceFormatToggle.SourceFormat.AUDIO
                        else -> SourceFormatToggle.SourceFormat.AUDIO
                    }
                )
            }
        }

        toggle.activeSourceProperty.onChangeAndDoNow {
            it?.let { activeSourceFormat ->
                when (activeSourceFormat) {
                    SourceFormatToggle.SourceFormat.AUDIO -> activatePlayer()
                    SourceFormatToggle.SourceFormat.TEXT -> activateText()
                }
            }
        }
    }

    private fun activatePlayer() {
        textBox.removeClass(TOGGLE_SOURCE_ACTIVE)
        textIcon.removeClass(TOGGLE_SOURCE_ICON_ACTIVE)
        audioBox.addClass(TOGGLE_SOURCE_ACTIVE)
        audioIcon.addClass(TOGGLE_SOURCE_ICON_ACTIVE)
    }

    private fun activateText() {
        textBox.addClass(TOGGLE_SOURCE_ACTIVE)
        textIcon.addClass(TOGGLE_SOURCE_ICON_ACTIVE)
        audioBox.removeClass(TOGGLE_SOURCE_ACTIVE)
        audioIcon.removeClass(TOGGLE_SOURCE_ICON_ACTIVE)
    }

    private fun loadFXML() {
        val loader = FXMLLoader(javaClass.getResource("SourceFormatToggle.fxml"))
        loader.setController(this)
        val root: Node = loader.load()
        children.add(root)
    }
}
