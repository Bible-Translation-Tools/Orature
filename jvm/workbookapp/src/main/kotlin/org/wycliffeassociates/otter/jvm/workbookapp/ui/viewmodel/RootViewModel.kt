/**
 * Copyright (C) 2020, 2021 Wycliffe Associates
 *
 * This file is part of Orature.
 *
 * Orature is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Orature is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Orature.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel

import com.jthemedetecor.OsThemeDetector
import java.lang.IllegalArgumentException
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javax.inject.Inject
import javax.sound.sampled.LineUnavailableException
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.ColorTheme
import org.wycliffeassociates.otter.common.domain.theme.AppTheme
import org.wycliffeassociates.otter.jvm.device.audio.AudioConnectionFactory
import org.wycliffeassociates.otter.jvm.device.audio.AudioErrorType
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import tornadofx.*
import java.util.function.Consumer

class RootViewModel : ViewModel() {

    val logger = LoggerFactory.getLogger(RootViewModel::class.java)

    val pluginOpenedProperty = SimpleBooleanProperty(false)
    val drawerOpenedProperty = SimpleBooleanProperty(false)

    val showAudioErrorDialogProperty = SimpleBooleanProperty(false)
    var audioErrorType = SimpleObjectProperty<AudioErrorType>()

    private val osThemeDetector = OsThemeDetector.getDetector()
    private val isOSDarkMode = SimpleBooleanProperty(osThemeDetector.isDark)

    private val onSystemColorModeChanged = Consumer<Boolean> {
        runLater { isOSDarkMode.set(it) }
    }

    @Inject
    lateinit var theme: AppTheme

    @Inject
    lateinit var audioConnectionFactory: AudioConnectionFactory

    init {
        (app as IDependencyGraphProvider).dependencyGraph.inject(this)
        initializeAudioErrorListener()
        initSystemThemeListener()
    }

    private fun initializeAudioErrorListener() {
        audioConnectionFactory
            .errorListener()
            .subscribe {
                logger.error("Audio Device Error", it.exception)
                showAudioErrorDialogProperty.set(true)
                when (it.exception) {
                    is LineUnavailableException, is IllegalArgumentException -> {
                        audioErrorType.set(it.type)
                        showAudioErrorDialogProperty.set(true)
                    }
                    else -> {
                        throw it.exception
                    }
                }
            }
    }

    fun updateTheme(selectedTheme: ColorTheme) {
        val themeColor: ColorTheme = if (selectedTheme == ColorTheme.SYSTEM) {
            bindSystemTheme()
            if (osThemeDetector.isDark)
                ColorTheme.DARK
            else
                ColorTheme.LIGHT
        } else {
            unBindSystemTheme()
            selectedTheme
        }

        when (themeColor) {
            ColorTheme.DARK -> setDarkMode()
            ColorTheme.LIGHT -> setLightMode()
        }

        theme.setPreferredThem(selectedTheme)
            .subscribe()
    }

    private fun initSystemThemeListener() {
        isOSDarkMode.onChange {
            if (it) {
                setDarkMode()
            } else {
                setLightMode()
            }
        }
    }

    private fun bindSystemTheme() {
        osThemeDetector.registerListener(onSystemColorModeChanged)
    }

    private fun unBindSystemTheme() {
        try {
            osThemeDetector.removeListener(onSystemColorModeChanged)
        } catch(ex: Exception) { }
    }

    private fun setLightMode() {
        primaryStage.scene.stylesheets.remove("/css/root_dark.css")
        primaryStage.scene.stylesheets.add("/css/root.css")
    }

    private fun setDarkMode() {
        primaryStage.scene.stylesheets.remove("/css/root.css")
        primaryStage.scene.stylesheets.add("/css/root_dark.css")
    }
}
