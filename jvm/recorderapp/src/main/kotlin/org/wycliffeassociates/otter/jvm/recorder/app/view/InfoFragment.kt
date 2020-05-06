package org.wycliffeassociates.otter.jvm.recorder.app.view

import org.kordamp.ikonli.javafx.FontIcon
import org.wycliffeassociates.otter.jvm.workbookplugin.plugin.ParameterizedScope
import tornadofx.*

class InfoFragment : Fragment() {

    val languageIcon = FontIcon("gmi-favorite")
    val bookIcon = FontIcon("fas-book")
    val chapterIcon = FontIcon("gmi-book")
    val unitIcon = FontIcon("gmi-bookmark-border")
    val fileIcon = FontIcon("gmi-insert-drive-file")
    val resourceIcon = FontIcon("gmi-forum")

    override val root = hbox {
        addClass("info")
    }

    init {
        addRecordingInfoFromParams()
    }

    private fun addRecordingInfoFromParams() {

        if (scope is ParameterizedScope) {
            val parameters = (scope as? ParameterizedScope)?.parameters

            parameters?.let {
                val language = parameters.named["language"]
                val book = parameters.named["book"]
                val chapter = parameters.named["chapter"]
                val cnum = parameters.named["chapter_number"]
                val unit = parameters.named["unit"]
                val unum = parameters.named["unit_number"]
                val resource = parameters.named["resource"]

                language?.let {
                    root.add(InfoItem(it, null, languageIcon))
                }
                book?.let {
                    root.add(InfoItem(it, null, bookIcon))
                }
                chapter?.let {
                    root.add(InfoItem(it, cnum, chapterIcon))
                }
                unit?.let {
                    root.add(InfoItem(it, unum, unitIcon))
                }
                resource?.let {
                    root.add(InfoItem(it, null, resourceIcon))
                }

                if (arrayOf(language, book, chapter, unit).all { it == null }) {
                    val wav = app.parameters.named["wav"]
                    wav?.let {
                        root.add(InfoItem(it, null, fileIcon))
                    }
                }
            }
        }
    }
}