package org.wycliffeassociates.otter.jvm.app.ui.menu.view

import org.wycliffeassociates.otter.jvm.app.theme.AppTheme
import tornadofx.Stylesheet
import tornadofx.px

class MainMenuStylesheet : Stylesheet() {
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