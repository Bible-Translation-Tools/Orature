package org.bibletranslationtools.scriptureburrito

import com.fasterxml.jackson.annotation.*


class Relationships: ArrayList<RelationshipSchema>()

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder(
    "relationType", "flavor", "id", "revision", "variant"
)
class RelationshipSchema {
    @get:JsonProperty("relationType")
    @set:JsonProperty("relationType")
    @JsonProperty("relationType")
    var relationType: RelationType? = null

    @get:JsonProperty("flavor")
    @set:JsonProperty("flavor")
    @JsonProperty("flavor")
    var flavor: String? = null

    @get:JsonProperty("id")
    @set:JsonProperty("id")
    @JsonProperty("id")
    @JsonPropertyDescription("Opaque system-specific identifier, prefixed with the name of the system as declared in idAuthorities.")
    var id: String? = null
    
    @get:JsonProperty("revision")
    @set:JsonProperty("revision")
    @JsonProperty("revision")
    @JsonPropertyDescription("Opaque system-specific revision identifier.")
    var revision: String? = null

    @get:JsonProperty("variant")
    @set:JsonProperty("variant")
    @JsonProperty("variant")
    var variant: String? = null

    enum class RelationType(private val value: String) {
        SOURCE("source"),
        TARGET("target"),
        EXPRESSION("expression"),
        PARASCRIPTURAL("parascriptural"),
        PERIPHERAL("peripheral");

        override fun toString(): String {
            return this.value
        }

        @JsonValue
        fun value(): String {
            return this.value
        }

        companion object {
            private val CONSTANTS: MutableMap<String, RelationType> = HashMap()

            init {
                for (c in values()) {
                    CONSTANTS[c.value] = c
                }
            }

            @JsonCreator
            fun fromValue(value: String): RelationType {
                val constant = CONSTANTS[value]
                requireNotNull(constant) { value }
                return constant
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RelationshipSchema) return false

        if (relationType != other.relationType) return false
        if (flavor != other.flavor) return false
        if (id != other.id) return false
        if (revision != other.revision) return false
        if (variant != other.variant) return false

        return true
    }

    override fun hashCode(): Int {
        var result = relationType?.hashCode() ?: 0
        result = 31 * result + (flavor?.hashCode() ?: 0)
        result = 31 * result + (id?.hashCode() ?: 0)
        result = 31 * result + (revision?.hashCode() ?: 0)
        result = 31 * result + (variant?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "RelationshipSchema(relationType=$relationType, flavor=$flavor, id=$id, revision=$revision, variant=$variant)"
    }
}
