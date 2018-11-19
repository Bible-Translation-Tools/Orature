package org.wycliffeassociates.otter.jvm.app.ui.projecteditor.view

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.geometry.Pos
import javafx.scene.effect.DropShadow
import javafx.scene.paint.Color
import javafx.scene.text.FontWeight
import org.wycliffeassociates.otter.jvm.app.UIColorsObject.Colors
import org.wycliffeassociates.otter.jvm.app.ui.viewtakes.view.ViewTakesStyles
import tornadofx.*

class ProjectEditorStyles : Stylesheet() {
    companion object {
        val chunkCard by cssclass()
        val disabledCard by cssclass()

        val recordContext by cssclass()
        val hasTakes by cssclass()
        val editContext by cssclass()
        val viewContext by cssclass()

        val recordMenuItem by cssclass()
        val editMenuItem by cssclass()
        val viewMenuItem by cssclass()

        val projectTitle by cssclass()
        val chunkGridContainer by cssclass()

        val active by csspseudoclass("active")

        val chapterList by cssclass()

        val chunksLoadingProgress by cssclass()

        val backButtonContainer by cssclass()
        val contextMenu by cssclass()

        // Icons
        fun recordIcon(size: String) = MaterialIconView(MaterialIcon.MIC_NONE, size)
        fun editIcon(size: String) = MaterialIconView(MaterialIcon.EDIT, size)
        fun viewTakesIcon(size: String) = MaterialIconView(MaterialIcon.APPS, size)
    }

    init {
        projectTitle {
            fontSize = 20.px
            padding = box(10.px)
            backgroundColor += Color.DARKGRAY
            textFill = Color.WHITE
            maxWidth = Double.MAX_VALUE.px
            alignment = Pos.BOTTOM_LEFT
            prefHeight = 100.px
        }

        chunkGridContainer {
            padding = box(0.px, 20.px)
        }

        datagrid {
            cellWidth = 200.px
            cellHeight = 200.px
            cell {
                backgroundColor += Color.TRANSPARENT
            }
        }

        chunkCard {
            backgroundColor += c(Colors["base"])
            effect = DropShadow(10.0, Color.LIGHTGRAY)

            label {
                textFill = c(Colors["baseText"])
            }

            and(disabledCard) {
                backgroundColor += c(Colors["baseBackground"])
            }

            and(recordContext) {
                button {
                    backgroundColor += c(Colors["primary"])
                }
                and(hasTakes) {
                    button {
                        backgroundColor += Color.WHITE
                        borderRadius += box(3.px)
                        borderColor += box(c(Colors["primary"]))
                        textFill = c(Colors["primary"])
                        child("*") {
                            fill = c(Colors["primary"])
                        }
                    }
                }
            }
            and(viewContext) {
                button {
                    backgroundColor += c(Colors["secondary"])
                }
            }
            and(editContext) {
                button {
                    backgroundColor += c(Colors["tertiary"])
                }
            }
        }

        s(recordMenuItem, viewMenuItem, editMenuItem) {
            padding = box(20.px)
            backgroundColor += Color.WHITE
            and(hover, active) {
                child("*") {
                    fill = Color.WHITE
                }
            }
        }

        contextMenu {
            padding = box(0.px)
        }

        recordMenuItem {
            and(hover, active) {
                backgroundColor += c(Colors["primary"])
            }
            child("*") {
                fill = c(Colors["primary"])
            }
        }

        viewMenuItem {
            and(hover, active) {
                backgroundColor += c(Colors["secondary"])
            }
            child("*") {
                fill = c(Colors["secondary"])
            }
        }

        editMenuItem {
            and(hover, active) {
                backgroundColor += c(Colors["tertiary"])
            }
            child("*") {
                fill = c(Colors["tertiary"])
            }
        }

        chapterList {
            focusColor = Color.TRANSPARENT
            faintFocusColor = Color.TRANSPARENT
            borderWidth += box(0.px)
            padding = box(10.px, 0.px, 0.px, 10.px)
            listCell {
                padding = box(0.px, 0.px, 0.px, 20.px)
                backgroundColor += Color.WHITE
                backgroundRadius += box(10.px)
                fontSize = 14.px
                fontWeight = FontWeight.BOLD
                prefHeight = 40.px
                and(hover) {
                    backgroundColor += Color.WHITE.deriveColor(
                            1.0, 1.0,
                            0.95, 1.0
                    )
                }
                and(selected) {
                    backgroundColor += c(Colors["primary"])
                    textFill = Color.WHITE
                }
            }
        }

        chunksLoadingProgress {
            progressColor = c(Colors["primary"])
        }

        backButtonContainer {
            padding = box(20.px)
            alignment = Pos.CENTER_RIGHT
        }
    }
}