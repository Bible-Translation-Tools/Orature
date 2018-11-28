package org.wycliffeassociates.otter.jvm.app.ui.menu.view

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import org.wycliffeassociates.otter.jvm.app.theme.AppTheme
import tornadofx.Stylesheet
import tornadofx.px

class MainMenuStyles : Stylesheet() {
    companion object {
        fun importIcon(size: String) = MaterialIconView(MaterialIcon.INPUT, size)
        fun addPluginIcon(size: String) = MaterialIconView(MaterialIcon.ADD, size)
        fun removePluginIcon(size: String) = MaterialIconView(MaterialIcon.DELETE, size)
        fun recorderIcon(size: String) = MaterialIconView(MaterialIcon.MIC, size)
        fun editorIcon(size: String) = MaterialIconView(MaterialIcon.MODE_EDIT, size)
    }
    init {
        menuBar {
            backgroundColor += AppTheme.colors.base
            menu {
                fontSize = 16.px
                backgroundColor += AppTheme.colors.base
                label {
                    textFill = AppTheme.colors.defaultText
                }
                and(hover, showing) {
                    backgroundColor += AppTheme.colors.appRed
                    label {
                        textFill = AppTheme.colors.white
                    }
                }
                maxHeight = Double.MAX_VALUE.px

                menuItem {
                    fontSize = 14.px
                    backgroundColor += AppTheme.colors.base
                    label {
                        textFill = AppTheme.colors.defaultText
                    }
                    graphicContainer {
                        child("*") {
                            fill = AppTheme.colors.defaultText
                        }
                    }
                    and(hover, focused, showing) {
                        backgroundColor += AppTheme.colors.appRed
                        label {
                            textFill = AppTheme.colors.white
                        }
                        graphicContainer {
                            child("*") {
                                fill = AppTheme.colors.white
                            }
                        }
                    }
                }
            }
            contextMenu {
                backgroundColor += AppTheme.colors.base
            }

        }
    }
}