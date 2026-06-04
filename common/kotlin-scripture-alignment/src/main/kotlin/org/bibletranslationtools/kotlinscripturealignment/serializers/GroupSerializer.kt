package org.bibletranslationtools.kotlinscripturealignment.serializers

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.bibletranslationtools.kotlinscripturealignment.model.Group
import org.bibletranslationtools.kotlinscripturealignment.model.Documents

class GroupSerializer @JvmOverloads constructor(t: Class<Group>? = null) : StdSerializer<Group>(t) {
    override fun serialize(value: Group?, gen: JsonGenerator, provider: SerializerProvider) {
        if (value == null) {
            gen.writeNull()
            return
        }

        gen.writeStartObject()
        if (value.documents != null) {
            gen.writeFieldName("documents")
            gen.writeObject(value.documents)
        }
        gen.writeFieldName("records")
        gen.writeObject(value.records)
        gen.writeEndObject()
    }
}
