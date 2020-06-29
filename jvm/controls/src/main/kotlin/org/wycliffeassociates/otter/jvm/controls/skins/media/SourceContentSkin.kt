package org.wycliffeassociates.otter.jvm.controls.skins.media

import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.layout.VBox
import org.kordamp.ikonli.javafx.FontIcon
import org.wycliffeassociates.otter.jvm.controls.sourcecontent.SourceContent
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

        playBtn.apply {
            visibleWhen(sourceContentController.audioActiveBinding())
        }
        audioSlider.apply {
            visibleWhen(sourceContentController.audioActiveBinding())
            managedWhen(visibleProperty())
        }
        sourceTextScroll.apply {
            visibleWhen(sourceContentController.textActiveBinding())
            managedWhen(visibleProperty())
        }
        sourceContentLabel.apply {
            textProperty().bind(
                sourceContentController.sourceContentLabelBinding(
                    sourceContent.sourceAudioLabelProperty,
                    sourceContent.sourceTextLabelProperty
                )
            )
            graphicProperty().bind(
                sourceContentController.sourceContentIconBinding(
                    AUDIO_PLAYER_ICON,
                    SOURCE_TEXT_ICON
                )
            )
        }
    }

    override fun loadFXML() {
        val loader = FXMLLoader(javaClass.getResource("SourceContent.fxml"))
        loader.setController(this)
        val root: Node = loader.load()
        children.add(root)
    }
}