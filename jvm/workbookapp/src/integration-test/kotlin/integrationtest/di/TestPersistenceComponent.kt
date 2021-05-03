//package integrationtest.di
//
//import dagger.Component
//import integrationtest.initialization.TestInitializeProjects
//import integrationtest.initialization.TestInitializeUlb
//import integrationtest.projects.TestProjectCreate
//import integrationtest.projects.TestProjectImport
//import integrationtest.projects.TestRcImport
//import org.wycliffeassociates.otter.jvm.workbookapp.di.AppDependencyGraph
//import org.wycliffeassociates.otter.jvm.workbookapp.di.modules.AppDatabaseModule
//import org.wycliffeassociates.otter.jvm.workbookapp.di.modules.AppPreferencesModule
//import org.wycliffeassociates.otter.jvm.workbookapp.di.modules.AppRepositoriesModule
//import org.wycliffeassociates.otter.jvm.workbookapp.di.modules.AudioModule
//import org.wycliffeassociates.otter.jvm.workbookapp.di.modules.ZipEntryTreeBuilderModule
//import javax.inject.Singleton
//
//@Component(
//    modules = [
//        AudioModule::class,
//        AppDatabaseModule::class,
//        AppPreferencesModule::class,
//        TestDirectoryProviderModule::class,
//        AppRepositoriesModule::class,
//        ZipEntryTreeBuilderModule::class
//    ]
//)
//@Singleton
//interface TestPersistenceComponent : AppDependencyGraph {
//    fun inject(test: TestInitializeUlb)
//    fun inject(test: TestInitializeProjects)
//    fun inject(test: TestProjectCreate)
//    fun inject(test: TestRcImport)
//    fun inject(test: TestProjectImport)
//}
