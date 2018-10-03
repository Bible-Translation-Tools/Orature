package org.wycliffeassociates.otter.jvm.persistence.injection

import dagger.Component
import org.wycliffeassociates.otter.common.data.persistence.IAppPreferences
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.jvm.persistence.database.IAppDatabase
import javax.inject.Singleton

@Component(modules = [PersistenceModule::class])
@Singleton
interface PersistenceComponent {
    fun injectDatabase(): IAppDatabase
    fun injectPreferences(): IAppPreferences
    fun injectDirectoryProvider(): IDirectoryProvider
}