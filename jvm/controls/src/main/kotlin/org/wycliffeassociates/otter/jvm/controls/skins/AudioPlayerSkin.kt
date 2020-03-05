package org.wycliffeassociates.otter.jvm.controls.skins

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
import javafx.scene.layout.HBox
import org.kordamp.ikonli.javafx.FontIcon
import org.wycliffeassociates.otter.common.device.AudioPlayerEvent
import org.wycliffeassociates.otter.jvm.controls.AudioPlayerNode
import java.util.concurrent.TimeUnit

private const val PLAY_ICON = "fa-play"
private const val PAUSE_ICON = "fa-pause"

class AudioPlayerSkin(private val player: AudioPlayerNode) : SkinBase<AudioPlayerNode>(player) {

    @FXML
    lateinit var playBtn: Button
    @FXML
    lateinit var sourceMissing: HBox
    @FXML
    lateinit var audioPlayer: HBox
    @FXML
    lateinit var audioSlider: Slider

    var disposable: Disposable? = null
    var dragging = false
    var resumeAfterDrag = false

    init {
        loadFXML()
        initializeControl()
    }

    private fun initializeControl() {
        playBtn.setOnMouseClicked {
            toggle()
        }
        if (player.sourceAvailable) {
            sourceMissing.visibleProperty().set(false)
            audioPlayer.visibleProperty().set(true)
        } else {
            sourceMissing.visibleProperty().set(true)
            audioPlayer.visibleProperty().set(false)
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
            println(audioSlider.value)
            if (dragging) {
                dragging = false
                if (resumeAfterDrag) {
                    toggle()
                    resumeAfterDrag = false
                }
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
        println(player.isPlaying)
        if (player.isPlaying) {
            player.pause()
            playBtn.graphic = FontIcon(PLAY_ICON)
        } else {
            disposable = startProgressUpdate()
            player.play()
            player.player?.addEventListener {
                if (it == AudioPlayerEvent.STOP) {
                    disposable?.dispose()
                }
                Platform.runLater {
                    playBtn.graphic = FontIcon(PLAY_ICON)
                }
            }
            playBtn.graphic = FontIcon(PAUSE_ICON)
        }
    }

    private fun loadFXML() {
        val loader = FXMLLoader(javaClass.getResource("AudioPlayer.fxml"))
        loader.setController(this)
        val root: Node = loader.load()
        children.add(root)
    }
}