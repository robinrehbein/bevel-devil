package com.robinrehbein.beveldevil

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Test sources must not read or write machine-local paths: a test that only passes where some scratch file exists
 * is green on a laptop and red in the release build.
 */
class NoLocalPathsTest {
    @Test
    fun testSourcesUseNoMachineLocalPaths() {
        val root = File("src/test")
        val forbidden = Regex("\"/tmp/|\"/root/|\"/home/|scratchpad/")
        val hits = root.walkTopDown()
            .filter { it.isFile && it.extension in setOf("kt", "java") && it.name != "NoLocalPathsTest.kt" }
            .flatMap { f -> f.readLines().withIndex().filter { forbidden.containsMatchIn(it.value) }.map { "${f.path}:${it.index + 1}: ${it.value.trim()}" } }
            .toList()
        assertTrue("machine-local paths in test sources (write to build/ instead):\n" + hits.joinToString("\n"), hits.isEmpty())
    }
}
