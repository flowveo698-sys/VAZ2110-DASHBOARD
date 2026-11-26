package kz.vaz2110.dashboard

import android.app.Activity
import android.content.Intent
import android.graphics.*
import android.media.AudioManager
import android.os.Bundle
import android.provider.Settings
import android.view.*
import android.widget.*
import java.text.SimpleDateFormat
import java.util.*

class DashboardActivity : Activity() {
    private lateinit var clock: TextView
    private lateinit var date: TextView
    private lateinit var audio: AudioManager
    private val timer = Timer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        audio = getSystemService(AUDIO_SERVICE) as AudioManager
        window.decorView.systemUiVisibility =
            (View.SYSTEM_UI_FLAG_FULLSCREEN
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)

        val root = FrameLayout(this)
        root.setBackgroundColor(Color.rgb(2, 7, 15))
        root.addView(AnimatedGifView(this), FrameLayout.LayoutParams(-1, -1))

        val shade = View(this).apply {
            setBackgroundColor(Color.argb(62, 0, 5, 15))
        }
        root.addView(shade, FrameLayout.LayoutParams(-1, -1))

        val column = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(14), dp(22), dp(14))
        }
        root.addView(column, FrameLayout.LayoutParams(-1, -1))

        // Top bar: live clock/date and connection indicators.
        val top = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val left = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        clock = TextView(this).apply {
            text = "--:--"
            textSize =  forty()
            typeface = Typeface.create("sans-serif", Typeface.BOLD)
            setTextColor(Color.WHITE)
            letterSpacing = 0.04f
        }
        date = TextView(this).apply {
            textSize = 13f
            setTextColor(Color.rgb(176, 215, 238))
        }
        left.addView(clock)
        left.addView(date)
        top.addView(left, LinearLayout.LayoutParams(0, -2, 1f))
        val rightInfo = TextView(this).apply {
            text = "04 REGION   •   NIGHT DRIVE"
            textSize = 13f
            letterSpacing = 0.12f
            setTextColor(Color.rgb(104, 218, 255))
            gravity = Gravity.RIGHT or Gravity.CENTER_VERTICAL
        }
        top.addView(rightInfo)
        column.addView(top)

        // Flexible center keeps the car visible.
        val spacer = Space(this)
        column.addView(spacer, LinearLayout.LayoutParams(1, 0, 1f))

        val bottom = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.BOTTOM or Gravity.CENTER_VERTICAL
        }

        val identity = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val model = TextView(this).apply {
            text = "VAZ 2110"
            textSize = 25f
            typeface = Typeface.create("sans-serif", Typeface.BOLD)
            letterSpacing = 0.12f
            setTextColor(Color.WHITE)
        }
        val plate = TextView(this).apply {
            text = "KZ 244 AEK 04"
            textSize = 14f
            letterSpacing = 0.18f
            setTextColor(Color.rgb(98, 220, 255))
        }
        identity.addView(model)
        identity.addView(plate)
        bottom.addView(identity, LinearLayout.LayoutParams(0, -2, 1f))

        val buttons = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        buttons.addView(actionButton("♫", "MUSIC") { openMusic() })
        buttons.addView(actionButton("▶", "PLAY") { mediaKey(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) })
        buttons.addView(actionButton("◉", "MAPS") { openMaps() })
        buttons.addView(actionButton("ᛒ", "BT") { openBluetooth() })
        buttons.addView(actionButton("▦", "APPS") { openApps() })
        buttons.addView(actionButton("⚙", "SET") { openSettings() })
        bottom.addView(buttons)
        column.addView(bottom)
        root.addView(column)
        setContentView(root)

        updateClock()
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() { runOnUiThread { updateClock() } }
        }, 1000L, 1000L)
    }

    private fun forty() =  fortySp()
    private fun fortySp() = 42f

    private fun updateClock() {
        val now = Date()
        clock.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now)
        date.text = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).format(now)
            .uppercase(Locale.getDefault())
    }

    private fun actionButton(icon: String, label: String, click: () -> Unit): View {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(11), dp(8), dp(11), dp(7))
            background = android.graphics.drawable.GradientDrawable().apply {
                setColor(Color.argb(145, 3, 19, 36))
                setStroke(dp(1), Color.argb(180, 68, 183, 255))
                cornerRadius = dp(13).toFloat()
            }
            isClickable = true
            isFocusable = true
            setOnClickListener { click() }
        }
        val glyph = TextView(this).apply {
            text = icon
            textSize = 23f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(127, 220, 255))
        }
        val caption = TextView(this).apply {
            text = label
            textSize = 9f
            letterSpacing = 0.08f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
        }
        box.addView(glyph)
        box.addView(caption)
        val lp = LinearLayout.LayoutParams(dp(64), dp(65))
        lp.setMargins(dp(4), 0, dp(4), 0)
        box.layoutParams = lp
        return box
    }

    private fun mediaKey(code: Int) {
        val down = KeyEvent(KeyEvent.ACTION_DOWN, code)
        val up = KeyEvent(KeyEvent.ACTION_UP, code)
        audio.dispatchMediaKeyEvent(down)
        audio.dispatchMediaKeyEvent(up)
    }

    private fun openMusic() {
        val intents = listOf(
            Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_MUSIC),
            Intent(Intent.ACTION_VIEW).setType("audio/*")
        )
        launchFirst(intents)
    }

    private fun openMaps() {
        launchFirst(listOf(
            Intent(Intent.ACTION_VIEW, android.net.Uri.parse("google.navigation:q=")),
            Intent(Intent.ACTION_VIEW, android.net.Uri.parse("geo:0,0?q="))
        ))
    }

    private fun openBluetooth() {
        launchFirst(listOf(
            Intent(Settings.ACTION_BLUETOOTH_SETTINGS),
            Intent(Settings.ACTION_SETTINGS)
        ))
    }

    private fun openSettings() {
        launchFirst(listOf(Intent(Settings.ACTION_SETTINGS)))
    }

    private fun openApps() {
        // Open the Android app list / launcher if the device exposes one.
        launchFirst(listOf(
            Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_BROWSER),
            Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        ))
    }

    private fun launchFirst(intents: List<Intent>) {
        for (intent in intents) {
            try {
                startActivity(intent)
                return
            } catch (_: Exception) {}
        }
        Toast.makeText(this, "Бұл функция магнитолада қолжетімсіз", Toast.LENGTH_SHORT).show()
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

    override fun onDestroy() {
        timer.cancel()
        super.onDestroy()
    }

    private class AnimatedGifView(context: android.content.Context) : View(context) {
        private val movie: Movie
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        private var started = 0L
        private val handler = android.os.Handler(android.os.Looper.getMainLooper())
        private val frame = object : Runnable {
            override fun run() {
                invalidate()
                handler.postDelayed(this, 40L)
            }
        }

        init {
            val stream = context.resources.openRawResource(R.raw.night_drive)
            movie = Movie.decodeStream(stream)
            stream.close()
            started = android.os.SystemClock.uptimeMillis()
        }

        override fun onAttachedToWindow() {
            super.onAttachedToWindow()
            handler.post(frame)
        }

        override fun onDetachedFromWindow() {
            handler.removeCallbacks(frame)
            super.onDetachedFromWindow()
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val mw = movie.width().toFloat().coerceAtLeast(1f)
            val mh = movie.height().toFloat().coerceAtLeast(1f)
            val scale = maxOf(width / mw, height / mh)
            val dx = (width - mw * scale) / 2f
            val dy = (height - mh * scale) / 2f
            val duration = movie.duration().takeIf { it > 0 } ?: 1000
            val time = ((android.os.SystemClock.uptimeMillis() - started) % duration).toInt()
            movie.setTime(time)
            canvas.save()
            canvas.translate(dx, dy)
            canvas.scale(scale, scale)
            movie.draw(canvas, 0f, 0f, paint)
            canvas.restore()
        }
    }
}
