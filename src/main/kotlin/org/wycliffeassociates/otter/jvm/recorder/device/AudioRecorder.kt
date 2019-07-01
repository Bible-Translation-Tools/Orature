package org.wycliffeassociates.otter.jvm.recorder.device

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.wycliffeassociates.otter.common.device.IAudioRecorder
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem

class AudioRecorder : IAudioRecorder{

    private val monitor = Object()

    @Volatile
    private var stop = false

    companion object {
        val SAMPLE_RATE = 44100F // Hz
        val SAMPLE_SIZE = 16 // bits
        val CHANNELS = 1
        val SIGNED = true
        val BIG_ENDIAN = false
        val FORMAT = AudioFormat(
            SAMPLE_RATE,
            SAMPLE_SIZE,
            CHANNELS,
            SIGNED,
            BIG_ENDIAN
        )
        val BUFFER_SIZE = 1024
    }

    private var line = AudioSystem.getTargetDataLine(FORMAT)
    private val audioByteObservable = PublishSubject.create<ByteArray>()
    private val recordingStream = Observable.fromCallable {
        val byteArray = ByteArray(BUFFER_SIZE)
        var totalRead = 0
            while (true) {
                if(line.isOpen || line.available() > 0) {
                    totalRead += line.read(byteArray, 0, byteArray.size)
                    audioByteObservable.onNext(byteArray)
                } else {
                    synchronized(monitor) {
                        monitor.wait()
                    }
                }
                if (stop) {
                    line.close()
                    break
                }
            }
    }.subscribeOn(Schedulers.io())
    .subscribe()

    @Synchronized //Synchronized so as to not subscribe to multiple streams on quick multipress
    override fun start() {
        line.open(FORMAT)
        line.start()
        synchronized(monitor) {
            monitor.notify()
        }
    }

    override fun pause() {
        line.stop()
        line.close()
    }

    override fun stop() {
        line.stop()
        stop = true
        synchronized(monitor) {
            monitor.notify()
        }
        audioByteObservable.onComplete()
    }

    override fun getAudioStream(): Observable<ByteArray> {
        return audioByteObservable
    }
}
