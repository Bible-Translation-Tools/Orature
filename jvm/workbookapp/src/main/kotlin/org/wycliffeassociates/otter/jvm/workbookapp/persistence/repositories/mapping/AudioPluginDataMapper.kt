package org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories.mapping

import org.wycliffeassociates.otter.common.data.config.AudioPluginData
import org.wycliffeassociates.otter.common.persistence.mapping.Mapper
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.entities.AudioPluginEntity
import java.io.File

class AudioPluginDataMapper : Mapper<AudioPluginEntity, AudioPluginData> {

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
