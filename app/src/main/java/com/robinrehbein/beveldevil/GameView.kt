package com.robinrehbein.beveldevil

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.WindowInsets
import com.robinrehbein.beveldevil.game.Game
import com.robinrehbein.beveldevil.game.Scheme
import com.robinrehbein.beveldevil.game.Screen
import com.robinrehbein.beveldevil.game.TouchInput
import com.robinrehbein.beveldevil.game.Ui
import com.robinrehbein.beveldevil.render.Layout
import com.robinrehbein.beveldevil.render.Renderer
import kotlin.math.max
import kotlin.math.min

/** Hosts the game loop on its own thread: fixed 120 Hz simulation, render every vsync-ish frame. */
@SuppressLint("ViewConstructor")
class GameView(context: Context, private val game: Game) : SurfaceView(context), SurfaceHolder.Callback {
    private val renderer = Renderer(context)
    private val layout = Layout()
    private val controls get() = layout.controls
    private var surfaceW = 0
    private var surfaceH = 0
    private val cut = IntArray(4)
    private val lock = Any()
    @Volatile private var running = false
    private var thread: Thread? = null

    private val touch = TouchInput()
    private var cfg = -1
    private var keyLeft = false
    private var keyRight = false
    private var keyJump = false

    init {
        holder.addCallback(this)
        isFocusable = true
        isFocusableInTouchMode = true
    }

