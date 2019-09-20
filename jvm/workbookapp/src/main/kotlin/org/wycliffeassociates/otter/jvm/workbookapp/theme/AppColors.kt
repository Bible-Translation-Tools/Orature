package org.wycliffeassociates.otter.jvm.workbookapp.theme

import javafx.scene.paint.Color
import tornadofx.c

abstract class AppColors {
    val white: Color = Color.WHITE
    val appRed: Color = c("#CC4141")
    val appBlue: Color = c("#0094F0")
    val appGreen: Color = c("#58BD2F")
    val appDarkGrey: Color = c("#E0E0E0")

    abstract val base: Color
    abstract val defaultBackground: Color
    abstract val defaultText: Color
    abstract val subtitle: Color
    abstract val cardBackground: Color
    abstract val disabledCardBackground: Color
    abstract val colorlessButton: Color
    abstract val dropShadow: Color
    abstract val imagePlaceholder: Color
    abstract val lightBackground: Color
}