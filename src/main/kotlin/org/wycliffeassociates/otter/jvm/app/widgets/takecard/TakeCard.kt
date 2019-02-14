package org.wycliffeassociates.otter.jvm.app.widgets.takecard

import com.jfoenix.controls.JFXButton
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
import org.wycliffeassociates.otter.jvm.app.theme.AppStyles
import org.wycliffeassociates.otter.jvm.app.theme.AppTheme
import org.wycliffeassociates.otter.jvm.app.widgets.SimpleAudioPlayer
import org.wycliffeassociates.otter.jvm.app.widgets.simpleaudioplayer
import tornadofx.*

class TakeCard(val take: Take, player: IAudioPlayer) : AnchorPane() {
    val playedProperty = SimpleBooleanProperty(take.played)

    var deleteButton: Button by singleAssign()
    var takeNumberLabel: Label by singleAssign()
    var timestampLabel: Label by singleAssign()
    var playButton: Button by singleAssign()
    var editButton: Button by singleAssign()

    var simpleAudioPlayer: SimpleAudioPlayer by singleAssign()

    init {
        importStylesheet<TakeCardStyles>()
        addClass(TakeCardStyles.defaultTakeCard)
        vbox {
            addClass(TakeCardStyles.content)
            //the top bar of the take card
            hbox(10) {
                vgrow = Priority.ALWAYS
                hbox(10.0) {
                    hgrow = Priority.ALWAYS
                    alignment = Pos.TOP_LEFT
                    takeNumberLabel = label("Take " + "%02d".format(take.number), TakeCardStyles.draggingIcon())
                    takeNumberLabel.addClass(TakeCardStyles.takeNumberLabel)
                    timestampLabel = label(take.timestamp.toString())
                    timestampLabel.addClass(TakeCardStyles.timestampLabel)
                }
                hbox {
                    alignment = Pos.TOP_RIGHT
                    hgrow = Priority.ALWAYS
                    deleteButton = button("Delete", MaterialIconView(MaterialIcon.DELETE, "15px"))
                }
            }
            // waveform and audio control buttons
            vbox(10.0) {
                alignment = Pos.TOP_CENTER
                hbox {
                    simpleAudioPlayer = simpleaudioplayer(take.path, player) {
                        vgrow = Priority.ALWAYS
                        alignment = Pos.CENTER_LEFT
                        playGraphic = TakeCardStyles.playIcon()
                        pauseGraphic = TakeCardStyles.pauseIcon()
                        with(playPauseButton) {
                            addEventHandler(ActionEvent.ACTION) {
                                if (!take.played) {
                                    take.played = true
                                }
                                playedProperty.value = take.played
                            }
                        }
                    }
                }
                hbox(15.0) {
                    playButton = JFXButton("PLAY", MaterialIconView(MaterialIcon.PLAY_ARROW, "25px"))
                            .addClass(TakeCardStyles.defaultButton)
                    editButton = JFXButton("EDIT", MaterialIconView(MaterialIcon.EDIT, "25px")
                            .apply { fill = TakeCardStyles.defaultGreen })
                            .addClass(TakeCardStyles.defaultButton).apply { textFill = TakeCardStyles.defaultGreen }

                    add(playButton)
                    add(editButton)
                }
            }
            anchorpaneConstraints {
                topAnchor = 0.0
                bottomAnchor = 0.0
                leftAnchor = 0.0
                rightAnchor = 0.0
            }
        }
    }
}

fun Pane.takecard(take: Take, player: IAudioPlayer, init: TakeCard.() -> Unit = {}): TakeCard {
    val takeCard = TakeCard(take, player)
    takeCard.init()
    add(takeCard)
    return takeCard
}