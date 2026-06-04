package org.bibletranslationtools.scriptureburrito.flavor

import com.fasterxml.jackson.annotation.*
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.bibletranslationtools.scriptureburrito.flavor.scripture.ScriptureFlavorSchema
import org.bibletranslationtools.scriptureburrito.flavor.scripture.audio.AudioFlavorSchema
import org.bibletranslationtools.scriptureburrito.flavor.scripture.braille.EmbossedBrailleScriptureSchema
import org.bibletranslationtools.scriptureburrito.flavor.scripture.print.TypesetScriptureSchema
import org.bibletranslationtools.scriptureburrito.flavor.scripture.text.TextTranslationSchema
import org.bibletranslationtools.scriptureburrito.flavor.scripture.video.SignLanguageVideoTranslationSchema
import java.io.IOException

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder(
    "name"
)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "name")
@JsonSubTypes(
    JsonSubTypes.Type(value = TextTranslationSchema::class, name = "textTranslation"),
    JsonSubTypes.Type(value = AudioFlavorSchema::class, name = "audioTranslation"),
    JsonSubTypes.Type(value = SignLanguageVideoTranslationSchema::class, name = "signLanguageVideoTranslation"),
    JsonSubTypes.Type(value = EmbossedBrailleScriptureSchema::class, name = "embossedBrailleScripture"),
    JsonSubTypes.Type(value = TypesetScriptureSchema::class, name = "typesetScripture")
)
abstract class FlavorSchema