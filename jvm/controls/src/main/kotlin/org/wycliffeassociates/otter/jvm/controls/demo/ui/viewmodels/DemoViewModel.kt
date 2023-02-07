package org.wycliffeassociates.otter.jvm.controls.demo.ui.viewmodels

import com.jthemedetecor.OsThemeDetector
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import org.wycliffeassociates.otter.common.data.ColorTheme
import tornadofx.*

class DemoViewModel : ViewModel() {
    val supportedThemes = observableListOf<ColorTheme>()
    val selectedThemeProperty = SimpleObjectProperty<ColorTheme>()

    val appColorMode = SimpleObjectProperty<ColorTheme>()
    private val osThemeDetector = OsThemeDetector.getDetector()
    private val isOSDarkMode = SimpleBooleanProperty(osThemeDetector.isDark)

    val shownFragment = SimpleObjectProperty<UIComponent>()

    init {
        osThemeDetector.registerListener {
            runLater { isOSDarkMode.set(it) }
        }
    }

    fun bind() {
        supportedThemes.setAll(ColorTheme.values().asList())
        selectedThemeProperty.set(ColorTheme.DARK)
    }

    fun updateTheme(selectedTheme: ColorTheme) {
        if (selectedTheme == ColorTheme.SYSTEM) {
            bindSystemTheme()
        } else {
            appColorMode.unbind()
            appColorMode.set(selectedTheme)
        }
    }

    inline fun <reified T: UIComponent> showContent() {
        val fragment = find<T>()
        shownFragment.set(fragment)
    }

    private fun bindSystemTheme() {
        appColorMode.bind(isOSDarkMode.objectBinding {
            if (it == true)
                ColorTheme.DARK
            else
                ColorTheme.LIGHT
        })
    }
}