package org.wycliffeassociates.otter.common.domain

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.wycliffeassociates.resourcecontainer.Config
import java.io.File
import java.io.IOException

class OtterResourceContainerConfig : Config {

    var config: OtterConfig? = null
    var extendedDublinCore: ExtendedDublinCore? = null

    override fun read(configFile: File): Config {
        if (configFile.exists()) {
            val mapper = ObjectMapper(YAMLFactory())
            mapper.registerModule(KotlinModule())
            config = configFile.bufferedReader().use {
                mapper.readValue(it, OtterConfig::class.java)
            }
            config?.let {
                extendedDublinCore = it.extendedDublinCore
            }
            return this
        } else {
            throw IOException("Missing config.yaml")
        }
    }

    override fun write(configFile: File) {
        val mapper = ObjectMapper(YAMLFactory())
        mapper.registerModule(KotlinModule())
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
        configFile.bufferedWriter().use {
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