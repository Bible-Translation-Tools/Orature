package org.wycliffeassociates.otter.jvm.app.ui.mainscreen.view

import javafx.stage.Screen
import org.wycliffeassociates.otter.jvm.app.theme.AppTheme
import tornadofx.*

class MainScreenStyles : Stylesheet() {
    companion object {
        val main by cssclass()
        val listMenu by cssclass()
        val listItem by cssclass()
        val navBoxInnercard by cssclass()
        val navbutton by cssclass()
    }

    init {

        main {
            prefWidth = Screen.getPrimary().visualBounds.width.px - 100.0
            prefHeight = Screen.getPrimary().visualBounds.height.px - 100.0
        }

        // this gets compiled down to list-menu
        listMenu {
            backgroundColor += AppTheme.colors.defaultBackground
        }

        // this gets compiled down to list-item
        listItem {
            backgroundColor += AppTheme.colors.defaultBackground
            padding = box(24.px)
        }
    }
}