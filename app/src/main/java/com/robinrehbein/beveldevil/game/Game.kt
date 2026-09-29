package com.robinrehbein.beveldevil.game

import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

interface Progress {
    var unlocked: Int
    var sound: Boolean
    var stickScheme: Boolean
    /** 0 = S, 1 = M, 2 = L. */
    var buttonSize: Int
    var haptics: Boolean
    var leftHanded: Boolean
    fun bestDeaths(level: Int): Int?
    fun saveBest(level: Int, deaths: Int)
    fun cardFound(card: Card): Boolean
    fun findCard(card: Card)
    fun cardDeaths(card: Card): Int
    fun addCardDeath(card: Card)
}

enum class Sound { JUMP, LAND, DIE, WIN, CARD, LAUGH, CLICK, CRASH, BONK, FLIP }

interface Audio {
    fun play(sound: Sound)
}

enum class Screen { TITLE, SELECT, PLAY, PAUSE, CLEAR, ALBUM, END, SETTINGS }

enum class Mood { GRIN, LAUGH, SULK, SHOCK }

/** A tappable rectangle in the 256×144 logical screen. */
data class Hit(val x: Int, val y: Int, val w: Int, val h: Int) {
    operator fun contains(p: Pair<Float, Float>) = p.first >= x && p.first < x + w && p.second >= y && p.second < y + h
}

/** Screen layout in logical pixels, shared by input handling and rendering. */
object Ui {
    const val W = 256
    const val H = 144
    val titlePlay = Hit(88, 90, 80, 18)
    val titleAlbum = Hit(88, 112, 80, 14)
    val sound = Hit(224, 6, 26, 12)
    val gear = Hit(196, 6, 24, 12)
    val pauseSettings = Hit(88, 102, 80, 16)
    const val SET_ROWS = 5
    val setCounts = intArrayOf(2, 3, 2, 2, 2)
    /** Option [i] of [n] in settings row [row]. */
    fun setOpt(row: Int, i: Int, n: Int): Hit {
        val w = (114 - (n - 1) * 4) / n
        return Hit(132 + i * (w + 4), 26 + row * 22, w, 16)
    }
    val back = Hit(6, 6, 20, 12)
    val selectAlbum = Hit(196, 122, 54, 14)
    fun levelTile(i: Int) = Hit(18 + (i % 6) * 38, 32 + (i / 6) * 42, 30, 32)
    val hudPause = Hit(4, 3, 14, 12)
    val pauseResume = Hit(88, 58, 80, 16)
    val pauseLevels = Hit(88, 80, 80, 16)
    val clearNext = Hit(88, 110, 80, 16)
    fun albumCard(i: Int) = Hit(18 + (i % 6) * 38, 28 + (i / 6) * 54, 30, 44)
    val endTitle = Hit(88, 112, 80, 16)
    val devilFrame = Hit(212, 4, 38, 38)
}

/** [size] in pixels; dust drifts and slows down instead of falling. */
class Particle(var x: Float, var y: Float, var vx: Float, var vy: Float, var life: Float, val color: Int, val size: Int = 2, val dust: Boolean = false) {
    val life0 = life
}

/** Everything above a single attempt: screens, Mephi, cards, progress. */
class Game(private val progress: Progress, private val audio: Audio) {
    var screen = Screen.TITLE
        private set
    var levelIndex = 0
        private set
    var world: World? = null
        private set
    val level get() = Levels.all[levelIndex]
    var deaths = 0
        private set
    var time = 0f
        private set

    var mood = Mood.GRIN
        private set
    private var moodTimer = 0f
    var bubble: String? = null
        private set
    var bubbleAge = 0f
        private set
    private var bubbleLife = 0f

    /** Card currently flying/showing, and its age in seconds. */
    var card: Card? = null
        private set
    var cardAge = 0f
        private set
    private var survivalCheck = -1f

    var shake = 0f
        private set
    /** Seconds since the background last got hot (a trap fired). */
    var heat = 0f
        private set
    val particles = ArrayList<Particle>()
    var hapticPulse = false
    private var deadTimer = 0f
    private var selectedAlbum = -1
    val input = Controls()
    private val rng = Random(7)
    private val fx = Random(11)
    private var lastTaunt = -1

