package org.wycliffeassociates.otter.jvm.app.ui.collectionsgrid.view

import javafx.geometry.Pos
import org.wycliffeassociates.otter.jvm.app.theme.AppTheme
import tornadofx.*

class CollectionGridStyles: Stylesheet() {

    companion object {
        val collectionsContainer by cssclass()
        val contentLoadingProgress by cssclass()
        val panelStyle by cssclass()
    }

    init {
        collectionsContainer {
            vgap = 32.px
            hgap = 24.px
            horizontalCellSpacing = 24.0.px
            verticalCellSpacing = 32.0.px
            alignment = Pos.CENTER

        }

        contentLoadingProgress {
            progressColor = AppTheme.colors.appRed
        }
        panelStyle {
            prefWidth = 3600.px
            prefHeight = 1400.px
        }
    }
}