package org.wycliffeassociates.otter.common.navigation

interface ITabGroupBuilder {
    fun build(type: TabGroupType): ITabGroup {
        return when (type) {
            TabGroupType.PROJECT -> buildProjectTabGroup()
            TabGroupType.CHAPTER -> buildChapterTabGroup()
            TabGroupType.RECORDABLE -> buildRecordableTabGroup()
            TabGroupType.RECORD_SCRIPTURE -> buildRecordScriptureTabGroup()
            TabGroupType.RECORD_RESOURCE -> buildRecordResourceTabGroup()
        }
    }

    fun buildProjectTabGroup(): ITabGroup

    fun buildChapterTabGroup(): ITabGroup

    fun buildRecordableTabGroup(): ITabGroup

    fun buildRecordScriptureTabGroup(): ITabGroup

    fun buildRecordResourceTabGroup(): ITabGroup
}
