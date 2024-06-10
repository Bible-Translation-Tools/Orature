package org.wycliffeassociates.otter.common.domain.resourcecontainer.burrito.auth

import org.bibletranslationtools.scriptureburrito.IdAuthoritiesSchema
import org.bibletranslationtools.scriptureburrito.IdentificationSchema

interface IdAuthorityProvider {
    fun createIdAuthority(): IdAuthoritiesSchema

}

interface IdentificationProvider {
    fun createIdentification(): IdentificationSchema
}

interface AuthProvider: IdAuthorityProvider, IdentificationProvider