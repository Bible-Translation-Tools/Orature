package org.wycliffeassociates.otter.jvm.app.ui.contentgrid.view

import javafx.geometry.Pos
import javafx.scene.paint.Color
import org.wycliffeassociates.otter.jvm.app.theme.AppTheme
import tornadofx.*


class ContentGridStyles: Stylesheet() {

    companion object {
        val contentContainer by cssclass()
        val contentLoadingProgress by cssclass()
        val panelStyle by cssclass()
    }

    init {
        contentContainer {
            vgap = 32.px
            hgap = 24.px
            alignment = Pos.CENTER
            horizontalCellSpacing = 24.0.px
            verticalCellSpacing = 32.0.px
            maxCellsInRow = 5
            // Add larger padding on bottom to keep FAB from blocking last row cards
        }

        contentLoadingProgress {
            progressColor = AppTheme.colors.appRed
        }
        panelStyle {
            prefWidth = 3600.px
            prefHeight = 2100.px
        }
    }
}