package integrationtest.projects

import integrationtest.di.DaggerTestPersistenceComponent
import io.mockk.every
import io.mockk.spyk
import io.mockk.verify
import io.reactivex.Single
import org.junit.Assert
import org.junit.Test
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.domain.project.ImportProjectUseCase
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResult
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

class TestSideloadSourceProject {

    @Inject
    lateinit var importer: ImportProjectUseCase

    init {
        DaggerTestPersistenceComponent.create().inject(this)
    }

    @Test
    fun testSideloadGLs() {
        val importer = spyk(importer) {
            every { import(any(), any(), any()) } returns Single.just(ImportResult.SUCCESS)
        }

        val reachableSources = ImportProjectUseCase.glSources
            .filter {
                urlExists(it.url)
            }

        fun langFromCode(code: String) = Language(code, "", "", "", true, "")

        val missingSources = reachableSources.filter {
            importer.getEmbeddedSource(langFromCode(it.languageCode)).exists().not()
        }

        val sb = StringBuilder()
        missingSources.forEach {
            sb.append(it.languageCode)
            sb.append(", ")
        }

        Assert.assertEquals("Expected Orature to have all reachable sources, missing: ${sb}", reachableSources.size, missingSources.size)

        reachableSources.forEach {
            val language = Language(slug = it.languageCode, "", "", "", true, "")
            importer.sideloadSource(language).blockingAwait()
        }

        verify(exactly = reachableSources.size) { importer.import(any(), any(), any()) }
    }

    /**
     * "Pings" the endpoint URL to see if they are available.
     */
    private fun urlExists(url: String): Boolean {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "HEAD"
        val responseCode = connection.responseCode
        return responseCode == HttpURLConnection.HTTP_OK
    }
}