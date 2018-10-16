package org.wycliffeassociates.otter.common.domain

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.wycliffeassociates.otter.common.data.model.*
import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.repositories.*
import java.io.File
import java.time.LocalDate

class CreateProject(
        val languageRepo: ILanguageRepository,
        val sourceRepo: ISourceRepository,
        val collectionRepo: ICollectionRepository,
        val projectRepo: IProjectRepository,
        val chunkRepo: IChunkRepository,
        val metadataRepository: IResourceMetadataRepository,
        val directoryProvider: IDirectoryProvider
) {
    fun getAllLanguages(): Single<List<Language>> {
        return languageRepo.getAll()
    }

    fun getSourceRepos(): Single<List<Collection>> {
        return sourceRepo.getAllRoot()
    }

    fun getAll(): Single<List<Collection>> {
        return collectionRepo.getAll()
    }

    private fun insertProjectCollection(
            projCollection: Collection,
            source: Collection,
            parent: Collection? = null
    ): Single<Collection> {
        return projectRepo
                // Insert the project collection
                .insert(projCollection)
                .doOnSuccess {
                    projCollection.id = it
                }
                // Update the source
                .toCompletable()
                .andThen(projectRepo.updateSource(projCollection, source))
                // Update the parent
                .andThen(
                    if (parent != null) {
                        projectRepo.updateParent(projCollection, parent)
                    } else
                        Completable.complete()
                )
                .toSingle {
                    projCollection
                }
    }

    private fun createProjectResourceMetadata(
            sourceMetadata: ResourceMetadata,
            targetLanguage: Language
    ): ResourceMetadata {
        // Does not actually create RC on disk
        val derivedMetadata = ResourceMetadata(
                sourceMetadata.conformsTo,
                "user",
                "",
                "",
                sourceMetadata.identifier,
                LocalDate.now(),
                targetLanguage,
                LocalDate.now(),
                "",
                sourceMetadata.subject,
                "book",
                sourceMetadata.title,
                "0.0.1",
                directoryProvider.resourceContainerDirectory // TODO: Use valid path
        )

        return derivedMetadata
    }

    fun newProject(sourceProject: Collection, targetLanguage: Language): Completable {
        // Some concat maps can be removed when dao synchronization is added
        if (sourceProject.resourceContainer == null) throw NullPointerException("Source project has no metadata")

        val metadata = createProjectResourceMetadata(sourceProject.resourceContainer!!, targetLanguage)
        return metadataRepository
                .insert(metadata)
                .doOnSuccess {
                    metadata.id = it
                }
                // Add the link to the source RC metadata
                .flatMapCompletable {
                    metadataRepository.addLink(metadata, sourceProject.resourceContainer!!)
                }
                // Insert the new project with the new metadata
                .andThen(insertProjectCollection(
                        sourceProject.copy(
                                id = 0,
                                labelKey = "project",
                                resourceContainer = metadata
                        ),
                        sourceProject
                ))
                // Get the chapter
                // No need to concatMap since only one thing in the stream (a Single)
                // No issue with order
                .flatMap { project ->
                    sourceRepo.getChildren(sourceProject).map { Pair(project, it) }
                }
                // Split the chapter list into a stream
                .flatMapObservable { (project, sourceChapters) ->
                    Observable.fromIterable(sourceChapters.map { Pair(project, it) })
                }
                // Insert each new project chapter
                .concatMap { (project, sourceChapter) ->
                    val projectChapter = sourceChapter.copy(id = 0, resourceContainer = metadata)
                    return@concatMap insertProjectCollection(projectChapter, sourceChapter, project)
                            .map { Pair(it, sourceChapter) }
                            .toObservable()
                }
                // Get all the chunks for the source chapter
                .concatMap { (projectChapter, sourceChapter) ->
                    chunkRepo
                            .getByCollection(sourceChapter)
                            .map { Pair(projectChapter, it) }
                            .toObservable()
                }
                // Split the list of chunks into a stream
                .concatMap { (projectChapter, chunks) ->
                    Observable.fromIterable(chunks.map { Pair(projectChapter, it) })
                }
                // Insert each new project chunk
                .concatMap { (projectChapter, sourceChunk) ->
                    val projectChunk = sourceChunk.copy(id = 0, selectedTake = null)
                    return@concatMap chunkRepo.insertForCollection(projectChunk, projectChapter)
                            .map {
                                projectChunk.id = it
                                return@map Pair(sourceChunk, projectChunk)
                            }
                            .toObservable()
                }
                // Add the source/target relationship for the chunk
                .concatMapCompletable { (sourceChunk, projectChunk) ->
                    chunkRepo.updateSources(projectChunk, listOf(sourceChunk))
                }
    }

    fun getResourceChildren(identifier: SourceCollection): Single<List<Collection>> {
        return sourceRepo.getChildren(identifier)
    }
}