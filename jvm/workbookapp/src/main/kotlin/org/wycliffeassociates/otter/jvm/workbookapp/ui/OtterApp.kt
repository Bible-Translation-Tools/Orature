/**
 * Copyright (C) 2020-2024 Wycliffe Associates
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

import com.jfoenix.controls.JFXSnackbar
import com.jfoenix.controls.JFXSnackbarLayout
import javafx.scene.control.ButtonBase
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.Pane
import javafx.stage.Stage
import javafx.stage.StageStyle
import javafx.util.Duration
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.OratureInfo
import org.wycliffeassociates.otter.common.domain.languages.LocaleLanguage
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.jvm.controls.event.AppCloseRequestEvent
import org.wycliffeassociates.otter.jvm.device.audio.AudioConnectionFactory
import org.wycliffeassociates.otter.jvm.workbookapp.NOTIFICATION_DURATION_SEC
import org.wycliffeassociates.otter.jvm.workbookapp.SnackbarHandler
import org.wycliffeassociates.otter.jvm.workbookapp.SnackbarHandler.showNotification
import org.wycliffeassociates.otter.jvm.workbookapp.di.DaggerAppDependencyGraph
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import org.wycliffeassociates.otter.jvm.workbookapp.logging.ConfigureLogger
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.DirectoryProvider
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.DatabaseInitializer
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.RootView
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.dialogs.SplashScreen
import tornadofx.*
import tornadofx.FX.Companion.messages
import javax.inject.Inject

class OtterApp : App(RootView::class), IDependencyGraphProvider {
    override val dependencyGraph = DaggerAppDependencyGraph.builder().build()
    var shouldBlockWindowCloseRequest = false

    @Inject
    lateinit var localeLanguage: LocaleLanguage

    @Inject
    lateinit var directoryProvider: IDirectoryProvider

    @Inject
    lateinit var audioConnectionFactory: AudioConnectionFactory

    val logger: Logger

    private lateinit var snackBarRoot: Pane

    init {
        DatabaseInitializer(
            DirectoryProvider(OratureInfo.SUITE_NAME)
        ).initialize()
        dependencyGraph.inject(this)
        Thread.setDefaultUncaughtExceptionHandler(OtterExceptionHandler(directoryProvider, localeLanguage))
        initializeLogger(directoryProvider)
        initializeAppLocale()

        logger = LoggerFactory.getLogger(OtterApp::class.java)
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
        logSystemProperties()

        stage.isMaximized = true
        stage.scene.window.setOnCloseRequest {
            if (shouldBlockWindowCloseRequest) {
                it.consume()
                showNotification(messages["applicationCloseBlocked"], snackBarRoot)
            } else {
                fire(AppCloseRequestEvent)
                audioConnectionFactory.releasePlayer()
            }
        }
        stage.scene.addEventHandler(KeyEvent.KEY_PRESSED) {
            if (it.code == KeyCode.ENTER && stage.scene?.focusOwner is ButtonBase) {
                (stage.scene?.focusOwner as? ButtonBase)?.fire()
            }
        }
        find<SplashScreen>().openModal(StageStyle.UNDECORATED)
    }

    /**
     * Logs properties about the system running, such as the java version, OS, and javaFX.
     *
     * This needs to run after JavaFX is initialized as javafx.version is not added to the system properties prior
     * to launching the app class.
     */
    private fun logSystemProperties() {
        logger.info(
            """
            JDK distribution: ${System.getProperty("java.vendor")}
            Java version: ${System.getProperty("java.version")}
            JavaFX version: ${System.getProperty("javafx.version")}
            OS: ${System.getProperty("os.name")}
            OS arch: ${System.getProperty("os.arch")}
            OS version: ${System.getProperty("os.version")}
            """ 
        )
    }

    override fun onBeforeShow(view: UIComponent) {
        // Configure Snackbar Handler to display received snackbars on the root window
        snackBarRoot = view.root as Pane
    }

    override fun shouldShowPrimaryStage(): Boolean {
        return false
    }

    override fun stop() {
        super.stop()
        directoryProvider.cleanTempDirectory()
    }
}
