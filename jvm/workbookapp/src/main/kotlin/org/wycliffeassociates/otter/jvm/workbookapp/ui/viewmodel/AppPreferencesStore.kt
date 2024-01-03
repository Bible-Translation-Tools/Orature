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
package org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel

import javafx.beans.property.SimpleIntegerProperty
import org.wycliffeassociates.otter.common.persistence.repositories.IAppPreferencesRepository
import org.wycliffeassociates.otter.jvm.controls.media.PlaybackRateChangedEvent
import org.wycliffeassociates.otter.jvm.controls.media.PlaybackRateType
import org.wycliffeassociates.otter.jvm.controls.media.SourceTextZoomRateChangedEvent
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import tornadofx.Component
import tornadofx.ScopedInstance
import javax.inject.Inject

class AppPreferencesStore : Component(), ScopedInstance {

    @Inject
    lateinit var appPrefRepository: IAppPreferencesRepository

    val workbookDataStore: WorkbookDataStore by inject()

    val sourceTextZoomRateProperty = SimpleIntegerProperty()

    init {
        (app as IDependencyGraphProvider).dependencyGraph.inject(this)
        workspace.subscribe<SourceTextZoomRateChangedEvent> { event ->
            sourceTextZoomRateProperty.set(event.rate)
            appPrefRepository.setSourceTextZoomRate(event.rate).subscribe()
        }
        workspace.subscribe<PlaybackRateChangedEvent> { event ->
            updatePlaybackSpeedRate(event)
        }
    }

    private fun updatePlaybackSpeedRate(event: PlaybackRateChangedEvent) {
        when (event.type) {
            PlaybackRateType.SOURCE -> workbookDataStore.workbook.translation.updateSourceRate(event.rate)
            PlaybackRateType.TARGET -> workbookDataStore.workbook.translation.updateTargetRate(event.rate)
        }
    }
}