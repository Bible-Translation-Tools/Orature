package org.wycliffeassociates.otter.jvm.device.audioplugin

import com.sun.javafx.application.ParametersImpl
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import javafx.application.Platform
import org.clapper.util.classutil.ClassFinder
import org.clapper.util.classutil.ClassInfo
import org.clapper.util.classutil.SubclassClassFilter
import org.wycliffeassociates.otter.common.data.audioplugin.AudioPluginData
import org.wycliffeassociates.otter.common.data.audioplugin.IAudioPlugin
import org.wycliffeassociates.otter.jvm.app.ui.AppWorkspace
import org.wycliffeassociates.otter.jvm.plugin.ParameterizedScope
import org.wycliffeassociates.otter.jvm.plugin.PluginEntrypoint
import org.wycliffeassociates.otter.jvm.recorder.app.view.RecorderView
import tornadofx.*
import java.io.File

class AudioPlugin(private val pluginData: AudioPluginData) : IAudioPlugin {

    private val appWorkspace: AppWorkspace = find()
    private val monitor = Object()

    override fun launch(audioFile: File): Completable {
        return when (File(pluginData.executable).extension) {
            "jar" -> launchJar(audioFile)
            //else -> launchBin(audioFile)
            else -> launchJar(audioFile)
        }
    }

    private fun launchJar(audioFile: File): Completable {
        return Completable
            .fromCallable {
                val finder = ClassFinder()
                finder.add(File(pluginData.executable))
                val filter = SubclassClassFilter(PluginEntrypoint::class.java)
                val foundClasses = mutableListOf<ClassInfo>()
                finder.findClasses(foundClasses, filter)
                val filtered = foundClasses.filter {
                    it.className.contains("RecordingApp")
                }
                //if (filtered.isNotEmpty()) {
                if (true) {
                    //val app = javaClass.classLoader.loadClass(filtered.first().className)
                    //val plugin = app.newInstance() as PluginEntrypoint
                    val parameters = ParametersImpl(
                        insertRequestedArguments(
                            listOf(
                                "\${wav}",
                                "\${lang}",
                                "\${book}",
                                "\${chap}",
                                "\${cnum}",
                                "\${unit}",
                                "\${unum}"
                            ),
                            audioFile.absolutePath
                        )
                    )
                    val scope = ParameterizedScope(parameters)
                    {
                        synchronized(monitor) {
                            monitor.notify()
                            appWorkspace.navigateBack()
                        }
                    }

                    val recorder = find<RecorderView>(scope)
                    Platform.runLater {
                        appWorkspace.dock(recorder)
                    }
                    synchronized(monitor) {
                        monitor.wait()
                    }
                }
            }.subscribeOn(Schedulers.io())
    }

    private fun launchBin(audioFile: File): Completable {
        val args = insertRequestedArguments(pluginData.args, audioFile.absolutePath)
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

    private fun insertRequestedArguments(requestedArgs: List<String>, audioFilePath: String): List<String> {
        val insertedArgs = mutableListOf<String>()
        requestedArgs.forEach { arg ->
            insertedArgs.add(
                when (arg) {
                    "\${wav}" -> "--wav=$audioFilePath"
                    "\${lang}" -> "--lang=English"
                    "\${book}" -> "--book=Matthew"
                    "\${chap}" -> "--chap=Chapter"
                    "\${cnum}" -> "--cnum=1"
                    "\${unit}" -> "--unit=Verse"
                    "\${unum}" -> "--unum=1"
                    else -> ""
                }
            )
        }
        return insertedArgs
    }
}