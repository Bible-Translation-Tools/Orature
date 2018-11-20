package org.wycliffeassociates.otter.jvm.app.widgets.takecard

import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.beans.property.SimpleBooleanProperty
import javafx.event.ActionEvent
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.Pane
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import org.wycliffeassociates.otter.common.data.model.Take
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.jvm.app.widgets.SimpleAudioPlayer
import org.wycliffeassociates.otter.jvm.app.widgets.simpleaudioplayer
import tornadofx.*

class TakeCard(val take: Take, player: IAudioPlayer) : AnchorPane() {
    val playedProperty = SimpleBooleanProperty(take.played)
    val badge = stackpane {
        addClass(TakeCardStyles.badge)
        val icon = MaterialDesignIconView(MaterialDesignIcon.CREATION, "18px")
        icon.addClass(TakeCardStyles.iconStyle)
        add(icon)
        isVisible = !take.played
    }

    var deleteButton: Button by singleAssign()
    var takeNumberLabel: Label by singleAssign()
    var timestampLabel: Label by singleAssign()
    var simpleAudioPlayer: SimpleAudioPlayer by singleAssign()

    init {
        importStylesheet<TakeCardStyles>()
        setRightAnchor(badge, 0.0)
        setTopAnchor(badge, 0.0)
        addClass(TakeCardStyles.defaultTakeCard)
        vbox {
            addClass(TakeCardStyles.content)
            hbox(10) {
                vgrow = Priority.ALWAYS
                alignment = Pos.CENTER_LEFT
                takeNumberLabel = label("%02d".format(take.number))
                takeNumberLabel.addClass(TakeCardStyles.takeNumberLabel)
                timestampLabel = label(take.timestamp.toString())
                timestampLabel.addClass(TakeCardStyles.timestampLabel)
            }
            hbox {
                alignment = Pos.CENTER
                simpleAudioPlayer = simpleaudioplayer(take.path, player) {
                    vgrow = Priority.ALWAYS
                    alignment = Pos.CENTER_LEFT
                    playGraphic = TakeCardStyles.playIcon()
                    pauseGraphic = TakeCardStyles.pauseIcon()
                    with(playPauseButton) {
                        addEventHandler(ActionEvent.ACTION) {
                            if (!take.played) {
                                take.played = true
                                badge.isVisible = false
                            }
                            playedProperty.value = take.played
                        }
                    }
                }
                deleteButton = button(graphic = TakeCardStyles.deleteIcon())
            }
            anchorpaneConstraints {
                topAnchor = 0.0
                bottomAnchor = 0.0
                leftAnchor = 0.0
                rightAnchor = 0.0
            }
        }

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