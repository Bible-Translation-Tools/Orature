/**
 * Copyright (C) 2020-2022 Wycliffe Associates
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
package integrationtest.di

import dagger.Component
import integrationtest.initialization.TestInitializeProjects
import integrationtest.initialization.TestInitializeUlb
import integrationtest.projects.*
import org.wycliffeassociates.otter.jvm.workbookapp.di.AppDependencyGraph
import org.wycliffeassociates.otter.jvm.workbookapp.di.modules.AppDatabaseModule
import org.wycliffeassociates.otter.jvm.workbookapp.di.modules.AppPreferencesModule
import org.wycliffeassociates.otter.jvm.workbookapp.di.modules.AppRepositoriesModule
import org.wycliffeassociates.otter.jvm.workbookapp.di.modules.ZipEntryTreeBuilderModule
import javax.inject.Singleton

@Component(
    modules = [
        TestAudioModule::class,
        AppDatabaseModule::class,
        AppPreferencesModule::class,
        TestDirectoryProviderModule::class,
        AppRepositoriesModule::class,
        ZipEntryTreeBuilderModule::class
    ]
)
@Singleton
interface TestPersistenceComponent : AppDependencyGraph {
    fun inject(test: TestInitializeUlb)
    fun inject(test: TestInitializeProjects)
    fun inject(test: TestProjectCreate)
    fun inject(test: TestRcImport)
    fun inject(test: TestProjectImport)
    fun inject(test: TestRemoveRc)
    fun inject(test: TestProjectExport)
}
