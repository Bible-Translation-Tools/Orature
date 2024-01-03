/**
 * Copyright (C) 2020-2023 Wycliffe Associates
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
import io.reactivex.Completable
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleStringProperty
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.assets.initialization.InitializeApp
import org.wycliffeassociates.otter.common.domain.theme.AppTheme
import org.wycliffeassociates.otter.jvm.device.ConfigureAudioSystem
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import tornadofx.*
import javax.inject.Inject

class SplashScreenViewModel : ViewModel() {
    private val logger = LoggerFactory.getLogger(SplashScreenViewModel::class.java)

    @Inject
    lateinit var initApp: InitializeApp

    @Inject
    lateinit var configureAudioSystem: ConfigureAudioSystem

    @Inject
    lateinit var theme: AppTheme

    val progressProperty = SimpleDoubleProperty(0.0)
    val progressTitleProperty = SimpleStringProperty()
    val progressBodyProperty = SimpleStringProperty()

    fun initApp(): Completable {
        (app as IDependencyGraphProvider).dependencyGraph.inject(this)

        return initApp.initApp()
            .doOnError { logger.error("Error initializing app: ", it) }
            .observeOnFx()
            .doOnNext { status ->
                status.titleKey?.let { title ->
                    progressTitleProperty.set(messages.format(title, status.titleMessage ?: ""))
                    progressBodyProperty.set(null)
                }
                status.subTitleKey?.let { body ->
                    progressBodyProperty.set(messages.format(body, status.subTitleMessage ?: ""))
                }
                status.percent?.let { progressProperty.set(it) }
            }
            .ignoreElements()
    }

    fun initAudioSystem() {
        configureAudioSystem.configure()
    }
}
