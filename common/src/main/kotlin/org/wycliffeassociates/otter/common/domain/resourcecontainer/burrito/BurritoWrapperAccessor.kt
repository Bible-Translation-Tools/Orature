package org.wycliffeassociates.otter.common.domain.resourcecontainer.burrito

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.bibletranslationtools.scriptureburrito.container.accessors.IContainerAccessor
import org.slf4j.LoggerFactory
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.io.Reader

/**
 * Accessor for Burrito Wrapper containers.
 * Provides access to wrapper-level files and can provide accessors to inner burritos.
 */
class BurritoWrapperAccessor(
    private val file: File
) : IContainerAccessor {
    
    private val logger = LoggerFactory.getLogger(BurritoWrapperAccessor::class.java)
    
    private val wrapperRoot: File = when {
        file.isDirectory -> file
        file.isFile && file.extension.lowercase() == "json" -> file.parentFile
        else -> file
    }

    private val metadataCandidates: List<String> = buildList {
        if (file.isFile && file.extension.lowercase() == "json") add(file.name)
        add("metadata.json")
        add("wrapper.json")
    }.distinct()

    // Delegate to the underlying container accessor (zip or directory)
    private val delegateAccessor: IContainerAccessor = when {
        wrapperRoot.isDirectory -> org.bibletranslationtools.scriptureburrito.container.accessors.DirectoryAccessor(wrapperRoot)
        file.extension.lowercase() in listOf("zip", "burrito", "orature") -> {
            org.bibletranslationtools.scriptureburrito.container.accessors.ZipAccessor(file)
        }
        else -> throw IllegalArgumentException("Unsupported burrito wrapper format: ${file.absolutePath}")
    }
    
    private var wrapperMetadata: ScriptureBurritoWrapper? = null
    
    /**
     * Gets the wrapper metadata, loading it if necessary.
     */
    fun getWrapperMetadata(): ScriptureBurritoWrapper? {
        if (wrapperMetadata == null) {
            try {
                val metadataPath = metadataCandidates.firstOrNull { delegateAccessor.fileExists(it) }
                if (metadataPath != null) {
                    val metadataReader = delegateAccessor.getReader(metadataPath)
                    val objectMapper = ObjectMapper().registerKotlinModule()
                    val metadataNode = objectMapper.readTree(metadataReader)
                    if (metadataNode != null && metadataNode.get("format")?.asText() == "scripture burrito wrapper") {
                        wrapperMetadata = objectMapper.treeToValue(metadataNode, ScriptureBurritoWrapper::class.java)
                    }
                }
            } catch (e: Exception) {
                logger.error("Failed to load wrapper metadata", e)
            }
        }
        return wrapperMetadata
    }
    
    /**
     * Gets an accessor for an inner burrito by its path.
     * @param burritoPath The path to the burrito within the wrapper (e.g., "audio", "text")
     * @return An IContainerAccessor for the inner burrito, or null if not found
     */
    fun getBurritoAccessor(burritoPath: String): IContainerAccessor? {
        // For zip files, use nested accessor
        if (!wrapperRoot.isDirectory) {
            return NestedBurritoAccessor(delegateAccessor, burritoPath)
        }
        
        // For directory-based wrappers, check if the burrito path exists
        val burritoFile = File(wrapperRoot, burritoPath)
        if (!burritoFile.exists()) {
            return null
        }
        
        // For directory wrappers, each inner burrito is represented as a directory subtree.
        return try {
            org.bibletranslationtools.scriptureburrito.container.accessors.DirectoryAccessor(burritoFile)
        } catch (e: Exception) {
            logger.warn("Failed to load burrito at $burritoPath", e)
            null
        }
    }
    
    /**
     * Gets accessors for all inner burritos.
     * @return Map of burrito path to accessor
     */
    fun getAllBurritoAccessors(): Map<String, IContainerAccessor> {
        val wrapperMeta = getWrapperMetadata() ?: return emptyMap()
        
        return wrapperMeta.contents.burritos.mapNotNull { burrito ->
            getBurritoAccessor(burrito.path)?.let { burrito.path to it }
        }.toMap()
    }
    
    // Delegate all IContainerAccessor methods to the underlying accessor
    
    override fun fileExists(filename: String): Boolean {
        return delegateAccessor.fileExists(filename)
    }
    
    override fun list(path: String): List<String> {
        return delegateAccessor.list(path)
    }
    
    override fun getInputStream(filename: String): InputStream {
        return delegateAccessor.getInputStream(filename)
    }
    
    override fun getInputStreams(path: String, extension: String): Map<String, InputStream> {
        return delegateAccessor.getInputStreams(path, extension)
    }
    
    override fun getInputStreams(path: String, extensions: List<String>): Map<String, InputStream> {
        return delegateAccessor.getInputStreams(path, extensions)
    }
    
    override fun getReader(filename: String): Reader {
        return delegateAccessor.getReader(filename)
    }
    
    override fun initWrite() {
        delegateAccessor.initWrite()
    }
    
    override fun write(filename: String, writeFunction: (OutputStream) -> Unit) {
        delegateAccessor.write(filename, writeFunction)
    }
    
    override fun write(files: Map<String, (OutputStream) -> Unit>) {
        delegateAccessor.write(files)
    }
    
    override val root: String?
        get() = delegateAccessor.root
    
    override fun close() {
        delegateAccessor.close()
    }
}

