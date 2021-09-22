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
package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.dialogs

import javafx.geometry.Pos
import org.wycliffeassociates.otter.jvm.workbookapp.ui.NavigationMediator
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.HomePage
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.SplashScreenViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.styles.SplashScreenStyles
import tornadofx.*

class SplashScreen : View() {
    private val viewModel: SplashScreenViewModel by inject()
    private val navigator: NavigationMediator by inject()

    override val root = stackpane {
        addStylesheet(SplashScreenStyles::class)
        addClass(SplashScreenStyles.splashRoot)
        alignment = Pos.TOP_CENTER
        add(resources.imageview("/orature_splash.png"))
        progressbar(viewModel.progressProperty) {
            addClass(SplashScreenStyles.splashProgress)
            prefWidth = 376.0
            translateY = 360.0
        }
    }

    init {
        viewModel
            .initApp()
            .subscribe(
                {},
                { finish() },
                { finish() }
            )
    }

    private fun finish() {
        viewModel.initAudioSystem()
        close()
        primaryStage.show()
        navigator.dock<HomePage>()
    }
}
