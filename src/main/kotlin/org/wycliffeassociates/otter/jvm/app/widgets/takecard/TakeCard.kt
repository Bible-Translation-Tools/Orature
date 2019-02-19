package org.wycliffeassociates.otter.jvm.app.widgets.takecard

import com.github.thomasnield.rxkotlinfx.bind
import com.jfoenix.controls.JFXButton
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.beans.property.SimpleBooleanProperty
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.Pane
import javafx.scene.layout.Priority
import org.wycliffeassociates.otter.common.data.model.Take
import org.wycliffeassociates.otter.common.device.IAudioPlayer
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
    private val isAudioPlaying: SimpleBooleanProperty = SimpleBooleanProperty()
    init {
        importStylesheet<TakeCardStyles>()
        addClass(TakeCardStyles.defaultTakeCard)
        vbox {
            addClass(TakeCardStyles.content)
            //the top bar of the take card
            hbox(10) {
                style {
                    maxHeight = 75.0.px
                }
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
            vbox(15.0) {
                vgrow = Priority.ALWAYS
                alignment = Pos.CENTER
                hbox {
                    alignment = Pos.CENTER
                    simpleAudioPlayer = simpleaudioplayer(take.path, player) {
                        vgrow = Priority.ALWAYS
                        addClass(TakeCardStyles.progressBar)
                        isAudioPlaying.bind(isPlaying)
                    }
                }
                hbox(15.0) {
                    playButton = JFXButton("PLAY", TakeCardStyles.playIcon())
                            .addClass(TakeCardStyles.defaultButton)
                            .apply {
                                isDisableVisualFocus = true
                                action {
                                    simpleAudioPlayer.buttonPressed()
                                }
                            }
                    editButton = JFXButton("EDIT", TakeCardStyles.editIcon())
                            .addClass(TakeCardStyles.defaultButton)
                            .apply {
                                textFill = TakeCardStyles.defaultGreen
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
            when(isAudioPlaying.value) {
                true -> {
                    playButton.apply {
                        graphic = TakeCardStyles.pauseIcon()
                        text = "PAUSE"
                    }
                }
                false -> {
                    playButton.apply {
                        graphic = TakeCardStyles.playIcon()
                        text = "PLAY"
                    }
                }
            }
        }
    }
}

fun takecard(take: Take, player: IAudioPlayer, init: TakeCard.() -> Unit = {}): TakeCard {
    val takeCard = TakeCard(take, player)
    takeCard.init()
    return takeCard
}