    override fun surfaceCreated(holder: SurfaceHolder) = start()
    override fun surfaceDestroyed(holder: SurfaceHolder) = stop()

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        surfaceW = width; surfaceH = height
        relayout()
    }

    override fun onApplyWindowInsets(insets: WindowInsets): WindowInsets {
        cut.fill(0)
        if (Build.VERSION.SDK_INT >= 28) insets.displayCutout?.let { c ->
            cut[0] = c.safeInsetLeft; cut[1] = c.safeInsetTop
            cut[2] = c.safeInsetRight; cut[3] = c.safeInsetBottom
        }
        relayout()
        return super.onApplyWindowInsets(insets)
    }

    private fun relayout() = synchronized(lock) {
        val c = controls
        c.stick = game.scheme == Scheme.STICK
        c.mirror = game.leftHanded
        c.sizeScale = SIZES[game.buttonSize.coerceIn(0, 2)]
        cfg = config()
        if (surfaceW == 0) return@synchronized
        val d = resources.displayMetrics.density
        layout.update(surfaceW, surfaceH, d, cut[0], cut[1], cut[2], cut[3])
        touch.width = surfaceW.toFloat(); touch.density = d
        touch.splitX = (c.leftX + c.rightX) / 2
        touch.scheme = game.scheme; touch.leftHanded = game.leftHanded
        touch.cancel()
        applyInput()
        val rects = exclusionRects(d)
        if (Build.VERSION.SDK_INT >= 29) post { systemGestureExclusionRects = rects }
    }

    private fun config() = (if (game.scheme == Scheme.STICK) 1 else 0) + game.buttonSize * 2 + (if (game.leftHanded) 8 else 0)

    /** Control areas at the bottom of each edge (Android caps exclusion at 200dp per edge). */
    private fun exclusionRects(d: Float): List<Rect> {
        val c = controls
        val top = max(0, surfaceH - (200 * d).toInt())
        val pad = (6 * d).toInt()
        val jumpFromEdge = if (c.mirror) 0 else (c.jumpX - c.r).toInt() - pad
        val mvEdge = if (c.stick) (150 * d).toInt() else (if (c.mirror) 0 else (c.rightX + c.r).toInt() + pad)
        val list = ArrayList<Rect>(2)
        val w = surfaceW
        if (c.mirror) {
            list += Rect(if (c.stick) w - mvEdge else (c.leftX - c.r).toInt() - pad, top, w, surfaceH)
            list += Rect(0, top, (c.jumpX + c.r).toInt() + pad, surfaceH)
        } else {
            list += Rect(0, top, mvEdge, surfaceH)
            list += Rect(jumpFromEdge, top, w, surfaceH)
        }
        return list
    }

    private fun start() {
        if (running) return
        running = true
        thread = Thread(::loop, "game").also { it.start() }
    }

    private fun stop() {
        running = false
        thread?.join(500)
        thread = null
    }

    fun onPauseApp() = synchronized(lock) {
        game.pause()
        touch.cancel()
        applyInput()
        keyLeft = false; keyRight = false; keyJump = false
    }

    fun back(): Boolean = synchronized(lock) { game.back() }

    private fun loop() {
        val step = 1f / 120f
        var last = System.nanoTime()
        var acc = 0f
        while (running) {
            val now = System.nanoTime()
            acc += min(0.1f, (now - last) / 1e9f)
            last = now
            var haptic = false
            synchronized(lock) {
                if (config() != cfg) relayout()
                while (acc >= step) {
                    game.update(step)
                    acc -= step
                }
                if (game.hapticPulse) { haptic = true; game.hapticPulse = false }
            }
            if (haptic && game.haptics) post { performHapticFeedback(HapticFeedbackConstants.LONG_PRESS) }
            val canvas = try { holder.lockHardwareCanvas() } catch (_: Exception) { null } ?: continue
            try {
                synchronized(lock) { renderer.draw(canvas, game, layout) }
            } finally {
                holder.unlockCanvasAndPost(canvas)
            }
        }
    }

    // ---------- touch ----------

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(e: MotionEvent): Boolean {
        synchronized(lock) {
            val i = e.actionIndex
            val id = e.getPointerId(i)
            when (e.actionMasked) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                    val lx = layout.lx(e.getX(i))
                    val ly = layout.ly(e.getY(i))
                    if (game.screen != Screen.PLAY) {
                        game.tap(lx - layout.stageX(game.screen), ly - layout.stageY(game.screen))
                    } else if ((lx to ly) in layout.pause) {
                        game.tap(Ui.hudPause.x + 1f, Ui.hudPause.y + 1f)
                    } else {
                        touch.down(id, e.getX(i), e.getY(i))
                    }
                }
                MotionEvent.ACTION_MOVE -> for (p in 0 until e.pointerCount) touch.move(e.getPointerId(p), e.getX(p))
                MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> touch.up(id)
                MotionEvent.ACTION_CANCEL -> touch.cancel()
            }
            applyInput()
        }
        return true
    }

    private fun applyInput() {
        if (touch.jumpPressed) {
            touch.jumpPressed = false
            game.input.jumpPressed = true
            if (game.haptics) performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        }
        val c = controls
        c.left = touch.left; c.right = touch.right; c.jump = touch.jump
        c.stickActive = touch.stickActive; c.stickX = touch.stickX; c.stickY = touch.stickY; c.knobX = touch.knobX
        game.input.left = touch.left || keyLeft
        game.input.right = touch.right || keyRight
        game.input.jump = touch.jump || keyJump
    }

    // ---------- keyboard / gamepad ----------

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        val handled = key(keyCode, true, event.repeatCount == 0)
        return handled || super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        val handled = key(keyCode, false, false)
        return handled || super.onKeyUp(keyCode, event)
    }

    private fun key(code: Int, down: Boolean, first: Boolean): Boolean = synchronized(lock) {
        when (code) {
            KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_A -> keyLeft = down
            KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.KEYCODE_D -> keyRight = down
            KeyEvent.KEYCODE_SPACE, KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_W, KeyEvent.KEYCODE_BUTTON_A -> {
                keyJump = down
                if (down && first) {
                    if (game.screen == Screen.PLAY) game.input.jumpPressed = true
                }
            }
            KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_DPAD_CENTER -> if (down && first && game.screen != Screen.PLAY) {
                game.tap(Ui.titlePlay.x + 1f, Ui.titlePlay.y + 1f)
            }
            else -> return false
        }
        applyInput()
        true
    }

    private companion object {
        val SIZES = floatArrayOf(0.8f, 1f, 1.25f)
    }
}
