package org.wycliffeassociates.otter.jvm.workbookapp.di.modules

import dagger.Binds
import dagger.Module
import org.wycliffeassociates.otter.common.domain.brightcove.BrightcoveClient
import org.wycliffeassociates.otter.common.domain.brightcove.BrightcoveClientImpl
import org.wycliffeassociates.otter.common.domain.brightcove.BrightcoveConfigProvider
import org.wycliffeassociates.otter.common.domain.brightcove.BrightcoveConfigProviderImpl
import org.wycliffeassociates.otter.common.domain.brightcove.CountryInfoResolver
import org.wycliffeassociates.otter.common.domain.brightcove.NullCountryInfoResolver
import org.wycliffeassociates.otter.common.domain.brightcove.OratureVerseTimingProvider
import org.wycliffeassociates.otter.common.domain.brightcove.VerseTimingProvider

@Module
abstract class BrightcoveModule {
    @Binds
    abstract fun bindBrightcoveClient(impl: BrightcoveClientImpl): BrightcoveClient

    @Binds
    abstract fun bindBrightcoveConfigProvider(impl: BrightcoveConfigProviderImpl): BrightcoveConfigProvider

    @Binds
    abstract fun bindCountryInfoResolver(impl: NullCountryInfoResolver): CountryInfoResolver

    @Binds
    abstract fun bindVerseTimingProvider(impl: OratureVerseTimingProvider): VerseTimingProvider
}
