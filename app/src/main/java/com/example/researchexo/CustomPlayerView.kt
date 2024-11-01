package com.example.researchexo

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi

@UnstableApi
class CustomPlayerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var player: Player? = null
    private lateinit var playPauseButton: ImageButton
    private lateinit var rewindButton: ImageButton
    private lateinit var forwardButton: ImageButton
    private lateinit var progressBar: SeekBar
    private lateinit var durationView: TextView
    private lateinit var currentTimeView: TextView
    private lateinit var controllerContainer: View
    private lateinit var surfaceView: SurfaceView
    private var controllerTimeout = 3000L
    private var isControllerVisible = true
    private val hideHandler = Handler(Looper.getMainLooper())
    private val updateProgressHandler = Handler(Looper.getMainLooper())

    init {
        LayoutInflater.from(context).inflate(R.layout.custom_player_view, this, true)
        initializeViews()
        setupSurface()
        setupListeners()
    }

    private fun initializeViews() {
        surfaceView = findViewById(R.id.surfaceView)
        playPauseButton = findViewById(R.id.playPauseButton)
        rewindButton = findViewById(R.id.rewindButton)
        forwardButton = findViewById(R.id.forwardButton)
        progressBar = findViewById(R.id.progressBar)
        durationView = findViewById(R.id.durationView)
        currentTimeView = findViewById(R.id.currentTimeView)
        controllerContainer = findViewById(R.id.controllerContainer)
    }

    private fun setupSurface() {
        surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                player?.setVideoSurface(holder.surface)
            }

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                player?.setVideoSurface(null)
            }
        })
    }

    private fun setupListeners() {
        playPauseButton.setOnClickListener {
            player?.let {
                if (it.isPlaying) {
                    it.pause()
                } else {
                    it.play()
                }
                updatePlayPauseButton()
            }
        }

        rewindButton.setOnClickListener {
            player?.seekBack()
        }

        forwardButton.setOnClickListener {
            player?.seekForward()
        }

        progressBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    player?.seekTo(progress.toLong())
                }
                updateCurrentTime()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                removeControllerHideCallbacks()
                stopProgressUpdate()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                scheduleControllerHide()
                startProgressUpdate()
            }
        })

        surfaceView.setOnClickListener {
            toggleControllerVisibility()
        }
    }

    fun setPlayer(newPlayer: Player) {
        player?.let { oldPlayer ->
            oldPlayer.removeListener(playerListener)
            if (surfaceView.holder.surface != null) {
                oldPlayer.setVideoSurface(null)
            }
        }

        player = newPlayer.also { player ->
            if (surfaceView.holder.surface != null) {
                player.setVideoSurface(surfaceView.holder.surface)
            }
            player.addListener(playerListener)
            updateAll()
            startProgressUpdate()
        }
    }

    private val playerListener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            updateAll()
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            updatePlayPauseButton()
            if (isPlaying) {
                scheduleControllerHide()
                startProgressUpdate()
            } else {
                removeControllerHideCallbacks()
                stopProgressUpdate()
            }
        }
    }

    private fun updateAll() {
        updatePlayPauseButton()
        updateProgress()
        updateDuration()
    }

    private fun updatePlayPauseButton() {
        val isPlaying = player?.isPlaying ?: false
        playPauseButton.setImageResource(
            if (isPlaying) R.drawable.ic_pause
            else R.drawable.ic_play
        )
    }

    private fun updateProgress() {
        player?.let {
            val duration = it.duration
            val position = it.currentPosition

            if (duration > 0) {
                progressBar.max = duration.toInt()
                progressBar.progress = position.toInt()
                updateCurrentTime()
            }
        }
    }

    private fun updateCurrentTime() {
        player?.let {
            currentTimeView.text = formatTime(it.currentPosition)
        }
    }

    private fun updateDuration() {
        player?.let {
            val duration = it.duration
            if (duration > 0) {
                durationView.text = formatTime(duration)
            }
        }
    }

    private val progressUpdateRunnable = object : Runnable {
        override fun run() {
            updateProgress()
            updateProgressHandler.postDelayed(this, 1000)
        }
    }

    private fun startProgressUpdate() {
        updateProgressHandler.post(progressUpdateRunnable)
    }

    private fun stopProgressUpdate() {
        updateProgressHandler.removeCallbacks(progressUpdateRunnable)
    }

    private fun formatTime(timeMs: Long): String {
        val totalSeconds = timeMs / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    private fun toggleControllerVisibility() {
        if (isControllerVisible) {
            hideController()
        } else {
            showController()
        }
    }

    private fun showController() {
        controllerContainer.visibility = View.VISIBLE
        isControllerVisible = true
        scheduleControllerHide()
    }

    private fun hideController() {
        controllerContainer.visibility = View.GONE
        isControllerVisible = false
    }

    private fun scheduleControllerHide() {
        removeControllerHideCallbacks()
        if (player?.isPlaying == true) {
            hideHandler.postDelayed({ hideController() }, controllerTimeout)
        }
    }

    private fun removeControllerHideCallbacks() {
        hideHandler.removeCallbacksAndMessages(null)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        removeControllerHideCallbacks()
        stopProgressUpdate()
        player?.let {
            it.removeListener(playerListener)
            it.setVideoSurface(null)
        }
        player = null
    }
}