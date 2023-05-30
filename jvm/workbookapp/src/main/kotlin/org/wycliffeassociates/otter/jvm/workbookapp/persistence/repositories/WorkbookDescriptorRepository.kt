package org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories

import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.slf4j.LoggerFactory
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

    private val logger = LoggerFactory.getLogger(javaClass)
    private val workbookDescriptorDao = database.workbookDescriptorDao
    private val workbookTypeDao = database.workbookTypeDao

    override fun getById(id: Int): Maybe<WorkbookDescriptor> {
        return Maybe
            .fromCallable {
                workbookDescriptorDao.fetchById(id)
            }
            .map {
                buildWorkbookDescriptor(it)
            }
            .subscribeOn(Schedulers.io())
    }

    override fun getAll(): Single<List<WorkbookDescriptor>> {
        return Single
            .fromCallable {
                workbookDescriptorDao.fetchAll()
                    .map {
                        buildWorkbookDescriptor(it)
                    }
            }
            .subscribeOn(Schedulers.io())
            .doOnError {
                logger.error("Error getting workbook descriptors.", it)
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
        val mode = workbookTypeDao.fetchById(entity.typeFk)!!

        return WorkbookDescriptor(
            entity.id,
            targetCollection.slug,
            targetCollection.titleKey,
            targetCollection.labelKey,
            sourceCollection,
            targetCollection,
            mode,
            progress,
            targetCollection.modifiedTs,
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