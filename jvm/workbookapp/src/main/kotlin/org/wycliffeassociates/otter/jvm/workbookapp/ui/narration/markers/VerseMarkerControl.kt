/**
 * Copyright (C) 2020-2024 Wycliffe Associates
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
package org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.markers

import javafx.beans.binding.BooleanBinding
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import javafx.scene.Cursor
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.layout.BorderPane
import javafx.scene.layout.Region
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.common.data.audio.AudioMarker
import org.wycliffeassociates.otter.common.domain.narration.statemachine.NarrationStateType
import org.wycliffeassociates.otter.common.domain.narration.statemachine.VerseItemState
import org.wycliffeassociates.otter.common.domain.narration.statemachine.VerseState
import org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.menu.VerseMenu
import tornadofx.*
import tornadofx.FX.Companion.messages


const val MARKER_AREA_WIDTH = 24.0
internal const val MARKER_WIDTH = 2.0

class VerseMarkerControl : BorderPane() {

    val verseProperty = SimpleObjectProperty<AudioMarker>()
    val verseIndexProperty = SimpleIntegerProperty()
    val labelProperty = SimpleStringProperty()
    val canBeMovedProperty: BooleanBinding = verseIndexProperty.greaterThan(0)
    val userIsDraggingProperty = SimpleBooleanProperty(false)

    val isPlayingEnabledProperty = SimpleBooleanProperty()
    val isEditVerseEnabledProperty = SimpleBooleanProperty()
    val isRecordAgainEnabledProperty = SimpleBooleanProperty()

    val dragAreaProperty = SimpleObjectProperty<Node>()

    private val normalMarker = FontIcon(MaterialDesign.MDI_BOOKMARK)
    private val outlinedMarker = FontIcon(MaterialDesign.MDI_BOOKMARK_OUTLINE)
    private val markerIconProperty = SimpleObjectProperty(outlinedMarker)

    init {
        addClass("verse-marker")

        left = stackpane {
            addClass("verse-marker__drag-area")
            minWidth = MARKER_AREA_WIDTH
            maxWidth = MARKER_AREA_WIDTH

            dragAreaProperty.set(this)

            stackpane {
                line {
                    addClass("verse-marker__line")

                    startXProperty().bind(this@stackpane.layoutXProperty().plus(MARKER_AREA_WIDTH / 2))
                    startYProperty().bind(this@stackpane.layoutYProperty())
                    endXProperty().bind(this@stackpane.widthProperty().minus(MARKER_AREA_WIDTH / 2))
                    endYProperty().bind(this@VerseMarkerControl.prefHeightProperty())
                    strokeWidth = MARKER_WIDTH
                }
            }

            label {
                addClass("verse-marker__icon")
                graphicProperty().bind(markerIconProperty)
            }

            setOnMouseEntered {
                if (canBeMovedProperty.value) {
                    cursor = Cursor.H_RESIZE
                    markerIconProperty.set(normalMarker)

                    addPseudoClass("movable")
                }
                it.consume()
            }

            setOnMouseExited {
                cursor = Cursor.DEFAULT
                markerIconProperty.set(outlinedMarker)

                removePseudoClass("movable")
                it.consume()
            }
        }

        center = label(labelProperty) {
            minWidth = Region.USE_PREF_SIZE
            addClass("verse-marker__title")
            setAlignment(this, Pos.BOTTOM_LEFT)
        }

        right = Button().apply {
            addClass("btn", "btn--icon", "verse-marker__menu")
            setAlignment(this, Pos.BOTTOM_LEFT)
            graphic = FontIcon(MaterialDesign.MDI_DOTS_VERTICAL)
            tooltip(messages["options"])

            val menu = VerseMenu().apply {
                isPlayingEnabledProperty.bind(this@VerseMarkerControl.isPlayingEnabledProperty)
                isEditVerseEnabledProperty.bind(this@VerseMarkerControl.isEditVerseEnabledProperty)
                isRecordAgainEnabledProperty.bind(this@VerseMarkerControl.isRecordAgainEnabledProperty)
                verseProperty.bind(this@VerseMarkerControl.verseProperty)
                verseIndexProperty.bind(this@VerseMarkerControl.verseIndexProperty)
            }

            menu.setOnShowing { addPseudoClass("active") }
            menu.setOnHidden { removePseudoClass("active") }

            action {
                val screenBound = localToScreen(boundsInLocal)
                menu.show(FX.primaryStage)
                menu.x = screenBound.centerX - (menu.width / 2)
                menu.y = screenBound.centerY - menu.height
            }
        }
    }
}