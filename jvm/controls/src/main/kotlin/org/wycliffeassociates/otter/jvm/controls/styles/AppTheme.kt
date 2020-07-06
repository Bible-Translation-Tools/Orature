package org.wycliffeassociates.otter.jvm.controls.styles

import javafx.scene.paint.Color
import tornadofx.c

object AppTheme {
    private var _colors: AppColors = LightColors()
    val colors: AppColors
        get() = _colors

    private class LightColors : AppColors() {
        override val base: Color = c("#FFF")
        override val defaultBackground: Color = c("#EEEEEE")
        override val defaultText: Color = Color.BLACK
        override val cardBackground: Color = Color.WHITE
        override val disabledCardBackground: Color = c("#E5E5E5")
        override val colorlessButton: Color = c("#DFDEE3")
        override val dropShadow: Color = c("#9e9e9e")
        override val imagePlaceholder: Color = c("#DFDEE3")
        override val subtitle: Color = c("#AAA")
        override val lightBackground: Color = c("#E6E8E9")
    }

    private class DarkColors : AppColors() {
        override val base: Color = c("#000")
        override val defaultBackground: Color = c("#222")
        override val defaultText: Color = Color.WHITE
        override val cardBackground: Color = c("#444")
        override val disabledCardBackground: Color = c("#222")
        override val colorlessButton: Color = c("#DFDEE3")
        override val dropShadow: Color = Color.BLACK
        override val imagePlaceholder: Color = c("#555")
        override val subtitle: Color = c("#AAA")
        override val lightBackground: Color = c("#E6E8E9")
    }

    fun useDarkColors() {
        _colors = DarkColors()
    }

    fun useLightColors() {
        _colors = LightColors()
    }
}
