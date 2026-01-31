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

        val category = node["meta"]["category"].asText()

        val meta: Meta = mapper.readValue(node["meta"].toString(), Meta::class.java)

        val type: TypeSchema = mapper.readValue(node["type"].toString(), TypeSchema::class.java)
        val format = mapper.readValue(node["format"].toString(), Format::class.java)
        val idAuthorities = mapper.readValue(node["idAuthorities"].toString(), IdAuthoritiesSchema::class.java)
        val identification = mapper.readValue(node["identification"].toString(), IdentificationSchema::class.java)
        val confidential = mapper.readValue(node["confidential"].toString(), Boolean::class.java)
        val copyright = mapper.readValue(node["copyright"].toString(), CopyrightSchema::class.java)
        val languages = mapper.readValue(node["languages"].toString(), Languages::class.java)
        val ingredients = mapper.readValue(node["ingredients"].toString(), IngredientsSchema::class.java)
        val localizedNames = mapper.readValue(node["localizedNames"].toString(), LocalizedNamesSchema::class.java)

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
}