package com.example.researchexo.base

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.widget.SeekBar
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatSeekBar
import androidx.appcompat.widget.AppCompatTextView
import androidx.media3.common.Player
import com.example.researchexo.R

class PlayPauseButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageButton(context, attrs, defStyleAttr), PlayerControl {

    private var player: Player? = null

    init {
        setOnClickListener {
            player?.let {
                if (it.isPlaying) it.pause() else it.play()
                updateState()
            }
        }
    }

    override fun attachPlayer(player: Player) {
        this.player = player
        updateState()
    }

    override fun detachPlayer() {
        player = null
    }

    override fun updateState() {
        setImageResource(
            if (player?.isPlaying == true) R.drawable.ic_pause
            else R.drawable.ic_play
        )
    }
}

// controls/ForwardRewindButton.kt
class ForwardRewindButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageButton(context, attrs, defStyleAttr), PlayerControl {

    private var player: Player? = null
    private var isForward: Boolean = true

    init {
        context.obtainStyledAttributes(attrs, R.styleable.ForwardRewindButton).apply {
            isForward = getBoolean(R.styleable.ForwardRewindButton_isForward, true)
            recycle()
        }

        setImageResource(if (isForward) R.drawable.ic_forwad else R.drawable.ic_rewind)

        setOnClickListener {
            if (isForward) player?.seekForward() else player?.seekBack()
        }
    }

    override fun attachPlayer(player: Player) {
        this.player = player
    }

    override fun detachPlayer() {
        player = null
    }

    override fun updateState() {}
}

class PlayerSeekBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatSeekBar(context, attrs, defStyleAttr), PlayerControl {

    private var player: Player? = null
    private val updateHandler = Handler(Looper.getMainLooper())
    private var isDragging = false

    private var progressColor: Int = Color.WHITE
    private var backgroundColor: Int = Color.parseColor("#4DFFFFFF")
    private var thumbColor: Int = Color.WHITE

    private val progressUpdateRunnable = object : Runnable {
        override fun run() {
            if (!isDragging) {
                updateProgress()
            }
            // Schedule the next update
            updateHandler.postDelayed(this, 1000)
        }
    }

    init {
        // Get custom attributes
        context.obtainStyledAttributes(attrs, R.styleable.PlayerSeekBar, defStyleAttr, 0).apply {
            try {
                progressColor = getColor(R.styleable.PlayerSeekBar_seekBarProgressColor, Color.WHITE)
                backgroundColor = getColor(R.styleable.PlayerSeekBar_seekBarBackgroundColor, Color.parseColor("#4DFFFFFF"))
                thumbColor = getColor(R.styleable.PlayerSeekBar_seekBarThumbColor, Color.WHITE)
            } finally {
                recycle()
            }
        }

        // Add padding for thumb
        val thumbOffset = resources.getDimensionPixelSize(R.dimen.seekbar_thumb_size) / 2
        setPadding(thumbOffset, thumbOffset, 0, thumbOffset)

        // Set minimum height
        minimumHeight = resources.getDimensionPixelSize(R.dimen.seekbar_height)

        setupSeekBar()
    }

    private fun setupSeekBar() {
        // Set split track to false to avoid track being split by thumb
        splitTrack = false

        // Set colors
        progressTintList = ColorStateList.valueOf(progressColor)
        progressBackgroundTintList = ColorStateList.valueOf(backgroundColor)
        thumbTintList = ColorStateList.valueOf(thumbColor)

        // Set SeekBar listener
        setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    player?.seekTo(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isDragging = true
                stopProgressUpdates()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                isDragging = false
                player?.let {
                    it.seekTo(progress.toLong())
                }
                startProgressUpdates()
            }
        })
    }

    /**
     * [player]
     *
     * */
    override fun attachPlayer(player: Player) {
        this.player = player
        // Set initial max if duration is available
        player.duration.takeIf { it > 0 }?.let { duration ->
            max = duration.toInt()
        }
        updateProgress()
        // Start progress updates
        startProgressUpdates()
    }

    override fun detachPlayer() {
        stopProgressUpdates()
        player = null
    }

    override fun updateState() {
        if (!isDragging) {
            updateProgress()
        }
    }

    /**
     * Update initial position
     * */
    private fun updateProgress() {
        player?.let {
            // Update duration (it might change for dynamic content)
            if (it.duration > 0 && max != it.duration.toInt()) {
                max = it.duration.toInt()
            }
            // Update progress if we're not currently dragging
            if (!isDragging && it.duration > 0) {
                progress = it.currentPosition.toInt()
            }
        }
    }

    private fun startProgressUpdates() {
        stopProgressUpdates()
        updateHandler.post(progressUpdateRunnable)
    }

    private fun stopProgressUpdates() {
        updateHandler.removeCallbacks(progressUpdateRunnable)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopProgressUpdates()
        player = null
    }

    /**
     * Set custom colors for the seekbar
     */
    fun setColors(
        progressColor: Int = this.progressColor,
        backgroundColor: Int = this.backgroundColor,
        thumbColor: Int = this.thumbColor
    ) {
        this.progressColor = progressColor
        this.backgroundColor = backgroundColor
        this.thumbColor = thumbColor

        progressTintList = ColorStateList.valueOf(progressColor)
        progressBackgroundTintList = ColorStateList.valueOf(backgroundColor)
        thumbTintList = ColorStateList.valueOf(thumbColor)
    }
}

// controls/TimeDisplay.kt
class TimeDisplay @JvmOverloads constructor(
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