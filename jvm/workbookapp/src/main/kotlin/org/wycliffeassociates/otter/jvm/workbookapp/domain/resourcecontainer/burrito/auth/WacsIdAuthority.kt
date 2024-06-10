package org.wycliffeassociates.otter.jvm.workbookapp.domain.resourcecontainer.burrito.auth

import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import org.bibletranslationtools.scriptureburrito.IdAuthoritiesSchema
import org.bibletranslationtools.scriptureburrito.IdAuthority
import org.bibletranslationtools.scriptureburrito.IdentificationSchema
import org.bibletranslationtools.scriptureburrito.PrimaryIdentification
import org.wycliffeassociates.otter.common.domain.resourcecontainer.burrito.auth.AuthProvider
import javax.inject.Inject

class WacsIdAuthority @Inject constructor():
    AuthProvider
{
    override fun createIdAuthority(): IdAuthoritiesSchema {
        val authority = IdAuthoritiesSchema()
        val wacs = IdAuthority()
        wacs.name = hashMapOf("en" to "Wycliffe Associates Content Service")
        wacs.id = "https://content.bibletranslationtools.org/"
        authority["wacs"] = wacs
        return authority
    }

    override fun createIdentification(): IdentificationSchema {
        return IdentificationSchema().apply {
            primary = PrimaryIdentification().apply {
                this["id"] = ObjectNode(JsonNodeFactory.instance)
            }
            name = hashMapOf("en" to "Wycliffe Associates Content Service")
        }
    }
}