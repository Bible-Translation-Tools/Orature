package org.bibletranslationtools.vtt

import java.io.File

class WebVttDocument internal constructor(private val file: File, cues: List<WebVttCue>) {

    private val cues = mutableListOf(*cues.toTypedArray())

    fun saveChanges() {
        WebVttDocumentWriter(file).writeDocument(cues)
    }

    fun writeDocument(file: File) {
        WebVttDocumentWriter(file).writeDocument(cues)
    }

    fun getCueContentsOfTag(tag: String): List<WebVttCueContent> {
        return cues.flatMap { cue ->
            cue.getContentForTag(tag).map { WebVttCueContent(tag, it, cue) }
        }
    }

    fun getCues(): MutableList<WebVttCue> {
        return cues
    }

    fun addCue(startTimeUs: Long, endTimeUs: Long, tag: String, content: String): WebVttCueContent {
        val cue = Cue.Builder().build()
        val wvc = WebVttCue(WebvttCueInfo(cue, startTimeUs, endTimeUs))
        cues.sortWith { first, second ->
            val startIsSame = first.startTimeUs == second.startTimeUs
            val endIsGreater = first.endTimeUs > second.endTimeUs
            when {
                startIsSame && endIsGreater -> -1 // the greater end should come first
                startIsSame && !endIsGreater -> 1
                else -> first.startTimeUs.compareTo(second.startTimeUs)
            }
        }
        return WebVttCueContent(tag, content, wvc)
    }

    class WebVttCueContent(tag: String, content: String, val cue: WebVttCue) {

        var content = content
            set(value) {

                val classes: MutableList<String> = cue.classes[tag] ?: run {
                    cue.classes[tag] = mutableListOf(content)
                    cue.classes[tag]!!
                }
                val index = classes.indexOf(field)
                classes[index] = value
                field = value
            }

        var tag = tag
            set(value) {
                val old = cue.classes.remove(field)
                cue.classes[value] = old ?: mutableListOf(content)
                field = value
            }

        var startTimeUs: Long
            get() = cue.startTimeUs
            set(value) {
                cue.startTimeUs = value
            }

        var endTimeUs: Long
            get() = cue.endTimeUs
            set(value) {
                cue.endTimeUs = value
            }
    }
}