    val soundOn get() = progress.sound
    fun unlocked() = progress.unlocked
    fun bestDeaths(i: Int) = progress.bestDeaths(i)
    fun cardFound(c: Card) = progress.cardFound(c)
    fun cardDeaths(c: Card) = progress.cardDeaths(c)
    val albumSelection get() = selectedAlbum
    val scheme get() = if (progress.stickScheme) Scheme.STICK else Scheme.BUTTONS
    val buttonSize get() = progress.buttonSize
    val haptics get() = progress.haptics
    val leftHanded get() = progress.leftHanded
    private var settingsFrom = Screen.TITLE

    fun update(dt: Float) {
        time += dt
        shake = (shake - dt * 3f).coerceAtLeast(0f)
        heat = (heat - dt * 0.8f).coerceAtLeast(0f)
        bubbleAge += dt
        if (bubble != null && bubbleAge > bubbleLife) bubble = null
        if (moodTimer > 0f) {
            moodTimer -= dt
            if (moodTimer <= 0f) mood = Mood.GRIN
        }
        if (card != null) {
            cardAge += dt
            if (cardAge > CARD_LIFE) card = null
        }
        updateParticles(dt)
        if (screen != Screen.PLAY) return

        val w = world ?: return
        w.step(dt, input)
        handleEvents(w)
        if (survivalCheck >= 0f) {
            survivalCheck -= dt
            if (survivalCheck < 0f && w.state == WorldState.PLAYING) setMood(Mood.SULK, 1.6f)
        }
        when (w.state) {
            WorldState.DEAD -> {
                deadTimer += dt
                if (deadTimer > 0.9f) restartAttempt()
            }
            WorldState.WON -> {
                deadTimer += dt
                if (deadTimer > WIN_DELAY) finishLevel()
            }
            WorldState.PLAYING -> {}
        }
    }

    private fun handleEvents(w: World) {
        for (e in w.events) when (e) {
            Event.Jump -> { audio.play(Sound.JUMP); dust(w, 3, 2.5f) }
            Event.Land -> { audio.play(Sound.LAND); dust(w, 5, 4f) }
            Event.Bonk -> { audio.play(Sound.BONK); shake = maxOf(shake, 0.4f) }
            Event.Flip -> audio.play(Sound.FLIP)
            Event.Crash -> audio.play(Sound.CRASH)
            is Event.Shake -> shake = maxOf(shake, e.amount)
            is Event.Say -> say(e.text.toString(), 2.6f)
            is Event.Played -> {
                card = e.card
                cardAge = 0f
                heat = 1f
                progress.findCard(e.card)
                audio.play(Sound.CARD)
                setMood(Mood.LAUGH, 1.2f)
                survivalCheck = 2.2f
            }
            is Event.Died -> {
                deaths++
                w.lastCard?.let { progress.addCardDeath(it) }
                survivalCheck = -1f
                burst(e.x, e.y)
                audio.play(Sound.DIE)
                audio.play(Sound.LAUGH)
                setMood(Mood.LAUGH, 1.6f)
                say(taunt(), 1.8f)
                hapticPulse = true
                deadTimer = 0f
            }
            Event.Won -> {
                audio.play(Sound.WIN)
                setMood(Mood.SHOCK, 99f)
                say(WIN_LINES[rng.nextInt(WIN_LINES.size)].toString(), 3f)
                deadTimer = 0f
            }
        }
        w.events.clear()
    }

