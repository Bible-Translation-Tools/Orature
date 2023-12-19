import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.junit.Test
import java.util.concurrent.TimeUnit

class DeleteMe {

    lateinit var disposableInnerTask: Disposable

    @Test
    fun deleteMe() {
        println("\n|\n|")
        val task = bounceAudioTask().subscribe()

        Thread.sleep(2000) // background execution
        task.dispose() /* cancel - interrupt & terminate */
//        disposableInnerTask.dispose()
        Thread.sleep(5000) // background execution
    }

    /**
     * This task can be cancelled by calling .dispose() on the subscription
     */
    private fun bounceAudioTask(): Completable {
        return Completable
            .create { emitter ->

                // this block represents the actual task execution
                val disposableTask = Observable
                    .interval(500, TimeUnit.MILLISECONDS)
                    .doOnNext { intervalCount ->
                        println("Executing step $intervalCount")
                        if (intervalCount > 5) {
                            println("Final step reached. Wrapping up")
                            emitter.onComplete()
                        }
                    }
                    .doOnDispose {
                        println("Inner task disposed")
                    }
                    .subscribe()

                disposableInnerTask = disposableTask

                emitter.setDisposable(
                    disposableTask
                )
            }
            .doOnDispose {
                println("Cancelled!")
            }
            .doOnComplete {
                println("Successfully completed!")
            }
            .subscribeOn(Schedulers.io())
    }
}