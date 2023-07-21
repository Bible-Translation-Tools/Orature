package org.wycliffeassociates.otter.jvm.controls.model

import org.wycliffeassociates.otter.common.data.primitives.ProjectMode

data class ProjectGroupKey(val sourceLanguage: String, val targetLanguage: String, val mode: ProjectMode)