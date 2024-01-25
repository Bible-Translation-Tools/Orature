package org.wycliffeassociates.otter.common.domain.resourcecontainer.projectimportexport

import org.wycliffeassociates.resourcecontainer.ResourceContainer
import org.wycliffeassociates.resourcecontainer.entity.Project
import java.io.File
import java.io.InputStream


/**
 * Merges the media contents from one resource container to the other.
 * This will overwrite media files with matching names.
 */
object MergeTextContent {
    fun merge(fromRC: ResourceContainer, toRC: ResourceContainer) {
        try {
            mergeManifest(fromRC, toRC)
            mergeTextFiles(fromRC, toRC)
        } finally {
            fromRC.close()
            toRC.close()
        }
    }

    private fun mergeManifest(fromRC: ResourceContainer, toRC: ResourceContainer) {
        val fromManifest = fromRC.manifest
        val toManifest = toRC.manifest

        if (fromManifest == null || toManifest == null) {
            return
        } else {
            val toMap = toRC.manifest!!.projects.associateBy { it.identifier } as MutableMap
            val fromMap = fromRC.manifest!!.projects.associateBy { it.identifier }

            val notInTo = fromMap.minus(toMap.keys).toMutableMap()
            val inBoth = fromMap.minus(notInTo.keys).toMutableMap()

            toMap.putAll(notInTo)
            mergeMatchingProjects(inBoth, toMap)
            toRC.manifest!!.projects = toMap.values.toList()
        }
        toRC.write()
    }

    private fun mergeMatchingProjects(
        from: MutableMap<String, Project>,
        to: MutableMap<String, Project>
    ) {
        val notInTo = from.minus(to.keys)
        val matching = from.minus(notInTo.keys)

        matching.forEach { (key, value) -> to.replace(key, value) }
        to.putAll(notInTo)
    }

    private fun mergeTextFiles(fromRC: ResourceContainer, toRC: ResourceContainer) {
        val _fromManifest = fromRC.manifest
        val filesToMerge = mutableMapOf<String, File>()
        try {
            _fromManifest?.let { fromManifest ->
                fromManifest.projects.forEach { project ->
                    val streams = manifestFilePermutations(fromRC, project)
                    filesToMerge.putAll(getMediaFilesToMerge(streams))
                }
            }
            toRC.addFilesToContainer(filesToMerge)
        } catch (e: Exception) {
            println(e)
        } finally {
            filesToMerge.values.forEach { it.delete() }
        }
    }

    private fun manifestFilePermutations(fromRc: ResourceContainer, project: Project): Map<String, InputStream> {
        //val content = fromRc.getProjectContent(project.identifier, fromRc.manifest.dublinCore.format)
        // TODO: ./ breaks file access, this should be fixed in the RC library
        val path = if (project.path.startsWith("./")) {
            project.path.substringAfter("./")
        } else {
            project.path
        }
        if (fromRc.manifest.dublinCore.type == "bundle") {
            if (fromRc.accessor.fileExists(path)) {
                return mapOf(path to fromRc.accessor.getInputStream(path))
            }
        } else if (fromRc.manifest.dublinCore.type == "book") {
            val projectPath = File(project.path)
            val projectDir = if (projectPath.extension == "") {
                path
            } else {
                projectPath.parent
            }
            return fromRc.accessor.getInputStreams(projectDir, listOf("usfm"))
        }
        return mapOf()
    }

    private fun getMediaFilesToMerge(
        files: Map<String, InputStream>
    ): Map<String, File> {
        val filesToMerge = mutableMapOf<String, File>()
        files.forEach { (path, stream) ->
            stream.use { ifs ->
                val temp = createTempFile()
                temp.outputStream().use { ofs ->
                    ifs.transferTo(ofs)
                }
                filesToMerge.put(path, temp)
            }
        }
        return filesToMerge
    }
}