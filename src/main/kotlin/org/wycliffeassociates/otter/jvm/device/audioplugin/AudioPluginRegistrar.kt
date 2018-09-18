package org.wycliffeassociates.otter.jvm.device.audioplugin

import org.wycliffeassociates.otter.jvm.device.audioplugin.parser.ParsedAudioPluginData
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.reactivex.Completable
import io.reactivex.Observable
import org.wycliffeassociates.otter.common.data.audioplugin.AudioPluginData
import org.wycliffeassociates.otter.common.data.dao.Dao
import org.wycliffeassociates.otter.common.domain.IAudioPluginRegistrar
import org.wycliffeassociates.otter.jvm.device.audioplugin.parser.ParsedAudioPluginDataMapper
import java.io.File

// Imports plugin data files into database
class AudioPluginRegistrar(private val pluginDataDao: Dao<AudioPluginData>) : IAudioPluginRegistrar {
    // Configure Jackson YAML processor
    private val mapper = ObjectMapper(YAMLFactory()).registerKotlinModule()

    override fun import(pluginFile: File): Completable {
        return Completable.fromObservable(
                Observable
                        .fromCallable {
                            val parsedAudioPlugin: ParsedAudioPluginData = mapper.readValue(pluginFile)
                            ParsedAudioPluginDataMapper().mapToAudioPluginData(parsedAudioPlugin, pluginFile)
                        }
                        .flatMap {
                            pluginDataDao.insert(it)
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