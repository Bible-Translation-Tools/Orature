package org.wycliffeassociates.otter.common.persistence.repositories

import io.reactivex.Completable
import org.wycliffeassociates.otter.common.collections.tree.Tree
import org.wycliffeassociates.resourcecontainer.ResourceContainer

interface IResourceContainerRepository {
    fun importResourceContainer(rc: ResourceContainer, rcTree: Tree, languageSlug: String): Completable
}