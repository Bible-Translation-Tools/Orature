package org.wycliffeassociates.otter.jvm.app.ui.inject

import org.wycliffeassociates.otter.jvm.device.audio.injection.DaggerAudioComponent
import org.wycliffeassociates.otter.jvm.device.audioplugin.injection.DaggerAudioPluginComponent
import org.wycliffeassociates.otter.jvm.domain.resourcecontainer.project.ZipEntryTreeBuilder
import org.wycliffeassociates.otter.jvm.persistence.injection.DaggerPersistenceComponent
import org.wycliffeassociates.otter.jvm.persistence.repositories.*
import org.wycliffeassociates.otter.jvm.persistence.repositories.mapping.LanguageMapper
import tornadofx.Component
import tornadofx.ScopedInstance

class Injector : Component(), ScopedInstance {
    private val persistenceComponent = DaggerPersistenceComponent.builder().build()
    private val database = persistenceComponent.injectDatabase()
    private val audioComponent = DaggerAudioComponent
            .builder()
            .build()
    private val audioPluginComponent = DaggerAudioPluginComponent
            .builder()
            .build()

    val directoryProvider = persistenceComponent.injectDirectoryProvider()

    val preferences = persistenceComponent.injectPreferences()

    val languageRepo = LanguageRepository(database, LanguageMapper())
    val collectionRepo = CollectionRepository(database, directoryProvider)
    val contentRepository = ContentRepository(database)
    val resourceRepository = ResourceRepository(database)
    val resourceContainerRepository = ResourceContainerRepository(database, collectionRepo, resourceRepository)
    val takeRepository = TakeRepository(database)
    val pluginRepository = AudioPluginRepository(database, preferences)
    val workbookRepository = WorkbookRepository(collectionRepo, contentRepository, resourceRepository, takeRepository)

    val audioPlayer
        get() = audioComponent.injectPlayer()

    val audioPluginRegistrar = audioPluginComponent.injectRegistrar()

    val zipEntryTreeBuilder = ZipEntryTreeBuilder
}
