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
        "DesignRules.kt" to "482a9823a3d9732c58229b1f7cfc92ba4a722041832dbfe9764989f1982ef110",
        "DesignTestBase.kt" to "644589b222668beae2727fa5b812996a29a7276f2b642cb00377c4e715b96b6a",
        "NaiveProbes.kt" to "0bb188aa7701341c1af56be12933d1f5e373469ac7721b905104d50fc204ff72",
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
