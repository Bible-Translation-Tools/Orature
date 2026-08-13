package org.bibletranslationtools.scriptureburrito.flavor.scripture.braille

import com.fasterxml.jackson.annotation.*

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder(
    "lineIndicatorSpaced", "startIndicator", "lineIndicator", "endIndicator"
)
class ContinuousPoetry {

    @get:JsonProperty("lineIndicatorSpaced")
    @set:JsonProperty("lineIndicatorSpaced")
    @JsonProperty("lineIndicatorSpaced")
    var lineIndicatorSpaced: LineIndicatorSpaced? = null

    @get:JsonProperty("startIndicator")
    @set:JsonProperty("startIndicator")
    @JsonProperty("startIndicator")
    var startIndicator: String? = null

    @get:JsonProperty("lineIndicator")
    @set:JsonProperty("lineIndicator")
    @JsonProperty("lineIndicator")
    var lineIndicator: String? = null

    @get:JsonProperty("endIndicator")
    @set:JsonProperty("endIndicator")
    @JsonProperty("endIndicator")
    var endIndicator: String? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ContinuousPoetry) return false

        if (lineIndicatorSpaced != other.lineIndicatorSpaced) return false
        if (startIndicator != other.startIndicator) return false
        if (lineIndicator != other.lineIndicator) return false
        if (endIndicator != other.endIndicator) return false

        return true
    }

    override fun hashCode(): Int {
        var result = lineIndicatorSpaced?.hashCode() ?: 0
        result = 31 * result + (startIndicator?.hashCode() ?: 0)
        result = 31 * result + (lineIndicator?.hashCode() ?: 0)
        result = 31 * result + (endIndicator?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "ContinuousPoetry(lineIndicatorSpaced=$lineIndicatorSpaced, startIndicator=$startIndicator, lineIndicator=$lineIndicator, endIndicator=$endIndicator)"
    }

    enum class LineIndicatorSpaced(private val value: String) {
        NEVER("never"),
        ALWAYS("always"),
        SOMETIMES("sometimes");

        override fun toString(): String {
            return this.value
        }

        @JsonValue
        fun value(): String {
            return this.value
        }

        companion object {
            private val CONSTANTS: MutableMap<String, LineIndicatorSpaced> = HashMap()

            init {
                for (c in values()) {
                    CONSTANTS[c.value] = c
                }
            }

            @JsonCreator
            fun fromValue(value: String): LineIndicatorSpaced {
                val constant = CONSTANTS[value]
                requireNotNull(constant) { value }
                return constant
            }
        }
    }
}
