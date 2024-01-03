package org.wycliffeassociates.otter.jvm.workbookapp.persistence.entities

data class WorkbookDescriptorEntity(
    var id: Int,
    var sourceFk: Int,
    var targetFk: Int,
    var typeFk: Int,
)
