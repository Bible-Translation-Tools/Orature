package org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories

import io.reactivex.Single
import org.wycliffeassociates.otter.common.data.primitives.Collection
import org.wycliffeassociates.otter.common.data.primitives.ContainerType
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
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
                        val sourceLanguage = sourceCollection.resourceContainer?.language
                        if (sourceLanguage == translation.source) {
                            buildWorkbookInfo(project, sourceCollection.resourceContainer!!)
                        } else {
                            null
                        }
                    }
            }
    }

    private fun buildWorkbookInfo(
        project: Collection,
        sourceMetadata: ResourceMetadata
    ): WorkbookInfo {
        val progress = getProgress(project)
        val hasSourceAudio = SourceAudioAccessor.hasSourceAudio(
            sourceMetadata,
            project.slug
        )
        return WorkbookInfo(
            project.id,
            project.slug,
            project.titleKey,
            project.labelKey,
            progress,
            project.modifiedTs!!,
            hasSourceAudio
        )
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