package org.bibletranslationtools.kotlinscripturealignment.serializers

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.bibletranslationtools.kotlinscripturealignment.model.Documents
import org.bibletranslationtools.kotlinscripturealignment.model.DocumentsList
import org.bibletranslationtools.kotlinscripturealignment.model.DocumentsMap
import org.bibletranslationtools.kotlinscripturealignment.model.DocumentReference

class DocumentsSerializer @JvmOverloads constructor(t: Class<Documents>? = null) : StdSerializer<Documents>(t) {
    override fun serialize(value: Documents?, gen: JsonGenerator, provider: SerializerProvider) {
        if (value == null) {
            gen.writeNull()
            return
        }
        when (value) {
            is DocumentsList -> gen.writeObject(value.list)
            is DocumentsMap -> {
                gen.writeStartObject()
                value.map.forEach { (key, docRef) ->
                    gen.writeFieldName(key)
                    gen.writeStartObject()
                    gen.writeStringField("scheme", docRef.scheme)
                    if (docRef.docid != null) {
                        gen.writeStringField("docid", docRef.docid)
                    }
                    gen.writeEndObject()
                }
                gen.writeEndObject()
            }
            else -> throw IllegalArgumentException("Unknown Documents subtype: ${value::class.java.name}")
        }
    }
}
