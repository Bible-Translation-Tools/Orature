package org.wycliffeassociates.otter.jvm.controls.skins.media

import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.control.SkinBase
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import org.kordamp.ikonli.javafx.Icon
import org.wycliffeassociates.otter.jvm.controls.sourceaudiotoggle.SourceAudioToggle
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import tornadofx.*

class SourceAudioToggleSkin(private val toggle: SourceAudioToggle): SkinBase<SourceAudioToggle>(toggle) {

    private val TOGGLE_SOURCE_ACTIVE = "toggle--source_audio__box--active"
    private val TOGGLE_SOURCE_ICON_ACTIVE = "toggle--source_audio__box__icon--active"

    @FXML
    lateinit var root: HBox
    @FXML
    lateinit var textBox: VBox
    @FXML
    lateinit var textIcon: Icon
    @FXML
    lateinit var audioBox: VBox
    @FXML
    lateinit var audioIcon: Icon

    init {
        loadFXML()
        initializeControl()
    }

    private fun initializeControl() {
        root.apply {
            setOnMouseClicked {
                toggle.displayPlayerProperty.set(toggle.displayPlayerProperty.get().not())
            }
        }

        toggle.displayPlayerProperty.onChangeAndDoNow {
            it?.let {
                if (it) activatePlayer() else activateText()
            }
        }
    }

    private fun activatePlayer() {
        textBox.removeClass(TOGGLE_SOURCE_ACTIVE)
        textIcon.removeClass(TOGGLE_SOURCE_ICON_ACTIVE)
        audioBox.addClass(TOGGLE_SOURCE_ACTIVE)
        audioIcon.addClass(TOGGLE_SOURCE_ICON_ACTIVE)
    }

    private fun activateText() {
        textBox.addClass(TOGGLE_SOURCE_ACTIVE)
        textIcon.addClass(TOGGLE_SOURCE_ICON_ACTIVE)
        audioBox.removeClass(TOGGLE_SOURCE_ACTIVE)
        audioIcon.removeClass(TOGGLE_SOURCE_ICON_ACTIVE)
    }

    private fun loadFXML() {
        val loader = FXMLLoader(javaClass.getResource("SourceAudioToggle.fxml"))
        loader.setController(this)
        val root: Node = loader.load()
        children.add(root)
    }
}