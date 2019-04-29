package org.wycliffeassociates.otter.jvm.app.ui.cardgrid.view

import javafx.geometry.Pos
import org.wycliffeassociates.otter.jvm.app.theme.AppTheme
import tornadofx.*


class CardGridStyles: Stylesheet() {

    companion object {
        val contentContainer by cssclass()
        val contentLoadingProgress by cssclass()
    }

    init {
        contentContainer {
            vgap = 32.px
            hgap = 24.px
            alignment = Pos.CENTER
            horizontalCellSpacing = 24.0.px
            verticalCellSpacing = 32.0.px
            // Add larger padding on bottom to keep FAB from blocking last row cards
        }

        contentLoadingProgress {
            progressColor = AppTheme.colors.appRed
        }
    }
}