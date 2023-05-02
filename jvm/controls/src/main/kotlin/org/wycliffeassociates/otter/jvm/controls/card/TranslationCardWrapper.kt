package org.wycliffeassociates.otter.jvm.controls.card

import javafx.beans.property.SimpleBooleanProperty
import javafx.event.EventTarget
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.jvm.controls.model.TranslationMode
import tornadofx.attachTo
import tornadofx.managedWhen
import tornadofx.visibleWhen

class TranslationCardWrapper(
    sourceLanguage: Language,
    targetLanguage: Language,
    mode: TranslationMode
): StackPane() {

    val isActiveProperty = SimpleBooleanProperty(false)

    init {
        activeTranslationCard(sourceLanguage.name, targetLanguage.name, mode) {
            visibleWhen(isActiveProperty)
            managedWhen(visibleProperty())
        }
        translationCard(sourceLanguage.slug, targetLanguage.slug, mode) {
            visibleWhen(isActiveProperty.not())
            managedWhen(visibleProperty())

            setOnMouseClicked {
                isActiveProperty.set(true)
            }
        }
    }
}

fun EventTarget.translationCardWrapper(
    sourceLanguage: Language,
    targetLanguage: Language,
    mode: TranslationMode,
    op: TranslationCardWrapper.() -> Unit = {}
) = TranslationCardWrapper(sourceLanguage, targetLanguage, mode).attachTo(this, op)
