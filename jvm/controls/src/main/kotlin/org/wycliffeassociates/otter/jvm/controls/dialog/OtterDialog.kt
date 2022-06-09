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
package org.wycliffeassociates.otter.jvm.controls.dialog

import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Bounds
import javafx.geometry.NodeOrientation
import javafx.scene.control.ButtonBase
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.StageStyle
import org.wycliffeassociates.otter.common.data.ColorTheme
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import tornadofx.*

abstract class OtterDialog : Fragment() {
    val orientationProperty = SimpleObjectProperty<NodeOrientation>()
    val themeProperty = SimpleObjectProperty<ColorTheme>()

    private val roundRadius = 15.0

    private val mainContainer = VBox().apply {
        addClass("otter-dialog-container")
    }

    override val root = VBox().apply {
        addClass("otter-dialog-overlay")
        nodeOrientationProperty().bind(orientationProperty)

        add(mainContainer)
    }

    init {
        tryImportStylesheet(resources.get("/css/otter-dialog.css"))
        /*
         * The dialog does not inherit style class from root view;
         * it needs its own theme configuration
         */
        tryImportStylesheet(resources["/css/theme/light-theme.css"])
        tryImportStylesheet(resources["/css/theme/dark-theme.css"])
        bindTheme()
    }

    fun open() {
        val stage = openModal(StageStyle.TRANSPARENT, Modality.APPLICATION_MODAL, false)
        stage?.let { _stage ->
            fitStageToParent(_stage)
            _stage.scene.addEventHandler(KeyEvent.KEY_PRESSED) {
                if (it.code == KeyCode.ENTER) {
                    if (_stage.scene?.focusOwner is ButtonBase) {
                        (_stage.scene?.focusOwner as? ButtonBase)?.fire()
                    }
                }
            }
        }
    }

    fun setContent(content: Region) {
        mainContainer.add(
            content.apply {
                addClass("otter-dialog-content")

                vgrow = Priority.NEVER
                maxWidth = Region.USE_PREF_SIZE

                layoutBoundsProperty().onChange {
                    it?.let {
                        clipRegion(content, it)
                    }
                }
            }
        )
    }

    private fun bindTheme() {
        themeProperty.onChange {
            when (it) {
                ColorTheme.LIGHT -> {
                    root.addClass("light-theme")
                    root.removeClass("dark-theme")
                }
                ColorTheme.DARK -> {
                    root.addClass("dark-theme")
                    root.removeClass("light-theme")
                }
            }
        }
    }

    private fun fitStageToParent(stage: Stage) {
        stage.width = primaryStage.width
        stage.height = primaryStage.height
        stage.x = primaryStage.x
        stage.y = primaryStage.y
        stage.scene.fill = Color.TRANSPARENT
    }

    private fun clipRegion(region: Region, bounds: Bounds) {
        val rect = Rectangle()
        rect.width = bounds.width
        rect.height = bounds.height
        rect.arcWidth = roundRadius
        rect.arcHeight = roundRadius
        region.clip = rect
    }
}
