package org.wycliffeassociates.otter.common.domain.collections

import io.reactivex.Completable
import org.wycliffeassociates.otter.common.persistence.repositories.ICollectionRepository
import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider

class DeleteProject(
    private val collectionRepository: ICollectionRepository,
    private val directoryProvider: IDirectoryProvider
) {

    fun delete(project: Collection, deleteAudio: Boolean): Completable {
        // Order matters here, files won't remove anything from the database
        // delete resources will only remove take entries, but needs derived RCs and links in tact
        // delete project may remove derived RCs and links, and thus needs to be last
        return deleteFiles(project, deleteAudio)
            .andThen(collectionRepository.deleteResources(project, deleteAudio))
            .andThen(collectionRepository.deleteProject(project, deleteAudio))

    }

    private fun deleteFiles(project: Collection, deleteAudio: Boolean): Completable {
        return collectionRepository.getSource(project).doOnSuccess {
            // If project audio should be deleted, get the folder for the project audio and delete it
            if (deleteAudio) {
                val sourceMetadata = it.resourceContainer
                    ?: throw RuntimeException("No source metadata found.")
                val audioDirectory = directoryProvider.getProjectAudioDirectory(
                    source = sourceMetadata,
                    target = project.resourceContainer,
                    book = project
                )
                audioDirectory.deleteRecursively()
            }
        }.ignoreElement()
    }
}