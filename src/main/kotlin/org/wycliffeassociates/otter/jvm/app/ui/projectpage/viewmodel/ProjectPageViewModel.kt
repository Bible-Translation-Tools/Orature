package org.wycliffeassociates.otter.jvm.app.ui.projectpage.viewmodel

import org.wycliffeassociates.otter.common.data.model.Chunk
import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.jvm.app.ui.projectpage.model.ProjectPageModel
import org.wycliffeassociates.otter.jvm.app.ui.projectpage.view.ChapterContext
import tornadofx.*

class ProjectPageViewModel: ViewModel() {
    private val model = ProjectPageModel()
    val projectTitleProperty = bind { model.projectTitleProperty }
    val children = model.children
    val chunks = model.chunks
    val contextProperty = bind { model.contextProperty }
    val showPluginActiveProperty = bind { model.showPluginActiveProperty }
    val activeChunkProperty = bind { model.activeChunkProperty }
    val activeChild = model.activeChildProperty

    fun changeContext(context: ChapterContext) {
        model.context = context
    }

    fun selectChildCollection(child: Collection) {
        model.selectChildCollection(child)
    }

    fun doChunkContextualAction(chunk: Chunk) {
        model.doChunkContextualAction(chunk)
    }

    fun setWorkspace(workspace: Workspace) {
        model.workspace = workspace
    }
}