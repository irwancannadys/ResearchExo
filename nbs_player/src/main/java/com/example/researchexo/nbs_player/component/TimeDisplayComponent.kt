package com.example.researchexo.nbs_player.component

import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.media3.common.Player
import com.example.researchexo.nbs_player.R
import com.example.researchexo.nbs_player.base.PlayerControl

class TimeDisplayComponent @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr), PlayerControl {

    private var player: Player? = null
    private val updateHandler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() {
            updateTime()
            updateHandler.postDelayed(this, 1000)
        }
    }

    private var isDuration = false

    init {
        context.obtainStyledAttributes(attrs, R.styleable.TimeDisplay).apply {
            isDuration = getBoolean(R.styleable.TimeDisplay_showDuration, false)
            recycle()
        }
        setTextColor(Color.WHITE)
    }

    override fun attachPlayer(player: Player) {
        this.player = player
        startUpdate()
    }

    override fun detachPlayer() {
        stopUpdate()
        player = null
    }

    override fun updateState() {
        updateTime()
    }

    private fun updateTime() {
        val durationOrigin = player?.duration ?: 0L
        val duration = if (durationOrigin < 0) 0L else durationOrigin
        val currentPosition = player?.currentPosition ?: 0L
        text = formatTime(if (isDuration) duration else currentPosition)
    }

    private fun startUpdate() {
        stopUpdate()
        updateHandler.post(updateRunnable)
    }

    private fun stopUpdate() {
        updateHandler.removeCallbacks(updateRunnable)
    }

    private fun formatTime(timeMs: Long): String {
        val totalSeconds = timeMs / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
}