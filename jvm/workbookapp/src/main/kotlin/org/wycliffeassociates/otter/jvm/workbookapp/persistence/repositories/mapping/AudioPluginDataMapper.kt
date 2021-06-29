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
package org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories.mapping

import org.wycliffeassociates.otter.common.domain.plugins.AudioPluginData
import org.wycliffeassociates.otter.common.persistence.mapping.Mapper
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.entities.AudioPluginEntity
import java.io.File
import javax.inject.Inject

class AudioPluginDataMapper @Inject constructor() : Mapper<AudioPluginEntity, AudioPluginData> {

    override fun mapFromEntity(type: AudioPluginEntity): AudioPluginData {
        return AudioPluginData(
            type.id,
            type.name,
            type.version,
            type.edit == 1,
            type.record == 1,
            type.mark == 1,
            type.bin,
            listOf(type.args),
            type.path?.let { File(type.path) }
        )
    }

    override fun mapToEntity(type: AudioPluginData): AudioPluginEntity {
        return AudioPluginEntity(
            type.id,
            type.name,
            type.version,
            type.executable,
            type.args.firstOrNull() ?: "",
            if (type.canEdit) 1 else 0,
            if (type.canRecord) 1 else 0,
            if (type.canMark) 1 else 0,
            type.pluginFile?.toURI()?.path
        )
    }
}
