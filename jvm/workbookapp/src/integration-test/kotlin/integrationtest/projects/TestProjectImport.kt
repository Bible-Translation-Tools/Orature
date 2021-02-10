package integrationtest.projects

import org.junit.Assert
import org.junit.Test
import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.common.data.model.ContainerType
import org.wycliffeassociates.otter.common.data.model.ContentType.BODY
import org.wycliffeassociates.otter.common.data.model.ContentType.META
import org.wycliffeassociates.otter.common.data.model.ContentType.TEXT
import org.wycliffeassociates.otter.common.data.model.ContentType.TITLE
import org.wycliffeassociates.otter.common.data.model.Language
import org.wycliffeassociates.otter.common.data.model.ResourceMetadata
import java.io.File
import java.time.LocalDate


class TestProjectImport {

    private val db = DatabaseEnvironment()

    private val sourceMetadata = ResourceMetadata(
        "rc0.2",
        "Door43 World Missions Community",
        "",
        "",
        "ulb",
        LocalDate.now(),
        Language("en", "", "", "", true),
        LocalDate.now(),
        "",
        "",
        ContainerType.Book,
        "",
        "12",
        File(".")
    )

    private val ulbTargetMetadata = sourceMetadata.copy(
        creator = "Orature",
        language = Language("en-x-demo1", "", "", "", true)
    )

    private val tnTargetMetadata = sourceMetadata.copy(
        creator = "Orature",
        language = Language("en-x-demo1", "", "", "", true),
        identifier = "tn",
        version = "11",
        type = ContainerType.Help
    )

    private val project = Collection(
        1,
        "rev",
        "rev",
        "",
        null
    )

    private val ulbProjectDir = db.directoryProvider.getProjectDirectory(
        sourceMetadata,
        ulbTargetMetadata,
        project
    )

    private val ulbSourceDir = db.directoryProvider.getProjectSourceDirectory(
        sourceMetadata,
        ulbTargetMetadata,
        project
    )

    private val ulbAudioDir = db.directoryProvider.getProjectAudioDirectory(
        sourceMetadata,
        ulbTargetMetadata,
        project
    )

    private val tnProjectDir = db.directoryProvider.getProjectDirectory(
        sourceMetadata,
        tnTargetMetadata,
        project
    )

    private val tnSourceDir = db.directoryProvider.getProjectSourceDirectory(
        sourceMetadata,
        tnTargetMetadata,
        project
    )

    private val tnAudioDir = db.directoryProvider.getProjectAudioDirectory(
        sourceMetadata,
        tnTargetMetadata,
        project
    )

    @Test
    fun ulb() {
        db.import("en-x-demo1-ulb-rev.zip")
            .assertRowCounts(
                RowCount(
                    contents = mapOf(
                        META to 1211,
                        TEXT to 31509
                    ),
                    collections = 1279,
                    links = 0
                )
            )

        Assert.assertEquals(true, ulbProjectDir.resolve("manifest.yaml").exists())
        Assert.assertEquals(true, ulbSourceDir.resolve("en_ulb.zip").exists())
        Assert.assertEquals(
            true,
            ulbAudioDir.walkTopDown()
                .filter { it.extension == "wav" }
                .count() == 3
        )
    }

    @Test
    fun ulbDirectory() {
        db.import("en-x-demo1-ulb-rev.zip", unzip = true)
            .assertRowCounts(
                RowCount(
                    contents = mapOf(
                        META to 1211,
                        TEXT to 31509
                    ),
                    collections = 1279,
                    links = 0
                )
            )

        Assert.assertEquals(true, ulbProjectDir.resolve("manifest.yaml").exists())
        Assert.assertEquals(true, ulbSourceDir.resolve("en_ulb.zip").exists())
        Assert.assertEquals(
            true,
            ulbAudioDir.walkTopDown()
                .filter { it.extension == "wav" }
                .count() == 3
        )
    }

    @Test
    fun tnHelps() {
        db.import("en-x-demo1-tn-rev.zip")
            .assertRowCounts(
                RowCount(
                    contents = mapOf(
                        META to 1211,
                        TEXT to 31509,
                        TITLE to 82025,
                        BODY to 79240
                    ),
                    collections = 1279,
                    links = 158796
                )
            )

        Assert.assertEquals(true, tnProjectDir.resolve("manifest.yaml").exists())
        Assert.assertEquals(true, tnSourceDir.resolve("en_ulb.zip").exists())
        Assert.assertEquals(true, tnSourceDir.resolve("en_tn-master.zip").exists())
        Assert.assertEquals(
            true,
            tnAudioDir.walkTopDown()
                .filter { it.extension == "wav" }
                .count() == 3
        )
    }
}
