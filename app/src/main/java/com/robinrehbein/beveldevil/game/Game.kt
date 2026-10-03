package com.robinrehbein.beveldevil.game

import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/** Layout of the saved progress: 1 = World 1 had 128 levels, 2 = 48 (and World 2 still 128), 3 = World 2 has 48 too. */
const val SAVE_VERSION = 3

interface Progress {
    var unlocked: Int
    /** Which [SAVE_VERSION] the save was written for; in-memory saves are always current. */
    var saveVersion: Int
        get() = SAVE_VERSION
        set(_) {}
    var sound: Boolean
    /** Background music on/off (separate from [sound], the effects). */
    var music: Boolean
        get() = true
        set(_) {}
    var stickScheme: Boolean
    /** 0 = S, 1 = M, 2 = L. */
    var buttonSize: Int
    var haptics: Boolean
    var leftHanded: Boolean
    /** The story intro has been shown once. */
    var introSeen: Boolean
    /** Tilt levels read the motion sensor; off: on-screen tilt and shake buttons. */
    var tiltSensor: Boolean
    /** "Remove ads" was bought; a cache so it works offline, the store stays the authority. */
    var adsRemoved: Boolean
        get() = false
        set(_) {}
    /** Lifetime deaths over all levels (restarts count, skips don't); shown in the album. */
    var totalDeaths: Int
        get() = 0
        set(_) {}
    fun bestDeaths(level: Int): Int?
    fun saveBest(level: Int, deaths: Int)
    fun cardFound(card: Card): Boolean
    fun findCard(card: Card)
    fun cardDeaths(card: Card): Int
    fun addCardDeath(card: Card)
}

enum class Sound { JUMP, LAND, DIE, WIN, CARD, LAUGH, CLICK, CRASH, BONK, FLIP, SWITCH, SIZZLE, HUM }

interface Audio {
    fun play(sound: Sound)
    /** What background music should play now (null = none) and whether softly, e.g. under the pause menu. */
    fun music(tune: Tune?, duck: Boolean) {}
}

enum class Screen { TITLE, SELECT, PLAY, PAUSE, CLEAR, ALBUM, END, SETTINGS, INTRO, WORLD_INTRO }

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
    val pauseSettings = Hit(88, 110, 80, 16)
    val pauseSkip = Hit(88, 128, 80, 12)
    val privacy = Hit(78, 128, 100, 11)
    /** Settings: reopens the ad consent form, where the law asks for it. */
    val adChoices = Hit(6, 128, 64, 11)
    /** "No ads" purchase button on the title and the settings. */
    val noAdsTitle = Hit(6, 6, 52, 12)
    val noAdsSettings = Hit(196, 6, 54, 12)
    const val SET_ROWS = 7
    const val SET_STEP = 15
    const val SET_TOP = 22
    const val SET_H = 13
    val setCounts = intArrayOf(2, 3, 2, 2, 2, 2, 2)
    /** Option [i] of [n] in settings row [row]. */
    fun setOpt(row: Int, i: Int, n: Int): Hit {
        val w = (114 - (n - 1) * 4) / n
        return Hit(132 + i * (w + 4), SET_TOP + row * SET_STEP, w, SET_H)
    }
    val back = Hit(6, 6, 20, 12)
    val selectAlbum = Hit(196, 122, 54, 14)
    /** Levels per page of the level select: one act, 8×2 tiles. */
    const val PAGE = 16
    /** Tile [slot] (0 until [PAGE]) of the current page. */
    fun levelTile(slot: Int) = Hit(18 + (slot % 8) * 28, 40 + (slot / 8) * 38, 24, 32)
    /** Tab of world [k] of [n], centered and clear of the back button. */
    fun worldTab(k: Int, n: Int): Hit {
        val w = minOf(58, (192 - (n - 1) * 4) / n)
        return Hit(128 - (n * w + (n - 1) * 4) / 2 + k * (w + 4), 6, w, 13)
    }
    /** Page arrows: the whole strip beside the tiles is tappable. */
    val pagePrev = Hit(0, 38, 17, 72)
    val pageNext = Hit(239, 38, 17, 72)
    /** Page pip [p] of [n], centered under the tiles. */
    fun pageDot(p: Int, n: Int) = Hit(128 - n * 5 + p * 10, 116, 10, 10)
    val hudPause = Hit(4, 3, 14, 12)
    val pauseResume = Hit(88, 50, 80, 16)
    val pauseRestart = Hit(88, 70, 80, 16)
    val pauseLevels = Hit(88, 90, 80, 16)
    val clearNext = Hit(88, 110, 80, 16)
    /** Cards per album page. */
    const val ALBUM_PAGE = 12
    /** Card [i] of the album; its slot on its page (12 per page, two rows of six). */
    fun albumCard(i: Int) = Hit(18 + (i % 6) * 38, 28 + ((i % ALBUM_PAGE) / 6) * 54, 30, 44)
    val endTitle = Hit(88, 112, 80, 16)
    val devilFrame = Hit(212, 4, 38, 38)
    val titleStory = Hit(160, 6, 32, 12)
    val introSkip = Hit(194, 4, 56, 12)
}

