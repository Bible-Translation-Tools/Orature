package org.wycliffeassociates.otter.jvm.workbookapp

import org.junit.Test
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.VersificationTreeBuilder
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import java.io.File
import kotlin.io.path.outputStream

class VersificationTreeBuilderTest {
    @Test
    fun testVersificationBuilder() {
        val tmp = File.createTempFile("rcFile", "zip").apply { deleteOnExit() }
        javaClass.classLoader.getResourceAsStream("content/en_ulb.zip").transferTo(tmp.outputStream())

        val rc = ResourceContainer.load(tmp)

        val tree = VersificationTreeBuilder().build(rc)
        println(tree)
    }
}