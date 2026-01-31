package org.bibletranslationtools.scriptureburrito.flavor.scripture.text

import com.fasterxml.jackson.annotation.*
import com.fasterxml.jackson.databind.JsonNode
import org.bibletranslationtools.scriptureburrito.flavor.FlavorSchema

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder(
    "name", "projectType", "translationType", "audience", "usfmVersion", "conventions"
)
class TextTranslationSchema: FlavorSchema() {

    @get:JsonProperty("projectType")
    @set:JsonProperty("projectType")
    @JsonProperty("projectType")
    var projectType: ProjectType? = null

    @get:JsonProperty("translationType")
    @set:JsonProperty("translationType")
    @JsonProperty("translationType")
    var translationType: TranslationType? = null

    @get:JsonProperty("audience")
    @set:JsonProperty("audience")
    @JsonProperty("audience")
    var audience: Audience? = null

    @get:JsonProperty("usfmVersion")
    @set:JsonProperty("usfmVersion")
    @JsonProperty("usfmVersion")
    var usfmVersion: String? = null

    @JsonProperty("conventions")
    private var conventions: JsonNode? = null

    @JsonProperty("conventions")
    fun getConventions(): JsonNode? {
        return conventions
    }

    @JsonProperty("conventions")
    fun setConventions(conventions: JsonNode?) {
        this.conventions = conventions
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TextTranslationSchema) return false

        if (projectType != other.projectType) return false
        if (translationType != other.translationType) return false
        if (audience != other.audience) return false
        if (usfmVersion != other.usfmVersion) return false
        if (conventions != other.conventions) return false

        return true
    }

    override fun hashCode(): Int {
        var result = 0
        result = 31 * result + (projectType?.hashCode() ?: 0)
        result = 31 * result + (translationType?.hashCode() ?: 0)
        result = 31 * result + (audience?.hashCode() ?: 0)
        result = 31 * result + (usfmVersion?.hashCode() ?: 0)
        result = 31 * result + (conventions?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "TextTranslationSchema(projectType=$projectType, translationType=$translationType, audience=$audience, usfmVersion=$usfmVersion, conventions=$conventions)"
    }

    enum class Audience(private val value: String) {
        BASIC("basic"),
        COMMON("common"),
        COMMON_LITERARY("common-literary"),
        LITERARY("literary"),
        LITURGICAL("liturgical"),
        CHILDREN("children");

        override fun toString(): String {
            return this.value
        }

        @JsonValue
        fun value(): String {
            return this.value
        }

        companion object {
            private val CONSTANTS: MutableMap<String, Audience> = HashMap()

            init {
                for (c in values()) {
                    CONSTANTS[c.value] = c
                }
            }

            @JsonCreator
            fun fromValue(value: String): Audience {
                val constant = CONSTANTS[value]
                requireNotNull(constant) { value }
                return constant
            }
        }
    }

    enum class ProjectType(private val value: String) {
        STANDARD("standard"),
        DAUGHTER("daughter"),
        STUDY_BIBLE("studyBible"),
        STUDY_BIBLE_ADDITIONS("studyBibleAdditions"),
        BACK_TRANSLATION("backTranslation"),
        AUXILIARY("auxiliary"),
        TRANSLITERATION_MANUAL("transliterationManual"),
        TRANSLITERATION_WITH_ENCODER("transliterationWithEncoder");

        override fun toString(): String {
            return this.value
        }

        @JsonValue
        fun value(): String {
            return this.value
        }

        companion object {
            private val CONSTANTS: MutableMap<String, ProjectType> = HashMap()

            init {
                for (c in values()) {
                    CONSTANTS[c.value] = c
                }
            }

            @JsonCreator
            fun fromValue(value: String): ProjectType {
                val constant = CONSTANTS[value]
                requireNotNull(constant) { value }
                return constant
            }
        }
    }

    enum class TranslationType(private val value: String) {
        FIRST_TRANSLATION("firstTranslation"),
        NEW_TRANSLATION("newTranslation"),
        REVISION("revision"),
        STUDY_OR_HELP_MATERIAL("studyOrHelpMaterial");

        override fun toString(): String {
            return this.value
        }

        @JsonValue
        fun value(): String {
            return this.value
        }

        companion object {
            private val CONSTANTS: MutableMap<String, TranslationType> = HashMap()

            init {
                for (c in values()) {
                    CONSTANTS[c.value] = c
                }
            }

            @JsonCreator
            fun fromValue(value: String): TranslationType {
                val constant = CONSTANTS[value]
                requireNotNull(constant) { value }
                return constant
            }
        }
    }
}