/** [size] in pixels; dust drifts and slows down instead of falling. */
class Particle(var x: Float, var y: Float, var vx: Float, var vy: Float, var life: Float, val color: Int, val size: Int = 2, val dust: Boolean = false) {
    val life0 = life
}

/** Everything above a single attempt: screens, Mephi, cards, progress. */
class Game(private val progress: Progress, private val audio: Audio, private val ads: Monetization = NoAds) {
    var screen = Screen.TITLE
        private set
    var levelIndex = 0
        private set
    var world: World? = null
        private set
    val level get() = sandbox ?: Levels.all[levelIndex]
    /** Plays this level instead of the registered one (tests and screenshots of levels outside the register). */
    internal var sandbox: Level? = null
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
    /** Where the flying card settles: -1 left, 0 stage center, 1 right. Center unless the player stands in its way. */
    var cardSide = 0
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
    /** The album page shown (12 cards each). */
    var albumPage = 0
        private set
    fun albumPages() = (Card.entries.size + Ui.ALBUM_PAGE - 1) / Ui.ALBUM_PAGE
    val input = Controls()
    private val rng = Random(7)
    private val fx = Random(11)
    private var lastTaunt = -1
    private var lastQuip: String? = null
    /** The bubble shows a level's own [Event.Say] line, which a quip must not cover. */
    private var bubbleIsTrap = false
    val totalDeaths get() = progress.totalDeaths

    val soundOn get() = progress.sound
    fun unlocked() = progress.unlocked
    /** [time] when the pause screen last opened. */
    var pausedAt = 0f
        private set
    /** The pause screen's RESUME and LEVELS buttons have traded places ([PauseTrick.SWAP]). */
    val pauseSwapped get() = world?.pauseTrick == PauseTrick.SWAP
    /** A fake clear screen or credits roll is showing instead of the HUD. */
    val fakeShown get() = screen == Screen.PLAY && world?.fakeShown == true
    fun bestDeaths(i: Int) = progress.bestDeaths(i)
    fun cardFound(c: Card) = progress.cardFound(c)
    fun cardDeaths(c: Card) = progress.cardDeaths(c)
    val albumSelection get() = selectedAlbum
    val scheme get() = if (progress.stickScheme) Scheme.STICK else Scheme.BUTTONS
    val buttonSize get() = progress.buttonSize
    val haptics get() = progress.haptics
    val leftHanded get() = progress.leftHanded
    val tiltSensor get() = progress.tiltSensor
    private var settingsFrom = Screen.TITLE

    // ---------- ads ----------

