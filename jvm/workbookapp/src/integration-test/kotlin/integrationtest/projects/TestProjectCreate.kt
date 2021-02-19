package integrationtest.projects

import integrationtest.di.DaggerTestPersistenceComponent
import org.junit.Test
import org.wycliffeassociates.otter.common.persistence.repositories.ICollectionRepository
import org.wycliffeassociates.otter.common.persistence.repositories.ILanguageRepository
import javax.inject.Inject
import javax.inject.Provider

class TestProjectCreate {
    private val numberOfChaptersInHebrews: Int = 13
    private val numberOfVersesInHebrews: Int = 303
    private val numberOfResourcesInTn: Int = 157581
    private val numberOfResourcesInTnHebrews: Int = 33758

    @Inject
    lateinit var collectionRepo: ICollectionRepository
    @Inject
    lateinit var languageRepo: ILanguageRepository

    @Inject
    lateinit var dbEnvProvider: Provider<DatabaseEnvironment>

    init {
        DaggerTestPersistenceComponent.create().inject(this)
    }

    @Test
    fun derivativeLinksForBook() {
        val env = dbEnvProvider.get()
        env
            .import("en_ulb.zip")
            .assertRowCounts(RowCount(links = 0, derivatives = 0))

        env.createProject(
            sourceProject = env.getHebrewsSourceBook().blockingGet(),
            targetLanguage = env.getHebrewLanguage().blockingGet()
        )
        env.assertRowCounts(RowCount(links = 0, derivatives = numberOfChaptersInHebrews + numberOfVersesInHebrews))
    }

    @Test
    fun derivativeLinksForHelps() {
        val env = dbEnvProvider.get()
        env
            .import("en_ulb.zip")
            .import("en_tn.zip")
            .assertRowCounts(RowCount(links = numberOfResourcesInTn, derivatives = 0))

        env.createProject(
            sourceProject = env.getHebrewsSourceBook().blockingGet(),
            targetLanguage = env.getHebrewLanguage().blockingGet()
        )

        env.assertRowCounts(
            RowCount(
                links = numberOfResourcesInTn,
                derivatives = numberOfResourcesInTnHebrews + numberOfChaptersInHebrews + numberOfVersesInHebrews
            )
        )
    }

    private fun DatabaseEnvironment.getHebrewsSourceBook() =
        collectionRepo.getSourceProjects().map { it.single { it.slug == "heb" } }.cache()

    private fun DatabaseEnvironment.getHebrewLanguage() =
        languageRepo.getBySlug("hbo").cache()
}
