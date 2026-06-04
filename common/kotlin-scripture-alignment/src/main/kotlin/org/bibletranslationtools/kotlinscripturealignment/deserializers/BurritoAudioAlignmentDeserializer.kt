package org.bibletranslationtools.kotlinscripturealignment.deserializers

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import org.bibletranslationtools.kotlinscripturealignment.model.BurritoAudioAlignment
import org.bibletranslationtools.kotlinscripturealignment.model.Documents
import org.bibletranslationtools.kotlinscripturealignment.model.DocumentsList
import org.bibletranslationtools.kotlinscripturealignment.model.DocumentsMap
import org.bibletranslationtools.kotlinscripturealignment.model.DocumentReference
import org.bibletranslationtools.kotlinscripturealignment.model.FormatType
import org.bibletranslationtools.kotlinscripturealignment.model.Group
import org.bibletranslationtools.kotlinscripturealignment.model.Record

class BurritoAudioAlignmentDeserializer @JvmOverloads constructor(vc: Class<*>? = null) : StdDeserializer<BurritoAudioAlignment>(vc) {
    override fun deserialize(jp: com.fasterxml.jackson.core.JsonParser, ctxt: DeserializationContext): BurritoAudioAlignment? {
        val node: JsonNode = jp.codec.readTree(jp)

        val format = node.get("format")?.asText()?.let { FormatType.fromValue(it) } ?: FormatType.ALIGNMENT
        val version = node.get("version")?.asText() ?: "0.3"
        val type = node.get("type")?.asText() ?: throw MismatchedInputException.from(jp, BurritoAudioAlignment::class.java, "Missing required field: type")

        val documentsNode = node.get("documents")
        val documents: Documents? = if (documentsNode != null && !documentsNode.isNull) {
            if (documentsNode.isArray) {
                val listType = ctxt.typeFactory.constructCollectionType(List::class.java, DocumentReference::class.java)
                DocumentsList(jp.codec.readValue(documentsNode.traverse(), listType))
            } else if (documentsNode.isObject) {
                val mapEntries = mutableMapOf<String, DocumentReference>()
                documentsNode.fields().forEach { (key, valueNode) ->
                    mapEntries[key] = jp.codec.treeToValue(valueNode, DocumentReference::class.java)
                }
                DocumentsMap(mapEntries)
            } else {
                null
            }
        } else {
            null
        }

        val roles = node.get("roles")?.map { it.asText() }

        val recordsNode = node.get("records")
        val records: List<Record> = if (recordsNode != null && recordsNode.isArray) {
            val listType = ctxt.typeFactory.constructCollectionType(List::class.java, Record::class.java)
            jp.codec.readValue(recordsNode.traverse(), listType)
        } else {
            listOf()
        }

        val groupsNode = node.get("groups")
        val groups: List<Group>? = if (groupsNode != null && groupsNode.isArray) {
            val groupList = mutableListOf<Group>()
            groupsNode.forEach { groupNode ->
                // Manually deserialize each Group object within the groups array
                val groupDocumentsNode = groupNode.get("documents")
                val groupRecordsNode = groupNode.get("records")

                val groupDocuments: Documents? = if (groupDocumentsNode != null && !groupDocumentsNode.isNull) {
                    if (groupDocumentsNode.isArray) {
                        val listType = ctxt.typeFactory.constructCollectionType(List::class.java, DocumentReference::class.java)
                        DocumentsList(jp.codec.readValue(groupDocumentsNode.traverse(), listType))
                    } else if (groupDocumentsNode.isObject) {
                        val mapEntries = mutableMapOf<String, DocumentReference>()
                        groupDocumentsNode.fields().forEach { (key, valueNode) ->
                            mapEntries[key] = jp.codec.treeToValue(valueNode, DocumentReference::class.java)
                        }
                        DocumentsMap(mapEntries)
                    } else {
                        null
                    }
                } else {
                    null
                }

                val groupRecords: List<Record> = if (groupRecordsNode != null && groupRecordsNode.isArray) {
                    if (groupRecordsNode.isEmpty) {
                        listOf()
                    } else {
                        val listType = ctxt.typeFactory.constructCollectionType(List::class.java, Record::class.java)
                        jp.codec.readValue(groupRecordsNode.traverse(), listType)
                    }
                } else {
                    listOf()
                }
                groupList.add(Group(groupDocuments, groupRecords))
            }
            groupList
        } else {
            null
        }

        return BurritoAudioAlignment(format, version, type, documents, roles, records, groups)
    }
}