    /** Work handed over from other threads (ad callbacks), run on the game thread at the next update. */
    private val inbox = ConcurrentLinkedQueue<() -> Unit>()
    fun post(job: () -> Unit) { inbox.add(job) }
    private var clearsSinceAd = 0
    private var lastAdAt = -AdRules.COOLDOWN
    /** An ad is on screen; the tap that started it must not start a second one. */
    private var adShowing = false
    val adsRemoved get() = ads.adsRemoved
    /** The "NO ADS" button is offered: the store is ready and the player has not bought yet. */
    val noAdsOffered get() = ads.canPurchase && !ads.adsRemoved
    val adChoicesOffered get() = ads.privacyOptionsRequired
    /** The pause menu offers skipping this level: stuck for a while, and not the finale. */
    val skipOffered get() = screen == Screen.PAUSE && deaths >= AdRules.SKIP_AFTER_DEATHS && levelIndex != Levels.all.lastIndex
    /** The skip can start now: free after the purchase, otherwise needs a loaded rewarded ad. */
    val skipReady get() = !adShowing && (ads.adsRemoved || ads.rewardedReady())
    /** The skip costs watching an ad (drives the button's label). */
    val skipNeedsAd get() = !ads.adsRemoved

    /** Intro page and seconds on it. */
    var introPage = 0
        private set
    var introAge = 0f
        private set
    private var introFirst = false
    /** World shown on the transition screen, and seconds on it. */
    var worldInfo: WorldInfo = Worlds.get(1)
        private set
    var worldAge = 0f
        private set
    /** Seconds on the ending: first the terminal with the kill, then the final card. */
    var endAge = 0f
        private set
    /** Seconds of CRT glitch left after Mephi played a card. */
    var glitch = 0f
        private set

    /** World and page shown on the level select. */
    var selWorld: WorldInfo = Worlds.all.first()
        private set
    var selPage = 0
        private set

    init {
        if (progress.saveVersion < SAVE_VERSION) {
            // World 1 shrank from 128 to 48 levels and World 2 followed, so an old save's global numbers point into
            // levels it never played. Clamp it to "World 1 done, World 2 level 1 open". Old best-death counts keep
            // their index and may show on other levels.
            progress.unlocked = progress.unlocked.coerceIn(1, World1.levels.size + 1)
            progress.saveVersion = SAVE_VERSION
        }
        // saves from before a world was added: a cleared last level unlocks the next world (only that one step,
        // or stale best-death entries would chain-unlock everything behind it)
        for (w in Worlds.all.drop(1)) {
            if (w.size > 0 && progress.unlocked == w.firstLevel && progress.bestDeaths(w.firstLevel - 1) != null) progress.unlocked++
        }
        if (!progress.introSeen) startIntro(first = true)
    }

    /** "2-17" for the current level. */
    val levelLabel get() = Worlds.label(levelIndex)
    fun worldOpen(w: WorldInfo) = w.size > 0 && w.firstLevel < progress.unlocked
    fun pages(w: WorldInfo = selWorld) = (w.size + Ui.PAGE - 1) / Ui.PAGE
    /** Global index of the level on tile [slot] of the shown page, or -1 past the world's end. */
    fun selLevel(slot: Int): Int {
        val local = selPage * Ui.PAGE + slot
        return if (slot in 0 until Ui.PAGE && local < selWorld.size) selWorld.firstLevel + local else -1
    }

    /** Turns the level select [d] pages, clamped to the world. */
    fun page(d: Int) {
        val p = (selPage + d).coerceIn(0, maxOf(0, pages() - 1))
        if (p != selPage) { selPage = p; click() }
    }

    /** Shows world [w] on the level select, on the page with its highest unlocked level. */
    private fun selectWorld(w: WorldInfo) {
        selWorld = w
        val hi = (progress.unlocked - 1).coerceIn(w.firstLevel, w.firstLevel + maxOf(0, w.size - 1))
        selPage = (hi - w.firstLevel) / Ui.PAGE
    }

    /** Opens the level select on the world of global level [i]. */
    private fun openSelect(i: Int) {
        selectWorld(Worlds.of(i))
        go(Screen.SELECT)
    }

