package org.wycliffeassociates.otter.jvm.controls.skins.cards

import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.SkinBase
import javafx.scene.control.Slider
import org.wycliffeassociates.otter.jvm.controls.card.ScriptureTakeCard
import tornadofx.*

class ScriptureTakeCardSkin(val card: ScriptureTakeCard) : SkinBase<ScriptureTakeCard>(card) {

    @FXML
    lateinit var playBtn: Button
    @FXML
    lateinit var editBtn: Button
    @FXML
    lateinit var deleteBtn: Button
    @FXML
    lateinit var slider: Slider
    @FXML
    lateinit var takeLabel: Label
    @FXML
    lateinit var timestampLabel: Label

    init {
        loadFXML()
        initializeControl()
    }

    fun initializeControl() {
        bindText()
        bindActions()
    }

    fun bindText() {
        deleteBtn.textProperty().bind(card.deleteTextProperty())
        editBtn.textProperty().bind(card.editTextProperty())
        playBtn.textProperty().set(card.playTextProperty().value)
        takeLabel.textProperty().bind(card.takeNumberProperty())
        timestampLabel.textProperty().bind(card.timestampProperty())
    }

    fun bindActions() {
        deleteBtn.onActionProperty().bind(card.onDeleteProperty())
        editBtn.onActionProperty().bind(card.onEditProperty())
    }

    private fun loadFXML() {
        val loader = FXMLLoader(javaClass.getResource("ScriptureTakeCard.fxml"))
        loader.setController(this)
        val root: Node = loader.load()
        children.add(root)

        importStylesheet(javaClass.getResource("/css/root.css").toExternalForm())
        importStylesheet(javaClass.getResource("/css/scripturetakecard.css").toExternalForm())
    }
}