package org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.daos

import jooq.Tables.*
import org.jooq.Record
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.entities.*

typealias ContentEntityTable = jooq.tables.ContentEntity

class RecordMappers {
    companion object {
        fun mapToPreferencesEntity(record: Record): PreferenceEntity {
            return PreferenceEntity(
                record.getValue(PREFERENCES.KEY),
                record.getValue(PREFERENCES.VALUE)
            )
        }

        fun mapToLanguageEntity(record: Record): LanguageEntity {
            return LanguageEntity(
                record.getValue(LANGUAGE_ENTITY.ID),
                record.getValue(LANGUAGE_ENTITY.SLUG),
                record.getValue(LANGUAGE_ENTITY.NAME),
                record.getValue(LANGUAGE_ENTITY.ANGLICIZED),
                record.getValue(LANGUAGE_ENTITY.DIRECTION),
                record.getValue(LANGUAGE_ENTITY.GATEWAY),
                record.getValue(LANGUAGE_ENTITY.REGION)
            )
        }

        fun mapToResourceMetadataEntity(record: Record): ResourceMetadataEntity {
            return ResourceMetadataEntity(
                record.getValue(DUBLIN_CORE_ENTITY.ID),
                record.getValue(DUBLIN_CORE_ENTITY.CONFORMSTO),
                record.getValue(DUBLIN_CORE_ENTITY.CREATOR),
                record.getValue(DUBLIN_CORE_ENTITY.DESCRIPTION),
                record.getValue(DUBLIN_CORE_ENTITY.FORMAT),
                record.getValue(DUBLIN_CORE_ENTITY.IDENTIFIER),
                record.getValue(DUBLIN_CORE_ENTITY.ISSUED),
                record.getValue(DUBLIN_CORE_ENTITY.LANGUAGE_FK),
                record.getValue(DUBLIN_CORE_ENTITY.MODIFIED),
                record.getValue(DUBLIN_CORE_ENTITY.PUBLISHER),
                record.getValue(DUBLIN_CORE_ENTITY.SUBJECT),
                record.getValue(DUBLIN_CORE_ENTITY.TYPE),
                record.getValue(DUBLIN_CORE_ENTITY.TITLE),
                record.getValue(DUBLIN_CORE_ENTITY.VERSION),
                record.getValue(DUBLIN_CORE_ENTITY.PATH),
                record.getValue(DUBLIN_CORE_ENTITY.DERIVEDFROM_FK)
            )
        }

        fun mapToCollectionEntity(record: Record): CollectionEntity {
            return CollectionEntity(
                record.getValue(COLLECTION_ENTITY.ID),
                record.getValue(COLLECTION_ENTITY.PARENT_FK),
                record.getValue(COLLECTION_ENTITY.SOURCE_FK),
                record.getValue(COLLECTION_ENTITY.LABEL),
                record.getValue(COLLECTION_ENTITY.TITLE),
                record.getValue(COLLECTION_ENTITY.SLUG),
                record.getValue(COLLECTION_ENTITY.SORT),
                record.getValue(COLLECTION_ENTITY.DUBLIN_CORE_FK)
            )
        }

        fun mapToContentEntity(record: Record, table: ContentEntityTable = CONTENT_ENTITY): ContentEntity {
            return ContentEntity(
                record.getValue(table.ID),
                record.getValue(table.SORT),
                record.getValue(table.LABEL),
                record.getValue(table.START),
                record.getValue(table.COLLECTION_FK),
                record.getValue(table.SELECTED_TAKE_FK),
                record.getValue(table.TEXT),
                record.getValue(table.FORMAT),
                record.getValue(table.TYPE_FK)
            )
        }

        fun mapToResourceLinkEntity(record: Record): ResourceLinkEntity {
            return ResourceLinkEntity(
                record.getValue(RESOURCE_LINK.ID),
                record.getValue(RESOURCE_LINK.RESOURCE_CONTENT_FK),
                record.getValue(RESOURCE_LINK.CONTENT_FK),
                record.getValue(RESOURCE_LINK.COLLECTION_FK),
                record.getValue(RESOURCE_LINK.DUBLIN_CORE_FK)
            )
        }

        fun mapToTakeEntity(record: Record): TakeEntity {
            return TakeEntity(
                record.getValue(TAKE_ENTITY.ID),
                record.getValue(TAKE_ENTITY.CONTENT_FK),
                record.getValue(TAKE_ENTITY.FILENAME),
                record.getValue(TAKE_ENTITY.PATH),
                record.getValue(TAKE_ENTITY.NUMBER),
                record.getValue(TAKE_ENTITY.CREATED_TS),
                record.getValue(TAKE_ENTITY.DELETED_TS),
                record.getValue(TAKE_ENTITY.PLAYED)
            )
        }

        fun mapToMarkerEntity(record: Record): MarkerEntity {
            return MarkerEntity(
                record.getValue(MARKER_ENTITY.ID),
                record.getValue(MARKER_ENTITY.TAKE_FK),
                record.getValue(MARKER_ENTITY.NUMBER),
                record.getValue(MARKER_ENTITY.POSITION),
                record.getValue(MARKER_ENTITY.LABEL)
            )
        }

        fun mapToAudioPluginEntity(record: Record): AudioPluginEntity {
            return AudioPluginEntity(
                record.getValue(AUDIO_PLUGIN_ENTITY.ID),
                record.getValue(AUDIO_PLUGIN_ENTITY.NAME),
                record.getValue(AUDIO_PLUGIN_ENTITY.VERSION),
                record.getValue(AUDIO_PLUGIN_ENTITY.BIN),
                record.getValue(AUDIO_PLUGIN_ENTITY.ARGS),
                record.getValue(AUDIO_PLUGIN_ENTITY.EDIT),
                record.getValue(AUDIO_PLUGIN_ENTITY.RECORD),
                record.getValue(AUDIO_PLUGIN_ENTITY.MARK),
                record.getValue(AUDIO_PLUGIN_ENTITY.PATH)
            )
        }

        fun mapToTranslationEntity(record: Record): TranslationEntity {
            return TranslationEntity(
                record.getValue(TRANSLATION_ENTITY.ID),
                record.getValue(TRANSLATION_ENTITY.SOURCE_FK),
                record.getValue(TRANSLATION_ENTITY.TARGET_FK)
            )
        }
    }
}
