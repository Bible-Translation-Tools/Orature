package org.bibletranslationtools.scriptureburrito.flavor.scripture.print

import com.fasterxml.jackson.annotation.*
import com.fasterxml.jackson.databind.JsonNode
import org.bibletranslationtools.scriptureburrito.flavor.FlavorSchema

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder(
    "name",
    "contentType",
    "pod",
    "pageCount",
    "width",
    "height",
    "scale",
    "orientation",
    "colorSpace",
    "edgeSpace",
    "fonts",
    "conventions"
)
class TypesetScriptureSchema: FlavorSchema() {

    @get:JsonProperty("contentType")
    @set:JsonProperty("contentType")
    @JsonProperty("contentType")
    var contentType: String? = null

    @get:JsonProperty("pod")
    @set:JsonProperty("pod")
    @JsonProperty("pod")
    var pod: Boolean? = null

    @get:JsonProperty("pageCount")
    @set:JsonProperty("pageCount")
    @JsonProperty("pageCount")
    var pageCount: Int? = null

    @get:JsonProperty("width")
    @set:JsonProperty("width")
    @JsonProperty("width")
    var width: String? = null

    @get:JsonProperty("height")
    @set:JsonProperty("height")
    @JsonProperty("height")
    var height: String? = null

    @get:JsonProperty("scale")
    @set:JsonProperty("scale")
    @JsonProperty("scale")
    var scale: String? = null

    @get:JsonProperty("orientation")
    @set:JsonProperty("orientation")
    @JsonProperty("orientation")
    var orientation: Orientation? = null

    @get:JsonProperty("colorSpace")
    @set:JsonProperty("colorSpace")
    @JsonProperty("colorSpace")
    var colorSpace: ColorSpace? = null

    @JsonProperty("edgeSpace")
    private var edgeSpace: EdgeSpace? = null

    @get:JsonProperty("fonts")
    @set:JsonProperty("fonts")
    @JsonProperty("fonts")
    var fonts: MutableList<String>? = ArrayList()

    @JsonProperty("conventions")
    private var conventions: JsonNode? = null

    @JsonProperty("edgeSpace")
    fun getEdgeSpace(): EdgeSpace? {
        return edgeSpace
    }

    @JsonProperty("edgeSpace")
    fun setEdgeSpace(edgeSpace: EdgeSpace?) {
        this.edgeSpace = edgeSpace
    }

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
        if (other !is TypesetScriptureSchema) return false

        if (contentType != other.contentType) return false
        if (pod != other.pod) return false
        if (pageCount != other.pageCount) return false
        if (width != other.width) return false
        if (height != other.height) return false
        if (scale != other.scale) return false
        if (orientation != other.orientation) return false
        if (colorSpace != other.colorSpace) return false
        if (edgeSpace != other.edgeSpace) return false
        if (fonts != other.fonts) return false
        if (conventions != other.conventions) return false

        return true
    }

    override fun hashCode(): Int {
        var result = 0
        result = 31 * result + (contentType?.hashCode() ?: 0)
        result = 31 * result + (pod?.hashCode() ?: 0)
        result = 31 * result + (pageCount ?: 0)
        result = 31 * result + (width?.hashCode() ?: 0)
        result = 31 * result + (height?.hashCode() ?: 0)
        result = 31 * result + (scale?.hashCode() ?: 0)
        result = 31 * result + (orientation?.hashCode() ?: 0)
        result = 31 * result + (colorSpace?.hashCode() ?: 0)
        result = 31 * result + (edgeSpace?.hashCode() ?: 0)
        result = 31 * result + (fonts?.hashCode() ?: 0)
        result = 31 * result + (conventions?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "TypesetScriptureSchema(contentType=$contentType, pod=$pod, pageCount=$pageCount, width=$width, height=$height, scale=$scale, orientation=$orientation, colorSpace=$colorSpace, edgeSpace=$edgeSpace, fonts=$fonts, conventions=$conventions)"
    }


    enum class ColorSpace(private val value: String) {
        CMYK("cmyk"),
        RGB("rgb");

        override fun toString(): String {
            return this.value
        }

        @JsonValue
        fun value(): String {
            return this.value
        }

        companion object {
            private val CONSTANTS: MutableMap<String, ColorSpace> = HashMap()

            init {
                for (c in values()) {
                    CONSTANTS[c.value] = c
                }
            }

            @JsonCreator
            fun fromValue(value: String): ColorSpace {
                val constant = CONSTANTS[value]
                requireNotNull(constant) { value }
                return constant
            }
        }
    }

    enum class Orientation(private val value: String) {
        PORTRAIT("portrait"),
        LANDSCAPE("landscape");

        override fun toString(): String {
            return this.value
        }

        @JsonValue
        fun value(): String {
            return this.value
        }

        companion object {
            private val CONSTANTS: MutableMap<String, Orientation> = HashMap()

            init {
                for (c in values()) {
                    CONSTANTS[c.value] = c
                }
            }

            @JsonCreator
            fun fromValue(value: String): Orientation {
                val constant = CONSTANTS[value]
                requireNotNull(constant) { value }
                return constant
            }
        }
    }
}
