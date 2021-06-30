/**
 * Copyright (C) 2020, 2021 Wycliffe Associates
 *
 * This file is part of Orature.
 *
 * Orature is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Orature is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Orature.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.wycliffeassociates.otter.common.domain.resourcecontainer

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.wycliffeassociates.resourcecontainer.Config
import java.io.OutputStream
import java.io.Reader

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

    override fun write(writer: OutputStream) {
        val mapper = ObjectMapper(YAMLFactory())
        mapper.registerModule(KotlinModule())
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
        mapper.writeValue(writer, config)
    }
}

class OtterConfig(
    @JsonProperty("extended_dublin_core")
    var extendedDublinCore: ExtendedDublinCore
)

class ExtendedDublinCore(
    var categories: List<Category>
)

data class Category(
    val identifier: String,
    val title: String,
    val type: String,
    val sort: Int
)