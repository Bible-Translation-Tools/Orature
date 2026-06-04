package org.wycliffeassociates.otter.jvm.workbookapp.di.modules

import dagger.Binds
import dagger.Module
import org.wycliffeassociates.otter.common.domain.brightcove.BrightcoveWorkerAuthService
import org.wycliffeassociates.otter.common.domain.brightcove.BrightcoveWorkerAuthServiceImpl
import org.wycliffeassociates.otter.common.domain.brightcove.BrightcoveWorkerAuthStore
import org.wycliffeassociates.otter.common.domain.brightcove.BrightcoveWorkerAuthStoreImpl
import org.wycliffeassociates.otter.common.domain.brightcove.BrightcoveWorkerClient
import org.wycliffeassociates.otter.common.domain.brightcove.BrightcoveWorkerClientImpl
import org.wycliffeassociates.otter.common.domain.brightcove.BrightcoveWorkerConfigProvider
import org.wycliffeassociates.otter.common.domain.brightcove.BrightcoveWorkerConfigProviderImpl
import org.wycliffeassociates.otter.common.domain.brightcove.CountryInfoResolver
import org.wycliffeassociates.otter.common.domain.brightcove.ExternalBrowserOpener
import org.wycliffeassociates.otter.common.domain.brightcove.NullCountryInfoResolver
import org.wycliffeassociates.otter.common.domain.brightcove.OratureVerseTimingProvider
import org.wycliffeassociates.otter.common.domain.brightcove.VerseTimingProvider
import org.wycliffeassociates.otter.jvm.workbookapp.brightcove.DesktopExternalBrowserOpener

@Module
abstract class BrightcoveModule {
    @Binds
    abstract fun bindBrightcoveWorkerClient(impl: BrightcoveWorkerClientImpl): BrightcoveWorkerClient

    @Binds
    abstract fun bindBrightcoveWorkerConfigProvider(impl: BrightcoveWorkerConfigProviderImpl): BrightcoveWorkerConfigProvider

    @Binds
    abstract fun bindBrightcoveWorkerAuthStore(impl: BrightcoveWorkerAuthStoreImpl): BrightcoveWorkerAuthStore

    @Binds
    abstract fun bindBrightcoveWorkerAuthService(impl: BrightcoveWorkerAuthServiceImpl): BrightcoveWorkerAuthService

    @Binds
    abstract fun bindExternalBrowserOpener(impl: DesktopExternalBrowserOpener): ExternalBrowserOpener

    @Binds
    abstract fun bindCountryInfoResolver(impl: NullCountryInfoResolver): CountryInfoResolver

    @Binds
    abstract fun bindVerseTimingProvider(impl: OratureVerseTimingProvider): VerseTimingProvider
}
