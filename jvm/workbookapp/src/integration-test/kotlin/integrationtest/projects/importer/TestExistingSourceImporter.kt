package integrationtest.projects.importer

import integrationtest.di.DaggerTestPersistenceComponent
import org.wycliffeassociates.otter.common.domain.project.importer.ExistingSourceImporter
import javax.inject.Inject
import javax.inject.Provider

class TestExistingSourceImporter {
    @Inject
    lateinit var existingProjectImporter: Provider<ExistingSourceImporter>

    init {
        DaggerTestPersistenceComponent.create().inject(this)
    }


}