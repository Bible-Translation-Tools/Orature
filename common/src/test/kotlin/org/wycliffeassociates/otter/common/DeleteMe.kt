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

    @Test
    fun validateGenerator() {
        val file = File("""D:\misc\temp\vi_ulb_psa_c001_meta_t2.wav""")
        MarkerGenerator().generate(file, verses)
    }

    private fun extractVerses(input: String): List<String> {
        val pattern = Regex("""\d{1,3}:\d{1,3}""")
        val matches = pattern.findAll(input).toList()

        // If no timestamps found, return whole input
        if (matches.isEmpty()) return listOf(input.trim())

        val result = mutableListOf<String>()

        for (i in matches.indices) {
            val start = matches[i].range.last + 1
            val end = if (i + 1 < matches.size) matches[i + 1].range.first else input.length
            val verse = input.substring(start, end).trim()
            result.add(verse)
        }

        return result
    }
    @Test
    fun util() {

        val input = """
        1:1 Dari Yudas, hamba Yesus Kristus dan saudara Yakobus, kepada mereka, yang terpanggil, yang dikasihi dalam Allah Bapa, dan yang dipelihara untuk Yesus Kristus. 1:2 Rahmat, damai sejahtera dan kasih kiranya melimpahi kamu.
        Hukuman atas guru-guru palsu
        1:3 Saudara-saudaraku yang kekasih, sementara aku bersungguh-sungguh berusaha menulis kepada kamu tentang keselamatan kita bersama, aku merasa terdorong untuk menulis ini kepada kamu dan menasihati kamu, supaya kamu tetap berjuang untuk mempertahankan iman yang telah disampaikan kepada orang-orang kudus. 1:4 Sebab ternyata ada orang tertentu yang telah masuk menyelusup di tengah-tengah kamu, yaitu orang-orang yang telah lama ditentukan untuk dihukum. Mereka adalah orang-orang yang fasik, yang menyalahgunakan kasih karunia Allah k ita untuk melampiaskan hawa nafsu mereka, dan yang menyangkal satu-satunya Penguasa dan Tuhan kita, Yesus Kristus. 1:5 Tetapi, sekalipun kamu telah mengetahui semuanya itu dan tidak meragukannya lagi, aku ingin mengingatkan kamu bahwa memang Tuhan menyelamatkan umat-Nya dari tanah Mesir, namun sekali lagi membinasakan mereka yang tidak percaya. 1:6 Dan bahwa Ia menahan malaikat-malaikat yang tidak taat pada batas-batas kekuasaan mereka, tetapi yang meninggalkan tempat kediaman mereka, dengan belenggu abadi di dalam dunia kekelaman sampai penghakiman pada hari besar, 1:7 sama seperti Sodom dan Gomora dan kota-kota sekitarnya, yang dengan cara yang sama melakukan percabulan dan mengejar kepuasan-kepuasan yang tak wajar, telah menanggung siksaan api kekal sebagai peringatan kepada semua orang. 1:8 Namun demikian orang-orang yang bermimpi-mimpian ini juga mencemarkan tubuh mereka dan menghina kekuasaan Allah serta menghujat semua yang mulia di sorga. 1:9 Tetapi penghulu malaikat, Mikhael, ketika dalam suatu perselisihan bertengkar dengan Iblis mengenai mayat Musa, tidak berani menghakimi Iblis itu dengan kata-kata hujatan, tetapi berkata: "Kiranya Tuhan menghardik engkau!" 1:10 Akan tetapi mereka menghujat segala sesuatu yang tidak mereka ketahui dan justru apa yang mereka ketahui dengan nalurinya seperti binatang yang tidak berakal, itulah yang mengakibatkan kebinasaan mereka. 1:11 Celakalah mereka, karena mereka mengikuti jalan yang ditempuh Kain dan karena mereka, oleh sebab upah, menceburkan diri ke dalam kesesatan Bileam, dan mereka binasa karena kedurhakaan seperti Korah. 1:12 Mereka inilah noda dalam perjamuan kasihmu, di mana mereka tidak malu-malu melahap dan hanya mementingkan dirinya sendiri; mereka bagaikan awan yang tak berair, yang berlalu ditiup angin; mereka bagaikan pohon-pohon yang dalam musim gugur tidak menghasilkan buah, pohon-pohon yang terbantun dengan akar-akarnya dan yang mati sama sekali. 1:13 Mereka bagaikan ombak laut yang ganas, yang membuihkan keaiban mereka sendiri; mereka bagaikan bintang-bintang yang baginya telah tersedia tempat di dunia kekelaman untuk selama-lamanya. 1:14 Juga tentang mereka Henokh, keturunan ketujuh dari Adam, telah bernubuat, katanya: "Sesungguhnya Tuhan datang dengan beribu-ribu orang kudus-Nya, 1:15 hendak menghakimi semua orang dan menjatuhkan hukuman atas orang-orang fasik karena semua perbuatan fasik, yang mereka lakukan dan karena semua kata-kata nista, yang diucapkan orang-orang berdosa yang fasik itu terhadap Tuhan." 1:16 Mereka itu orang-orang yang menggerutu dan mengeluh tentang nasibnya, hidup menuruti hawa nafsunya, tetapi mulut mereka mengeluarkan perkataan-perkataan yang bukan-bukan dan mereka menjilat orang untuk mendapat keuntungan.
        Nasihat-nasihat untuk meneguhkan iman
        1:17 Tetapi kamu, saudara-saudaraku yang kekasih, ingatlah akan apa yang dahulu telah dikatakan kepada kamu oleh rasul-rasul Tuhan kita, Yesus Kristus. 1:18 Sebab mereka telah mengatakan kepada kamu: "Menjelang akhir zaman akan tampil pengejek-pengejek yang akan hidup menuruti hawa nafsu kefasikan mereka." 1:19 Mereka adalah pemecah belah yang dikuasai hanya oleh keinginan-keinginan dunia ini dan yang hidup tanpa Roh Kudus. 1:20 Akan tetapi kamu, saudara-saudaraku yang kekasih, bangunlah dirimu sendiri di atas dasar imanmu yang paling suci dan berdoalah dalam Roh Kudus. 1:21 Peliharalah dirimu demikian dalam kasih Allah sambil menantikan rahmat Tuhan kita, Yesus Kristus, untuk hidup yang kekal. 1:22 Tunjukkanlah belas kasihan kepada mereka yang ragu-ragu, 1:23 selamatkanlah mereka dengan jalan merampas mereka dari api. Tetapi tunjukkanlah belas kasihan yang disertai ketakutan kepada orang-orang lain juga, dan bencilah pakaian mereka yang dicemarkan oleh keinginan-keinginan dosa.
        Penutup
        1:24 Bagi Dia, yang berkuasa menjaga supaya jangan kamu tersandung dan yang membawa kamu dengan tak bernoda dan penuh kegembiraan di hadapan kemuliaan-Nya, 1:25 Allah yang esa, Juruselamat kita oleh Yesus Kristus, Tuhan kita, bagi Dia adalah kemuliaan, kebesaran, kekuatan dan kuasa sebelum segala abad dan sekarang dan sampai selama-lamanya. Amin.
    """.trimIndent()

        val verses = extractVerses(input)
        verses.forEachIndexed { i, v -> println("${i+1} $v") }
    }

    @Test
    fun generateMarkerForChapterAudio() {
        val audio = File("""D:\misc\temp\jude-id.mp3""")
        val input = """
        1:1 Dari Yudas, hamba Yesus Kristus dan saudara Yakobus, kepada mereka, yang terpanggil, yang dikasihi dalam Allah Bapa, dan yang dipelihara untuk Yesus Kristus. 1:2 Rahmat, damai sejahtera dan kasih kiranya melimpahi kamu.
        Hukuman atas guru-guru palsu
        1:3 Saudara-saudaraku yang kekasih, sementara aku bersungguh-sungguh berusaha menulis kepada kamu tentang keselamatan kita bersama, aku merasa terdorong untuk menulis ini kepada kamu dan menasihati kamu, supaya kamu tetap berjuang untuk mempertahankan iman yang telah disampaikan kepada orang-orang kudus. 1:4 Sebab ternyata ada orang tertentu yang telah masuk menyelusup di tengah-tengah kamu, yaitu orang-orang yang telah lama ditentukan untuk dihukum. Mereka adalah orang-orang yang fasik, yang menyalahgunakan kasih karunia Allah k ita untuk melampiaskan hawa nafsu mereka, dan yang menyangkal satu-satunya Penguasa dan Tuhan kita, Yesus Kristus. 1:5 Tetapi, sekalipun kamu telah mengetahui semuanya itu dan tidak meragukannya lagi, aku ingin mengingatkan kamu bahwa memang Tuhan menyelamatkan umat-Nya dari tanah Mesir, namun sekali lagi membinasakan mereka yang tidak percaya. 1:6 Dan bahwa Ia menahan malaikat-malaikat yang tidak taat pada batas-batas kekuasaan mereka, tetapi yang meninggalkan tempat kediaman mereka, dengan belenggu abadi di dalam dunia kekelaman sampai penghakiman pada hari besar, 1:7 sama seperti Sodom dan Gomora dan kota-kota sekitarnya, yang dengan cara yang sama melakukan percabulan dan mengejar kepuasan-kepuasan yang tak wajar, telah menanggung siksaan api kekal sebagai peringatan kepada semua orang. 1:8 Namun demikian orang-orang yang bermimpi-mimpian ini juga mencemarkan tubuh mereka dan menghina kekuasaan Allah serta menghujat semua yang mulia di sorga. 1:9 Tetapi penghulu malaikat, Mikhael, ketika dalam suatu perselisihan bertengkar dengan Iblis mengenai mayat Musa, tidak berani menghakimi Iblis itu dengan kata-kata hujatan, tetapi berkata: "Kiranya Tuhan menghardik engkau!" 1:10 Akan tetapi mereka menghujat segala sesuatu yang tidak mereka ketahui dan justru apa yang mereka ketahui dengan nalurinya seperti binatang yang tidak berakal, itulah yang mengakibatkan kebinasaan mereka. 1:11 Celakalah mereka, karena mereka mengikuti jalan yang ditempuh Kain dan karena mereka, oleh sebab upah, menceburkan diri ke dalam kesesatan Bileam, dan mereka binasa karena kedurhakaan seperti Korah. 1:12 Mereka inilah noda dalam perjamuan kasihmu, di mana mereka tidak malu-malu melahap dan hanya mementingkan dirinya sendiri; mereka bagaikan awan yang tak berair, yang berlalu ditiup angin; mereka bagaikan pohon-pohon yang dalam musim gugur tidak menghasilkan buah, pohon-pohon yang terbantun dengan akar-akarnya dan yang mati sama sekali. 1:13 Mereka bagaikan ombak laut yang ganas, yang membuihkan keaiban mereka sendiri; mereka bagaikan bintang-bintang yang baginya telah tersedia tempat di dunia kekelaman untuk selama-lamanya. 1:14 Juga tentang mereka Henokh, keturunan ketujuh dari Adam, telah bernubuat, katanya: "Sesungguhnya Tuhan datang dengan beribu-ribu orang kudus-Nya, 1:15 hendak menghakimi semua orang dan menjatuhkan hukuman atas orang-orang fasik karena semua perbuatan fasik, yang mereka lakukan dan karena semua kata-kata nista, yang diucapkan orang-orang berdosa yang fasik itu terhadap Tuhan." 1:16 Mereka itu orang-orang yang menggerutu dan mengeluh tentang nasibnya, hidup menuruti hawa nafsunya, tetapi mulut mereka mengeluarkan perkataan-perkataan yang bukan-bukan dan mereka menjilat orang untuk mendapat keuntungan.
        Nasihat-nasihat untuk meneguhkan iman
        1:17 Tetapi kamu, saudara-saudaraku yang kekasih, ingatlah akan apa yang dahulu telah dikatakan kepada kamu oleh rasul-rasul Tuhan kita, Yesus Kristus. 1:18 Sebab mereka telah mengatakan kepada kamu: "Menjelang akhir zaman akan tampil pengejek-pengejek yang akan hidup menuruti hawa nafsu kefasikan mereka." 1:19 Mereka adalah pemecah belah yang dikuasai hanya oleh keinginan-keinginan dunia ini dan yang hidup tanpa Roh Kudus. 1:20 Akan tetapi kamu, saudara-saudaraku yang kekasih, bangunlah dirimu sendiri di atas dasar imanmu yang paling suci dan berdoalah dalam Roh Kudus. 1:21 Peliharalah dirimu demikian dalam kasih Allah sambil menantikan rahmat Tuhan kita, Yesus Kristus, untuk hidup yang kekal. 1:22 Tunjukkanlah belas kasihan kepada mereka yang ragu-ragu, 1:23 selamatkanlah mereka dengan jalan merampas mereka dari api. Tetapi tunjukkanlah belas kasihan yang disertai ketakutan kepada orang-orang lain juga, dan bencilah pakaian mereka yang dicemarkan oleh keinginan-keinginan dosa.
        Penutup
        1:24 Bagi Dia, yang berkuasa menjaga supaya jangan kamu tersandung dan yang membawa kamu dengan tak bernoda dan penuh kegembiraan di hadapan kemuliaan-Nya, 1:25 Allah yang esa, Juruselamat kita oleh Yesus Kristus, Tuhan kita, bagi Dia adalah kemuliaan, kebesaran, kekuatan dan kuasa sebelum segala abad dan sekarang dan sampai selama-lamanya. Amin.
    """.trimIndent()
        val verses = extractVerses(input)

        MarkerGenerator().generate(audio, verses)
    }
}