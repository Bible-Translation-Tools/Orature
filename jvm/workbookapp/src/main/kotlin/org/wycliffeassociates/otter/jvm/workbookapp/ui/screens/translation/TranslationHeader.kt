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
package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.translation

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.event.EventTarget
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.jvm.controls.chapterselector.chapterSelector
import org.wycliffeassociates.otter.jvm.controls.event.GoToNextChapterEvent
import org.wycliffeassociates.otter.jvm.controls.event.GoToPreviousChapterEvent
import org.wycliffeassociates.otter.jvm.controls.event.OpenInPluginEvent
import org.wycliffeassociates.otter.jvm.controls.event.RedoChunkingPageEvent
import org.wycliffeassociates.otter.jvm.controls.event.UndoChunkingPageEvent
import org.wycliffeassociates.otter.jvm.controls.model.ChapterGridItemData
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.popup.ChapterSelectorPopup
import tornadofx.*
import tornadofx.FX.Companion.messages

class TranslationHeader : HBox() {

    val titleProperty = SimpleStringProperty()
    val chapterTitleProperty = SimpleStringProperty()
    val canUndoProperty = SimpleBooleanProperty(false)
    val canRedoProperty = SimpleBooleanProperty(false)
    val canGoNextProperty = SimpleBooleanProperty(false)
    val canGoPreviousProperty = SimpleBooleanProperty(false)
    val canOpenInProperty = SimpleBooleanProperty(false)
    val chapterList = observableListOf<ChapterGridItemData>()
    private val popupMenu = ChapterSelectorPopup()

    init {
        addClass("top-navigation-pane")

        hbox {
            addClass("top-navigation-pane__title-section")
            hgrow = Priority.ALWAYS
            label(titleProperty) {
                addClass("h3")
            }
        }
        hbox {
            addClass("top-navigation-pane__control-section")

            button {
                addClass("btn", "btn--tertiary")
                tooltip = tooltip(messages["undoAction"])
                graphic = FontIcon(MaterialDesign.MDI_UNDO)
                enableWhen(canUndoProperty)

                setOnAction {
                    FX.eventbus.fire(UndoChunkingPageEvent())
                }
            }
            button {
                addClass("btn", "btn--tertiary")
                tooltip = tooltip(messages["redoAction"])
                graphic = FontIcon(MaterialDesign.MDI_REDO)
                enableWhen(canRedoProperty)

                setOnAction {
                    FX.eventbus.fire(RedoChunkingPageEvent())
                }
            }
            button {
                addClass("btn", "btn--tertiary")
                tooltip = tooltip(messages["openIn"])
                graphic = FontIcon(MaterialDesign.MDI_OPEN_IN_NEW)

                setOnAction {
                    FX.eventbus.fire(OpenInPluginEvent())
                }

                visibleProperty().bind(canOpenInProperty)
                managedProperty().bind(visibleProperty())
            }
            chapterSelector {
                chapterTitleProperty.bind(this@TranslationHeader.chapterTitleProperty)

                nextDisabledProperty.bind(canGoNextProperty.not())
                prevDisabledProperty.bind(canGoPreviousProperty.not())

                setOnNextChapter {
                    FX.eventbus.fire(GoToNextChapterEvent())
                }

                setOnPreviousChapter {
                    FX.eventbus.fire(GoToPreviousChapterEvent())
                }

                setOnChapterSelectorOpenedProperty {
                    popupMenu.updateChapterGrid(chapterList)

                    val bound = this.boundsInLocal
                    val screenBound = this.localToScreen(bound)

                    popupMenu.show(FX.primaryStage)

                    popupMenu.x = screenBound.minX - popupMenu.width + this.width
                    popupMenu.y = screenBound.maxY - 25
                }
            }
        }
    }

    fun dismissChapterSelector() {
        popupMenu.hide()
    }
}

fun EventTarget.translationHeader(op: TranslationHeader.() -> Unit) = TranslationHeader().attachTo(this, op)