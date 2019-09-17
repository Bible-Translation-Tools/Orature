package org.wycliffeassociates.otter.jvm.app.ui.inject

import org.wycliffeassociates.otter.common.persistence.repositories.WorkbookRepository
import org.wycliffeassociates.otter.jvm.device.audio.injection.AudioComponent
import org.wycliffeassociates.otter.jvm.device.audio.injection.DaggerAudioComponent
import org.wycliffeassociates.otter.jvm.device.audioplugin.injection.AudioPluginComponent
import org.wycliffeassociates.otter.jvm.device.audioplugin.injection.DaggerAudioPluginComponent
import org.wycliffeassociates.otter.jvm.domain.resourcecontainer.project.ZipEntryTreeBuilder
import org.wycliffeassociates.otter.jvm.persistence.injection.DaggerPersistenceComponent
import org.wycliffeassociates.otter.jvm.persistence.injection.PersistenceComponent
import org.wycliffeassociates.otter.jvm.persistence.repositories.*
import org.wycliffeassociates.otter.jvm.persistence.repositories.mapping.LanguageMapper
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
    val takeRepository = TakeRepository(database)
    val pluginRepository = AudioPluginRepository(database, preferences)
    val workbookRepository = WorkbookRepository(collectionRepo, contentRepository, resourceRepository, takeRepository)

    val audioPlayer
        get() = audioComponent.injectPlayer()

    val audioPluginRegistrar = audioPluginComponent.injectRegistrar()

    val zipEntryTreeBuilder = ZipEntryTreeBuilder
}