    /** Keyboard/gamepad confirm on the level select: the highest unlocked level of the shown world. */
    fun selectConfirm() {
        if (screen != Screen.SELECT || !worldOpen(selWorld)) return
        click()
        startLevel((progress.unlocked - 1).coerceIn(selWorld.firstLevel, selWorld.firstLevel + selWorld.size - 1))
    }

    /** The loop for the current screen; levels keep their world's tune across deaths, restarts and the pause menu. */
    private fun tune(): Tune? = when (screen) {
        Screen.PLAY, Screen.PAUSE, Screen.CLEAR -> Tune.ofWorld(Worlds.of(levelIndex).number)
        Screen.SETTINGS -> if (settingsFrom == Screen.PAUSE) Tune.ofWorld(Worlds.of(levelIndex).number) else Tune.TITLE
        else -> Tune.TITLE
    }

    fun update(dt: Float) {
        while (true) (inbox.poll() ?: break)()
        audio.music(if (progress.music) tune() else null, screen == Screen.PAUSE || (screen == Screen.SETTINGS && settingsFrom == Screen.PAUSE))
        time += dt
        shake = (shake - dt * 3f).coerceAtLeast(0f)
        heat = (heat - dt * 0.8f).coerceAtLeast(0f)
        glitch = (glitch - dt).coerceAtLeast(0f)
        introAge += dt
        worldAge += dt
        endAge += dt
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
            Event.Hop -> audio.play(Sound.FLIP)
            Event.Switch -> audio.play(Sound.SWITCH)
            Event.Sizzle -> audio.play(Sound.SIZZLE)
            Event.Hum -> audio.play(Sound.HUM)
            is Event.Shake -> shake = maxOf(shake, e.amount)
            is Event.Say -> { say(e.text.toString(), 2.6f); bubbleIsTrap = bubble != null }
            is Event.Played -> {
                card = e.card
                cardAge = 0f
                cardSide = world?.player?.box?.let { p ->
                    // the card is about 5.5×7.5 tiles around (16, 8.25); keep a margin for the player walking on
                    val hits = abs(p.cx - 16f) < 2.75f + 2f + p.w / 2 && abs(p.cy - 8.25f) < 3.75f + 1f + p.h / 2
                    if (!hits) 0 else if (p.cx > 16f) -1 else 1
                } ?: 0
                heat = 1f
                glitch = GLITCH_TIME
                progress.findCard(e.card)
                audio.play(Sound.CARD)
                setMood(Mood.LAUGH, 1.2f)
                survivalCheck = 2.2f
            }
            is Event.Died -> {
                countDeath()
                w.lastCard?.let { progress.addCardDeath(it) }
                survivalCheck = -1f
                burst(e.x, e.y)
                audio.play(Sound.DIE)
                audio.play(Sound.LAUGH)
                setMood(Mood.LAUGH, 1.6f)
                val quip = if (bubbleIsTrap && bubble != null) null else quip()
                if (quip != null) say(quip, QUIP_LIFE) else say(taunt(), 1.8f)
                hapticPulse = true
                deadTimer = 0f
            }
            Event.FakeWon -> {
                audio.play(Sound.WIN)
                setMood(Mood.SHOCK, 99f)
                say(WIN_LINES[rng.nextInt(WIN_LINES.size)].toString(), 3f)
            }
            Event.Unfaked -> {
                glitch = GLITCH_TIME * 1.6f
                heat = 1f
                shake = maxOf(shake, 0.8f)
                audio.play(Sound.CARD)
                audio.play(Sound.LAUGH)
                setMood(Mood.LAUGH, 1.6f)
                say(Twists.nope.toString(), 1.6f)
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

    private fun countDeath() {
        deaths++
        progress.totalDeaths = progress.totalDeaths + 1
    }

    /** Mephi's quip for the death just counted when it is the 3rd, 6th or 10th in this level; never in the finale. */
    private fun quip(): String? {
        if (sandbox == null && levelIndex == Levels.all.lastIndex) return null
        val line = DevilQuips.pick(levelIndex, deaths, lastQuip) ?: return null
        lastQuip = line
        return line
    }

    private fun say(text: String, life: Float) {
        bubbleIsTrap = false
        // an empty line is Mephi saying nothing: no bubble (the layout can't wrap "")
        if (text.isBlank()) { bubble = null; return }
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

    /** Plays [l] as if it were level [levelIndex]; for tests and demos. */
    fun startCustom(l: Level) {
        sandbox = l
        startLevel(levelIndex)
    }

    private fun restartAttempt() {
        world = World(level, world?.trail)
        deadTimer = 0f
        card = null
        survivalCheck = -1f
        input.jumpPressed = false
        input.shake = false
    }

    /** The pause menu's RESTART: a fresh attempt like after a death, counted as one, but instant and without resuming traps. */
    private fun restartFromPause() {
        countDeath()
        particles.clear()
        restartAttempt()
        setMood(Mood.GRIN, 0f)
        bubble = null
        go(Screen.PLAY)
    }

    private fun finishLevel() {
        val best = progress.bestDeaths(levelIndex)
        if (best == null || deaths < best) progress.saveBest(levelIndex, deaths)
        clearsSinceAd++
        if (progress.unlocked < levelIndex + 2) progress.unlocked = minOf(Levels.all.size, levelIndex + 2)
        if (levelIndex == Levels.all.lastIndex) endAge = 0f
        screen = if (levelIndex == Levels.all.lastIndex) Screen.END else Screen.CLEAR
    }

    /** The clear screen's NEXT: an interstitial first when one is due, then [next]. */
    private fun advance() {
        if (adShowing) return
        val due = !ads.adsRemoved && AdRules.interstitialDue(levelIndex, clearsSinceAd, time - lastAdAt)
        if (due && ads.showInterstitial { post { adShowing = false; next() } }) {
            adShowing = true
            clearsSinceAd = 0
            lastAdAt = time
        } else next()
    }

    /** The pause menu's SKIP: the level counts as cleared (without a best score) after a rewarded ad, or at once with "no ads". */
    private fun skip() {
        if (!skipOffered || adShowing) return
        if (ads.adsRemoved) { skipLevel(); return }
        if (ads.showRewarded { earned -> post { adShowing = false; if (earned) skipLevel() } }) adShowing = true
    }

    private fun skipLevel() {
        if (screen != Screen.PAUSE) return
        if (progress.unlocked < levelIndex + 2) progress.unlocked = minOf(Levels.all.size, levelIndex + 2)
        clearsSinceAd = 0
        lastAdAt = time
        go(Screen.CLEAR)
    }

    /** From the clear screen: the next level, or first the transition screen when it opens a new world. */
    private fun next() {
        val n = levelIndex + 1
        val w = Worlds.of(n)
        if (n == w.firstLevel) showWorldIntro(w.number) else startLevel(n)
    }

    /** Starts the story. [first] is the automatic run on first start: it leads on to world 1. */
    fun startIntro(first: Boolean = false) {
        introPage = 0
        introAge = 0f
        introFirst = first
        setMood(Mood.GRIN, 0f)
        screen = Screen.INTRO
    }

    private fun finishIntro() {
        progress.introSeen = true
        if (introFirst) { introFirst = false; showWorldIntro(1) } else go(Screen.TITLE)
    }

    /** Shows the transition screen of world [n]; a tap continues into its first level. */
    fun showWorldIntro(n: Int) {
        worldInfo = Worlds.get(n)
        worldAge = 0f
        setMood(Mood.GRIN, 0f)
        screen = Screen.WORLD_INTRO
    }

    fun totalBestDeaths() = Levels.all.indices.sumOf { progress.bestDeaths(it) ?: 0 }

    /** Handles a tap in logical coordinates. */
    fun tap(x: Float, y: Float) {
        val p = x to y
        when (screen) {
            Screen.TITLE -> when {
                p in Ui.sound -> { progress.sound = !progress.sound; click() }
                p in Ui.gear -> openSettings()
                p in Ui.noAdsTitle && noAdsOffered -> ads.purchaseRemoveAds()
                p in Ui.titleStory -> { click(); startIntro() }
                p in Ui.titleAlbum -> go(Screen.ALBUM)
                else -> openSelect(progress.unlocked - 1)
            }
            Screen.SELECT -> tapSelect(p)
            Screen.PLAY -> if (p in Ui.hudPause && world?.pausePressed() != false) go(Screen.PAUSE)
            Screen.PAUSE -> when {
                p in (if (pauseSwapped) Ui.pauseResume else Ui.pauseLevels) -> openSelect(levelIndex)
                p in Ui.pauseRestart -> restartFromPause()
                p in Ui.pauseSettings -> openSettings()
                p in Ui.pauseSkip && skipOffered -> if (skipReady) skip()
                else -> { world?.resumed(); go(Screen.PLAY) }
            }
            Screen.SETTINGS -> if (p in Ui.back) go(settingsFrom)
            else if (p in Ui.noAdsSettings && noAdsOffered) ads.purchaseRemoveAds()
            else if (p in Ui.adChoices && adChoicesOffered) ads.showPrivacyOptions()
            else {
                for (row in 0 until Ui.SET_ROWS) for (i in 0 until Ui.setCounts[row]) if (p in Ui.setOpt(row, i, Ui.setCounts[row])) { setOption(row, i); click() }
            }
            Screen.INTRO -> when {
                p in Ui.introSkip -> finishIntro()
                introAge < Intro.duration(introPage) -> introAge = Intro.duration(introPage)
                introPage >= Intro.pages.lastIndex -> { click(); finishIntro() }
                else -> { click(); introPage++; introAge = 0f }
            }
            Screen.WORLD_INTRO -> if (worldAge > 0.4f) {
                if (worldInfo.size > 0) { click(); startLevel(worldInfo.firstLevel) } else openSelect(progress.unlocked - 1)
            }
            Screen.CLEAR -> if (p in Ui.clearNext) { click(); advance() }
            Screen.ALBUM -> {
                val first = albumPage * Ui.ALBUM_PAGE
                when {
                    p in Ui.back -> go(Screen.TITLE)
                    p in Ui.pagePrev && albumPages() > 1 -> albumTo(albumPage - 1)
                    p in Ui.pageNext && albumPages() > 1 -> albumTo(albumPage + 1)
                    else -> selectedAlbum = (first until minOf(Card.entries.size, first + Ui.ALBUM_PAGE))
                        .firstOrNull { p in Ui.albumCard(it) && progress.cardFound(Card.entries[it]) } ?: -1
                }
            }
            Screen.END -> when {
                endAge < Intro.endDuration -> endAge = Intro.endDuration
                p in Ui.endTitle -> go(Screen.TITLE)
            }
        }
    }

    private fun tapSelect(p: Pair<Float, Float>) {
        val n = Worlds.all.size
        val pages = pages()
        when {
            p in Ui.back -> go(Screen.TITLE)
            p in Ui.selectAlbum -> go(Screen.ALBUM)
            p in Ui.pagePrev -> page(-1)
            p in Ui.pageNext -> page(1)
            else -> {
                val tab = (0 until n).firstOrNull { p in Ui.worldTab(it, n) }
                if (tab != null) {
                    val w = Worlds.all[tab]
                    if (worldOpen(w) && w !== selWorld) { selectWorld(w); click() }
                    return
                }
                val dot = (0 until pages).firstOrNull { p in Ui.pageDot(it, pages) }
                if (dot != null) { page(dot - selPage); return }
                val i = (0 until Ui.PAGE).firstOrNull { p in Ui.levelTile(it) }?.let { selLevel(it) } ?: -1
                if (i >= 0 && i < progress.unlocked) { click(); startLevel(i) }
            }
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
            4 -> progress.music = i == 0
            5 -> progress.leftHanded = i == 0
            6 -> progress.tiltSensor = i == 0
        }
    }

    /** Which option of settings [row] is active. */
    fun optionOf(row: Int) = when (row) {
        0 -> if (progress.stickScheme) 1 else 0
        1 -> progress.buttonSize
        2 -> if (progress.haptics) 0 else 1
        3 -> if (progress.sound) 0 else 1
        4 -> if (progress.music) 0 else 1
        5 -> if (progress.leftHanded) 0 else 1
        else -> if (progress.tiltSensor) 0 else 1
    }

    private fun click() = audio.play(Sound.CLICK)

    private fun albumTo(page: Int) {
        val p = page.coerceIn(0, albumPages() - 1)
        if (p != albumPage) { albumPage = p; selectedAlbum = -1; click() }
    }

    private fun go(s: Screen) {
        click()
        if (s == Screen.ALBUM) { selectedAlbum = -1; albumPage = 0 }
        if (s == Screen.TITLE || s == Screen.SELECT) setMood(Mood.GRIN, 0f)
        if (s == Screen.PAUSE && screen == Screen.PLAY) pausedAt = time
        screen = s
    }

    /** Android back button. Returns false when the app should close. */
    fun back(): Boolean {
        when (screen) {
            Screen.TITLE -> return false
            Screen.SELECT, Screen.ALBUM, Screen.END -> go(Screen.TITLE)
            Screen.PLAY -> go(Screen.PAUSE)
            Screen.SETTINGS -> go(settingsFrom)
            Screen.INTRO -> finishIntro()
            Screen.WORLD_INTRO -> openSelect(if (worldInfo.size > 0) worldInfo.firstLevel else progress.unlocked - 1)
            Screen.PAUSE -> openSelect(levelIndex)
            // after a world's last level, back shows the world just opened
            Screen.CLEAR -> openSelect(levelIndex + 1)
        }
        return true
    }

    /** App went to background. */
    fun pause() {
        if (screen == Screen.PLAY) { screen = Screen.PAUSE; pausedAt = time }
        input.left = false; input.right = false; input.jump = false; input.jumpPressed = false; input.shake = false
    }

    companion object {
        const val CARD_LIFE = 2.4f
        /** Base life of a devil quip; [say] adds reading time, so it hangs about two seconds. */
        const val QUIP_LIFE = 0.4f
        /** Length of the CRT glitch when Mephi plays a card. */
        const val GLITCH_TIME = 0.32f
        /** Seconds between touching the door and the clear screen, for the win animation. */
        const val WIN_DELAY = 0.8f

        val TAUNTS = listOf(
            T("Segmentation fault. Yours.", "Segmentation Fault. Deiner."),
            T("Have you tried turning it off and on again? Oh, you just did.", "Schon mal neu gestartet? Ach, machst du ja gerade."),
            T("Death number %d. Logged.", "Tod Nummer %d. Steht im Log."),
            T("Error 404: survival not found.", "Fehler 404: Überleben nicht gefunden."),
            T("That's not a bug. That's a feature.", "Das ist kein Bug. Das ist ein Feature."),
            T("Ticket closed. Reason: user error.", "Ticket geschlossen. Grund: Benutzerfehler."),
            T("Uptime: 3 seconds. Impressive.", "Laufzeit: 3 Sekunden. Beeindruckend."),
            T("Undo? Not in this system.", "Rückgängig? Gibt's hier nicht."),
            T("Permission denied.", "Zugriff verweigert."),
            T("Did you save? Oh. Right.", "Hast du gespeichert? Ach nein."),
        )
        val WIN_LINES = listOf(
            T("Access granted. Ugh.", "Zugriff gewährt. Widerwillig."),
            T("Who gave you the password?!", "Wer hat dir das Passwort gegeben?!"),
            T("A security hole. I'll patch that.", "Eine Sicherheitslücke. Die patche ich noch."),
            T("Must be a caching issue.", "Muss ein Cache-Problem sein."),
            T("Wait for the next level. I'm deploying a patch.", "Na warte. Ich spiele gleich ein Update ein."),
        )
    }
}
