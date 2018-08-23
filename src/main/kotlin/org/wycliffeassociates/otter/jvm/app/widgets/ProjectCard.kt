package org.wycliffeassociates.otter.jvm.app.widgets

import javafx.geometry.Pos
import javafx.scene.control.Button
import org.wycliffeassociates.otter.common.data.model.Project
import org.wycliffeassociates.otter.common.ui.widgets.IProjectCard
import tornadofx.*
import tornadofx.Stylesheet.Companion.root

class ProjectCard(project: Project) : Button() {
    init {
        with(root) {
            vbox(20) {
                alignment = Pos.CENTER
                label(project.book.toString())
                label(project.targetLanguage.toString())
                button("LOAD") {
                }
            }
        }
}
}
