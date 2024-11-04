package com.example.researchexo.nbs_player.component

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.widget.SeekBar
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.appcompat.widget.AppCompatSeekBar
import androidx.media3.common.Player
import com.example.researchexo.nbs_player.R
import com.example.researchexo.nbs_player.base.PlayerControl

class PlayerSeekBarComponent @JvmOverloads constructor(
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
            updateHandler.postDelayed(this, 1000)
        }
    }

    init {
        context.obtainStyledAttributes(attrs, R.styleable.PlayerSeekBar, defStyleAttr, 0).apply {
            try {
                progressColor = getColor(R.styleable.PlayerSeekBar_seekBarProgressColor, Color.WHITE)
                backgroundColor = getColor(R.styleable.PlayerSeekBar_seekBarBackgroundColor, Color.parseColor("#4DFFFFFF"))
                thumbColor = getColor(R.styleable.PlayerSeekBar_seekBarThumbColor, Color.WHITE)
            } finally {
                recycle()
            }
        }

        val thumbOffset = resources.getDimensionPixelSize(R.dimen.seekbar_thumb_size) / 2
        setPadding(thumbOffset, thumbOffset, 0, thumbOffset)
        minimumHeight = resources.getDimensionPixelSize(R.dimen.seekbar_height)

        setupSeekBar()
    }

    private fun setupSeekBar() {
        splitTrack = false
        progressTintList = ColorStateList.valueOf(progressColor)
        progressBackgroundTintList = ColorStateList.valueOf(backgroundColor)
        thumbTintList = ColorStateList.valueOf(thumbColor)

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

    override fun attachPlayer(player: Player) {
        this.player = player
        player.duration.takeIf { it > 0 }?.let { duration ->
            max = duration.toInt()
        }
        updateProgress()
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

    private fun updateProgress() {
        player?.let {
            if (it.duration > 0 && max != it.duration.toInt()) {
                max = it.duration.toInt()
            }
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

    fun setColors(
        @ColorInt progressColor: Int = this.progressColor,
        @ColorInt backgroundColor: Int = this.backgroundColor,
        @ColorInt thumbColor: Int = this.thumbColor
    ) {
        this.progressColor = progressColor
        this.backgroundColor = backgroundColor
        this.thumbColor = thumbColor

        progressTintList = ColorStateList.valueOf(progressColor)
        progressBackgroundTintList = ColorStateList.valueOf(backgroundColor)
        thumbTintList = ColorStateList.valueOf(thumbColor)
    }

    fun setColorResources(
        @ColorRes progressColorRes: Int = R.color.white,
        @ColorRes backgroundColorRes: Int = R.color.white,
        @ColorRes thumbColorRes: Int = R.color.white
    ) {
        setColors(
            progressColor = context.getColor(progressColorRes),
            backgroundColor = context.getColor(backgroundColorRes),
            thumbColor = context.getColor(thumbColorRes)
        )
    }
}