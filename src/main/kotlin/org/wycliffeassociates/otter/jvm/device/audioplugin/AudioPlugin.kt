package org.wycliffeassociates.otter.jvm.device.audioplugin

import com.sun.corba.se.spi.orbutil.threadpool.Work
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import javafx.application.Platform
import javafx.stage.Stage
import org.clapper.util.classutil.ClassFilter
import org.clapper.util.classutil.ClassFinder
import org.clapper.util.classutil.ClassInfo
import org.clapper.util.classutil.SubclassClassFilter
import org.wycliffeassociates.otter.common.data.audioplugin.AudioPluginData
import org.wycliffeassociates.otter.common.data.audioplugin.IAudioPlugin
import org.wycliffeassociates.otter.jvm.recorder.app.view.RecorderView
import tornadofx.*
import java.io.File
import java.net.URL
import java.net.URLClassLoader
import java.util.jar.JarFile

class ParameterizedScope(
    val parameters: Map<String, String>,
    private val onNavigateBackCallback: () -> Unit,
    workspace: Workspace
): Scope(workspace) {
    fun navigateBack() {
        onNavigateBackCallback()
        workspace.navigateBack()
    }
}

class AudioPlugin(private val pluginData: AudioPluginData) : IAudioPlugin {

    val monitor = Object()

    override fun launch(audioFile: File): Completable {
        return when (File(pluginData.executable).extension) {
            "jar" -> launchJar(audioFile)
            else -> launchBin(audioFile)
        }
    }

    private fun launchJar(audioFile: File): Completable {
        return Completable
            .fromCallable {
                val finder = ClassFinder()
                finder.add(File(pluginData.executable))
                val filter = SubclassClassFilter(App::class.java)
                val foundClasses = mutableListOf<ClassInfo>()
                finder.findClasses(foundClasses, filter)
                foundClasses.forEach {
                    println(it.className)
                }
            }.subscribeOn(Schedulers.io())
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