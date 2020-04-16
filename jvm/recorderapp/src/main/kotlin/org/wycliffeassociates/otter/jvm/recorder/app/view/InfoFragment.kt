package org.wycliffeassociates.otter.jvm.recorder.app.view

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import org.wycliffeassociates.otter.jvm.workbookplugin.plugin.ParameterizedScope
import tornadofx.*

class InfoFragment : Fragment() {

    val languageIcon = MaterialIconView(MaterialIcon.FAVORITE, "24pt")
    val bookIcon = FontAwesomeIconView(FontAwesomeIcon.BOOK, "24pt")
    val chapterIcon = MaterialIconView(MaterialIcon.BOOK, "24pt")
    val unitIcon = MaterialIconView(MaterialIcon.BOOKMARK_BORDER, "24pt")
    val fileIcon = MaterialIconView(MaterialIcon.INSERT_DRIVE_FILE, "24pt")

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