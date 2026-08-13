package org.bibletranslationtools.kotlinscripturealignment.model

import com.fasterxml.jackson.annotation.JsonProperty
import org.bibletranslationtools.kotlinscripturealignment.model.Documents

data class Group(
    @JsonProperty("documents")
    val documents: Documents? = null,
    @JsonProperty("records")
    val records: List<Record> = listOf()
)
