package org.wycliffeassociates.otter.jvm.workbookapp.audioplugin

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
import org.wycliffeassociates.otter.jvm.workbookapp.plugin.ParameterizedScope
import org.wycliffeassociates.otter.jvm.workbookapp.plugin.PluginEntrypoint
import tornadofx.*
import java.io.File
import kotlin.jvm.internal.Reflection
import kotlin.reflect.KClass

class AudioPlugin(private val pluginData: AudioPluginData) : IAudioPlugin {

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
                    runInOtterMainWindow(pluginClass, parameters)
                } else {
                    runProcess(
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
                    processArgs = listOf(
                        pluginData.executable,
                        *args
                    )
                )
            }
            .subscribeOn(Schedulers.io())
    }

    private fun buildJarArguments(requestedArgs: List<String>, audioFilePath: String): Parameters {
        val insertedArgs =
            requestedArgs.map { arg ->
                when (arg) {
                    "\${wav}" -> "--wav=$audioFilePath"
                    else -> ""
                }
            }.filterNot {
                it.isEmpty()
            }.ifEmpty {
                listOf("--wav=$audioFilePath")
            }
        return ParametersImpl(insertedArgs)
    }

    private fun buildBinArguments(requestedArgs: List<String>, audioFilePath: String): Array<String> {
        return requestedArgs.map { arg ->
            when (arg) {
                "\${wav}" -> "$audioFilePath"
                else -> ""
            }
        }.filterNot {
            it.isEmpty()
        }.ifEmpty {
            listOf("$audioFilePath")
        }.toTypedArray()
    }

    private fun findPlugin(jar: File): KClass<PluginEntrypoint>? {
        val finder = ClassFinder()
        finder.add(jar)
        val filter = SubclassClassFilter(PluginEntrypoint::class.java)
        val foundClasses = mutableListOf<ClassInfo>()
        finder.findClasses(foundClasses, filter)
        return foundClasses
            .firstOrNull()
            ?.let { foundClass ->
                val pluginClass = javaClass.classLoader.loadClass(foundClass.className)
                @Suppress("UNCHECKED_CAST")
                Reflection.createKotlinClass(pluginClass) as KClass<PluginEntrypoint>
            }
    }

    private fun runProcess(processArgs: List<String>) {
        val processBuilder = ProcessBuilder(processArgs)
        processBuilder.redirectErrorStream(true)
        val process = processBuilder.start()
        process.outputStream.close()
        while (process.inputStream.read() >= 0) {
        }
        process.waitFor()
    }

    private fun runInOtterMainWindow(pluginClass: KClass<PluginEntrypoint>, parameters: Parameters) {
        val appWorkspace: Workspace = find()
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