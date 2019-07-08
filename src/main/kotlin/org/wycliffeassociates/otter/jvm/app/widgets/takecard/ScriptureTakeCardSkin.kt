package org.wycliffeassociates.otter.jvm.app.widgets.takecard

import com.jfoenix.controls.JFXButton
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.geometry.Pos
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import tornadofx.*
import tornadofx.FX.Companion.messages

class ScriptureTakeCardSkin(control: TakeCard) : TakeCardSkin(control) {

    private var container: VBox

    init {
        importStylesheet<TakeCardStyles>()

        container = VBox().apply {
            anchorpane {
                addClass(TakeCardStyles.scriptureTakeCard)
                vbox {
                    addClass(TakeCardStyles.content)
                    //the top bar of the take card
                    hbox(10) {
                        style {
                            maxHeight = 75.0.px
                        }
                        hbox(10.0) {
                            hgrow = Priority.ALWAYS
                            alignment = Pos.CENTER_LEFT
                            label(
                                "${messages["take"]} %02d".format(control.take.number),
                                TakeCardStyles.draggingIcon()
                            ) {
                                addClass(TakeCardStyles.takeNumberLabel)
                            }
                            label(control.take.modifiedTimestamp.value.toString()) {
                                addClass(TakeCardStyles.timestampLabel)
                            }
                        }
                        hbox {
                            alignment = Pos.TOP_RIGHT
                            hgrow = Priority.SOMETIMES
                            add(JFXButton(messages["delete"], MaterialIconView(MaterialIcon.DELETE, "18px")))
                        }
                    }
                    // waveform and audio control buttons
                    vbox(15.0) {
                        vgrow = Priority.ALWAYS
                        alignment = Pos.CENTER
                        hbox {
                            alignment = Pos.CENTER
                            add(control.simpleAudioPlayer.apply {
                                addClass(TakeCardStyles.takeProgressBar)
                            })
                        }
                        hbox(15.0) {
                            add(playButton.addClass(TakeCardStyles.defaultButton))
                            add(
                                JFXButton(messages["edit"], MaterialIconView(MaterialIcon.EDIT, "18px").apply {
                                    fill = TakeCardStyles.defaultGreen
                                }).apply {
                                    addClass(TakeCardStyles.defaultButton)
                                    addClass(TakeCardStyles.editButton)
                                }
                            )
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
        children.addAll(container)
    }
}