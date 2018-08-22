package org.wycliffeassociates.otter.common.domain

import org.wycliffeassociates.otter.common.data.dao.Dao
import org.wycliffeassociates.otter.common.data.model.Project
import io.reactivex.Observable


class GetProjectsUseCase(val projectRepo: Dao<Project>) {

    fun getProjects(): Observable<List<Project>> {
        return projectRepo.getAll()
    }
}