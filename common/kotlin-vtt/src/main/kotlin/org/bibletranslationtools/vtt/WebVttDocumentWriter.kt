package org.bibletranslationtools.vtt

import java.io.File
import java.io.OutputStream
import java.io.Writer
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.*

class WebVttDocumentWriter(val file: File) {

    fun writeDocument(cues: List<WebVttCue>) {
        file.outputStream().use {
            it.writer(Charsets.UTF_8).use {
                it.write("WEBVTT\n\n")
                for (cue in cues) {
                    writeCue(cue, it)
                }
            }
        }
    }

    fun writeCue(cue: WebVttCue, writer: Writer) {
        writer.write("${timestamp(cue.startTimeUs)} --> ${timestamp(cue.endTimeUs)}\n")
        for (tag in cue.classes.keys) {
            if (tag == "text") continue
            val content = cue.classes[tag]!!.joinToString("\n")
            writer.write("<${tag}>$content</${tag}>")
        }
        if ("text" in cue.classes.keys) {
            writer.write("\n")
            val content = cue.classes["text"]!!.joinToString("\n -")
            writer.write("- $content")
        }
        writer.write("\n\n")
    }
}