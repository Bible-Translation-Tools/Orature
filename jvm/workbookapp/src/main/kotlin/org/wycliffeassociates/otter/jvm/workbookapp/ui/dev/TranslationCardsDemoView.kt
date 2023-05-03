package org.wycliffeassociates.otter.jvm.workbookapp.ui.dev

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.jvm.controls.card.CreateTranslationCard
import org.wycliffeassociates.otter.jvm.controls.card.newTranslationCard
import org.wycliffeassociates.otter.jvm.controls.card.translationCardWrapper
import org.wycliffeassociates.otter.jvm.controls.model.TranslationMode
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import tornadofx.View
import tornadofx.action
import tornadofx.borderpane
import tornadofx.button
import tornadofx.managedWhen
import tornadofx.paddingAll
import tornadofx.vbox
import tornadofx.visibleWhen

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

        borderpane {
            center = translationCardWrapper(
                languages[0],
                languages[1],
                TranslationMode.TRANSLATION
            ) {
                top = button("Reset") {
                    action {
                        this@translationCardWrapper.isActiveProperty.set(false)
                        showNewTranslationCard.set(false)
                    }
                }
            }
        }

        newTranslationCard(
            SimpleObjectProperty<Language>(
                Language("en", "English", "English", "", true, "")
            ),
            SimpleObjectProperty<Language>(null),
            mode = TranslationMode.NARRATION
        ) {
            visibleWhen(showNewTranslationCard)
            managedWhen(visibleProperty())

            setOnCancelAction {
                showNewTranslationCard.set(false)
            }
        }
        add(
            CreateTranslationCard().apply {
                visibleWhen(showNewTranslationCard.not())
                managedWhen(visibleProperty())

                setOnAction {
                    showNewTranslationCard.set(true)
                }
            }
        )
    }
}