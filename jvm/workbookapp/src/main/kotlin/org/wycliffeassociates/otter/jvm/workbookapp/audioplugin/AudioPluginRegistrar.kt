package org.wycliffeassociates.otter.jvm.workbookapp.audioplugin

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import java.io.File
import org.wycliffeassociates.otter.common.domain.plugins.IAudioPluginRegistrar
import org.wycliffeassociates.otter.common.persistence.repositories.IAudioPluginRepository
import org.wycliffeassociates.otter.jvm.workbookapp.audioplugin.parser.ParsedAudioPluginData
import org.wycliffeassociates.otter.jvm.workbookapp.audioplugin.parser.ParsedAudioPluginDataMapper

// Imports plugin data files into database
class AudioPluginRegistrar(private val audioPluginRepository: IAudioPluginRepository) : IAudioPluginRegistrar {
    // Configure Jackson YAML processor
    private val mapper = ObjectMapper(YAMLFactory()).registerKotlinModule()

    override fun import(pluginFile: File): Completable {
        return Completable.fromSingle(
                Single
                        .fromCallable {
                            val parsedAudioPlugin: ParsedAudioPluginData = mapper.readValue(pluginFile)
                            ParsedAudioPluginDataMapper().mapToAudioPluginData(parsedAudioPlugin, pluginFile)
                        }
                        .flatMap {
                            audioPluginRepository.insert(it)
                        }
        )
    }

    override fun importAll(pluginDir: File): Completable {
        val audioPluginCompletables = pluginDir
                .listFiles()
                .filter {
                    // Only load yaml files
                    it
                            .path
                            .toLowerCase()
                            .endsWith(".yaml")
                }
                .map {
                    import(it)
                }
        return Completable.fromObservable(Observable
                .fromIterable(audioPluginCompletables)
                .flatMap {
                    it
                            .toSingleDefault(true)
                            .onErrorReturnItem(false)
                            .toObservable()
                }
        )
    }
}
