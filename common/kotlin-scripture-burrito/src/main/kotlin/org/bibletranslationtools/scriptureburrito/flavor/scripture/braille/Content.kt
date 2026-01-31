package org.bibletranslationtools.scriptureburrito.flavor.scripture.braille

import com.fasterxml.jackson.annotation.*

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder(
    "chapterNumberStyle",
    "chapterHeadingsNumberFirst",
    "versedParagraphs",
    "verseSeparator",
    "includeIntros",
    "footnotes",
    "characterStyles",
    "crossReferences"
)
class Content {

    @get:JsonProperty("chapterNumberStyle")
    @set:JsonProperty("chapterNumberStyle")
    @JsonProperty("chapterNumberStyle")
    var chapterNumberStyle: ChapterNumberStyle? = null

    @get:JsonProperty("chapterHeadingsNumberFirst")
    @set:JsonProperty("chapterHeadingsNumberFirst")
    @JsonProperty("chapterHeadingsNumberFirst")
    var chapterHeadingsNumberFirst: Boolean? = null

    @get:JsonProperty("versedParagraphs")
    @set:JsonProperty("versedParagraphs")
    @JsonProperty("versedParagraphs")
    var versedParagraphs: Boolean? = null

    @get:JsonProperty("verseSeparator")
    @set:JsonProperty("verseSeparator")
    @JsonProperty("verseSeparator")
    var verseSeparator: String? = null

    @get:JsonProperty("includeIntros")
    @set:JsonProperty("includeIntros")
    @JsonProperty("includeIntros")
    var includeIntros: Boolean? = null

    @JsonProperty("footnotes")
    private var footnotes: Footnotes? = null

    @JsonProperty("characterStyles")
    private var characterStyles: CharacterStyles? = null

    @JsonProperty("crossReferences")
    private var crossReferences: CrossReferences? = null

    @JsonProperty("footnotes")
    fun getFootnotes(): Footnotes? {
        return footnotes
    }

    @JsonProperty("footnotes")
    fun setFootnotes(footnotes: Footnotes?) {
        this.footnotes = footnotes
    }

    @JsonProperty("characterStyles")
    fun getCharacterStyles(): CharacterStyles? {
        return characterStyles
    }

    @JsonProperty("characterStyles")
    fun setCharacterStyles(characterStyles: CharacterStyles?) {
        this.characterStyles = characterStyles
    }

    @JsonProperty("crossReferences")
    fun getCrossReferences(): CrossReferences? {
        return crossReferences
    }

    @JsonProperty("crossReferences")
    fun setCrossReferences(crossReferences: CrossReferences?) {
        this.crossReferences = crossReferences
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Content) return false

        if (chapterNumberStyle != other.chapterNumberStyle) return false
        if (chapterHeadingsNumberFirst != other.chapterHeadingsNumberFirst) return false
        if (versedParagraphs != other.versedParagraphs) return false
        if (verseSeparator != other.verseSeparator) return false
        if (includeIntros != other.includeIntros) return false
        if (footnotes != other.footnotes) return false
        if (characterStyles != other.characterStyles) return false
        if (crossReferences != other.crossReferences) return false

        return true
    }

    override fun hashCode(): Int {
        var result = chapterNumberStyle?.hashCode() ?: 0
        result = 31 * result + (chapterHeadingsNumberFirst?.hashCode() ?: 0)
        result = 31 * result + (versedParagraphs?.hashCode() ?: 0)
        result = 31 * result + (verseSeparator?.hashCode() ?: 0)
        result = 31 * result + (includeIntros?.hashCode() ?: 0)
        result = 31 * result + (footnotes?.hashCode() ?: 0)
        result = 31 * result + (characterStyles?.hashCode() ?: 0)
        result = 31 * result + (crossReferences?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "Content(chapterNumberStyle=$chapterNumberStyle, chapterHeadingsNumberFirst=$chapterHeadingsNumberFirst, versedParagraphs=$versedParagraphs, verseSeparator=$verseSeparator, includeIntros=$includeIntros, footnotes=$footnotes, characterStyles=$characterStyles, crossReferences=$crossReferences)"
    }

    enum class ChapterNumberStyle(private val value: String) {
        UPPER("upper"),
        LOWER("lower");

        override fun toString(): String {
            return this.value
        }

        @JsonValue
        fun value(): String {
            return this.value
        }

        companion object {
            private val CONSTANTS: MutableMap<String, ChapterNumberStyle> = HashMap()

            init {
                for (c in values()) {
                    CONSTANTS[c.value] = c
                }
            }

            @JsonCreator
            fun fromValue(value: String): ChapterNumberStyle {
                val constant = CONSTANTS[value]
                requireNotNull(constant) { value }
                return constant
            }
        }
    }
}
