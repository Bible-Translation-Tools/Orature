package org.wycliffeassociates.otter.jvm.device.audioplugin

import com.sun.javafx.application.ParametersImpl
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import javafx.application.Application.Parameters
import javafx.application.Platform
import org.clapper.util.classutil.ClassFinder
import org.clapper.util.classutil.ClassInfo
import org.clapper.util.classutil.SubclassClassFilter
import org.wycliffeassociates.otter.common.data.audioplugin.AudioPluginData
import org.wycliffeassociates.otter.common.data.audioplugin.IAudioPlugin
import org.wycliffeassociates.otter.jvm.app.ui.AppWorkspace
import org.wycliffeassociates.otter.jvm.plugin.ParameterizedScope
import org.wycliffeassociates.otter.jvm.plugin.PluginEntrypoint
import tornadofx.*
import java.io.File
import kotlin.jvm.internal.Reflection
import kotlin.reflect.KClass

class AudioPlugin(private val pluginData: AudioPluginData) : IAudioPlugin {

    private val appWorkspace: AppWorkspace = find()
    private val monitor = Object()

    override fun launch(audioFile: File): Completable {
        return when (File(pluginData.executable).extension) {
            "jar" -> launchJar(audioFile)
            else -> launchBin(audioFile)
        }
    }

    private fun launchJar(audioFile: File): Completable {
        val parameters = buildJarArguments(pluginData.args, audioFile.absolutePath)
        return Completable
            .fromCallable {
                val pluginClass = findPlugin(File(pluginData.executable))
                if (pluginClass != null) {
                    runInWindow(pluginClass, parameters)
                } else {
                    runProcess(
                        audioFile,
                        processArgs = listOf(
                            "java",
                            "-jar",
                            pluginData.executable,
                            *parameters.raw.toTypedArray()
                        )
                    )
                }
            }.subscribeOn(Schedulers.io())
    }

    private fun launchBin(audioFile: File): Completable {
        val args = buildBinArguments(pluginData.args, audioFile.absolutePath)
        return Completable
            .fromCallable {
                runProcess(
                    audioFile,
                    processArgs = listOf(
                        pluginData.executable,
                        *args
                    )
                )
            }
            .subscribeOn(Schedulers.io())
    }

    private fun buildJarArguments(requestedArgs: List<String>, audioFilePath: String): Parameters {
        val insertedArgs = mutableListOf<String>()
        requestedArgs.forEach { arg ->
            insertedArgs.add(
                when (arg) {
                    "\${wav}" -> "--wav=$audioFilePath"
                    else -> ""
                }
            )
        }
        insertedArgs.removeAll { it.isEmpty() }
        if (insertedArgs.isEmpty()) {
            insertedArgs.add("--wav=$audioFilePath")
        }
        return ParametersImpl(insertedArgs)
    }

    private fun buildBinArguments(requestedArgs: List<String>, audioFilePath: String): Array<String> {
        val insertedArgs = mutableListOf<String>()
        requestedArgs.forEach { arg ->
            insertedArgs.add(
                when (arg) {
                    "\${wav}" -> audioFilePath
                    else -> ""
                }
            )
        }
        insertedArgs.removeAll { it.isEmpty() }
        if (insertedArgs.isEmpty()) {
            insertedArgs.add(audioFilePath)
        }
        return insertedArgs.toTypedArray()
    }

    private fun findPlugin(jar: File): KClass<PluginEntrypoint>? {
        val finder = ClassFinder()
        finder.add(jar)
        val filter = SubclassClassFilter(PluginEntrypoint::class.java)
        val foundClasses = mutableListOf<ClassInfo>()
        finder.findClasses(foundClasses, filter)
        return if (foundClasses.isNotEmpty()) {
            val pluginClass = javaClass.classLoader.loadClass(foundClasses.first().className)
            Reflection.createKotlinClass(pluginClass) as KClass<PluginEntrypoint>
        } else {
            null
        }
    }


    private fun runProcess(audioFile: File, processArgs: List<String>) {
        // Build and start the process
        val processBuilder = ProcessBuilder(processArgs)
        processBuilder.redirectErrorStream(true)
        val process = processBuilder.start()
        process.outputStream.close()
        while (process.inputStream.read() >= 0) {
        }
        process.waitFor()
    }

    private fun runInWindow(pluginClass: KClass<PluginEntrypoint>, parameters: Parameters) {
        val scope = ParameterizedScope(parameters)
        {
            synchronized(monitor) {
                monitor.notify()
                appWorkspace.navigateBack()
            }
        }
        val plugin = find(pluginClass, scope)
        Platform.runLater {
            appWorkspace.dock(plugin)
        }
        synchronized(monitor) {
            monitor.wait()
        }
    }
}