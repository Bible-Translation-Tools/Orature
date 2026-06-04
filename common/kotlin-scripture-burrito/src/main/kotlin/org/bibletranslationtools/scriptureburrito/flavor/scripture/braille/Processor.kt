package org.bibletranslationtools.scriptureburrito.flavor.scripture.braille

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder(
    "name", "version", "table"
)
class Processor {

    @get:JsonProperty("name")
    @set:JsonProperty("name")
    @JsonProperty("name")
    var name: String? = null

    @get:JsonProperty("version")
    @set:JsonProperty("version")
    @JsonProperty("version")
    var version: String? = null

    @JsonProperty("table")
    private var table: Table? = null
    @JsonProperty("table")
    fun getTable(): Table? {
        return table
    }

    @JsonProperty("table")
    fun setTable(table: Table?) {
        this.table = table
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Processor) return false

        if (name != other.name) return false
        if (version != other.version) return false
        if (table != other.table) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name?.hashCode() ?: 0
        result = 31 * result + (version?.hashCode() ?: 0)
        result = 31 * result + (table?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "Processor(name=$name, version=$version, table=$table)"
    }
}
