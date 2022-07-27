/**
 * Copyright (C) 2020-2022 Wycliffe Associates
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
package org.wycliffeassociates.otter.jvm.workbookapp.ui

import javafx.scene.control.ButtonBase
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.Pane
import javafx.stage.Stage
import javafx.stage.StageStyle
import org.wycliffeassociates.otter.common.domain.languages.LocaleLanguage
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.jvm.controls.event.AppCloseRequestEvent
import org.wycliffeassociates.otter.jvm.device.audio.AudioConnectionFactory
import org.wycliffeassociates.otter.jvm.workbookapp.SnackbarHandler
import org.wycliffeassociates.otter.jvm.workbookapp.di.DaggerAppDependencyGraph
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import org.wycliffeassociates.otter.jvm.workbookapp.logging.ConfigureLogger
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.RootView
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.dialogs.SplashScreen
import tornadofx.*
import tornadofx.FX.Companion.messages
import javax.inject.Inject

class OtterApp : App(RootView::class), IDependencyGraphProvider {
    override val dependencyGraph = DaggerAppDependencyGraph.builder().build()
    var shouldBlockWindowCloseRequest = false

    @Inject lateinit var localeLanguage: LocaleLanguage
    @Inject lateinit var directoryProvider: IDirectoryProvider
    @Inject lateinit var audioConnectionFactory: AudioConnectionFactory

    init {
        dependencyGraph.inject(this)
        directoryProvider.cleanTempDirectory()
        Thread.setDefaultUncaughtExceptionHandler(OtterExceptionHandler(directoryProvider, localeLanguage))
        initializeLogger(directoryProvider)
        initializeAppLocale()
    }

    fun initializeLogger(directoryProvider: IDirectoryProvider) {
        ConfigureLogger(
            directoryProvider.logsDirectory
        ).configure()
    }

    fun initializeAppLocale() {
        FX.locale = localeLanguage.preferredLocale()
    }

    override fun start(stage: Stage) {
        super.start(stage)
        stage.isMaximized = true
        stage.scene.window.setOnCloseRequest {
            if (shouldBlockWindowCloseRequest) {
                it.consume()
                SnackbarHandler.enqueue(messages["applicationCloseBlocked"])
            } else {
                fire(AppCloseRequestEvent)
                audioConnectionFactory.releasePlayer()
            }
        }
        stage.scene.addEventHandler(KeyEvent.KEY_PRESSED) {
            if (it.code == KeyCode.ENTER) {
                if (stage.scene?.focusOwner is ButtonBase) {
                    (stage.scene?.focusOwner as? ButtonBase)?.fire()
                }
            }
        }
        find<SplashScreen>().openModal(StageStyle.UNDECORATED)
    }

    override fun onBeforeShow(view: UIComponent) {
        // Configure Snackbar Handler to display received snackbars on the root window
        SnackbarHandler.setWindowRoot(view.root as Pane)
    }

    override fun shouldShowPrimaryStage(): Boolean {
        return false
    }
}
