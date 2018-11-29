package org.wycliffeassociates.otter.jvm.app.widgets.projectcard

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import tornadofx.Stylesheet
import tornadofx.cssclass

class ProjectCardStyles : Stylesheet() {
    companion object {
        val projectCardButton by cssclass()
        val deleteProjectButton by cssclass()
        fun deleteIcon(size: String = "1em") = MaterialIconView(MaterialIcon.DELETE, size)
        fun targetIcon(size: String = "1em") = MaterialIconView(MaterialIcon.RECORD_VOICE_OVER, size)
    }
}