package org.wycliffeassociates.otter.jvm.device.audioplugin

import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import org.wycliffeassociates.otter.common.data.audioplugin.AudioPluginData
import org.wycliffeassociates.otter.common.data.audioplugin.IAudioPlugin
import java.io.File
import java.util.jar.JarFile

class AudioPlugin(val pluginData: AudioPluginData) : IAudioPlugin {
    override fun launch(audioFile: File): Completable {
        return when (File(pluginData.executable).extension) {
            "jar" -> launchJar(audioFile)
            else -> launchBin(audioFile)
        }
    }

    private fun launchJar(audioFile: File): Completable {
        return Completable
            .fromCallable {
                // TODO
            }
    }

    private fun launchBin(audioFile: File): Completable {
        return Completable
            .fromCallable {
                // Build and start the process
                val processBuilder = ProcessBuilder(
                    listOf(
                        pluginData.executable,
                        *(pluginData.args.toTypedArray()),
                        audioFile.toString()
                    )
                )
                processBuilder.redirectErrorStream(true)
                val process = processBuilder.start()
                process.outputStream.close()
                while (process.inputStream.read() >= 0) {
                }
                process.waitFor()
            }
            .subscribeOn(Schedulers.io())
    }
}