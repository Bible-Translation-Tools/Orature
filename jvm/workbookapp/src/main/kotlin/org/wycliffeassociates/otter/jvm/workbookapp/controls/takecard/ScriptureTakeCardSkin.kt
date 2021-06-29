/**
 * Copyright (C) 2020, 2021 Wycliffe Associates
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
package org.wycliffeassociates.otter.jvm.workbookapp.controls.takecard

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.geometry.Pos
import javafx.scene.layout.Priority
import tornadofx.*
import tornadofx.FX.Companion.messages

class ScriptureTakeCardSkin(control: TakeCard) : TakeCardSkin(control) {

    init {
        importStylesheet<TakeCardStyles>()

        back.addClass(TakeCardStyles.scriptureTakeCardPlaceholder, TakeCardStyles.scriptureTakeCardDropShadow)

        front.apply {
            anchorpane {
                addClass(TakeCardStyles.scriptureTakeCard)
                vbox {
                    addClass(TakeCardStyles.content)
                    // the top bar of the take card
                    hbox(10) {
                        style {
                            maxHeight = 75.0.px
                        }
                        hbox(10.0) {
                            hgrow = Priority.ALWAYS
                            alignment = Pos.CENTER_LEFT
                            label(
                                "${messages["take"]} %02d".format(control.model.take.number),
                                TakeCardStyles.draggingIcon()
                            ) {
                                addClass(TakeCardStyles.takeNumberLabel)
                            }
                            label(control.model.take.createdTimestamp.toString()) {
                                addClass(TakeCardStyles.timestampLabel)
                            }
                        }
                        hbox {
                            alignment = Pos.TOP_RIGHT
                            hgrow = Priority.SOMETIMES
                            add(deleteButton.apply {
                                text = messages["delete"]
                                graphic = MaterialIconView(MaterialIcon.DELETE, "18px")
                            })
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
                            add(editButton.apply {
                                text = messages["edit"]
                                graphic = MaterialIconView(MaterialIcon.EDIT, "18px").apply {
                                    fill = TakeCardStyles.defaultGreen
                                }
                                addClass(TakeCardStyles.defaultButton)
                                addClass(TakeCardStyles.editButton)
                            })
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
    }
}
