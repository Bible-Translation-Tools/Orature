package integrationtest.rcimport

import org.junit.Test
import org.wycliffeassociates.otter.common.data.model.ContentType.*

class TestRcImport {

    @Test
    fun ulb() {
        DatabaseEnvironment()
            .import("en_ulb.zip")
            .assertRowCounts(
                RowCount(
                    contents = mapOf(
                        TEXT to 31103,
                        META to 1189
                    ),
                    collections = 1256,
                    links = 0
                )
            )
    }

    /**
     * Runs the same test as ulb(), but rather than test the provided and tested ulb resource container,
     * we instead test the version downloaded from WACS through the downloadUlb gradle task. Failure of this
     * test while succeeding the ulb() test therefore implies either potential issues in the WACS repository
     * or a failure to download the content.
     */
    @Test
    fun ulbFromWacs() {
        DatabaseEnvironment()
            .import("en_ulb.zip", true)
            .assertRowCounts(
                RowCount(
                    contents = mapOf(
                        TEXT to 31104,
                        META to 1189
                    ),
                    collections = 1256,
                    links = 0
                )
            )
    }

    @Test
    fun ulbAndHelps() {
        DatabaseEnvironment()
            .import("en_ulb.zip")
            .import("en_tn.zip")
            .assertRowCounts(
                message = "Row counts after importing TN",
                expected = RowCount(
                    contents = mapOf(
                        META to 1189,
                        TEXT to 31103,
                        TITLE to 80148,
                        BODY to 77433
                    ),
                    collections = 1256,
                    links = 157573
                )
            )
            .import("en_tq-v19-10.zip")
            .assertRowCounts(
                message = "Row counts after importing TQ",
                expected = RowCount(
                    contents = mapOf(
                        META to 1189,
                        TEXT to 31103,
                        TITLE to 98520,
                        BODY to 95805
                    ),
                    collections = 1256,
                    links = 194315
                )
            )
    }

    @Test
    fun obsV6() {
        DatabaseEnvironment()
            .import("obs-biel-v6.zip")
            .assertRowCounts(
                RowCount(
                    collections = 57,
                    contents = mapOf(
                        META to 55,
                        TEXT to 1314
                    ),
                    links = 0
                )
            )
    }

    @Test
    fun obsAndTnV6() {
        DatabaseEnvironment()
            .import("obs-biel-v6.zip")
            .import("obs-tn-biel-v6.zip")
            .assertRowCounts(
                RowCount(
                    contents = mapOf(
                        META to 55,
                        TEXT to 1314,
                        TITLE to 2237,
                        BODY to 2237
                    ),
                    collections = 57,
                    links = 4474
                )
            )
    }

    @Test
    fun obsSlugs() {
        DatabaseEnvironment()
            .import("obs-biel-v6.zip")
            .assertSlugs(
                "obs",
                CollectionDescriptor(label = "book", slug = "obs"),
                CollectionDescriptor(label = "project", slug = "obs"),
                CollectionDescriptor(label = "chapter", slug = "obs_1")
            )
    }

    @Test
    fun ulbSlugs() {
        DatabaseEnvironment()
            .import("en_ulb.zip")
            .assertSlugs(
                "ulb",
                CollectionDescriptor(label = "bundle", slug = "ulb"),
                CollectionDescriptor(label = "project", slug = "gen"),
                CollectionDescriptor(label = "chapter", slug = "gen_1")
            )
    }
}
