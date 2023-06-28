package org.wycliffeassociates.otter.jvm.workbookapp.ui.dev

import javafx.scene.Parent
import javafx.scene.layout.Priority
import org.wycliffeassociates.otter.jvm.controls.card.translationTypeCard
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import tornadofx.*

class TranslationWizardDemoView : View() {

    init {
        tryImportStylesheet("/css/translation-card-2.css")
    }

    override val root = vbox {
        paddingAll = 20.0
        maxWidth = 800.0

        vbox {
            addClass("translation-wizard__main")
            vgrow = Priority.ALWAYS

            translationTypeCard("oralTranslation", "oralTranslationDesc")
            translationTypeCard("narration", "narrationDesc")
            translationTypeCard("dialect", "dialectDesc") {
                addPseudoClass("last")
            }
        }
    }
}