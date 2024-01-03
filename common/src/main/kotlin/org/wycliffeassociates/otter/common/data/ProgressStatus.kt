package org.wycliffeassociates.otter.common.data

/**
 * Status of the current stage of the background task to be shown in the UI.
 *
 * @param titleKey the localized key for the title text
 * @param titleMessage the message to be formatted with the localized title
 * @param subTitleKey the localized key for the subtitle text
 * @param subTitleMessage the message to be formatted with the localized subtitle
 */
data class ProgressStatus(
    val titleKey: String? = null,
    val titleMessage: String? = null,
    val subTitleKey: String? = null,
    val subTitleMessage: String? = null,
    val percent: Double? = null,
)
