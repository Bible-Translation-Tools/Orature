package org.wycliffeassociates.otter.jvm.persistence.injection

import dagger.Component
import org.wycliffeassociates.otter.common.data.persistence.AppDatabase
import org.wycliffeassociates.otter.common.data.persistence.AppPreferences
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import javax.inject.Singleton

@Component(modules = [PersistenceModule::class])
@Singleton
interface PersistenceComponent {
    fun injectDatabase(): AppDatabase
    fun injectPreferences(): AppPreferences
    fun injectDirectoryProvider(): IDirectoryProvider
}