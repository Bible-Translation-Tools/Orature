package org.bibletranslationtools.scriptureburrito.container

import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.apache.tika.Tika
import org.apache.tika.mime.MediaType
import org.bibletranslationtools.scriptureburrito.MetadataDeserializer
import org.bibletranslationtools.scriptureburrito.container.accessors.DirectoryAccessor
import org.bibletranslationtools.scriptureburrito.container.accessors.IContainerAccessor
import org.bibletranslationtools.scriptureburrito.container.accessors.ZipAccessor
import org.bibletranslationtools.scriptureburrito.MetadataSchema
import java.io.File
import java.io.IOException
import java.io.OutputStream

const val MANIFEST_FILENAME = "metadata.json"

class BurritoContainer private constructor(
    val file: File
) : AutoCloseable {

    lateinit var manifest: MetadataSchema

    val accessor: IContainerAccessor = when {
        // file may not exist at creation of a rc with .zip suffix in file path
        file.extension == "zip" -> ZipAccessor(file)
        file.isFile && detectFileType() == MediaType.APPLICATION_ZIP -> ZipAccessor(file)
        else -> DirectoryAccessor(file)
    }

    private fun detectFileType(): MediaType {
        return MediaType.parse(Tika().detect(file))
    }

    private fun read(): MetadataSchema {
        if (accessor.fileExists(MANIFEST_FILENAME)) {
            val mapper = ObjectMapper(YAMLFactory())
            mapper.registerModules(
                SimpleModule().addDeserializer(MetadataSchema::class.java, MetadataDeserializer())
            )
            mapper.registerKotlinModule()
            manifest = accessor.getReader(MANIFEST_FILENAME).use {
                mapper.readValue(it, MetadataSchema::class.java)
            }
            return manifest
        } else {
            throw IOException("Missing metadata.json")
        }
    }

    fun write() {
        writeManifest()
    }

    fun writeManifest() {
        accessor.initWrite()
        accessor.write(MANIFEST_FILENAME) { writeManifest(it) }
    }

    private fun writeManifest(writer: OutputStream) {
        val factory = YAMLFactory()
        factory.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET)
        val mapper = ObjectMapper(factory)
        mapper.registerKotlinModule()
        mapper.setSerializationInclusion(Include.NON_NULL)
        mapper.writeValue(writer, manifest)
        writer.flush()
    }

    fun addFileToContainer(file: File, pathInRC: String) {
        accessor.write(pathInRC) { ofs ->
            file.inputStream().use { ifs ->
                ifs.copyTo(ofs)
            }
        }
    }

    fun addFilesToContainer(files: Map<String, File>) {
        val map = files.entries.associate { (pathInRC, file) ->
            pathInRC to { ofs: OutputStream ->
                file.inputStream().use { ifs ->
                    ifs.copyTo(ofs)
                }
                Unit
            }
        }
        accessor.write(map)
    }

    override fun close() {
        accessor.close()
    }

    companion object {
        fun create(file: File, init: BurritoContainer.() -> Unit): BurritoContainer {
            val rc = BurritoContainer(file)
            rc.init()
            return rc
        }

        fun load(dir: File): BurritoContainer {
            val rc = BurritoContainer(dir)
            rc.read()

            return rc
        }
    }
}