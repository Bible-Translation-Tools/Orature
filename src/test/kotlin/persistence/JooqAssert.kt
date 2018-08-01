package persistence

import org.junit.Assert
import jooq.tables.pojos.LanguageEntity
import jooq.tables.pojos.UserEntity
import jooq.tables.pojos.UserLanguagesEntity
import jooq.tables.pojos.UserPreferencesEntity

object JooqAssert {

    fun assertLanguageEqual(expected: LanguageEntity, result: LanguageEntity){
        Assert.assertEquals(expected.id, result.id)
        Assert.assertEquals(expected.name, result.name)
        Assert.assertEquals(expected.slug, expected.slug)
        Assert.assertEquals(expected.isgateway, expected.isgateway)
        Assert.assertEquals(expected.anglicizedname, expected.anglicizedname)
    }

    fun assertUserPreferencesEqual(expected: UserPreferencesEntity, result: UserPreferencesEntity){
        Assert.assertEquals(expected.userfk, result.userfk)
        Assert.assertEquals(expected.sourcelanguagefk, result.sourcelanguagefk)
        Assert.assertEquals(expected.targetlanguagefk, result.targetlanguagefk)
    }

    fun assertUserLanguageEqual(expected: UserLanguagesEntity, result: UserLanguagesEntity){
        Assert.assertEquals(expected.userfk, result.userfk)
        Assert.assertEquals(expected.languagefk, result.languagefk)
        Assert.assertEquals(expected.issource, result.issource)
    }

    fun assertUserEqual(expected: UserEntity, result: UserEntity){
        Assert.assertEquals(expected.id, result.id)
        Assert.assertEquals(expected.audiohash, result.audiohash)
        Assert.assertEquals(expected.audiopath, result.audiopath)
        Assert.assertEquals(expected.imgpath, result.imgpath)
    }
}