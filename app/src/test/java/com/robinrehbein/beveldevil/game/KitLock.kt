package com.robinrehbein.beveldevil.game

import java.io.File
import java.security.MessageDigest
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The guard-rail kit (docs/LEVEL_DESIGN_V2.md §9) is locked while the rollout runs: [DesignRules] and [DesignTestBase]
 * may only change through the orchestrator, who updates the hashes here. A builder whose level is red changes the
 * level, never the rules.
 */
object KitLock {
    const val MESSAGE = "kit is locked; only the orchestrator may change it"

    /**
     * SHA-256 of the file content, by file name under app/src/test/java/com/robinrehbein/beveldevil/game/. PendingRounds.kt
     * is deliberately not here: it may only shrink (docs/LEVEL_DESIGN_V2.md §9a), and deleting a line needs no new hash.
     */
    val HASHES = mapOf(
        "DesignRules.kt" to "827117f98050586250084dab5812e44d6da199b7257e34933e4190951a5dbd11",
        "DesignTestBase.kt" to "de858b4a146faaceef6114263b756d46df8635ae9f5dab823c950392544d849c",
        "NaiveProbes.kt" to "915788d33fe652b16fd4be4023576997e4c171fddc8051c851456c6d6c929dc6",
    )

    private const val DIR = "src/test/java/com/robinrehbein/beveldevil/game"

    /** The kit file [name], from the module directory (where Gradle runs the tests) or the repository root. */
    fun file(name: String): File = listOf(File("$DIR/$name"), File("app/$DIR/$name")).firstOrNull { it.exists() }
        ?: error("kit file $name not found")

    fun sha256(f: File): String = MessageDigest.getInstance("SHA-256").digest(f.readBytes()).joinToString("") { "%02x".format(it) }

    /** One line per kit file whose content does not match its locked hash. */
    fun violations(): List<String> = HASHES.mapNotNull { (name, hash) ->
        val now = sha256(file(name))
        if (now == hash) null else "$MESSAGE: $name has SHA-256 $now, locked $hash"
    }
}

class KitLockTest {
    @Test
    fun theKitIsLocked() {
        val v = KitLock.violations()
        assertTrue(v.joinToString("\n"), v.isEmpty())
    }

    @Test
    fun aChangedFileBreaksTheLock() {
        val f = File.createTempFile("kit", ".kt").apply { writeText("x"); deleteOnExit() }
        val before = KitLock.sha256(f)
        f.writeText("y")
        assertTrue(before != KitLock.sha256(f))
        assertTrue(KitLock.MESSAGE == "kit is locked; only the orchestrator may change it")
    }
}
