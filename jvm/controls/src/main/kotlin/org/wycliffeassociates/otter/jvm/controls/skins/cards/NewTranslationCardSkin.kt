package org.wycliffeassociates.otter.jvm.controls.skins.cards

import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.SkinBase
import org.wycliffeassociates.otter.jvm.controls.card.NewTranslationCard
import tornadofx.*

class NewTranslationCardSkin(private val card: NewTranslationCard) : SkinBase<NewTranslationCard>(card) {

    @FXML
    lateinit var sourceLanguageText: Label

    @FXML
    lateinit var targetLanguageText: Label

    @FXML
    lateinit var newTranslationBtn: Button

    init {
        importStylesheet(javaClass.getResource("/css/new-translation-card.css").toExternalForm())

        loadFXML()
        initializeControl()
    }

    private fun initializeControl() {
        bindText()
        bindAction()
    }

    private fun bindText() {
        sourceLanguageText.textProperty().bind(card.sourceLanguageProperty)
        targetLanguageText.textProperty().bind(card.targetLanguageProperty)
        newTranslationBtn.textProperty().bind(card.newTranslationTextProperty)
    }

    private fun bindAction() {
        newTranslationBtn.onActionProperty().bind(card.onActionProperty)
    }

    private fun loadFXML() {
        val loader = FXMLLoader(javaClass.getResource("NewTranslationCard.fxml"))
        loader.setController(this)
        val root: Node = loader.load()
        children.add(root)
    }
}
