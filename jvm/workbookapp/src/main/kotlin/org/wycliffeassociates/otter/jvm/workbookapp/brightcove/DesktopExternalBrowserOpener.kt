package org.wycliffeassociates.otter.jvm.workbookapp.brightcove

import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.domain.brightcove.ExternalBrowserOpener
import java.awt.Desktop
import java.net.URI
import javax.inject.Inject

class DesktopExternalBrowserOpener @Inject constructor() : ExternalBrowserOpener {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun open(url: String) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(URI(url))
            } else {
                logger.warn("Desktop browsing is not supported; user must open URL manually: {}", url)
            }
        } catch (e: Exception) {
            logger.error("Failed to open browser for URL: {}", url, e)
        }
    }
}
