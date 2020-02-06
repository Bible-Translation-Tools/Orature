package integrationtest.projects

import org.junit.Test

class TestProjectCreate {
    private val numberOfChaptersInHebrews: Int = 13
    private val numberOfVersesInHebrews: Int = 303
    private val numberOfResourcesInTn: Int = 157573
    private val numberOfResourcesInTnHebrews: Int = 33758

    @Test
    fun derivativeLinksForBook() {
        val env = DatabaseEnvironment()
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
        val env = DatabaseEnvironment()
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
        injector.collectionRepo.getSourceProjects().map { it.single { it.slug == "heb" } }.cache()

    private fun DatabaseEnvironment.getHebrewLanguage() =
        injector.languageRepo.getBySlug("hbo").cache()
}
