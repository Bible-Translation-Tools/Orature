package org.bibletranslationtools.kotlinscripturealignment.serializers

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.bibletranslationtools.kotlinscripturealignment.model.Record

class RecordSerializer @JvmOverloads constructor(t: Class<Record>? = null) : StdSerializer<Record>(t) {
    override fun serialize(value: Record?, gen: JsonGenerator, provider: SerializerProvider) {
        if (value == null) {
            gen.writeNull()
            return
        }

        gen.writeStartObject()
        if (value.timecode != null && value.timecode.isNotEmpty()) {
            gen.writeFieldName("timecode")
            gen.writeObject(value.timecode)
        }
        if (value.textReference != null && value.textReference.isNotEmpty()) {
            gen.writeFieldName("text-reference")
            gen.writeObject(value.textReference)
        }
        if (value.references.isNotEmpty()) {
            gen.writeFieldName("references")
            gen.writeObject(value.references)
        }
        gen.writeEndObject()
    }
}
