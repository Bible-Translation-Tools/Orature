package org.wycliffeassociates.otter.jvm.persistence.mapping

import data.model.Language
import org.junit.Assert
import org.junit.Test
import org.wycliffeassociates.otter.jvm.persistence.JooqAssert
import jooq.tables.pojos.LanguageEntity
import org.wycliffeassociates.otter.jvm.persistence.mapping.LanguageMapper
import java.util.*

class LanguageMapperTest {

    val LANGUAGE_TABLE = listOf(
        mapOf(
            "name" to "Español",
            "slug" to "esp",
            "anglicizedName" to "Spanish",
            "canBeSource" to "true"
        ),
        mapOf(
            "name" to "हिन्दी",
            "slug" to "hin",
            "anglicizedName" to "Hindi",
            "canBeSource" to "true"
        )
    )

    @Test
    fun testIfLanguageEntityCorrectlyMappedToLanguage() {
        for (testCase in LANGUAGE_TABLE) {
            val input = LanguageEntity(
                Random().nextInt(),
                testCase["name"],
                testCase["slug"],
                if (testCase["canBeSource"] == "true") 1 else 0,
                testCase["anglicizedName"]

            )


            val expected = Language(
                id = input.id,
                slug = input.slug,
                name = input.name,
                anglicizedName = input.anglicizedname,
                isGateway = input.isgateway == 1
            )

            val result = LanguageMapper().mapFromEntity(input)
            try {
                Assert.assertEquals(expected, result)
            } catch (e: AssertionError) {
                println("Input: ${input.name}")
                println("Result: ${result.name}")
                throw e
            }
        }
    }

    @Test
    fun testIfLanguageCorrectlyMappedToLanguageEntity() {
        for (testCase in LANGUAGE_TABLE) {
            val input = Language(
                id = Random().nextInt(),
                slug = testCase["slug"].orEmpty(),
                name = testCase["name"].orEmpty(),
                anglicizedName = testCase["anglicizedName"].orEmpty(),
                isGateway = (testCase["canBeSource"] == "true")
            )

            val expected = LanguageEntity(
                input.id,
                input.slug,
                input.name,
                if (input.isGateway) 1 else 0,
                input.anglicizedName
            )

            val result = LanguageMapper().mapToEntity(input)
            try {
                JooqAssert.assertLanguageEqual(expected, result)
            } catch (e: AssertionError) {
                println("Input: ${input.name}")
                println("Result: ${result.name}")
                throw e
            }
        }
    }

}