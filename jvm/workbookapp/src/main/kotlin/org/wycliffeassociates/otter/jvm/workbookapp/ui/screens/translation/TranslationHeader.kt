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
                addClass("btn", "btn--secondary")
                tooltip = tooltip(messages["undoAction"])
                graphic = FontIcon(MaterialDesign.MDI_UNDO)
                enableWhen(canUndoProperty)

                setOnAction {
                    FX.eventbus.fire(UndoChunkingPageEvent())
                }
            }
            button {
                addClass("btn", "btn--secondary")
                tooltip = tooltip(messages["redoAction"])
                graphic = FontIcon(MaterialDesign.MDI_REDO)
                enableWhen(canRedoProperty)

                setOnAction {
                    FX.eventbus.fire(RedoChunkingPageEvent())
                }
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
}

fun EventTarget.translationHeader(op: TranslationHeader.() -> Unit) = TranslationHeader().attachTo(this, op)
