///**
// * Copyright (C) 2020-2022 Wycliffe Associates
// *
// * This file is part of Orature.
// *
// * Orature is free software: you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as published by
// * the Free Software Foundation, either version 3 of the License, or
// * (at your option) any later version.
// *
// * Orature is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// *
// * You should have received a copy of the GNU General Public License
// * along with Orature.  If not, see <https://www.gnu.org/licenses/>.
// */
//package integrationtest.initialization
//
//import integrationtest.di.DaggerTestPersistenceComponent
//import io.reactivex.Completable
//import io.reactivex.observers.TestObserver
//import org.junit.Assert
//import org.junit.Before
//import org.junit.Test
//import org.wycliffeassociates.otter.assets.initialization.InitializeUlb
//import org.wycliffeassociates.otter.common.domain.languages.ImportLanguages
//import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.AppDatabase
//import javax.inject.Inject
//import javax.inject.Provider
//
//class TestInitializeUlb {
//
//    @Inject
//    lateinit var database: AppDatabase
//
//    @Inject
//    lateinit var initUlbProvider: Provider<InitializeUlb>
//
//    @Inject
//    lateinit var importLanguages: Provider<ImportLanguages>
//
//    @Before
//    fun setup() {
//        DaggerTestPersistenceComponent.create().inject(this)
//        val langNames = ClassLoader.getSystemResourceAsStream("content/langnames.json")!!
//        importLanguages.get().import(langNames).blockingGet()
//    }
//
//    @Test
//    fun testImportEnUlb() {
//        val testSub = TestObserver<Completable>()
//
//        val init = initUlbProvider.get()
//        init.exec()
//            .subscribe(testSub)
//
//        testSub.assertComplete()
//        testSub.assertNoErrors()
//
//        Assert.assertEquals(init.version, database.installedEntityDao.fetchVersion(init))
//    }
//}
