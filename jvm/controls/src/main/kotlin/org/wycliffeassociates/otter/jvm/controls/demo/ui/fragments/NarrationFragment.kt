package org.wycliffeassociates.otter.jvm.controls.demo.ui.fragments

import javafx.collections.FXCollections
import org.wycliffeassociates.otter.common.data.primitives.Verse
import org.wycliffeassociates.otter.jvm.controls.narration.Narration
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import tornadofx.*

class NarrationFragment : Fragment() {
    private val verses = FXCollections.observableArrayList(
        Verse("1", "Then Jonah prayed to Yahweh his God from the fish's stomach."),
        Verse("2", "He said, I called out to Yahweh about my distress and he answered me; " +
                "from the belly of Sheol I cried out for help! You heard my voice."),
        Verse("3", "You had thrown me into the depths, into the heart of the seas, " +
                "and the currents surrounded me; all your waves and billows passed over me."),
        Verse("4", "I said, 'I am driven out from before your eyes; yet I will again " +
                "look toward your holy temple.'"),
        Verse("5", "The waters closed around me up to my neck; the deep was all around me; " +
                "seaweed wrapped around my head."),
        Verse("6", "I went down to the bases of the mountains; the earth with its bars " +
                "closed upon me forever. Yet you brought up my life from the pit, Yahweh, my God!"),
        Verse("7", "When my soul fainted within me, I called Yahweh to mind; then my " +
                "prayer came to you to your holy temple."),
        Verse("8", "They give attention to meaningless gods while they abandon covenant faithfulness."),
        Verse("9", "But as for me, I will sacrifice to you with a voice of thanksgiving; " +
                "I will fulfill that which I have vowed. Salvation comes from Yahweh!"),
        Verse("10", "Then Yahweh spoke to the fish, and it vomited up Jonah upon the dry land."),
    )

    override val root = stackpane {
        add(Narration(verses))
    }

    init {
        tryImportStylesheet(resources["/css/narration.css"])
    }
}