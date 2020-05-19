package org.wycliffeassociates.otter.common.domain.resourcecontainer.projectimportexport

import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import org.wycliffeassociates.resourcecontainer.entity.MediaProject

class MediaMerge(
    val directoryProvider: IDirectoryProvider,
    val from: ResourceContainer,
    val to: ResourceContainer
) {

    fun merge() {
        mergeManifest()
        mergeMediaFiles()
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

    private fun copyUnmatchedProjects(from: MutableMap<String, MediaProject>, to: MutableMap<String, MediaProject>) {
        to.putAll(from)
    }

    private fun mergeMatchingProjects(from: MutableMap<String, MediaProject>, to: MutableMap<String, MediaProject>) {
        from.forEach { key, value ->
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
                    if (media.chapterUrl.isNotEmpty()) copyMediaFile(media.chapterUrl)
                    if (media.url.isNotEmpty()) copyMediaFile(media.url)
                }
            }
        }
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

private fun String.isURL(): Boolean = this.toLowerCase().startsWith("http")