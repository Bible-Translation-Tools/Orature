package org.wycliffeassociates.otter.jvm.app.widgets.takecard

import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.control.Control
import javafx.scene.control.Skin
import org.wycliffeassociates.otter.jvm.app.widgets.simpleaudioplayer
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.device.IAudioPlayer

class TakeCard(val take: Take, player: IAudioPlayer, takeEventObservable: Observable<TakeEvent?>) : Control() {
    val isAudioPlayingProperty = SimpleBooleanProperty()
    val simpleAudioPlayer = simpleaudioplayer(take.file, player) {
        isAudioPlayingProperty.bind(isPlaying)
    }
    private val disposables = CompositeDisposable()

    init {
        addPlayEventHandler()
        addPauseEventHandler()
        subscribeToOtherPlayEvents(takeEventObservable)
    }

    @Suppress("ProtectedInFinal", "Unused")
    protected fun finalize() {
        clearDisposables()
    }

    private fun clearDisposables() {
        disposables.clear()
    }

    private fun addPlayEventHandler() {
        addEventHandler(TakeEvent.PLAY) {
            simpleAudioPlayer.buttonPressed()
        }
    }

    private fun addPauseEventHandler() {
        addEventHandler(TakeEvent.PAUSE) {
            if (isAudioPlayingProperty.get()) {
                simpleAudioPlayer.buttonPressed()
            }
        }
    }

    private fun subscribeToOtherPlayEvents(takeEventObservable: Observable<TakeEvent?>) {
        val sub = takeEventObservable
            .filter { it.eventType == TakeEvent.PLAY }
            .filter { it.target != this }
            .subscribe {
                firePauseEvent()
            }
        disposables.add(sub)
    }

    private fun firePauseEvent() {
        fireEvent(TakeEvent(TakeEvent.PAUSE))
    }
}

private fun createTakeCard(
    take: Take,
    player: IAudioPlayer,
    takeEventObservable: Observable<TakeEvent?>,
    skinFactory: (TakeCard) -> Skin<TakeCard>,
    init: TakeCard.() -> Unit = {}
): TakeCard {
    val tc = TakeCard(take, player, takeEventObservable)
    tc.skin = skinFactory(tc)
    tc.init()
    return tc
}

fun scripturetakecard(
    take: Take,
    player: IAudioPlayer,
    takeEventObservable: Observable<TakeEvent?>,
    init: TakeCard.() -> Unit = {}
): TakeCard {
    return createTakeCard(take, player, takeEventObservable, { ScriptureTakeCardSkin(it) }, init)
}

fun resourcetakecard(
    take: Take,
    player: IAudioPlayer,
    takeEventObservable: Observable<TakeEvent?>,
    init: TakeCard.() -> Unit = {}
): TakeCard {
    return createTakeCard(take, player, takeEventObservable, { ResourceTakeCardSkin(it) }, init)
}