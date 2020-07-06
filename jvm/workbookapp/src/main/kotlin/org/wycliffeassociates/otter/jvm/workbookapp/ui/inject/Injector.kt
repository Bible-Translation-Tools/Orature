package org.wycliffeassociates.otter.jvm.workbookapp.ui.inject

import org.wycliffeassociates.otter.common.persistence.repositories.WorkbookRepository
import org.wycliffeassociates.otter.jvm.workbookapp.di.audio.AudioComponent
import org.wycliffeassociates.otter.jvm.workbookapp.di.audio.DaggerAudioComponent
import org.wycliffeassociates.otter.jvm.workbookapp.di.audioplugin.AudioPluginComponent
import org.wycliffeassociates.otter.jvm.workbookapp.di.audioplugin.DaggerAudioPluginComponent
import org.wycliffeassociates.otter.jvm.workbookapp.di.persistence.DaggerPersistenceComponent
import org.wycliffeassociates.otter.jvm.workbookapp.di.persistence.PersistenceComponent
import org.wycliffeassociates.otter.jvm.workbookapp.domain.resourcecontainer.project.ZipEntryTreeBuilder
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories.*
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories.mapping.LanguageMapper
import tornadofx.*

class Injector(
    private val audioComponent: AudioComponent = DaggerAudioComponent.builder().build(),
    audioPluginComponent: AudioPluginComponent = DaggerAudioPluginComponent.builder().build(),
    persistenceComponent: PersistenceComponent = DaggerPersistenceComponent.builder().build()
) : Component(), ScopedInstance {
    private val database = persistenceComponent.injectDatabase()

    val directoryProvider = persistenceComponent.injectDirectoryProvider()

    val preferences = persistenceComponent.injectPreferences()

    val languageRepo = LanguageRepository(database, LanguageMapper())
    val collectionRepo = CollectionRepository(database, directoryProvider)
    val contentRepository = ContentRepository(database)
    val resourceRepository = ResourceRepository(database)
    val resourceContainerRepository = ResourceContainerRepository(database, collectionRepo, resourceRepository)
    val resourceMetadataRepository = ResourceMetadataRepository(database)
    val takeRepository = TakeRepository(database)
    val pluginRepository = AudioPluginRepository(database, preferences)
    val workbookRepository = WorkbookRepository(
        collectionRepo,
        contentRepository,
        resourceRepository,
        resourceMetadataRepository,
        takeRepository
    )
    val installedEntityRepository = InstalledEntityRepository(database)

    val audioPlayer
        get() = audioComponent.injectPlayer()

    val audioPluginRegistrar = audioPluginComponent.injectRegistrar()

    val zipEntryTreeBuilder = ZipEntryTreeBuilder
}
