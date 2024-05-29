package org.wycliffeassociates.otter.jvm.workbookapp.di.modules

import dagger.Module
import dagger.Provides
import org.wycliffeassociates.otter.common.data.IAppInfo
import org.wycliffeassociates.otter.jvm.workbookapp.ui.system.AppInfo

@Module
class MetadataModule {
    @Provides
    fun providesAppInfo(): IAppInfo = AppInfo()
}
