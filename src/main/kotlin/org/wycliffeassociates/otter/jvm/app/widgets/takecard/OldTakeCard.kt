package org.wycliffeassociates.otter.jvm.app.widgets.takecard

import com.jfoenix.controls.JFXButton
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.beans.property.SimpleBooleanProperty
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.Priority
import org.wycliffeassociates.otter.common.data.model.Take
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.jvm.app.widgets.SimpleAudioPlayer
import org.wycliffeassociates.otter.jvm.app.widgets.simpleaudioplayer
import tornadofx.*

class OldTakeCard(val take: Take, player: IAudioPlayer, val takePrefix: String) : AnchorPane() {
    val playedProperty = SimpleBooleanProperty(take.played)

    var deleteButton: Button by singleAssign()
    var takeNumberLabel: Label by singleAssign()
    var timestampLabel: Label by singleAssign()
    var playButton: Button by singleAssign()
    var editButton: Button by singleAssign()


    var simpleAudioPlayer: SimpleAudioPlayer by singleAssign()
    private val isAudioPlaying: SimpleBooleanProperty = SimpleBooleanProperty()

    init {
        importStylesheet<OldTakeCardStyles>()
        addClass(OldTakeCardStyles.defaultTakeCard)
        vbox {
            addClass(OldTakeCardStyles.content)
            //the top bar of the take card
            hbox(10) {
                style {
                    maxHeight = 75.0.px
                }
                hbox(10.0) {
                    hgrow = Priority.ALWAYS
                    alignment = Pos.CENTER_LEFT
                    takeNumberLabel = label("$takePrefix %02d".format(take.number), OldTakeCardStyles.draggingIcon())
                    takeNumberLabel.addClass(OldTakeCardStyles.takeNumberLabel)
                    timestampLabel = label(take.created.toString())
                    timestampLabel.addClass(OldTakeCardStyles.timestampLabel)
                }
                hbox {
                    alignment = Pos.TOP_RIGHT
                    hgrow = Priority.SOMETIMES
                    deleteButton = button("Delete", MaterialIconView(MaterialIcon.DELETE, "15px"))
                }
            }
            // waveform and audio control buttons
            vbox(15.0) {
                vgrow = Priority.ALWAYS
                alignment = Pos.CENTER
                hbox {
                    alignment = Pos.CENTER
                    simpleAudioPlayer = simpleaudioplayer(take.path, player) {
                        vgrow = Priority.ALWAYS
                        addClass(OldTakeCardStyles.takeProgressBar)
                        isAudioPlaying.bind(isPlaying)
                    }
                    add(simpleAudioPlayer)
                }
                hbox(15.0) {
                    playButton = JFXButton("PLAY", OldTakeCardStyles.playIcon())
                            .addClass(OldTakeCardStyles.defaultButton)
                            .apply {
                                isDisableVisualFocus = true
                                action {
                                    simpleAudioPlayer.buttonPressed()
                                }
                            }
                    editButton = JFXButton("EDIT", OldTakeCardStyles.editIcon())
                            .addClass(OldTakeCardStyles.defaultButton)
                            .apply {
                                textFill = OldTakeCardStyles.defaultGreen
                                isDisableVisualFocus = true
                            }

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

        isAudioPlaying.onChange {
            when (isAudioPlaying.value) {
                true -> {
                    playButton.apply {
                        graphic = OldTakeCardStyles.pauseIcon()
                        text = "PAUSE"
                    }
                }
                false -> {
                    playButton.apply {
                        graphic = OldTakeCardStyles.playIcon()
                        text = "PLAY"
                    }
                }
            }
        }
    }
}

fun oldtakecard(take: Take, player: IAudioPlayer, takePrefix: String, init: OldTakeCard.() -> Unit = {}): OldTakeCard {
    val takeCard = OldTakeCard(take, player, takePrefix)
    takeCard.init()
    return takeCard
}