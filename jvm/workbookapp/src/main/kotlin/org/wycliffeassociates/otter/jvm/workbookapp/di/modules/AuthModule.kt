package org.wycliffeassociates.otter.jvm.workbookapp.di.modules

import dagger.Module
import dagger.Provides
import org.wycliffeassociates.otter.common.domain.resourcecontainer.burrito.auth.IdAuthorityProvider
import org.wycliffeassociates.otter.jvm.workbookapp.domain.resourcecontainer.burrito.auth.WacsIdAuthority

@Module
class AuthModule {
    @Provides
    fun providesWacsAuth(): IdAuthorityProvider = WacsIdAuthority()
}