/**
 * Nested accessor for burritos within a zip wrapper.
 * Handles path prefixing to access files within a subdirectory of the zip.
 */
private class NestedBurritoAccessor(
    private val parentAccessor: IContainerAccessor,
    private val prefix: String
) : IContainerAccessor {
    
    private fun prefixPath(path: String): String {
        val normalizedPrefix = prefix.trimEnd('/', '\\')
        val normalizedPath = path.trimStart('/', '\\')
        return "$normalizedPrefix/$normalizedPath"
    }
    
    override fun fileExists(filename: String): Boolean {
        return parentAccessor.fileExists(prefixPath(filename))
    }
    
    override fun list(path: String): List<String> {
        val prefixedPath = prefixPath(path)
        val files = parentAccessor.list(prefixedPath)
        // Remove the prefix from the returned paths
        val prefixWithSlash = "$prefix/"
        return files.map { it.removePrefix(prefixWithSlash) }
    }
    
    override fun getInputStream(filename: String): InputStream {
        return parentAccessor.getInputStream(prefixPath(filename))
    }
    
    override fun getInputStreams(path: String, extension: String): Map<String, InputStream> {
        val prefixedPath = prefixPath(path)
        val streams = parentAccessor.getInputStreams(prefixedPath, extension)
        // Remove prefix from keys
        val prefixWithSlash = "$prefix/"
        return streams.mapKeys { it.key.removePrefix(prefixWithSlash) }
    }
    
    override fun getInputStreams(path: String, extensions: List<String>): Map<String, InputStream> {
        val prefixedPath = prefixPath(path)
        val streams = parentAccessor.getInputStreams(prefixedPath, extensions)
        // Remove prefix from keys
        val prefixWithSlash = "$prefix/"
        return streams.mapKeys { it.key.removePrefix(prefixWithSlash) }
    }
    
    override fun getReader(filename: String): Reader {
        return parentAccessor.getReader(prefixPath(filename))
    }
    
    override fun initWrite() {
        parentAccessor.initWrite()
    }
    
    override fun write(filename: String, writeFunction: (OutputStream) -> Unit) {
        parentAccessor.write(prefixPath(filename), writeFunction)
    }
    
    override fun write(files: Map<String, (OutputStream) -> Unit>) {
        val prefixedFiles = files.mapKeys { prefixPath(it.key) }
        parentAccessor.write(prefixedFiles)
    }
    
    override val root: String?
        get() = null
    
    override fun close() {
        // Don't close parent accessor, it may be used elsewhere
    }
}
