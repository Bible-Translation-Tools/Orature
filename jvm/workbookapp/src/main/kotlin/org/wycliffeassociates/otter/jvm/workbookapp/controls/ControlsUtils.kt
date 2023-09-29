package org.wycliffeassociates.otter.jvm.workbookapp.controls

import javafx.event.EventTarget
import org.wycliffeassociates.otter.jvm.controls.chapterselector.ChapterSelector
import tornadofx.attachTo

fun EventTarget.chapterSelector(op: ChapterSelector.() -> Unit = {}) =
    ChapterSelector().attachTo(this, op)