package org.wycliffeassociates.otter.common.domain.resourcecontainer.project.markdown

import java.io.File
import junit.framework.TestCase
import kotlin.IllegalArgumentException
import org.junit.Test
import org.wycliffeassociates.otter.common.collections.tree.OtterTree
import org.wycliffeassociates.otter.common.collections.tree.OtterTreeNode
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.IProjectReader
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.OtterFile

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
