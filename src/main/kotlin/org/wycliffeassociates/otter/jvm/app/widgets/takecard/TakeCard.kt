package org.wycliffeassociates.otter.jvm.app.widgets.takecard

import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.control.Control
import javafx.scene.control.Skin
import org.wycliffeassociates.otter.jvm.app.widgets.simpleaudioplayer
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.device.IAudioPlayer

class TakeCard(val take: Take, player: IAudioPlayer) : Control() {

    val isAudioPlayingProperty = SimpleBooleanProperty()

    val simpleAudioPlayer = simpleaudioplayer(take.file, player) {
        isAudioPlayingProperty.bind(isPlaying)
    }

    init {
        addEventHandler(TakeEvent.PLAY) {
            simpleAudioPlayer.buttonPressed()
        }
        addEventHandler(TakeEvent.PAUSE) {
            if (isAudioPlayingProperty.get()) {
                simpleAudioPlayer.buttonPressed()
            }
        }
    }
}

private fun createTakeCard(
    take: Take, player: IAudioPlayer,
    skinFactory: (TakeCard) -> Skin<TakeCard>,
    init: TakeCard.() -> Unit = {}
): TakeCard {
    val tc = TakeCard(take, player)
    tc.skin = skinFactory(tc)
    tc.init()
    return tc
}

fun scripturetakecard(take: Take, player: IAudioPlayer, init: TakeCard.() -> Unit = {}): TakeCard {
    return createTakeCard(take, player, { ScriptureTakeCardSkin(it) }, init)
}

fun resourcetakecard(take: Take, player: IAudioPlayer, init: TakeCard.() -> Unit = {}): TakeCard {
    return createTakeCard(take, player, { ResourceTakeCardSkin(it) }, init)
}
