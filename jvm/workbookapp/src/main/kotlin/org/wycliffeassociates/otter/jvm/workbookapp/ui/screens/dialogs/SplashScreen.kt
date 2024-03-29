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
package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.dialogs

import com.github.thomasnield.rxkotlinfx.observeOnFx
import javafx.geometry.Pos
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import org.wycliffeassociates.otter.jvm.workbookapp.ui.NavigationMediator
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.drawer.ThemeColorEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.HomePage2
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.SettingsViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.SplashScreenViewModel
import tornadofx.*

class SplashScreen : View() {
    private val viewModel: SplashScreenViewModel by inject()
    private val settingsViewModel: SettingsViewModel by inject()
    private val navigator: NavigationMediator by inject()

    override val root = stackpane {
        addClass("splash__root")
        add(resources.imageview("/orature_splash.png"))
        vbox {
            addClass("splash__status")
            alignment = Pos.BOTTOM_CENTER

            progressbar(viewModel.progressProperty) {
                addClass("splash__progress")
                fitToParentWidth()
            }
            vbox {
                addClass("splash__text-block")
                alignment = Pos.CENTER
                label(viewModel.progressTitleProperty) {
                    addClass("splash__status-title")
                }
                label(viewModel.progressBodyProperty) {
                    addClass("splash__status-text")
                }
            }
        }
    }

    init {
        tryImportStylesheet(resources["/css/common.css"])
        tryImportStylesheet(resources["/css/splash-screen.css"])

        viewModel
            .initApp()
            .subscribe(
                { finish() },
                { finish() }
            )
    }

    private fun finish() {
        viewModel.initAudioSystem()
        viewModel.theme.preferredTheme
            .observeOnFx()
            .doFinally {
                close()
                settingsViewModel.setAppOrientation()
                primaryStage.show()
                navigator.dock<HomePage2>()
            }
            .observeOnFx()
            .subscribe { theme ->
                fire(ThemeColorEvent(this::class, theme))
            }
    }
}
