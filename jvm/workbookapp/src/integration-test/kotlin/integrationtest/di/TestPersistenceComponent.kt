/**
 * Copyright (C) 2020-2024 Wycliffe Associates
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

import integrationtest.projects.importer.MergeMediaTest
import dagger.Component
import integrationtest.initialization.TestInitializeProjects
import integrationtest.initialization.TestInitializeSources
import integrationtest.initialization.TestInitializeUlb
import integrationtest.projects.TestProjectCreate
import integrationtest.projects.TestProjectImport
import integrationtest.projects.TestRcImport
import integrationtest.projects.TestRemoveRc
import integrationtest.projects.TestSideloadSourceProject
import integrationtest.projects.export.TestBackupProjectExporter
import integrationtest.projects.importer.TestExistingSourceImporter
import integrationtest.projects.export.TestSourceProjectExporter
import integrationtest.projects.importer.TestAudioProjectExporter
import integrationtest.projects.importer.TestOngoingProjectImporter
import integrationtest.projects.importer.TestRCImporterFactory
import org.wycliffeassociates.otter.jvm.workbookapp.di.AppDependencyGraph
import org.wycliffeassociates.otter.jvm.workbookapp.di.modules.AppDatabaseModule
import org.wycliffeassociates.otter.jvm.workbookapp.di.modules.AppPreferencesModule
import org.wycliffeassociates.otter.jvm.workbookapp.di.modules.ZipEntryTreeBuilderModule
import javax.inject.Singleton

@Component(
    modules = [
        TestAudioModule::class,
        AppDatabaseModule::class,
        AppPreferencesModule::class,
        TestDirectoryProviderModule::class,
        TestRepositoriesModule::class,
        ZipEntryTreeBuilderModule::class,
    ]
)
@Singleton
interface TestPersistenceComponent : AppDependencyGraph {
    fun inject(test: TestInitializeSources)
    fun inject(test: TestInitializeUlb)
    fun inject(test: TestInitializeProjects)
    fun inject(test: TestProjectCreate)
    fun inject(test: TestRcImport)
    fun inject(test: TestProjectImport)
    fun inject(test: TestExistingSourceImporter)
    fun inject(test: TestOngoingProjectImporter)
    fun inject(test: TestRCImporterFactory)
    fun inject(test: TestSideloadSourceProject)
    fun inject(test: TestRemoveRc)
    fun inject(test: TestSourceProjectExporter)
    fun inject(test: TestBackupProjectExporter)
    fun inject(test: TestAudioProjectExporter)
    fun inject(test: MergeMediaTest)
}
