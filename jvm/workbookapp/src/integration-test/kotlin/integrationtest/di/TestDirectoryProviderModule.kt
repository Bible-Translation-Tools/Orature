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

import dagger.Module
import dagger.Provides
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.DirectoryProvider
import java.io.File

@Module
class TestDirectoryProviderModule {
    private val root = createTempDir("orature-test").also(File::deleteOnExit)

    @Provides
    fun providesDirectoryProvider(): IDirectoryProvider = DirectoryProvider(
        appName = "Orature",
        userHome = root.resolve("user").apply { mkdirs() }.path,
        windowsAppData = root.resolve("workbookapp").apply { mkdirs() }.path
    )
}
