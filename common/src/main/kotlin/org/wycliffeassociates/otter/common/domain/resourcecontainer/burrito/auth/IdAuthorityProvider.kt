package org.wycliffeassociates.otter.common.domain.resourcecontainer.burrito.auth

import org.bibletranslationtools.scriptureburrito.IdAuthoritiesSchema

interface IdAuthorityProvider {
    fun createIdAuthority(): IdAuthoritiesSchema
}