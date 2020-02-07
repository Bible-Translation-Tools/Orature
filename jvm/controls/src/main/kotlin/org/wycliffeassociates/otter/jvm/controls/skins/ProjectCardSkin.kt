package org.wycliffeassociates.otter.jvm.controls.skins

import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXListView
import com.jfoenix.controls.JFXPopup
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.event.EventTarget
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.control.SkinBase
import javafx.scene.layout.Priority
import javafx.scene.text.Text
import org.kordamp.ikonli.javafx.FontIcon
import org.wycliffeassociates.otter.jvm.controls.card.ProjectCard
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import tornadofx.*
import tornadofx.FX.Companion.messages


class ProjectCardSkin(private val card: ProjectCard) : SkinBase<ProjectCard>(card) {

    @FXML
    lateinit var bookTitle: Text
    @FXML
    lateinit var cardMoreButton: JFXButton
    @FXML
    lateinit var bookSlug: Text
    @FXML
    lateinit var language: Label
    @FXML
    lateinit var cardPrimaryButton: JFXButton

    private val popup = JFXPopup()
    private val list = JFXListView<Label>()

    init {
        loadFXML()
        initializeControl()
    }

    private fun initializeControl() {
        bindText()
        bindActions()
        bindPopup()
    }

    private fun bindText() {
        bookTitle.textProperty().bind(card.titleTextProperty())
        bookSlug.textProperty().bind(card.slugTextProperty())
        language.textProperty().bind(card.languageTextProperty())
        cardPrimaryButton.textProperty().bind(card.actionTextProperty())
    }

    private fun bindActions() {
        cardPrimaryButton.setOnAction {
            card.onPrimaryActionProperty().value.invoke()
        }
        card.onPrimaryActionProperty().onChange { op ->
            cardPrimaryButton.setOnAction {
                op?.invoke()
            }
        }
    }

    private fun bindPopup() {
        card.secondaryActionsList.onChangeAndDoNow { actions ->
            val popup = JFXPopup()
            val items = actions.map { action ->
                Label().apply {
                    vgrow = Priority.ALWAYS
                    text = action.text
                    graphic = FontIcon(action.iconCode)
                    setOnMouseClicked {
                        action.onClicked.invoke()
                    }
                }
            }
            list.setOnMouseClicked {
                popup.hide()
            }
            list.items.setAll(items)
            popup.popupContent = list
            cardMoreButton.setOnAction {
                popup.show(cardMoreButton)
            }
        }
    }

    private fun loadFXML() {
        val loader = FXMLLoader(javaClass.getResource("ProjectCard.fxml"))
        loader.setController(this)
        val root: Node = loader.load()
        children.add(root)
    }
}