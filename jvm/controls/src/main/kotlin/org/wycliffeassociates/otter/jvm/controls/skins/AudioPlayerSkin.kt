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
import org.wycliffeassociates.otter.jvm.controls.AudioPlayer
import java.util.concurrent.TimeUnit
import javax.sound.sampled.LineEvent

private const val PLAY_ICON = "fa-play"
private const val PAUSE_ICON = "fa-pause"

class AudioPlayerSkin(private val player: AudioPlayer) : SkinBase<AudioPlayer>(player) {

    @FXML
    lateinit var recordBtn: Button
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
        recordBtn.setOnMouseClicked {
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
            println("was playing now pause")
            player.pause()
            recordBtn.graphic = FontIcon(PLAY_ICON)
        } else {
            println("was pause now playing")
            disposable = startProgressUpdate()
            player.play()
            player.clip.addLineListener {
                if (it.type == LineEvent.Type.STOP) {
                    disposable?.dispose()
                    player.clip.microsecondPosition = 0L
                    audioSlider.value = 0.0
                    Platform.runLater {
                        recordBtn.graphic = FontIcon(PLAY_ICON)
                    }
                }
            }
            recordBtn.graphic = FontIcon(PAUSE_ICON)
        }
    }

    private fun loadFXML() {
        val loader = FXMLLoader(javaClass.getResource("AudioPlayer.fxml"))
        loader.setController(this)
        val root: Node = loader.load()
        children.add(root)
    }
}