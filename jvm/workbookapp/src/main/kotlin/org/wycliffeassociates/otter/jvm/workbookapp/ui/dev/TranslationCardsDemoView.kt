package org.wycliffeassociates.otter.jvm.workbookapp.ui.dev

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.jvm.controls.card.newTranslationCard
import org.wycliffeassociates.otter.jvm.controls.card.translationCard
import org.wycliffeassociates.otter.jvm.controls.card.translationCreationCard
import org.wycliffeassociates.otter.common.data.primitives.ProjectMode
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import tornadofx.*

class TranslationCardsDemoView : View() {

    val languages = listOf(
        Language("en", "English", "English", "", true, ""),
        Language("fr", "français", "French", "", true, ""),
    )

    init {
        tryImportStylesheet("/css/translation-card-2.css")
    }

    private val showNewTranslationCard = SimpleBooleanProperty(false)

    override val root = vbox {
        spacing = 10.0
        paddingAll = 20.0
        maxWidth = 300.0

        translationCard(
            languages[0],
            languages[1],
            ProjectMode.TRANSLATION
        )

        newTranslationCard(
            SimpleObjectProperty<Language>(
                Language("en", "English", "English", "", true, "")
            ),
            SimpleObjectProperty<Language>(null),
            mode = ProjectMode.NARRATION
        ) {
            visibleWhen(showNewTranslationCard)
            managedWhen(visibleProperty())

            setOnCancelAction {
                showNewTranslationCard.set(false)
            }
        }
        translationCreationCard {
            visibleWhen(showNewTranslationCard.not())
            managedWhen(visibleProperty())

            setOnAction {
                showNewTranslationCard.set(true)
            }
        }
    }
}