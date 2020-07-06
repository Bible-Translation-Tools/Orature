package org.wycliffeassociates.otter.common.navigation

import java.util.Stack

interface INavigator {
    val tabGroupMap: MutableMap<TabGroupType, ITabGroup>
    val navBackStack: Stack<ITabGroup>
    var currentGroup: ITabGroup?
    val tabGroupBuilder: ITabGroupBuilder

    fun back() {
        if (navBackStack.isNotEmpty()) {
            currentGroup?.deactivate()
            val previousGroup = navBackStack.pop()
            previousGroup.activate()
            currentGroup = previousGroup
        }
    }

    fun navigateTo(tabGroupType: TabGroupType) {
        currentGroup?.let {
            it.deactivate()
            navBackStack.push(it)
        }

        val nextGroup = tabGroupMap[tabGroupType]
            ?: createTabGroup(tabGroupType)

        nextGroup.activate()
        currentGroup = nextGroup
    }

    private fun createTabGroup(tabGroupType: TabGroupType): ITabGroup {
        val group = tabGroupBuilder.build(tabGroupType)
        tabGroupMap[tabGroupType] = group
        return group
    }
}
