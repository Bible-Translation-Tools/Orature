package org.wycliffeassociates.otter.common.domain.project

import org.wycliffeassociates.otter.common.data.OratureFileFormat
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import java.io.File
import java.io.InvalidObjectException
import java.lang.Exception
import java.lang.IllegalArgumentException
import kotlin.jvm.Throws

object ProjectFormatIdentifier {
    @Throws(
        IllegalArgumentException::class,
        InvalidResourceContainerException::class,
    )
    fun getProjectFormat(file: File): ProjectFormat {
        return when {
            OratureFileFormat.isSupported(file.extension) || file.isDirectory -> {
                validateOratureFile(file)
                ProjectFormat.RESOURCE_CONTAINER
            }
            else -> {
                throw IllegalArgumentException("The following file is not supported: $file")
            }
        }
    }

    private fun validateOratureFile(file: File) {
        try {
            ResourceContainer.load(file).close()
        } catch (e: Exception) {
            throw InvalidResourceContainerException("Invalid resource container file $file")
        }
    }
}

class InvalidResourceContainerException(
    override val message: String,
) : InvalidObjectException(message)
