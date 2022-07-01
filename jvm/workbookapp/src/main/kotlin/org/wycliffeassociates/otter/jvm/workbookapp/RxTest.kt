package org.wycliffeassociates.otter.jvm.workbookapp

import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

fun main() {
    val obs = Observable.fromCallable {
        for (i in 0..10) {
            println(i)
            try {
                Thread.sleep(1000)
            } catch (e: InterruptedException) {}

        }
    }
        .subscribeOn(Schedulers.io())
        .subscribe({},{},{ println("complete")})

    Thread.sleep(2000)
    obs.dispose()



    val disposable = RxTest.create()
        .cache()
        .subscribeOn(Schedulers.io())
        .observeOn(Schedulers.io())
        .subscribe(
            { println("$it ${Thread.currentThread()}") },
            {},
            { println("complete 1") }
        )
    Thread.sleep(5000)
    val dis2 = RxTest.create()
        .subscribeOn(Schedulers.io())
        .observeOn(Schedulers.io())
        .subscribe(
            { println("    $it ${Thread.currentThread()}") },
            {},
            { println("complete 2") }
        )
    Thread.sleep(5000)
    disposable.dispose()
    Thread.sleep(5000)

    RxTest.cancel()

    while (RxTest.started) {
    }
}

class RxTest() {
    companion object {

        private val buffer = mutableListOf<Int>()
        private var listeners = mutableListOf<ObservableEmitter<Int>>()

        @Volatile
        var started = false

        @Volatile
        var cancelled = false

        fun start() {
            Thread {
                while (listeners.any { !it.isDisposed } || cancelled) {
                    val randInt = (Math.random() * 100).toInt()
                    buffer.add(randInt)
                    listeners.forEach {
                        it.onNext(randInt)
                    }
                    Thread.sleep(1000)
                }
                buffer.clear()
                listeners.clear()
                started = false
            }.start()
        }

        fun cancel() {
            cancelled = true
            listeners.forEach { it.onComplete() }
        }

        fun create(): Observable<Int> {
            return Observable.create { emitter ->
                println("subscribing on ${Thread.currentThread()}")
                emitter.setDisposable(
                    object : Disposable {
                        @Volatile
                        var disposed = false

                        override fun dispose() {
                            emitter.onComplete()
                            println("dispose on ${Thread.currentThread()}")
                            disposed = true
                        }

                        override fun isDisposed(): Boolean {
                            return disposed
                        }
                    }
                )
                buffer.forEach {
                    emitter.onNext(it)
                }
                listeners.add(emitter)
                if (!started) {
                    started = true
                    start()
                }
            }
        }
    }
}
