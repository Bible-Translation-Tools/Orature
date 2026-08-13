package org.bibletranslationtools.vtt

import org.bibletranslationtools.vtt.WebvttParserUtil.validateWebvttHeaderLine
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.util.*
import kotlin.collections.ArrayList

class VTTParser {

    private val EVENT_NONE = -1
    private val EVENT_END_OF_FILE = 0
    private val EVENT_COMMENT = 1
    private val EVENT_STYLE_BLOCK = 2
    private val EVENT_CUE = 3


    private val COMMENT_START = "NOTE"
    private val STYLE_START = "STYLE"

    private var parsableWebvttData: ParsableByteArray = ParsableByteArray()

    fun parseDocument(file: File): WebVttDocument {
        return WebVttDocument(file, parse(file))
    }

    fun parse(file: File): List<WebVttCue> {
        file.inputStream().use {
            val out = parse(it)
            return out.map { WebVttCue(it) }
        }
    }

    fun parse(inputStream: InputStream): List<WebvttCueInfo> {
        val output = object : Consumer<CuesWithTiming> {
            val contents: MutableList<CuesWithTiming> = arrayListOf()
            override fun accept(t: CuesWithTiming) {
                contents.addLast(t)
            }
        }
        val bytes = inputStream.readAllBytes()
        parse(
            bytes,
            0,
            bytes.size,
            OutputOptions.allCuesUngrouped(),
            output
        )

        return output.contents
            .map { WebvttCueInfo(it.cues.first(), it.startTimeUs, it.endTimeUs) }
            .toTypedArray()
            .copyOf(output.contents.size).mapNotNull {
                it
            }
            .toList()
    }

    fun parse(
        data: ByteArray,
        offset: Int,
        length: Int,
        outputOptions: OutputOptions,
        output: Consumer<CuesWithTiming>
    ) {
        parsableWebvttData.reset(data,  /* limit= */offset + length)
        parsableWebvttData.setPosition(offset)
        // Validate the first line of the header, and skip the remainder.
        try {
            validateWebvttHeaderLine(parsableWebvttData)
        } catch (e: Exception) {
            throw IllegalArgumentException(e)
        }
        while (parsableWebvttData.readLine()?.isNotEmpty() == true) {
        }

        var event: Int
        val cueInfos: MutableList<WebvttCueInfo> = ArrayList()
        while ((getNextEvent(parsableWebvttData).also {
                event = it
            }) != EVENT_END_OF_FILE) {
            if (event == EVENT_COMMENT) {
                skipComment(parsableWebvttData)
            } else if (event == EVENT_STYLE_BLOCK) {
                require(cueInfos.isEmpty()) { "A style block was found after the first cue." }
                parsableWebvttData.readLine() // Consume the "STYLE" header.
            } else if (event == EVENT_CUE) {
                val cueInfo: WebvttCueInfo? = VTTCueParser.parseCue(parsableWebvttData)
                if (cueInfo != null) {
                    cueInfos.add(cueInfo)
                }
            }
        }
        val subtitle: WebvttSubtitle = WebvttSubtitle(cueInfos)

        if (outputOptions.groupCuesInRange) {
            LegacySubtitleUtil.toCuesWithTiming(subtitle, outputOptions, output)
        } else {
            cueInfos
                .map { CuesWithTiming(listOf(it.cue), it.startTimeUs, it.endTimeUs - it.startTimeUs) }
                .forEach { output.accept(it) }
        }
    }

    /**
     * Positions the input right before the next event, and returns the kind of event found. Does not
     * consume any data from such event, if any.
     *
     * @return The kind of event found.
     */
    private fun getNextEvent(parsableWebvttData: ParsableByteArray): Int {
        var foundEvent: Int = EVENT_NONE
        var currentInputPosition = 0
        while (foundEvent == EVENT_NONE) {
            currentInputPosition = parsableWebvttData.getPosition()
            val line: String? = parsableWebvttData.readLine()
            foundEvent = if (line == null) {
                EVENT_END_OF_FILE
            } else if (STYLE_START == line) {
                EVENT_STYLE_BLOCK
            } else if (line.startsWith(COMMENT_START)) {
                EVENT_COMMENT
            } else {
                EVENT_CUE
            }
        }
        parsableWebvttData.setPosition(currentInputPosition)

        return foundEvent
    }

    private fun skipComment(parsableWebvttData: ParsableByteArray) {
        while (parsableWebvttData.readLine()?.isNotEmpty() == true) {
        }
    }
}