package org.wycliffeassociates.otter.jvm.recorder.app.view

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundFill
import javafx.scene.layout.CornerRadii
import javafx.scene.paint.Paint
import tornadofx.Fragment
import tornadofx.hbox
import tornadofx.label

class InfoFragment : Fragment() {

    override val root = hbox {
        minHeight = 50.0
        alignment = Pos.CENTER_LEFT
        background = Background(BackgroundFill(Paint.valueOf("#333333"), CornerRadii.EMPTY, Insets.EMPTY))
    }

    init {
        addRecordingInfoFromParams()
    }

    private fun addRecordingInfoFromParams() {
        val language = app.parameters.named["lang"]
        val book = app.parameters.named["book"]
        val chapter = app.parameters.named["chap"]
        val cnum = app.parameters.named["cnum"]
        val unit = app.parameters.named["unit"]
        val unum = app.parameters.named["unum"]

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
            root.add(InfoItem(it,unum, false))
        }

        if(arrayOf(language, book, chapter, unit).all { it == null }) {
            val wav = app.parameters.named["wav"]
            wav?.let {
                root.add(InfoItem(it))
            }
        }
    }
}