    /** The cube shatters: a 3×3 grid of chunks flies apart, plus fine sparks. */
    private fun burst(x: Float, y: Float) {
        val up = -6f * (world?.gravity ?: 1f)
        for (i in 0 until 9) {
            val gx = i % 3 - 1
            val gy = i / 3 - 1
            val s = 5f + fx.nextFloat() * 5f
            val color = when (gy) { -1 -> 0xFFB8FFE6.toInt(); 0 -> 0xFF6CF2C2.toInt(); else -> 0xFF2AA97F.toInt() }
            particles += Particle(x + gx * 0.25f, y + gy * 0.25f, gx * s + fx.nextFloat() * 2f - 1f, gy * s * 0.6f + up, 0.7f + fx.nextFloat() * 0.4f, color, size = 3)
        }
        repeat(14) {
            val a = fx.nextFloat() * 6.283f
            val s = 8f + fx.nextFloat() * 12f
            particles += Particle(x, y, cos(a) * s, sin(a) * s + up, 0.3f + fx.nextFloat() * 0.35f, if (it % 2 == 0) 0xFFFFFFFF.toInt() else 0xFFB8FFE6.toInt(), size = 1)
        }
    }

    /** Puffs at the player's feet (their head, when gravity is flipped). */
    private fun dust(w: World, n: Int, spread: Float) {
        val b = w.player.box
        val feet = if (w.gravity < 0) b.y else b.b
        repeat(n) {
            val side = if (it % 2 == 0) 1f else -1f
            val s = spread * (0.4f + fx.nextFloat() * 0.6f)
            particles += Particle(b.cx + side * (0.3f + fx.nextFloat() * 0.2f), feet - 0.1f * w.gravity, side * s, -w.gravity * (0.4f + fx.nextFloat() * 0.8f), 0.3f + fx.nextFloat() * 0.2f, 0xFFEADCCB.toInt(), size = 4, dust = true)
        }
    }

    private fun updateParticles(dt: Float) {
        val g = 40f * (world?.gravity ?: 1f)
        val it = particles.iterator()
        while (it.hasNext()) {
            val p = it.next()
            p.life -= dt
            if (p.life <= 0f) { it.remove(); continue }
            if (p.dust) { p.vx *= 1f - dt * 6f; p.vy *= 1f - dt * 4f } else p.vy += g * dt
            p.x += p.vx * dt
            p.y += p.vy * dt
        }
    }

    private fun taunt(): String {
        var i: Int
        do i = rng.nextInt(TAUNTS.size) while (i == lastTaunt && TAUNTS.size > 1)
        lastTaunt = i
        return TAUNTS[i].toString().replace("%d", deaths.toString())
    }

    private fun say(text: String, life: Float) {
        bubble = text
        bubbleAge = 0f
        bubbleLife = life + text.length / 28f
    }

    private fun setMood(m: Mood, seconds: Float) {
        mood = m
        moodTimer = seconds
    }

    // ---------- flow ----------

    fun startLevel(i: Int) {
        levelIndex = i
        deaths = 0
        particles.clear()
        world = World(level)
        deadTimer = 0f
        card = null
        survivalCheck = -1f
        setMood(Mood.GRIN, 0f)
        say(level.intro.toString(), 2.8f)
        screen = Screen.PLAY
    }

    private fun restartAttempt() {
        world = World(level)
        deadTimer = 0f
        card = null
        survivalCheck = -1f
        input.jumpPressed = false
    }

    private fun finishLevel() {
        val best = progress.bestDeaths(levelIndex)
        if (best == null || deaths < best) progress.saveBest(levelIndex, deaths)
        if (progress.unlocked < levelIndex + 2) progress.unlocked = minOf(Levels.all.size, levelIndex + 2)
        screen = if (levelIndex == Levels.all.lastIndex) Screen.END else Screen.CLEAR
    }

    fun totalBestDeaths() = Levels.all.indices.sumOf { progress.bestDeaths(it) ?: 0 }

