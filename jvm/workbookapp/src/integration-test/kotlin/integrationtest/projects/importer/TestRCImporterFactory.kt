package integrationtest.projects.importer

import integrationtest.di.DaggerTestPersistenceComponent
import org.junit.Assert
import org.junit.Test
import org.wycliffeassociates.otter.common.domain.project.importer.ExistingSourceImporter
import org.wycliffeassociates.otter.common.domain.project.importer.NewSourceImporter
import org.wycliffeassociates.otter.common.domain.project.importer.OngoingProjectImporter
import org.wycliffeassociates.otter.common.domain.project.importer.RCImporterFactory
import javax.inject.Inject

class TestRCImporterFactory {
    @Inject
    lateinit var factory: RCImporterFactory

    init {
        DaggerTestPersistenceComponent.create().inject(this)
    }

    @Test
    fun testMakeImporter() {
        val importer = factory.makeImporter()

        Assert.assertTrue(importer is OngoingProjectImporter)
        Assert.assertTrue(importer.getNext() is ExistingSourceImporter)
        Assert.assertTrue(importer.getNext()!!.getNext() is NewSourceImporter)
    }
}