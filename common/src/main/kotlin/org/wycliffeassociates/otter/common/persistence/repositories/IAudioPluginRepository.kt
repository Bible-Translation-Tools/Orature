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
package org.wycliffeassociates.otter.common.persistence.repositories

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import org.wycliffeassociates.otter.common.domain.plugins.AudioPluginData
import org.wycliffeassociates.otter.common.domain.plugins.IAudioPlugin

enum class PluginType {
    MARKER,
    RECORDER,
    EDITOR
}

interface IAudioPluginRepository : IRepository<AudioPluginData> {
    fun insert(data: AudioPluginData): Single<Int>
    fun getAllPlugins(): Single<List<IAudioPlugin>>
    fun getPlugin(type: PluginType): Maybe<IAudioPlugin>
    fun getPluginData(type: PluginType): Maybe<AudioPluginData>
    fun setPluginData(type: PluginType, default: AudioPluginData): Completable
    fun initSelected(): Completable
}
