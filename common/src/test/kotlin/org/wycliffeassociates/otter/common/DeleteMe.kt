package org.wycliffeassociates.otter.common

import org.junit.Test
import org.wycliffeassociates.otter.common.domain.audio.MarkerGenerator
import java.io.File

class DeleteMe {

    val verseList = listOf("Verse one 1 comes first. ", "Verse number two second!")
    val transcription = listOf(Pair("Verse", 0), Pair("one", 10), Pair("comes", 20), Pair("first",30), Pair("Verse", 40), Pair("number", 50), Pair("two", 60), Pair("second", 70))
    val badTranscription = listOf(Pair("Vers", 0), Pair("on", 10), Pair("comes", 20), Pair("first",30), Pair("Verse", 40), Pair("number", 50), Pair("to", 60), Pair("second", 70))

    @Test
    fun deleteMe() {

        val markerPositions = mutableListOf<Double>()
        val wordArray = transcription
        var wordPosition = 0

        markerPositions.add(0.0) // first marker

        for (verse in verseList) {
            var textOfVerse = verse

            while (wordPosition < wordArray.size) {
                val word = wordArray[wordPosition].first
                if (textOfVerse.contains(word)) {
                    textOfVerse = textOfVerse.substringAfter(word)
                    wordPosition++
                } else {
                    break
                }
            }
            // position at the beginning of next verse
            if (wordPosition < wordArray.size) {
                markerPositions.add(wordArray[wordPosition].second.toDouble())
            }
        }

        println(markerPositions)
    }
    val verses = listOf(
        "Phước cho ai không bước đi theo lời chỉ bảo của kẻ ác, không đứng trên đường cùng với tội nhân, không ngồi trong hội của bọn người chế nhạo.",
        "Nhưng vui về luật pháp của Đức Giê-hô-va, ngày đêm suy gẫm luật pháp Ngài.",
        "Người đó sẽ như cây trồng cạnh suối nước, ra trái theo mùa, lá nó không khô héo, mọi việc người làm đều thịnh vượng.",
        "Kẻ ác thì không như vậy, nhưng thay vào đó giống như rơm rạ bị gió đùa đi.",
        "Cho nên kẻ ác sẽ không đứng nổi khi bị phán xét, tội nhân cũng không được ở trong hội người công chính.",
        "Vì Đức Giê-hô-va tán thành đường lối người công chính, nhưng đường lối kẻ ác sẽ bị diệt vong."
    )
    val verseTL = listOf(
        "Mapalad ang taong hindi lumalakad sa payo ng masama, o tumatayo sa daanan kasama ang mga makasalanan, o umuupo sa kapulungan ng mga nangungutya.",
        "Pero ang kaniyang kagalakan ay nasa batas ni Yahweh, at sa kaniyang batas, nagbubulay-bulay siya araw at gabi.",
        "Siya ay matutulad sa isang puno na nakatanim malapit sa mga batis ng tubig na namumunga sa panahon nito, na ang mga dahon ay hindi nalalanta; anuman ang kaniyang gawin ay sasagana.",
        "Ang mga masasama ay hindi katulad nito, sa halip sila ay katulad ng ipa na tinatangay ng hangin. ",
        "Kaya ang masama ay hindi makatatayo sa paghahatol, maging ang mga makasalanan sa kapulungan ng matuwid.",
        "Dahil sinasang-ayunan ni Yahweh ang daanan ng matuwid, pero ang daanan ng masama ay mapupuksa."
    )
    @Test
    fun validateGenerator() {
        val file = File("""D:\misc\temp\vi_ulb_psa_c001_meta_t2.wav""")
        MarkerGenerator().generate(file, verses)
    }
    @Test
    fun tryIt() {
        MarkerGenerator().fillMarkers(
            verseList.joinToString("\n"),
            badTranscription.map {it.first}
        )
    }
}