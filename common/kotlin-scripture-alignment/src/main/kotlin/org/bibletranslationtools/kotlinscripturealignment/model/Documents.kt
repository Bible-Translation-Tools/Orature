package org.bibletranslationtools.kotlinscripturealignment.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer

sealed interface Documents
data class DocumentsList(val list: List<DocumentReference>) : Documents
data class DocumentsMap(val map: Map<String, DocumentReference>) : Documents
