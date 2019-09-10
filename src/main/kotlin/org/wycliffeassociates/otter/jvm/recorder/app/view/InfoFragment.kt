package org.wycliffeassociates.otter.jvm.recorder.app.view

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundFill
import javafx.scene.layout.CornerRadii
import javafx.scene.paint.Paint
import org.wycliffeassociates.otter.jvm.plugin.ParameterizedScope
import tornadofx.Fragment
import tornadofx.hbox

class InfoFragment : Fragment() {

    val parameters = (scope as ParameterizedScope).parameters

    override val root = hbox {
        minHeight = 50.0
        alignment = Pos.CENTER_LEFT
        background = Background(BackgroundFill(Paint.valueOf("#333333"), CornerRadii.EMPTY, Insets.EMPTY))
    }

    init {
        addRecordingInfoFromParams()
    }

    private fun addRecordingInfoFromParams() {
        val language = parameters.named["lang"]
        val book = parameters.named["book"]
        val chapter = parameters.named["chap"]
        val cnum = parameters.named["cnum"]
        val unit = parameters.named["unit"]
        val unum = parameters.named["unum"]

        language?.let {
            root.add(InfoItem(it))
        }
        book?.let {
            root.add(InfoItem(it))
        }
        chapter?.let {
            root.add(InfoItem(it, cnum, false))
        }
        unit?.let {
            root.add(InfoItem(it, unum, false))
        }

        if (arrayOf(language, book, chapter, unit).all { it == null }) {
            val wav = app.parameters.named["wav"]
            wav?.let {
                root.add(InfoItem(it))
            }
        }
    }
}