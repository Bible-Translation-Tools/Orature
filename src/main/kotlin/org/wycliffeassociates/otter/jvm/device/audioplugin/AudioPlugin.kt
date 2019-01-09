package org.wycliffeassociates.otter.jvm.device.audioplugin

import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import org.wycliffeassociates.otter.common.data.audioplugin.AudioPluginData
import org.wycliffeassociates.otter.common.data.audioplugin.IAudioPlugin
import java.io.File

class AudioPlugin(val pluginData: AudioPluginData) : IAudioPlugin {
    override fun launch(file: File): Completable {
        return Completable
                .fromCallable {
                    // Build and start the process
                    val processBuilder = ProcessBuilder(
                            listOf(
                                    pluginData.executable,
                                    *(pluginData.args.toTypedArray()),
                                    file.toString()
                            )
                    )
                    processBuilder.redirectErrorStream(true)
                    val process = processBuilder.start()
                    process.outputStream.close()
                    while (process.inputStream.read() >= 0) { }
                    process.waitFor()
                }
                .subscribeOn(Schedulers.io())
    }
}