package org.wycliffeassociates.otter.common.domain.resourcecontainer

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.wycliffeassociates.resourcecontainer.Config
import java.io.Reader
import java.io.Writer

class OtterResourceContainerConfig : Config {
    var config: OtterConfig? = null
    var extendedDublinCore: ExtendedDublinCore? = null

    override fun read(reader: Reader): Config {
        val mapper = ObjectMapper(YAMLFactory())
        mapper.registerModule(KotlinModule())
        config = reader.use {
            mapper.readValue(it, OtterConfig::class.java)
        }
        config?.let {
            extendedDublinCore = it.extendedDublinCore
        }
        return this
    }

    override fun write(writer: Writer) {
        val mapper = ObjectMapper(YAMLFactory())
        mapper.registerModule(KotlinModule())
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
        writer.use {
            mapper.writeValue(it, config)
        }
    }
}

class OtterConfig (
    @JsonProperty("extended_dublin_core")
    var extendedDublinCore: ExtendedDublinCore
)


class ExtendedDublinCore (
    var categories: List<Category>
)

data class Category(
        val identifier: String,
        val title: String,
        val type: String,
        val sort: Int
)