package org.wycliffeassociates.otter.jvm.workbookapp.di.modules

import dagger.Binds
import dagger.Module
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.IZipEntryTreeBuilder
import org.wycliffeassociates.otter.jvm.workbookapp.domain.resourcecontainer.project.ZipEntryTreeBuilder

@Module
abstract class ZipEntryTreeBuilderModule {
    @Binds
    abstract fun providesZipEntryTreeBuilder(treeBuilder: ZipEntryTreeBuilder): IZipEntryTreeBuilder
}
