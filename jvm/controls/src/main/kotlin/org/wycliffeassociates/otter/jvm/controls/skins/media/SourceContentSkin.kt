package org.wycliffeassociates.otter.jvm.controls.skins.media

import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.layout.VBox
import org.kordamp.ikonli.javafx.FontIcon
import org.wycliffeassociates.otter.jvm.controls.sourcecontent.SourceContent
import org.wycliffeassociates.otter.jvm.controls.sourceformattoggle.SourceFormatToggle
import tornadofx.*

class SourceContentSkin(override val sourceContent: SourceContent) : SourceContentBaseSkin(sourceContent) {

    private val AUDIO_PLAYER_ICON = FontIcon("gmi-hearing")
    private val SOURCE_TEXT_ICON = FontIcon("gmi-sort-by-alpha")

    @FXML
    lateinit var root: VBox

    @FXML
    lateinit var sourceContentLabel: Label

    init {
        loadFXML()
        initializeControl()
    }

    override fun initializeControl() {
        super.initializeControl()

        if (sourceContent.applyRoundedStyleProperty.value) {
            root.styleClass.add("source-content--rounded")
        }

        playBtn.apply {
            visibleWhen {
                sourceContentController.activeSourceProperty.booleanBinding {
                    it == SourceFormatToggle.SourceFormat.AUDIO
                }
            }
        }
    }

    override fun loadFXML() {
        val loader = FXMLLoader(javaClass.getResource("SourceContent.fxml"))
        loader.setController(this)
        val root: Node = loader.load()
        children.add(root)
    }
}