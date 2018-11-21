package org.wycliffeassociates.otter.jvm.app.ui.projecteditor.view

import javafx.geometry.Pos
import javafx.scene.effect.DropShadow
import javafx.scene.paint.Color
import javafx.scene.text.FontWeight
import org.wycliffeassociates.otter.jvm.app.theme.AppTheme
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
    }

    init {
        projectTitle {
            fontSize = 20.px
            padding = box(10.px)
            backgroundColor += AppTheme.colors.imagePlaceholder
            textFill = AppTheme.colors.defaultText
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
            backgroundColor += AppTheme.colors.cardBackground
            effect = DropShadow(10.0, AppTheme.colors.dropShadow)

            label {
                textFill = AppTheme.colors.defaultText
            }

            and(disabledCard) {
                backgroundColor += AppTheme.colors.disabledCardBackground
            }

            and(recordContext) {
                button {
                    backgroundColor += AppTheme.colors.appRed
                }
                and(hasTakes) {
                    button {
                        backgroundColor += AppTheme.colors.cardBackground
                        borderRadius += box(3.px)
                        borderColor += box(AppTheme.colors.appRed)
                        textFill = AppTheme.colors.appRed
                        child("*") {
                            fill = AppTheme.colors.appRed
                        }
                    }
                }
            }
            and(viewContext) {
                button {
                    backgroundColor += AppTheme.colors.appBlue
                }
            }
            and(editContext) {
                button {
                    backgroundColor += AppTheme.colors.appGreen
                }
            }
        }

        s(recordMenuItem, viewMenuItem, editMenuItem) {
            padding = box(20.px)
            backgroundColor += AppTheme.colors.base
            and(hover, active) {
                child("*") {
                    fill = AppTheme.colors.white
                }
            }
        }

        contextMenu {
            padding = box(0.px)
        }

        recordMenuItem {
            and(hover, active) {
                backgroundColor += AppTheme.colors.appRed
            }
            child("*") {
                fill = AppTheme.colors.appRed
            }
        }

        viewMenuItem {
            and(hover, active) {
                backgroundColor += AppTheme.colors.appBlue
            }
            child("*") {
                fill = AppTheme.colors.appBlue
            }
        }

        editMenuItem {
            and(hover, active) {
                backgroundColor += AppTheme.colors.appGreen
            }
            child("*") {
                fill = AppTheme.colors.appGreen
            }
        }

        chapterList {
            focusColor = Color.TRANSPARENT
            faintFocusColor = Color.TRANSPARENT
            borderWidth += box(0.px)
            padding = box(10.px, 0.px, 0.px, 10.px)
            backgroundColor += AppTheme.colors.base
            listCell {
                padding = box(0.px, 0.px, 0.px, 20.px)
                backgroundColor += AppTheme.colors.base
                backgroundRadius += box(10.px)
                fontSize = 14.px
                fontWeight = FontWeight.BOLD
                prefHeight = 40.px
                label {
                    textFill = AppTheme.colors.defaultText
                    child("*") {
                        fill = AppTheme.colors.defaultText
                    }
                }

                and(hover) {
                    backgroundColor += AppTheme.colors.defaultBackground
                }
                and(selected) {
                    backgroundColor += AppTheme.colors.appRed
                    label {
                        textFill = AppTheme.colors.white
                        child("*") {
                            fill = AppTheme.colors.white
                        }
                    }
                }
            }
        }

        chunksLoadingProgress {
            progressColor = AppTheme.colors.appRed
        }

        backButtonContainer {
            padding = box(20.px)
            alignment = Pos.CENTER_RIGHT
        }
    }
}