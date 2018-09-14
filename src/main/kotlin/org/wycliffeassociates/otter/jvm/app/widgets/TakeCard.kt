package org.wycliffeassociates.otter.jvm.app.widgets

import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.event.ActionEvent
import javafx.geometry.Pos
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.Pane
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.jvm.app.UIColorsObject.Colors
import tornadofx.*

class TakeCard(take: Take, player: IAudioPlayer) : AnchorPane() {
    private val badge = stackpane {
        style {
            backgroundColor += c(Colors["primary"])
            backgroundRadius += box(0.px, 10.px, 0.px, 10.px)
            padding = box(8.px)
        }
        val icon = MaterialDesignIconView(MaterialDesignIcon.CREATION, "18px")
        icon.style(true) {
            fill = Color.WHITE
        }
        add(icon)
        isVisible = !take.played
    }


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
                label("%tD".format(take.date)) {
                    style {
                        fontSize = 12.px
                    }
                }
            }
            simpleaudioplayer(take.file, player) {
                vgrow = Priority.ALWAYS
                style {
                    alignment = Pos.CENTER_LEFT
                }
                playGraphic = MaterialIconView(MaterialIcon.PLAY_CIRCLE_OUTLINE, "30px")
                playGraphic?.apply {
                    style(true) {
                        fill = c(Colors["primary"])
                    }
                }
                pauseGraphic = MaterialIconView(MaterialIcon.PAUSE_CIRCLE_OUTLINE, "30px")
                pauseGraphic?.apply {
                    style(true) {
                        fill = c(Colors["primary"])
                    }
                }
                with(playPauseButton) {
                    style(true) {
                        backgroundColor += Color.TRANSPARENT
                    }
                    addEventHandler(ActionEvent.ACTION) {
                        if (!take.played) {
                            take.played = true
                            badge.isVisible = false
                        }
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