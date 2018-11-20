package org.wycliffeassociates.otter.jvm.app.theme

import javafx.scene.paint.Color
import tornadofx.c

abstract class AppColors {
    val white: Color = Color.WHITE
    val lightGray: Color = c("#DFDEE3")
    val gray: Color = c("#AAA")
    val appRed: Color = c("#CC4141")
    val appBlue: Color = c("#0094F0")
    val appGreen: Color = c("#58BD2F")

    abstract val base: Color
    abstract val defaultBackground: Color
    abstract val defaultText: Color
    abstract val cardBackground: Color
    abstract val disabledCardBackground: Color
    abstract val colorlessButton: Color
    abstract val dropShadow: Color
    abstract val imagePlaceholder: Color
}