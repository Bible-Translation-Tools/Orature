package org.wycliffeassociates.otter.jvm.app.ui.chromeablestage.tabgroups

import org.wycliffeassociates.otter.common.navigation.ITabGroupBuilder

class TabGroupBuilder : ITabGroupBuilder {
    override fun buildProjectTabGroup() = ProjectTabGroup()
    override fun buildChapterTabGroup() = ChapterTabGroup()
    override fun buildRecordableTabGroup() = RecordableTabGroup()
    override fun buildRecordScriptureTabGroup() = RecordScriptureTabGroup()
    override fun buildRecordResourceTabGroup() = RecordResourceTabGroup()
}