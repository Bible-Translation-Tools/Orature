package org.wycliffeassociates.otter.jvm.app.widgets.takecard

import com.jfoenix.controls.JFXButton
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.scene.control.SkinBase
import tornadofx.*

abstract class TakeCardSkin(control: TakeCard) : SkinBase<TakeCard>(control) {

    val playButton = JFXButton()
    val defaultPlayPauseIconSize = 30
    // These can be overridden
    open val playIconView = MaterialIconView(MaterialIcon.PLAY_ARROW, "${defaultPlayPauseIconSize}px")
    open val pauseIconView = MaterialIconView(MaterialIcon.PAUSE, "${defaultPlayPauseIconSize}px")

    init {
        // If playIconView is overriden in child class, the graphic needs to be set in the child class
        playButton.graphic = playIconView

        control.isAudioPlayingProperty.onChange {
            playButton.graphic = when(it) {
                true -> pauseIconView
                false -> playIconView
            }
        }

        playButton.action {
            skinnable.fireEvent(TakeEvent(
                when (control.isAudioPlayingProperty.get()) {
                    true -> TakeEvent.PAUSE
                    false -> TakeEvent.PLAY
                }
            ))
        }
    }
}