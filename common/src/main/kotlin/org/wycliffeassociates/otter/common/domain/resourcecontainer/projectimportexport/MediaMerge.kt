package org.wycliffeassociates.otter.common.domain.resourcecontainer.projectimportexport

import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import org.wycliffeassociates.resourcecontainer.entity.Media
import org.wycliffeassociates.resourcecontainer.entity.MediaProject


/**
 * Merges the media contents from one resource container to the other.
 * This will overwrite media with matching names.
 */
class MediaMerge(
    val directoryProvider: IDirectoryProvider,
    val from: ResourceContainer,
    val to: ResourceContainer
) {

    fun merge() {
        if (from.media != null) {
            mergeManifest()
            mergeMediaFiles()
        }
    }

    private fun mergeManifest() {
        val fromMedia = from.media
        val toMedia = to.media

        if (fromMedia == null) {
            return
        }
        if (toMedia == null) {
            to.media = fromMedia.copy()
        } else {
            val toMap = to.media!!.projects.associateBy { it.identifier } as MutableMap
            val fromMap = from.media!!.projects.associateBy { it.identifier }

            val notInTo = fromMap.minus(toMap.keys).toMutableMap()
            val inBoth = fromMap.minus(notInTo.keys).toMutableMap()

            toMap.putAll(notInTo)
            mergeMatchingProjects(inBoth, toMap)
            to.media!!.projects = toMap.values.toList()
        }
        to.write()
    }

    private fun mergeMatchingProjects(
        from: MutableMap<String, MediaProject>,
        to: MutableMap<String, MediaProject>
    ) {
        from.forEach { (key, value) ->
            val fromMediaMap = value.media.associateBy { it.identifier } as MutableMap
            val toMediaMap = to[key]!!.media.associateBy { it.identifier } as MutableMap

            val notInTo = fromMediaMap.minus(toMediaMap.keys)
            val matching = fromMediaMap.minus(notInTo.keys)

            matching.forEach { (key, value) -> toMediaMap.replace(key, value) }
            toMediaMap.putAll(notInTo)
            to[key]!!.media = toMediaMap.values.toList()
        }
    }

    private fun mergeMediaFiles() {
        val _fromMedia = from.media
        _fromMedia?.let { fromMedia ->
            fromMedia.projects.forEach {
                it.media.forEach { media ->
                    val files = mediaFilePermutations(media)
                    files.forEach { path ->
                        copyMediaFile(path)
                    }
                }
            }
        }
    }

    private fun mediaFilePermutations(media: Media): List<String> {
        val list = mutableListOf<String>()
        if (media.chapterUrl.isNotEmpty()) list.add(media.chapterUrl)
        if (media.url.isNotEmpty()) list.add(media.url)

        insertVariables(list, "{quality}", media.quality)
        insertVariables(list, "{version}", listOf(media.version))
        insertVariables(list, "{chapter}", generateChapterOptions())

        return list
    }

    private fun insertVariables(list: MutableList<String>, variable: String, options: List<String>) {
        if(options.isNotEmpty()) {
            val toAdd = mutableListOf<String>()
            list.forEach { unqualified ->
                if(unqualified.contains(variable)) {
                    options.forEach {
                        toAdd.add(unqualified.replace(variable, it))
                    }
                }
            }
            list.removeAll { it.contains(variable) }
            list.addAll(toAdd)
        }
    }

    private fun generateChapterOptions(max: Int = 200): List<String> {
        val list = mutableListOf<String>()
        val digits = max.toString().length
        for(i in 0..max) {
            for(j in 1..digits) {
                list.add(i.toString().padStart(j, '0'))
            }
        }
        return list
    }

    private fun copyMediaFile(file: String) {
        if (!file.isURL()) {
            if (from.accessor.fileExists(file)) {
                from.accessor.getInputStream(file).use { ifs ->
                    val temp = createTempFile()
                    try {
                        temp.outputStream().use { ofs ->
                            ifs.transferTo(ofs)
                        }
                        to.addFileToContainer(temp, file)
                    } finally {
                        temp.delete()
                    }
                }
            }
        }
    }
}

// This is not exhaustive or sufficient by itself, but a fileExists call is still used after
private fun String.isURL(): Boolean = this.toLowerCase().startsWith("http://")