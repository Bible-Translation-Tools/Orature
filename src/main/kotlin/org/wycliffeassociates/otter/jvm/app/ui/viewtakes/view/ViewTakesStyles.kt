package org.wycliffeassociates.otter.jvm.app.ui.viewtakes.view

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.scene.Cursor
import javafx.scene.effect.DropShadow
import javafx.scene.paint.Color
import org.wycliffeassociates.otter.jvm.app.UIColorsObject
import org.wycliffeassociates.otter.jvm.app.UIColorsObject.Colors
import tornadofx.*

class ViewTakesStyles : Stylesheet() {
    companion object {
        val acceptButton by cssclass()
        val rejectButton by cssclass()
        val deleteButton by cssclass()
        val dragTarget by cssclass()
        val takeCard by cssclass()
        val badge by cssclass()
        val placeholder by cssclass()
        val headerContainer by cssclass()
        val takeFlowPane by cssclass()
        val glow by cssclass()
        val recordButton by cssclass()
        val playPauseButton by cssclass()
        fun recordIcon(size: String) = MaterialIconView(MaterialIcon.MIC_NONE, size)
    }

    init {
        takeFlowPane {
            borderColor += box(Color.LIGHTGRAY)
            borderWidth += box(0.px, 0.px, 0.px, 0.px)
            backgroundColor += Color.TRANSPARENT
            spacing = 10.px
            padding = box(20.px)
            vgap = 16.px
            hgap = 16.px
        }
        button {
            and(acceptButton, rejectButton) {
                padding = box(5.px, 30.px)
                backgroundRadius += box(5.px)
                borderRadius += box(5.px)
                borderColor += box(c(Colors["primary"]))
            }

            and(acceptButton) {
                backgroundColor += c(Colors["primary"])
                child("*") {
                    fill = Color.WHITE
                }
            }

            and(rejectButton) {
                backgroundColor += Color.WHITE
                child("*") {
                    fill = c(Colors["primary"])
                }
            }
        }

        glow {
            effect = DropShadow(5.0, c(Colors["secondary"]))
        }

        dragTarget {
            backgroundColor += c(Colors["base"]).deriveColor(0.0, 1.0, 1.0, 0.8)
            borderRadius += box(10.px)
            backgroundRadius += box(10.px)
            maxHeight = 100.px
            maxWidth = 250.px
            label {
                fontSize = 16.px
            }
            child("*") {
                fill = c(Colors["secondary"])
            }
        }

        takeCard {
            borderRadius += box(10.px)
            borderColor += box(c(Colors["baseText"]))
            borderWidth += box(1.px)
            badge {
                backgroundColor += c(Colors["primary"])
            }
            button {
                and(deleteButton) {
                    child("*") {
                        fill = c(Colors["baseText"])
                    }
                }
                and(playPauseButton) {
                    child("*") {
                        fill = c(Colors["primary"])
                    }
                }
            }
        }

        placeholder {
            backgroundColor += c(Colors["neutralTone"])
            borderRadius += box(10.px)
            backgroundRadius += box(10.px)
            minHeight = 100.px
            minWidth = 250.px
        }

        headerContainer {
            backgroundColor += Color.WHITE
            padding = box(20.px)
        }

        recordButton {
            backgroundRadius += box(25.px)
            borderRadius += box(25.px)
            backgroundColor += c(UIColorsObject.Colors["base"])
            minHeight = 50.px
            minWidth = 50.px
            maxHeight = 50.px
            maxWidth = 50.px
            cursor = Cursor.HAND
            effect = DropShadow(10.0, Color.GRAY)
            unsafe("-jfx-button-type", raw("RAISED"))
            child("*") {
                fill = c(UIColorsObject.Colors["primary"])
            }
        }
    }
}