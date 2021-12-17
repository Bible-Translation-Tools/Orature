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

import java.lang.IllegalArgumentException
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.jvm.device.audio.AudioConnectionFactory
import org.wycliffeassociates.otter.jvm.device.audio.AudioErrorType
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import tornadofx.*
import javax.inject.Inject
import javax.sound.sampled.LineUnavailableException

class RootViewModel : ViewModel() {

    val logger = LoggerFactory.getLogger(RootViewModel::class.java)

    val pluginOpenedProperty = SimpleBooleanProperty(false)
    val drawerOpenedProperty = SimpleBooleanProperty(false)

    val showAudioErrorDialogProperty = SimpleBooleanProperty(false)
    var audioErrorType = SimpleObjectProperty<AudioErrorType>()

    @Inject
    lateinit var audioConnectionFactory: AudioConnectionFactory

    init {
        (app as IDependencyGraphProvider).dependencyGraph.inject(this)
        initializeAudioErrorListener()
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
}
