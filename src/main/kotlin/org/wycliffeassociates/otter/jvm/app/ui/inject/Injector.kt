package org.wycliffeassociates.otter.jvm.app.ui.inject

import org.wycliffeassociates.otter.jvm.device.audio.injection.DaggerAudioComponent
import org.wycliffeassociates.otter.jvm.persistence.injection.DaggerPersistenceComponent
import org.wycliffeassociates.otter.jvm.persistence.repositories.*
import org.wycliffeassociates.otter.jvm.persistence.repositories.mapping.CollectionMapper
import org.wycliffeassociates.otter.jvm.persistence.repositories.mapping.LanguageMapper
import org.wycliffeassociates.otter.jvm.persistence.repositories.mapping.ResourceMetadataMapper

object Injector {
    private val persistenceComponent = DaggerPersistenceComponent.builder().build()
    private val database = persistenceComponent.injectDatabase()

    val directoryProvider = persistenceComponent.injectDirectoryProvider()
    val resourceContainerDirectory = directoryProvider.resourceContainerDirectory
    val languageRepo = LanguageRepository(database, LanguageMapper())
    val collectionRepo = CollectionRepository(database, CollectionMapper(), ResourceMetadataMapper(), LanguageMapper())
    val metadataRepo = ResourceMetadataRepository(database, ResourceMetadataMapper(), LanguageMapper())
    val sourceRepo = SourceRepository(database)
    val projectRepo = ProjectRepository(database)
    val chunkRepository = ChunkRepository(database)
    val takeRepository = TakeRepository(database)
    val pluginRepository = AudioPluginRepository(database)

    private val audioComponent = DaggerAudioComponent
            .builder()
            .build()

    val audioPlayer
        get() = audioComponent.injectPlayer()
}
