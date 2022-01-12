/**
 * Copyright (C) 2020-2022 Wycliffe Associates
 *
 * This file is part of Orature.
 *
 * Orature is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Orature is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Orature.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.wycliffeassociates.otter.common.domain.resourcecontainer.project.markdown

import junit.framework.TestCase
import org.junit.Test
import org.wycliffeassociates.otter.common.collections.OtterTree
import org.wycliffeassociates.otter.common.collections.OtterTreeNode
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.IProjectReader
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.OtterFile
import java.io.File
import kotlin.IllegalArgumentException

class MarkdownProjectReaderTest {
    private val pwd = File(System.getProperty("user.dir"))
    private val fileTree: OtterTree<OtterFile> = pwd.buildFileTree()

    private fun <T> dfs(tree: OtterTreeNode<T>, f: (T, List<T>) -> Boolean, acc: List<T> = listOf()): T? {
        return if (f(tree.value, acc))
            tree.value
        else {
            var ret: T? = null
            (tree as? OtterTree)?.children?.find {
                ret = dfs(it, f, acc + tree.value)
                ret != null
            }
            return ret
        }
    }

    @Test
    fun testBuilderMarkdown() {
        val reader = IProjectReader.build("text/markdown", false)
        TestCase.assertTrue(reader is MarkdownProjectReader)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testBuilderUnknown() {
        IProjectReader.build("unknown", false)
    }

    @Test
    fun testFileTree_NotEmpty() {
        TestCase.assertTrue(fileTree.children.isNotEmpty())
    }

    @Test
    fun testFileTree_CorrectRoot() {
        val rootFile = pwd
        val tree = rootFile.buildFileTree()

        TestCase.assertEquals(rootFile.absolutePath, tree.value.absolutePath)
    }

    @Test
    fun testFileTree_NoDupes() {
        val set = hashSetOf<OtterFile>()
        val foundADupe: (OtterFile, Any) -> Boolean = { f: OtterFile, _: Any -> !set.add(f) }
        TestCase.assertNull(dfs(fileTree, foundADupe))
    }

    @Test
    fun testFileTree_ParentChild() {
        dfs(fileTree, { f, parents ->
            parents.lastOrNull()?.let {
                TestCase.assertEquals("$f not child of $it.", f.parentFile, it)
            }
            false
        })
    }
}
