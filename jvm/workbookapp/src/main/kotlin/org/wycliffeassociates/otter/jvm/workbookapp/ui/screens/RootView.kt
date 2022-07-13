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
package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens

import javafx.application.Platform
import javafx.scene.layout.Priority
import org.wycliffeassociates.otter.common.data.ColorTheme
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import org.wycliffeassociates.otter.jvm.workbookapp.plugin.PluginClosedEvent
import org.wycliffeassociates.otter.jvm.workbookapp.plugin.PluginOpenedEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.OtterApp
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.AppBar
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.audioerrordialog
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.RootViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.SettingsViewModel
import tornadofx.*

class RootView : View() {

    private val viewModel: RootViewModel by inject()
    private val settingsViewModel: SettingsViewModel by inject()

    init {
        // Configure the Workspace: sets up the window menu and external app open events

        // Plugins being opened should block the app from closing as this could result in a
        // loss of communication between the app and the external plugin, thus data loss
        workspace.subscribe<PluginOpenedEvent> {
            (app as OtterApp).shouldBlockWindowCloseRequest = !it.isNative
            viewModel.pluginOpenedProperty.set(true)
        }
        workspace.subscribe<PluginClosedEvent> {
            (app as OtterApp).shouldBlockWindowCloseRequest = false
            viewModel.pluginOpenedProperty.set(false)
        }
        workspace.header.removeFromParent()
        workspace.root.vgrow = Priority.ALWAYS

        tryImportStylesheet(resources.get("/css/audio-error-dialog.css"))
        initThemeStylesheets()
        bindThemeClassToRoot()

        initAudioErrorDialog()
    }

    override val root = stackpane {
        prefWidth = 800.0
        prefHeight = 600.0

        nodeOrientationProperty().bind(settingsViewModel.orientationProperty)

        borderpane {
            left<AppBar>()
            center<AppContent>()
        }
    }

    private fun initThemeStylesheets() {
        tryImportStylesheet(resources["/css/theme/light-theme.css"])
        tryImportStylesheet(resources["/css/theme/dark-theme.css"])
    }

    private fun initAudioErrorDialog() {
        val errorDialog = audioerrordialog {
            titleTextProperty.set(messages["error"])
            inputMessageTitleTextProperty.set(messages["unableToRecord"])
            inputMessageTextProperty.set(messages["audioErrorMessage"])

            outputMessageTitleTextProperty.set(messages["unableToPlaySound"])
            outputMessageTextProperty.set(messages["audioErrorMessage"])

            backgroundImageProperty.set(resources.image("/images/audio_error.png"))
            cancelButtonTextProperty.set(messages["close"])
            orientationProperty.bind(settingsViewModel.orientationProperty)

            errorTypeProperty.bind(viewModel.audioErrorType)

            onCloseAction { viewModel.showAudioErrorDialogProperty.set(false) }
            onCancelAction { viewModel.showAudioErrorDialogProperty.set(false) }
        }

        viewModel.showAudioErrorDialogProperty.onChangeAndDoNow {
            Platform.runLater { if (it!!) errorDialog.open() else errorDialog.close() }
        }
    }

    private fun bindThemeClassToRoot() {
        settingsViewModel.appColorMode.onChange {
            when (it) {
                ColorTheme.LIGHT -> {
                    root.addClass("light-theme")
                    root.removeClass("dark-theme")
                }
                ColorTheme.DARK -> {
                    root.addClass("dark-theme")
                    root.removeClass("light-theme")
                }
            }
        }
    }
}
