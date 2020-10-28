package org.wycliffeassociates.otter.jvm.workbookapp.controls.takecard

import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.control.Control
import javafx.scene.control.Skin
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.jvm.controls.simpleaudioplayer
import org.wycliffeassociates.otter.jvm.controls.card.events.DeleteTakeEvent
import org.wycliffeassociates.otter.jvm.controls.card.events.EditTakeEvent
import org.wycliffeassociates.otter.jvm.controls.card.events.PlayOrPauseEvent
import org.wycliffeassociates.otter.jvm.controls.card.events.PlayOrPauseEvent.PauseEvent
import org.wycliffeassociates.otter.jvm.controls.card.events.PlayOrPauseEvent.PlayEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.takemanagement.TakeCardModel

class TakeCard(
    val model: TakeCardModel,
    playOrPauseEventObservable: Observable<PlayOrPauseEvent>
) : Control() {

    private val logger = LoggerFactory.getLogger(TakeCard::class.java)

    val isAudioPlayingProperty = SimpleBooleanProperty()
    val simpleAudioPlayer = simpleaudioplayer(model.take.file, model.audioPlayer) {
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
        fireEvent(
            EditTakeEvent(model.take) {
                model.audioPlayer.load(model.take.file)
            }
        )
    }

    fun fireDeleteTakeEvent() {
        fireEvent(DeleteTakeEvent(model.take))
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

    private fun subscribeToOtherPlayEvents(playOrPauseEventObservable: Observable<PlayOrPauseEvent>) {
        val sub = playOrPauseEventObservable
            .filter { it is PlayEvent }
            .filter { it.target != this }
            .doOnError { e ->
                logger.error("Error in take card playback event listener", e)
            }
            .subscribe {
                firePauseEvent()
            }
        disposables.add(sub)
    }

    private fun firePauseEvent() {
        fireEvent(PauseEvent())
    }
}

private fun createTakeCard(
    take: TakeCardModel,
    playOrPauseEventObservable: Observable<PlayOrPauseEvent>,
    skinFactory: (TakeCard) -> Skin<TakeCard>,
    init: TakeCard.() -> Unit = {}
): TakeCard {
    val tc = TakeCard(take, playOrPauseEventObservable)
    tc.skin = skinFactory(tc)
    tc.init()
    return tc
}

fun scripturetakecard(
    take: TakeCardModel,
    playOrPauseEventObservable: Observable<PlayOrPauseEvent>,
    init: TakeCard.() -> Unit = {}
): TakeCard {
    return createTakeCard(take, playOrPauseEventObservable, { ScriptureTakeCardSkin(it) }, init)
}

fun resourcetakecard(
    take: TakeCardModel,
    playOrPauseEventObservable: Observable<PlayOrPauseEvent>,
    init: TakeCard.() -> Unit = {}
): TakeCard {
    return createTakeCard(take, playOrPauseEventObservable, { ResourceTakeCardSkin(it) }, init)
}
