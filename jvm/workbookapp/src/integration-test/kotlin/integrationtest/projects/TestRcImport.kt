//package integrationtest.projects
//
//import integrationtest.di.DaggerTestPersistenceComponent
//import org.junit.Test
//import org.wycliffeassociates.otter.common.data.primitives.ContentType.*
//import javax.inject.Inject
//import javax.inject.Provider
//
//class TestRcImport {
//
//    @Inject
//    lateinit var dbEnvProvider: Provider<DatabaseEnvironment>
//
//    init {
//        DaggerTestPersistenceComponent.create().inject(this)
//    }
//
//    @Test
//    fun ulb() {
//        dbEnvProvider.get()
//            .import("en_ulb.zip")
//            .assertRowCounts(
//                RowCount(
//                    contents = mapOf(
//                        TEXT to 31104,
//                        META to 1189
//                    ),
//                    collections = 1256,
//                    links = 0
//                )
//            )
//    }
//
//    /**
//     * Runs the same test as ulb(), but rather than test the provided and tested ulb resource container,
//     * we instead test the version downloaded from WACS through the downloadUlb gradle task. Failure of this
//     * test while succeeding the ulb() test therefore implies either potential issues in the WACS repository
//     * or a failure to download the content.
//     */
//    @Test
//    fun ulbFromWacs() {
//        dbEnvProvider.get()
//            .import("en_ulb.zip", true)
//            .assertRowCounts(
//                RowCount(
//                    contents = mapOf(
//                        TEXT to 31104,
//                        META to 1189
//                    ),
//                    collections = 1256,
//                    links = 0
//                )
//            )
//    }
//
//    @Test
//    fun ulbAndHelps() {
//        dbEnvProvider.get()
//            .import("en_ulb.zip")
//            .import("en_tn.zip")
//            .assertRowCounts(
//                message = "Row counts after importing TN",
//                expected = RowCount(
//                    contents = mapOf(
//                        META to 1189,
//                        TEXT to 31104,
//                        TITLE to 80148,
//                        BODY to 77433
//                    ),
//                    collections = 1256,
//                    links = 157581
//                )
//            )
//            .import("en_tq-v19-10.zip")
//            .assertRowCounts(
//                message = "Row counts after importing TQ",
//                expected = RowCount(
//                    contents = mapOf(
//                        META to 1189,
//                        TEXT to 31104,
//                        TITLE to 98520,
//                        BODY to 95805
//                    ),
//                    collections = 1256,
//                    links = 194325
//                )
//            )
//    }
//
//    @Test
//    fun obsV6() {
//        dbEnvProvider.get()
//            .import("obs-biel-v6.zip")
//            .assertRowCounts(
//                RowCount(
//                    collections = 57,
//                    contents = mapOf(
//                        META to 55,
//                        TEXT to 1314
//                    ),
//                    links = 0
//                )
//            )
//    }
//
//    @Test
//    fun obsAndTnV6() {
//        dbEnvProvider.get()
//            .import("obs-biel-v6.zip")
//            .import("obs-tn-biel-v6.zip")
//            .assertRowCounts(
//                RowCount(
//                    contents = mapOf(
//                        META to 55,
//                        TEXT to 1314,
//                        TITLE to 2237,
//                        BODY to 2237
//                    ),
//                    collections = 57,
//                    links = 4474
//                )
//            )
//    }
//
//    @Test
//    fun obsSlugs() {
//        dbEnvProvider.get()
//            .import("obs-biel-v6.zip")
//            .assertSlugs(
//                "obs",
//                CollectionDescriptor(label = "book", slug = "obs"),
//                CollectionDescriptor(label = "project", slug = "obs"),
//                CollectionDescriptor(label = "chapter", slug = "obs_1")
//            )
//    }
//
//    @Test
//    fun ulbSlugs() {
//        dbEnvProvider.get()
//            .import("en_ulb.zip")
//            .assertSlugs(
//                "ulb",
//                CollectionDescriptor(label = "bundle", slug = "ulb"),
//                CollectionDescriptor(label = "project", slug = "gen"),
//                CollectionDescriptor(label = "chapter", slug = "gen_1")
//            )
//    }
//}
