package org.wycliffeassociates.otter.jvm.workbookapp.plugin

import com.sun.javafx.application.ParametersImpl
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import javafx.application.Application.Parameters
import javafx.application.Platform
import org.clapper.util.classutil.ClassFinder
import org.clapper.util.classutil.ClassInfo
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.domain.plugins.AudioPluginData
import org.wycliffeassociates.otter.common.domain.plugins.IAudioPlugin
import org.wycliffeassociates.otter.common.domain.plugins.PluginParameters
import org.wycliffeassociates.otter.jvm.workbookplugin.plugin.ParameterizedScope
import org.wycliffeassociates.otter.jvm.workbookplugin.plugin.PluginEntrypoint
import tornadofx.*
import java.io.File
import java.net.URL
import java.net.URLClassLoader
import java.text.MessageFormat
import kotlin.jvm.internal.Reflection
import kotlin.reflect.KClass

class AudioPlugin(private val pluginData: AudioPluginData) : IAudioPlugin {

    private val logger = LoggerFactory.getLogger(AudioPlugin::class.java)

    private val monitor = Object()

    override fun isNativePlugin(): Boolean {
        val pluginClass = findPlugin(File(pluginData.executable))
        return pluginClass != null
    }

    override fun launch(audioFile: File, pluginParameters: PluginParameters): Completable {
        return when (File(pluginData.executable).extension) {
            "jar" -> launchJar(audioFile, pluginParameters)
            else -> launchBin(audioFile)
        }
    }

    private fun launchJar(audioFile: File, pluginParameters: PluginParameters): Completable {
        val parameters = buildJarArguments(pluginData.args, audioFile.absolutePath, pluginParameters)
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
            }
            .doOnError { e ->
                logger.error("Error in launch jar for file: $audioFile with params: $pluginParameters", e)
            }
            .subscribeOn(Schedulers.io())
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
            .doOnError { e ->
                logger.error("Error in launch bin for file: $audioFile", e)
            }
            .subscribeOn(Schedulers.io())
    }

    private fun buildJarArguments(
        requestedArgs: List<String>,
        audioFilePath: String,
        pluginParameters: PluginParameters
    ): Parameters {
        val insertedArgs =
            requestedArgs.map { arg ->
                when (arg) {
                    "\${wav}" -> "--wav=$audioFilePath"
                    "\${language}" -> "--language=${pluginParameters.languageName}"
                    "\${book}" -> "--book=${pluginParameters.bookTitle}"
                    else -> ""
                }
            }.filterNot {
                it.isEmpty()
            }.ifEmpty {
                listOf(
                    "--wav=$audioFilePath",
                    "--language=${pluginParameters.languageName}",
                    "--book=${pluginParameters.bookTitle}",
                    "--chapter=${pluginParameters.chapterLabel}",
                    "--chapter_number=${pluginParameters.chapterNumber}",
                    "--marker_total=${pluginParameters.verseTotal}",
                    (if (pluginParameters.chunkLabel != null) "--unit=${pluginParameters.chunkLabel}" else ""),
                    (if (pluginParameters.chunkNumber != null) "--unit_number=${pluginParameters.chunkNumber}" else ""),
                    (if (pluginParameters.resourceLabel != null) "--resource=${pluginParameters.resourceLabel}" else ""),
                    "--chapter_audio=${pluginParameters.sourceChapterAudio?.absolutePath}",
                    "--source_chunk_start=${pluginParameters.sourceChunkStart}",
                    "--source_chunk_end=${pluginParameters.sourceChunkEnd}",
                    "--source_text=${pluginParameters.sourceText}",
                    "--action_title=${pluginParameters.actionText}",
                    "--content_title=${
                        MessageFormat.format(
                            FX.messages["bookChapterTitle"],
                            pluginParameters.bookTitle,
                            pluginParameters.chapterNumber
                        )
                    }"
                )
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
        val foundClasses = mutableListOf<ClassInfo>()
        finder.findClasses(foundClasses, null)
        return foundClasses
            .firstOrNull {
                PluginEntrypoint::class.java.name == it.superClassName
            }
            ?.let { foundClass ->
                val urls = arrayOf(URL("jar:file:${jar.absolutePath}!/"))
                val jarClassLoader = URLClassLoader.newInstance(urls)
                val pluginClass = jarClassLoader.loadClass(foundClass.className)
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
        val scope = ParameterizedScope(parameters) {
            synchronized(monitor) {
                monitor.notify()
                appWorkspace.navigateBack()
            }
        }
        Platform.runLater {
            val plugin = find(pluginClass, scope)
            appWorkspace.dock(plugin)
        }
        synchronized(monitor) {
            monitor.wait()
        }
    }
}
