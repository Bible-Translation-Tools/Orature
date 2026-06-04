package org.bibletranslationtools.scriptureburrito.flavor

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.bibletranslationtools.scriptureburrito.flavor.scripture.ScriptureFlavorSchema
import org.bibletranslationtools.scriptureburrito.flavor.scripture.audio.AudioFlavorSchema
import java.io.IOException

class FlavorSchemaDeserializer : JsonDeserializer<FlavorSchema?>() {
    val mapper = ObjectMapper().registerKotlinModule()

    @Throws(IOException::class, JsonProcessingException::class)
    override fun deserialize(jp: JsonParser, ctx: DeserializationContext?): FlavorSchema {
        val node: JsonNode = jp.readValueAsTree() // Get the complete JSON structure

        // Extract the "format" field from the package object (assuming it's nested)
        val format = node["name"].asText()

        val flavor: FlavorSchema = when (format) {
            "textTranslation" -> mapper.readValue(node.toString(), ScriptureFlavorSchema::class.java)
            "audioTranslation" -> mapper.readValue(node.toString(), AudioFlavorSchema::class.java)

            else -> throw JsonMappingException("Unsupported format string: $format")
        }
        return flavor
    }
}