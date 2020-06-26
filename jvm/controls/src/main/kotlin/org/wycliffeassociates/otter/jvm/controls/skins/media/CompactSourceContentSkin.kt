package org.wycliffeassociates.otter.jvm.controls.skins.media

import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.layout.HBox
import org.wycliffeassociates.otter.jvm.controls.sourcecontent.SourceContent
import org.wycliffeassociates.otter.jvm.controls.sourceformattoggle.SourceFormatToggle
import tornadofx.*

class CompactSourceContentSkin(override val sourceContent: SourceContent) : SourceContentBaseSkin(sourceContent) {

    @FXML
    lateinit var sourceMissing: HBox

    @FXML
    lateinit var sourceContentContainer: HBox

    @FXML
    lateinit var audioPlayerContainer: HBox

    @FXML
    lateinit var sourceTextContainer: HBox

    init {
        loadFXML()
        initializeControl()
    }

    override fun initializeControl() {
        super.initializeControl()

        sourceMissing.apply {
            hiddenWhen { sourceContent.sourceAudioAvailableProperty }
            managedWhen { visibleProperty() }
        }
        sourceContentContainer.apply {
            visibleWhen { sourceContent.sourceAudioAvailableProperty }
            managedWhen { visibleProperty() }
        }
        audioPlayerContainer.apply {
            visibleWhen {
                sourceContentController.activeSourceProperty.booleanBinding {
                    it == SourceFormatToggle.SourceFormat.AUDIO
                }
            }
            managedWhen { visibleProperty() }
        }
        sourceTextContainer.apply {
            visibleWhen {
                sourceContentController.activeSourceProperty.booleanBinding {
                    it == SourceFormatToggle.SourceFormat.TEXT
                }
            }
            managedWhen { visibleProperty() }
        }
    }

    override fun loadFXML() {
        val loader = FXMLLoader(javaClass.getResource("CompactSourceContent.fxml"))
        loader.setController(this)
        val root: Node = loader.load()
        children.add(root)
    }
}