package org.wycliffeassociates.otter.jvm.controls.skins.media

import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.SkinBase
import javafx.scene.control.Slider
import org.kordamp.ikonli.javafx.FontIcon
import org.wycliffeassociates.otter.common.device.AudioPlayerEvent
import org.wycliffeassociates.otter.jvm.controls.AudioPlayerNode
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import java.util.concurrent.TimeUnit

class SourceAudioSkin(private val player: AudioPlayerNode) : SkinBase<AudioPlayerNode>(player) {

    private val PLAY_ICON = FontIcon("fa-play")
    private val PAUSE_ICON = FontIcon("fa-pause")

    @FXML
    lateinit var playBtn: Button
    @FXML
    lateinit var audioSlider: Slider

    private var disposable: Disposable? = null
    private var dragging = false
    private var resumeAfterDrag = false

    init {
        loadFXML()
        initializeControl()
    }

    private fun initializeControl() {
        playBtn.setOnMouseClicked {
            toggle()
        }
        audioSlider.value = 0.0
        audioSlider.setOnDragDetected {
            if (player.isPlaying) {
                resumeAfterDrag = true
                toggle()
            }
            dragging = true
        }
        audioSlider.setOnMouseReleased {
            player.seek(audioSlider.value.toFloat() / 100F)
            if (dragging) {
                dragging = false
                if (resumeAfterDrag) {
                    toggle()
                    resumeAfterDrag = false
                }
            }
        }
        player.isPlayingProperty.onChangeAndDoNow {
            if (it == true) {
                playBtn.graphicProperty().set(PAUSE_ICON)
            } else {
                playBtn.graphicProperty().set(PLAY_ICON)
            }
        }
    }

    private fun startProgressUpdate(): Disposable {
        return Observable
            .interval(20, TimeUnit.MILLISECONDS)
            .observeOnFx()
            .subscribe {
                if (player.isPlaying && !audioSlider.isValueChanging && !dragging) {
                    audioSlider.value = player.playbackPosition()
                }
            }
    }

    private fun toggle() {
        disposable?.dispose()
        if (player.isPlaying) {
            player.pause()
        } else {
            disposable = startProgressUpdate()
            player.play()
            player.player?.addEventListener {
                if (
                    it == AudioPlayerEvent.PAUSE ||
                    it == AudioPlayerEvent.STOP ||
                    it == AudioPlayerEvent.COMPLETE
                ) {
                    disposable?.dispose()
                    Platform.runLater {
                        player.isPlayingProperty.set(false)
                        if (it == AudioPlayerEvent.COMPLETE) {
                            audioSlider.value = 0.0
                        }
                    }
                }
            }
            player.isPlayingProperty.set(true)
        }
    }

    private fun loadFXML() {
        val loader = FXMLLoader(javaClass.getResource("SourceAudioPlayer.fxml"))
        loader.setController(this)
        val root: Node = loader.load()
        children.add(root)
    }
}