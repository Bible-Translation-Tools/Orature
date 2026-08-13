package org.bibletranslationtools.scriptureburrito

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.bibletranslationtools.scriptureburrito.flavor.FlavorType
import org.bibletranslationtools.scriptureburrito.flavor.scripture.audio.*
import org.junit.Test
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*

class TestMetadataSubtypes {

    val copyright = CopyrightSchema().apply {
        this.shortStatements = mutableListOf(ShortStatement("CC BY-SA", "en"))
    }

    val formats = Formats().apply {
        this["format-wav"] = AudioFormat(Compression.WAV)
        this["format-mp3"] = AudioFormat(Compression.MP3)
    }
    val audioFlavor = TypeSchema(
        FlavorType(
            name = Flavor.SCRIPTURE,
            flavor = AudioFlavorSchema(),
            currentScope = ScopeSchema().apply {
                this["GEN"] = mutableListOf("1")
            }
        )
    )

    val enLanguage = Languages().apply {
        add(
            LanguageSchema("en")
        )
    }

    val derivedAudio = DerivedMetadataSchema(
        Format.SCRIPTURE_BURRITO,
        DerivedMetaSchema(
            dateCreated = Date.from(Instant.now()),
            version = MetaVersionSchema._1_0_0,
            defaultLocale = "en",
            generator = SoftwareAndUserInfoSchema().apply {
                softwareName = "test"
                softwareVersion = "1.0.0"
            }
        ),
        IdAuthoritiesSchema(),
        IdentificationSchema(),
        confidential = false,
        copyright = copyright,
        type = audioFlavor,
        languages = enLanguage
    )

    val sourceAudio = SourceMetadataSchema(
        Format.SCRIPTURE_BURRITO,
        SourceMetaSchema(
            dateCreated = Date.from(Instant.now()),
            version = MetaVersionSchema._1_0_0,
            defaultLocale = "en",
            generator = SoftwareAndUserInfoSchema().apply {
                softwareName = "test"
                softwareVersion = "1.0.0"
            }
        ),
        IdAuthoritiesSchema(),
        IdentificationSchema(),
        confidential = false,
        copyright = copyright,
        type = audioFlavor,
        languages = enLanguage
    )

    val templateAudio = TemplateMetadataSchema(
        Format.SCRIPTURE_BURRITO,
        TemplateMetaSchema(
            dateCreated = Date.from(Instant.now()),
            version = MetaVersionSchema._1_0_0,
            defaultLocale = "en",
            templateName = LocalizedText(short = hashMapOf("en" to "testTemplate")),
            generator = SoftwareAndUserInfoSchema().apply {
                softwareName = "test"
                softwareVersion = "1.0.0"
            }
        ),
        IdAuthoritiesSchema(),
        IdentificationSchema(),
        confidential = false,
        copyright = copyright,
        type = audioFlavor,
        languages = enLanguage
    )

    val mapper = ObjectMapper()

    init {
        mapper.registerKotlinModule()
        mapper.configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, false)
        mapper.setDateFormat(SimpleDateFormat("yyyy-MM-dd"))
    }

    @Test
    fun testSerializesToSourceMetadata() {
        val audio = mapper.writeValueAsString(sourceAudio)
        val read = mapper.readValue(audio, SourceMetadataSchema::class.java)
        assert(read is SourceMetadataSchema)
    }

    @Test
    fun testSerializesToTemplateMetadata() {
        val audio = mapper.writeValueAsString(templateAudio)
        val read = mapper.readValue(audio, TemplateMetadataSchema::class.java)
        assert(read is TemplateMetadataSchema)
    }

    @Test
    fun testSerializesToDerivedMetadata() {
        val audio = mapper.writeValueAsString(derivedAudio)
        val read = mapper.readValue(audio, DerivedMetadataSchema::class.java)
        assert(read is DerivedMetadataSchema)
    }

    @Test
    fun testDeserializesToSourceMetadata() {
        val audio = mapper.writeValueAsString(sourceAudio)
        val read = mapper.readTree(audio)
        assert(read["format"].asText() == Format.SCRIPTURE_BURRITO.value())
        assert(read["meta"]["category"].asText() == "source")
        assert(read["type"]["flavorType"]["name"].asText() == "scripture")
        assert(read["type"]["flavorType"]["flavor"]["name"].asText() == "audioTranslation")
        assert(read["languages"][0]["tag"].asText() == "en")
    }

    @Test
    fun testDeserializesToTemplateMetadata() {
        val audio = mapper.writeValueAsString(templateAudio)
        val read = mapper.readTree(audio)
        assert(read["format"].asText() == Format.SCRIPTURE_BURRITO.value())
        assert(read["meta"]["category"].asText() == "template")
        assert(read["type"]["flavorType"]["name"].asText() == "scripture")
        assert(read["type"]["flavorType"]["flavor"]["name"].asText() == "audioTranslation")
        assert(read["languages"][0]["tag"].asText() == "en")
    }

    @Test
    fun testDeserializesToDerivedMetadata() {
        val audio = mapper.writeValueAsString(derivedAudio)
        val read = mapper.readTree(audio)
        assert(read["format"].asText() == Format.SCRIPTURE_BURRITO.value())
        assert(read["meta"]["category"].asText() == "derived")
        assert(read["type"]["flavorType"]["name"].asText() == "scripture")
        assert(read["type"]["flavorType"]["flavor"]["name"].asText() == "audioTranslation")
        assert(read["languages"][0]["tag"].asText() == "en")
    }
}