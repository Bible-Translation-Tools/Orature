package org.wycliffeassociates.otter.common.domain.resourcecontainer.burrito

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.bibletranslationtools.scriptureburrito.IngredientSchema
import org.bibletranslationtools.scriptureburrito.MetadataSchema
import org.bibletranslationtools.scriptureburrito.container.accessors.IContainerAccessor
import org.junit.Assert
import org.junit.Test
import org.wycliffeassociates.otter.common.domain.versification.Versification
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.repositories.IVersificationRepository
import org.wycliffeassociates.resourcecontainer.IResourceContainerAccessor
import java.io.File

class BurritoToResourceContainerConverterUsfmGenerationTest {

    private val directoryProvider = mockk<IDirectoryProvider> {
        every { tempDirectory } returns File(System.getProperty("java.io.tmpdir"))
    }
    private val versificationRepository = mockk<IVersificationRepository>()
    private val converter = BurritoToResourceContainerConverter(directoryProvider, versificationRepository)

}
