package org.wycliffeassociates.otter.jvm.workbookapp.domain.resourcecontainer.burrito.auth

import org.bibletranslationtools.scriptureburrito.IdAuthoritiesSchema
import org.bibletranslationtools.scriptureburrito.IdAuthority
import org.wycliffeassociates.otter.common.domain.resourcecontainer.burrito.auth.IdAuthorityProvider
import javax.inject.Inject

class WacsIdAuthority @Inject constructor(): IdAuthorityProvider {
    override fun createIdAuthority(): IdAuthoritiesSchema {
        val authority = IdAuthoritiesSchema()
        val wacs = IdAuthority()
        wacs.name = hashMapOf("en" to "Wycliffe Associates Content Service ")
        wacs.id = "https://content.bibletranslationtools.org/"
        authority["wacs"] = wacs
        return authority
    }
}