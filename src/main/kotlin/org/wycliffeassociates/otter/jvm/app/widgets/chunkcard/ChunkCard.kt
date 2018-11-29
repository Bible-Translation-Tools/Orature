package org.wycliffeassociates.otter.jvm.app.widgets.contentcard


import com.jfoenix.controls.JFXButton
import javafx.scene.control.Label
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.wycliffeassociates.otter.common.data.model.Content
import tornadofx.*
import tornadofx.FX.Companion.messages

class ContentCard(initialContent: Content? = null) : VBox() {
    var actionButton = JFXButton()
    var titleLabel by singleAssign<Label>()
    var content: Content by property(initialContent)

    fun contentProperty() = getProperty(ContentCard::content)

    init {
        importStylesheet<ContentCardStyles>()
        addClass(ContentCardStyles.defaultContentCard)
        titleLabel = label(contentProperty().stringBinding {
            "${messages[it?.labelKey ?: ""]} ${it?.start ?: ""}"
        }) {
            vgrow = Priority.ALWAYS
            maxHeight = Double.MAX_VALUE
            addClass(ContentCardStyles.titleLabel)
        }
        label(contentProperty().stringBinding {
            if (it?.selectedTake != null) "${messages["take"]} ${it.selectedTake?.number ?: ""}" else ""
        }) {
            vgrow = Priority.ALWAYS
            maxHeight = Double.MAX_VALUE
            addClass(ContentCardStyles.selectedTakeLabel)
        }
        actionButton.isDisableVisualFocus = true
        add(actionButton)
    }
}

fun contentcard(verse: Content, init: ContentCard.() -> Unit): ContentCard {
    val vc = ContentCard(verse)
    vc.init()
    return vc
}