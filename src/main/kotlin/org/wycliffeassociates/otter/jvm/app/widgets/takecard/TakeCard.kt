package org.wycliffeassociates.otter.jvm.app.widgets

import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.beans.property.SimpleBooleanProperty
import javafx.event.ActionEvent
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.Pane
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import org.wycliffeassociates.otter.common.data.model.Take
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import tornadofx.*

class TakeCard(val take: Take, player: IAudioPlayer) : AnchorPane() {
    val playedProperty = SimpleBooleanProperty(take.played)
    private val badge = stackpane {
        // custom css class
        style {
            backgroundRadius += box(0.px, 10.px, 0.px, 10.px)
            padding = box(8.px)
        }
        addClass("badge")
        val icon = MaterialDesignIconView(MaterialDesignIcon.CREATION, "18px")
        icon.style(true) {
            fill = Color.WHITE
        }
        add(icon)
        isVisible = !take.played
    }

    var deleteButton: Button by singleAssign()

    init {
        setRightAnchor(badge, 0.0)
        setTopAnchor(badge, 0.0)
        style {
            minWidth = 250.px
            maxWidth = minWidth
            minHeight = 100.px
            maxHeight = minHeight
            backgroundColor += Color.WHITE
            backgroundRadius += box(10.px)
        }
        val content = vbox {
            style {
                padding = box(10.px)
            }
            hbox(10) {
                vgrow = Priority.ALWAYS
                style {
                    alignment = Pos.CENTER_LEFT
                }
                label("%02d".format(take.number)) {
                    style {
                        fontSize = 20.px
                    }
                }
                label(take.timestamp.toString()) {
                    style {
                        fontSize = 12.px
                    }
                }

            }
            hbox {
                alignment = Pos.CENTER
                simpleaudioplayer(take.path, player) {
                    vgrow = Priority.ALWAYS
                    style {
                        alignment = Pos.CENTER_LEFT
                    }
                    playGraphic = MaterialIconView(MaterialIcon.PLAY_CIRCLE_OUTLINE, "30px")
                    pauseGraphic = MaterialIconView(MaterialIcon.PAUSE_CIRCLE_OUTLINE, "30px")
                    with(playPauseButton) {
                        style(true) {
                            backgroundColor += Color.TRANSPARENT
                        }
                        addEventHandler(ActionEvent.ACTION) {
                            if (!take.played) {
                                take.played = true
                                badge.isVisible = false
                            }
                            playedProperty.value = take.played
                        }
                    }
                }
                deleteButton = button {
                    graphic = MaterialIconView(MaterialIcon.DELETE, "25px")
                    style {
                        backgroundColor += Color.TRANSPARENT
                    }
                }
            }
        }
        setTopAnchor(content, 0.0)
        setRightAnchor(content, 0.0)
        setBottomAnchor(content, 0.0)
        setLeftAnchor(content, 0.0)

        // Make sure badge appears on top
        badge.toFront()
    }
}

fun Pane.takecard(take: Take, player: IAudioPlayer, init: TakeCard.() -> Unit = {}): TakeCard {
    val takeCard = TakeCard(take, player)
    takeCard.init()
    add(takeCard)
    return takeCard
}