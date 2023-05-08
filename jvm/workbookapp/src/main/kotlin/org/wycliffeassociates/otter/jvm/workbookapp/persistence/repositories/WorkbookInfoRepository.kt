package org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories

import io.reactivex.Single
import org.wycliffeassociates.otter.common.data.primitives.Collection
import org.wycliffeassociates.otter.common.data.primitives.ContainerType
import org.wycliffeassociates.otter.common.data.workbook.Translation
import org.wycliffeassociates.otter.common.data.workbook.WorkbookInfo
import org.wycliffeassociates.otter.common.domain.resourcecontainer.SourceAudioAccessor
import org.wycliffeassociates.otter.common.persistence.repositories.ICollectionRepository
import org.wycliffeassociates.otter.common.persistence.repositories.IContentRepository
import org.wycliffeassociates.otter.common.persistence.repositories.IWorkbookInfoRepository
import javax.inject.Inject

class WorkbookInfoRepository @Inject constructor(
    private val collectionRepo: ICollectionRepository,
    private val contentRepo: IContentRepository
) : IWorkbookInfoRepository {

    override fun getProjects(translation: Translation): Single<List<WorkbookInfo>> {
        return collectionRepo.getDerivedProjects()
            .map { projects ->
                projects
                    .filter {
                        it.resourceContainer?.type == ContainerType.Book &&
                            it.resourceContainer?.language == translation.target
                    }
                    .mapNotNull { project ->
                        val sourceCollection = collectionRepo.getSource(project).blockingGet()
                        if (sourceCollection.resourceContainer?.language == translation.source) {
                            Pair(project, sourceCollection.resourceContainer!!)
                        } else {
                            null
                        }
                    }
                    .map { (collection, sourceMetadata) ->
                        WorkbookInfo(
                            collection.id,
                            collection.slug,
                            collection.titleKey,
                            collection.labelKey,
                            getProgress(collection),
                            collection.modifiedTs!!,
                            SourceAudioAccessor.hasSourceAudio(
                                sourceMetadata,
                                collection.slug
                            )
                        )
                    }
            }
    }

    private fun getProgress(collection: Collection): Double {
        val chapters = collectionRepo.getChildren(collection)
            .flattenAsObservable { it }
            .flatMapSingle { chapter ->
                contentRepo.getCollectionMetaContent(chapter)
            }
            .blockingIterable().toList()

        return chapters.count { it.selectedTake != null }.toDouble() / chapters.size
    }
}