    /** Handles a tap in logical coordinates. */
    fun tap(x: Float, y: Float) {
        val p = x to y
        when (screen) {
            Screen.TITLE -> when {
                p in Ui.sound -> { progress.sound = !progress.sound; click() }
                p in Ui.gear -> openSettings()
                p in Ui.titleAlbum -> go(Screen.ALBUM)
                else -> go(Screen.SELECT)
            }
            Screen.SELECT -> {
                when {
                    p in Ui.back -> go(Screen.TITLE)
                    p in Ui.selectAlbum -> go(Screen.ALBUM)
                    else -> Levels.all.indices.firstOrNull { p in Ui.levelTile(it) && it < progress.unlocked }?.let { click(); startLevel(it) }
                }
            }
            Screen.PLAY -> if (p in Ui.hudPause) go(Screen.PAUSE)
            Screen.PAUSE -> when {
                p in Ui.pauseLevels -> go(Screen.SELECT)
                p in Ui.pauseSettings -> openSettings()
                else -> go(Screen.PLAY)
            }
            Screen.SETTINGS -> if (p in Ui.back) go(settingsFrom) else {
                for (row in 0 until Ui.SET_ROWS) for (i in 0 until Ui.setCounts[row]) if (p in Ui.setOpt(row, i, Ui.setCounts[row])) { setOption(row, i); click() }
            }
            Screen.CLEAR -> if (p in Ui.clearNext) { click(); startLevel(levelIndex + 1) }
            Screen.ALBUM -> {
                if (p in Ui.back) go(Screen.TITLE)
                else selectedAlbum = Card.entries.indices.firstOrNull { p in Ui.albumCard(it) && progress.cardFound(Card.entries[it]) } ?: -1
            }
            Screen.END -> if (p in Ui.endTitle) go(Screen.TITLE)
        }
    }

    private fun openSettings() {
        settingsFrom = screen
        go(Screen.SETTINGS)
    }

    fun setOption(row: Int, i: Int) {
        when (row) {
            0 -> progress.stickScheme = i == 1
            1 -> progress.buttonSize = i
            2 -> progress.haptics = i == 0
            3 -> progress.sound = i == 0
            4 -> progress.leftHanded = i == 0
        }
    }

    /** Which option of settings [row] is active. */
    fun optionOf(row: Int) = when (row) {
        0 -> if (progress.stickScheme) 1 else 0
        1 -> progress.buttonSize
        2 -> if (progress.haptics) 0 else 1
        3 -> if (progress.sound) 0 else 1
        else -> if (progress.leftHanded) 0 else 1
    }

    private fun click() = audio.play(Sound.CLICK)

    private fun go(s: Screen) {
        click()
        if (s == Screen.ALBUM) selectedAlbum = -1
        if (s == Screen.TITLE || s == Screen.SELECT) setMood(Mood.GRIN, 0f)
        screen = s
    }

    /** Android back button. Returns false when the app should close. */
    fun back(): Boolean {
        when (screen) {
            Screen.TITLE -> return false
            Screen.SELECT, Screen.ALBUM, Screen.END -> go(Screen.TITLE)
            Screen.PLAY -> go(Screen.PAUSE)
            Screen.SETTINGS -> go(settingsFrom)
            Screen.PAUSE, Screen.CLEAR -> go(Screen.SELECT)
        }
        return true
    }

    /** App went to background. */
    fun pause() {
        if (screen == Screen.PLAY) screen = Screen.PAUSE
        input.left = false; input.right = false; input.jump = false; input.jumpPressed = false
    }

    companion object {
        const val CARD_LIFE = 2.4f
        /** Seconds between touching the door and the clear screen, for the win animation. */
        const val WIN_DELAY = 0.8f

        val TAUNTS = listOf(
            T("Ouch.", "Autsch."),
            T("That was on purpose. Mine.", "Das war Absicht. Meine."),
            T("Again!", "Nochmal!"),
            T("Saw that coming.", "Hab ich kommen sehen."),
            T("Almost! No, not really.", "Fast! Nein, eigentlich nicht."),
            T("Death number %d. Nice.", "Tod Nummer %d. Nicht schlecht."),
            T("The floor says hi.", "Der Boden lässt grüßen."),
            T("Keep going, I'm having fun.", "Weiter so, ich amüsier mich."),
        )
        val WIN_LINES = listOf(
            T("That... was not the plan.", "Das... war so nicht geplant."),
            T("Pure luck.", "Pures Glück."),
            T("Just wait for the next one.", "Na warte, nächstes Level."),
        )
    }
}
