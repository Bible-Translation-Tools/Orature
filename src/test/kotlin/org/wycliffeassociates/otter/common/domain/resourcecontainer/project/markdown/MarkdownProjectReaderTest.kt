package org.wycliffeassociates.otter.common.domain.resourcecontainer.project.markdown

import junit.framework.TestCase
import org.junit.Test
import org.wycliffeassociates.otter.common.collections.tree.OtterTree
import org.wycliffeassociates.otter.common.collections.tree.OtterTreeNode
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.IProjectReader
import java.io.File

class MarkdownProjectReaderTest {
    private val pwd = File(System.getProperty("user.dir"))
    private val fileTree: OtterTree<File> = pwd.buildFileTree()

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
        val reader = IProjectReader.build("text/markdown")
        TestCase.assertTrue(reader is MarkdownProjectReader)
    }

    @Test
    fun testBuilderUnknown() {
        val reader = IProjectReader.build("unknown")
        TestCase.assertTrue(reader == null)
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
        val set = hashSetOf<File>()
        val foundADupe: (File, Any) -> Boolean = { f: File, _: Any -> !set.add(f) }
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
