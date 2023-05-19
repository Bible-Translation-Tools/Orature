package org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories

import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.wycliffeassociates.otter.common.data.primitives.Collection
import org.wycliffeassociates.otter.common.data.workbook.WorkbookDescriptor
import org.wycliffeassociates.otter.common.domain.resourcecontainer.SourceAudioAccessor
import org.wycliffeassociates.otter.common.persistence.repositories.ICollectionRepository
import org.wycliffeassociates.otter.common.persistence.repositories.IContentRepository
import org.wycliffeassociates.otter.common.persistence.repositories.IWorkbookDescriptorRepository
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.AppDatabase
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.entities.WorkbookDescriptorEntity
import javax.inject.Inject

class WorkbookDescriptorRepository @Inject constructor(
    database: AppDatabase,
    private val collectionRepository: ICollectionRepository,
    private val contentRepository: IContentRepository
) : IWorkbookDescriptorRepository {

    private val workbookDao = database.workbookDescriptorDao

    override fun getById(id: Int): Maybe<WorkbookDescriptor> {
        return Maybe
            .fromCallable {
                workbookDao.fetchById(id)
            }
            .map {
                buildWorkbookDescriptor(it)
            }
            .subscribeOn(Schedulers.io())
    }

    override fun getAll(): Single<List<WorkbookDescriptor>> {
        return Single.fromCallable {
            workbookDao.fetchAll()
                .map {
                    buildWorkbookDescriptor(it)
                }
        }
    }

    private fun buildWorkbookDescriptor(entity: WorkbookDescriptorEntity): WorkbookDescriptor {
        val targetCollection = collectionRepository.getProject(entity.targetFk).blockingGet()
        val sourceCollection = collectionRepository.getProject(entity.sourceFk).blockingGet()
        val progress = getProgress(targetCollection)
        val hasSourceAudio = SourceAudioAccessor.hasSourceAudio(
            sourceCollection.resourceContainer!!,
            sourceCollection.slug
        )
        return WorkbookDescriptor(
            sourceCollection.id,
            sourceCollection.slug,
            sourceCollection.titleKey,
            sourceCollection.labelKey,
            progress,
            sourceCollection.modifiedTs,
            hasSourceAudio
        )
    }


    private fun getProgress(collection: Collection): Double {
        val chapters = collectionRepository.getChildren(collection)
            .flattenAsObservable { it }
            .flatMapSingle { chapter ->
                contentRepository.getCollectionMetaContent(chapter)
            }
            .blockingIterable().toList()

        return chapters.count { it.selectedTake != null }.toDouble() / chapters.size
    }
}