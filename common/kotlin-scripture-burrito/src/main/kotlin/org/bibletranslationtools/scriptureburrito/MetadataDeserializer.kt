package org.bibletranslationtools.scriptureburrito

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.IOException

class MetadataDeserializer : JsonDeserializer<MetadataSchema>() {

    private val mapper = ObjectMapper()
    init {
        mapper.registerKotlinModule()
        mapper.registerModules(
            // SimpleModule().addDeserializer(FlavorSchema::class.java, FlavorSchemaDeserializer())
        )
    }

    @Throws(IOException::class, JsonProcessingException::class)
    override fun deserialize(jp: JsonParser, ctx: DeserializationContext?): MetadataSchema {
        val node: JsonNode = jp.readValueAsTree() // Get the complete JSON structure

        val category = node["meta"]?.get("category")?.asText()
            ?: throw JsonMappingException(jp, "Missing required field: meta.category")

        val meta: Meta = mapper.readValue(node["meta"].toString(), Meta::class.java)

        val type: TypeSchema = readRequired(node, "type", TypeSchema::class.java, jp)
        val format: Format = readRequired(node, "format", Format::class.java, jp)
        val idAuthorities = readOptional(node, "idAuthorities", IdAuthoritiesSchema::class.java)
        val identification = readOptional(node, "identification", IdentificationSchema::class.java)
        val confidential = readOptional(node, "confidential", Boolean::class.java)
        val copyright = readOptional(node, "copyright", CopyrightSchema::class.java) ?: CopyrightSchema()
        val languages = readOptional(node, "languages", Languages::class.java) ?: Languages()
        val ingredients = readOptional(node, "ingredients", IngredientsSchema::class.java) ?: IngredientsSchema()
        val localizedNames = readOptional(node, "localizedNames", LocalizedNamesSchema::class.java) ?: LocalizedNamesSchema()

        val metadata: MetadataSchema = when (category) {
            "source" -> {
                val out = SourceMetadataSchema(
                    format,
                    meta as SourceMetaSchema,
                    idAuthorities,
                    identification,
                    confidential,
                    type,
                    copyright,
                    languages = languages,
                    ingredients = ingredients,
                    localizedNames = localizedNames
                )
                out
            }

            "derived" -> {
                val out = DerivedMetadataSchema(
                    format,
                    meta as DerivedMetaSchema,
                    idAuthorities,
                    identification,
                    confidential,
                    type,
                    copyright,
                    languages = languages,
                    ingredients = ingredients,
                    localizedNames = localizedNames
                )
                out
            }
            "template" -> {
                val out = TemplateMetadataSchema(
                    format,
                    meta as TemplateMetaSchema,
                    idAuthorities,
                    identification,
                    confidential,
                    type,
                    copyright,
                    languages = languages,
                    ingredients = ingredients,
                    localizedNames = localizedNames
                )
                out
            }
            else -> throw JsonMappingException("Unsupported format string: $category")
        }
        return metadata
    }

    private fun <T> readOptional(node: JsonNode, field: String, clazz: Class<T>): T? {
        val child = node[field] ?: return null
        if (child.isNull) return null
        return mapper.readValue(child.toString(), clazz)
    }

    private fun <T> readRequired(node: JsonNode, field: String, clazz: Class<T>, jp: JsonParser): T {
        return readOptional(node, field, clazz)
            ?: throw JsonMappingException(jp, "Missing required field: $field")
    }
}
