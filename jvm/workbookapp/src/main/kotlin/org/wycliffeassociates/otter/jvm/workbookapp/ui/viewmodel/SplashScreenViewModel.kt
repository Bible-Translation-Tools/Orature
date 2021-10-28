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

import com.github.thomasnield.rxkotlinfx.observeOnFx
import com.jthemedetecor.OsThemeDetector
import io.reactivex.Observable
import javafx.beans.property.SimpleDoubleProperty
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.assets.initialization.InitializeApp
import org.wycliffeassociates.otter.common.data.ColorTheme
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import tornadofx.*
import javax.inject.Inject
import org.wycliffeassociates.otter.common.domain.theme.AppTheme
import org.wycliffeassociates.otter.jvm.device.ConfigureAudioSystem

class SplashScreenViewModel : ViewModel() {
    private val logger = LoggerFactory.getLogger(SplashScreenViewModel::class.java)

    @Inject
    lateinit var initApp: InitializeApp

    @Inject
    lateinit var configureAudioSystem: ConfigureAudioSystem

    @Inject
    lateinit var theme: AppTheme

    val progressProperty = SimpleDoubleProperty(0.0)

    fun initApp(): Observable<Double> {
        (app as IDependencyGraphProvider).dependencyGraph.inject(this)


        return initApp.initApp()
            .observeOnFx()
            .doOnError { logger.error("Error initializing app: ", it) }
            .map {
                progressProperty.value = it
                it
            }
    }

    fun initAudioSystem() {
        configureAudioSystem.configure()
    }
}
