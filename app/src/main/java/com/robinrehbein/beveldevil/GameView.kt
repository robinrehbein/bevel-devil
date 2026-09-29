package com.robinrehbein.beveldevil

import android.annotation.SuppressLint
import android.content.Context
import android.view.HapticFeedbackConstants
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.robinrehbein.beveldevil.game.Game
import com.robinrehbein.beveldevil.game.Screen
import com.robinrehbein.beveldevil.game.Ui
import com.robinrehbein.beveldevil.render.ControlLayout
import com.robinrehbein.beveldevil.render.Renderer
import kotlin.math.min

/** Hosts the game loop on its own thread: fixed 120 Hz simulation, render every vsync-ish frame. */
@SuppressLint("ViewConstructor")
class GameView(context: Context, private val game: Game) : SurfaceView(context), SurfaceHolder.Callback {
    private val renderer = Renderer(context)
    private val controls = ControlLayout()
    private val lock = Any()
    @Volatile private var running = false
    private var thread: Thread? = null

    private enum class Zone { LEFT, RIGHT, JUMP, NONE }
    private val pointers = HashMap<Int, Zone>()
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
        synchronized(lock) {
            renderer.layout(width, height)
            val dp = resources.displayMetrics.density
            val r = min(34f * dp, height * 0.11f)
            val margin = 18f * dp
            controls.r = r
            controls.y = height - margin - r
            controls.leftX = margin + r
            controls.rightX = margin + 3.3f * r
            controls.jumpX = width - margin - r
        }
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
        pointers.clear()
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
                while (acc >= step) {
                    game.update(step)
                    acc -= step
                }
                if (game.hapticPulse) { haptic = true; game.hapticPulse = false }
            }
            if (haptic) post { performHapticFeedback(HapticFeedbackConstants.LONG_PRESS) }
            val canvas = try { holder.lockHardwareCanvas() } catch (_: Exception) { null } ?: continue
            try {
                synchronized(lock) { renderer.draw(canvas, game, controls) }
            } finally {
                holder.unlockCanvasAndPost(canvas)
            }
        }
    }

    // ---------- touch ----------

    private fun toLogical(x: Float, y: Float) = ((x - renderer.originX) / renderer.scale) to ((y - renderer.originY) / renderer.scale)

    private fun zoneAt(x: Float): Zone = when {
        x >= width / 2f -> Zone.JUMP
        x < (controls.leftX + controls.rightX) / 2f -> Zone.LEFT
        else -> Zone.RIGHT
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(e: MotionEvent): Boolean {
        synchronized(lock) {
            val i = e.actionIndex
            val id = e.getPointerId(i)
            when (e.actionMasked) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                    val (lx, ly) = toLogical(e.getX(i), e.getY(i))
                    if (game.screen != Screen.PLAY || (lx to ly) in Ui.hudPause) {
                        game.tap(lx, ly)
                    } else {
                        val z = zoneAt(e.getX(i))
                        pointers[id] = z
                        if (z == Zone.JUMP) game.input.jumpPressed = true
                    }
                }
                MotionEvent.ACTION_MOVE -> for (p in 0 until e.pointerCount) {
                    val pid = e.getPointerId(p)
                    val z = pointers[pid] ?: continue
                    if (z != Zone.JUMP) {
                        val nz = zoneAt(e.getX(p))
                        pointers[pid] = if (nz == Zone.JUMP) z else nz
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> pointers.remove(id)
                MotionEvent.ACTION_CANCEL -> pointers.clear()
            }
            applyInput()
        }
        return true
    }

    private fun applyInput() {
        val zones = pointers.values
        val left = Zone.LEFT in zones
        val right = Zone.RIGHT in zones
        val jump = Zone.JUMP in zones
        controls.left = left; controls.right = right; controls.jump = jump
        game.input.left = left || keyLeft
        game.input.right = right || keyRight
        game.input.jump = jump || keyJump
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
}
