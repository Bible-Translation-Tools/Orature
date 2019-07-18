package org.wycliffeassociates.otter.jvm.app.widgets.takecard

import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.control.Control
import javafx.scene.control.Skin
import org.wycliffeassociates.otter.jvm.app.widgets.simpleaudioplayer
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.device.IAudioPlayer

class TakeCard(
    val take: Take,
    private val player: IAudioPlayer,
    playOrPauseEventObservable: Observable<PlayOrPauseEvent?>
) : Control() {
    val isAudioPlayingProperty = SimpleBooleanProperty()
    val simpleAudioPlayer = simpleaudioplayer(take.file, player) {
        isAudioPlayingProperty.bind(isPlaying)
    }
    private val disposables = CompositeDisposable()

    init {
        addPlayEventHandler()
        addPauseEventHandler()
        subscribeToOtherPlayEvents(playOrPauseEventObservable)
    }

    @Suppress("ProtectedInFinal", "Unused")
    protected fun finalize() {
        clearDisposables()
    }

    fun fireEditTakeEvent() {
        fireEvent(EditTakeEvent(EditTakeEvent.EDIT_TAKE, take) {
            player.load(take.file)
        })
    }

    fun fireDeleteTakeEvent() {
        fireEvent(DeleteTakeEvent(DeleteTakeEvent.DELETE_TAKE, take))
    }

    private fun clearDisposables() {
        disposables.clear()
    }

    private fun addPlayEventHandler() {
        addEventHandler(PlayOrPauseEvent.PLAY) {
            simpleAudioPlayer.buttonPressed()
        }
    }

    private fun addPauseEventHandler() {
        addEventHandler(PlayOrPauseEvent.PAUSE) {
            if (isAudioPlayingProperty.get()) {
                simpleAudioPlayer.buttonPressed()
            }
        }
    }

    private fun subscribeToOtherPlayEvents(playOrPauseEventObservable: Observable<PlayOrPauseEvent?>) {
        val sub = playOrPauseEventObservable
            .filter { it.eventType == PlayOrPauseEvent.PLAY }
            .filter { it.target != this }
            .subscribe {
                firePauseEvent()
            }
        disposables.add(sub)
    }

    private fun firePauseEvent() {
        fireEvent(PlayOrPauseEvent(PlayOrPauseEvent.PAUSE))
    }
}

private fun createTakeCard(
    take: Take,
    player: IAudioPlayer,
    playOrPauseEventObservable: Observable<PlayOrPauseEvent?>,
    skinFactory: (TakeCard) -> Skin<TakeCard>,
    init: TakeCard.() -> Unit = {}
): TakeCard {
    val tc = TakeCard(take, player, playOrPauseEventObservable)
    tc.skin = skinFactory(tc)
    tc.init()
    return tc
}

fun scripturetakecard(
    take: Take,
    player: IAudioPlayer,
    playOrPauseEventObservable: Observable<PlayOrPauseEvent?>,
    init: TakeCard.() -> Unit = {}
): TakeCard {
    return createTakeCard(take, player, playOrPauseEventObservable, { ScriptureTakeCardSkin(it) }, init)
}

fun resourcetakecard(
    take: Take,
    player: IAudioPlayer,
    playOrPauseEventObservable: Observable<PlayOrPauseEvent?>,
    init: TakeCard.() -> Unit = {}
): TakeCard {
    return createTakeCard(take, player, playOrPauseEventObservable, { ResourceTakeCardSkin(it) }, init)
}