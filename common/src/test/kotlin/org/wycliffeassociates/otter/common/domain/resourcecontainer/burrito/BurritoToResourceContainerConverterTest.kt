package org.wycliffeassociates.otter.common.domain.resourcecontainer.burrito

import io.mockk.every
import io.mockk.mockk
import org.bibletranslationtools.scriptureburrito.IdentificationSchema
import org.bibletranslationtools.scriptureburrito.LanguageSchema
import org.bibletranslationtools.scriptureburrito.Languages
import org.bibletranslationtools.scriptureburrito.MetadataSchema
import org.junit.Assert
import org.junit.Test
import org.wycliffeassociates.resourcecontainer.entity.Language
import java.text.SimpleDateFormat
import java.util.Date

class BurritoToResourceContainerConverterTest {

    val metadata = mockk<MetadataSchema> {
        """
            {
                
            }
        """.trimIndent()
    }

    @Test
    fun testGetDescriptionFromBurrito() {
        val defaultLocale = "vi"

        val withEnglishDescription = mockk<IdentificationSchema> {
            every { description["en"] } returns "en"
        }

        val withoutEnglishDescription = mockk<IdentificationSchema> {
            every { description["en"] } returns null
            every { description[defaultLocale] } returns defaultLocale
        }

        val emptyDescription = mockk<IdentificationSchema> {
            every { description["en"] } returns null
            every { description[defaultLocale] } returns null
        }

        val withEnglishTest = mockk<MetadataSchema> {
            every { meta.defaultLocale } returns defaultLocale
            every { identification } returns withEnglishDescription
        }

        val defaultLanguageFallback = mockk<MetadataSchema> {
            every { meta.defaultLocale } returns defaultLocale
            every { identification } returns withoutEnglishDescription
        }

        val emptyFallback = mockk<MetadataSchema> {
            every { meta.defaultLocale } returns defaultLocale
            every { identification } returns emptyDescription
        }

        Assert.assertEquals(getDescriptionFromBurrito(withEnglishTest), "en")
        Assert.assertEquals(getDescriptionFromBurrito(defaultLanguageFallback), "vi")
        Assert.assertEquals(getDescriptionFromBurrito(emptyFallback), "")
    }

    @Test
    fun testGetCreationDateFromBurrito() {
        val dateCreated = "2024-08-07"
        val burrito = mockk<MetadataSchema> {
            every { meta.dateCreated } returns SimpleDateFormat("yyyy-MM-dd").parse(dateCreated)
        }

        Assert.assertEquals(getCreationDateFromBurrito(burrito), dateCreated)
    }

    @Test
    fun testGetTitleFromBurrito() {
        val defaultLocale = "fr"

        val withEnglishTitle = mockk<IdentificationSchema> {
            every { abbreviation["en"] } returns "ulb"
            every { name["en"] } returns "Unlocked Literal Bible"
        }

        val withoutEnglishTitle = mockk<IdentificationSchema> {
            every { abbreviation["en"] } returns null
            every { name["en"] } returns null
            every { abbreviation[defaultLocale] } returns "f10"
            every { name[defaultLocale] } returns "Louis Segond"
        }

        val emptyTitle = mockk<IdentificationSchema> {
            every { abbreviation["en"] } returns null
            every { name["en"] } returns null
            every { abbreviation[defaultLocale] } returns null
            every { name[defaultLocale] } returns null
        }

        val withEnglishTest = mockk<MetadataSchema> {
            every { meta.defaultLocale } returns defaultLocale
            every { identification } returns withEnglishTitle
        }

        val defaultLanguageFallback = mockk<MetadataSchema> {
            every { meta.defaultLocale } returns defaultLocale
            every { identification } returns withoutEnglishTitle
        }

        val emptyFallback = mockk<MetadataSchema> {
            every { meta.defaultLocale } returns defaultLocale
            every { identification } returns emptyTitle
        }

        Assert.assertEquals(
            getTitleFromBurrito(withEnglishTest),
            Pair("ulb", "Unlocked Literal Bible")
        )
        Assert.assertEquals(
            getTitleFromBurrito(defaultLanguageFallback),
            Pair("f10", "Louis Segond")
        )
        Assert.assertEquals(getTitleFromBurrito(emptyFallback), Pair("", ""))
    }

    @Test
    fun testGetLanguageFromBurrito() {
        val withEnglishNameAndLtr = mockk<LanguageSchema> {
            every { tag } returns "en"
            every { name["en"] } returns "English"
            every { scriptDirection } returns LanguageSchema.ScriptDirection.LTR
        }

        val withArabicNameAndRtl = mockk<LanguageSchema> {
            every { tag } returns "ar"
            every { name["ar"] } returns "Arabic"
            every { scriptDirection } returns LanguageSchema.ScriptDirection.RTL
        }

        val withEnglish = mockk<MetadataSchema> {
            every { meta.defaultLocale } returns "en"
            every { languages } returns Languages().apply { add(withEnglishNameAndLtr) }
        }

        val withArabicFallback = mockk<MetadataSchema> {
            every { meta.defaultLocale } returns "ar"
            every { languages } returns Languages().apply { add(withArabicNameAndRtl) }
        }

        val expectedWithEnglish = Language("ltr", "en", "English")
        val expectedWithArabic = Language("rtl", "ar", "Arabic")

        Assert.assertEquals(getLanguageFromBurrito(withEnglish), expectedWithEnglish)
        Assert.assertEquals(getLanguageFromBurrito(withArabicFallback), expectedWithArabic)
    }
}