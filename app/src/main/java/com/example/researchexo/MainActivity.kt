package com.example.researchexo

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.example.researchexo.databinding.ActivityMainBinding

@UnstableApi
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var player: ExoPlayer? = null
    private var playWhenReady = true
    private var mediaItemIndex = 0
    private var playbackPosition = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onStart() {
        super.onStart()
        initializePlayer()
    }

    override fun onResume() {
        super.onResume()
        if (player == null) {
            initializePlayer()
        }
    }

    override fun onPause() {
        super.onPause()
        savePlaybackState()
    }

    override fun onStop() {
        super.onStop()
        releasePlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
    }

    private fun initializePlayer() {
        try {
            player = ExoPlayer.Builder(this)
                .setLoadControl(
                    DefaultLoadControl.Builder()
                        .setBufferDurationsMs(
                            DefaultLoadControl.DEFAULT_MIN_BUFFER_MS * 2,
                            DefaultLoadControl.DEFAULT_MAX_BUFFER_MS * 2,
                            DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_MS * 2,
                            DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS * 2
                        )
                        .setPrioritizeTimeOverSizeThresholds(true)
                        .build()
                )
                .build()
                .also { exoPlayer ->
                    binding.customPlayerView.setPlayer(exoPlayer)
                    setupPlayerListeners(exoPlayer)

                    binding.customPlayerView.post {
                        prepareMedia(exoPlayer)
                    }
                    exoPlayer.seekTo(mediaItemIndex, playbackPosition)
                    exoPlayer.playWhenReady = playWhenReady
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing player", e)
            Toast.makeText(this, "Error initializing player", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupPlayerListeners(exoPlayer: ExoPlayer) {
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_IDLE -> {
                        binding.loadingIndicator.visibility = View.GONE
                        Log.d(TAG, "Player State: IDLE")
                    }
                    Player.STATE_BUFFERING -> {
                        binding.loadingIndicator.visibility = View.VISIBLE
                        Log.d(TAG, "Player State: BUFFERING")
                    }
                    Player.STATE_READY -> {
                        binding.loadingIndicator.visibility = View.GONE
                        Log.d(TAG, "Player State: READY")
                    }
                    Player.STATE_ENDED -> {
                        binding.loadingIndicator.visibility = View.GONE
                        Log.d(TAG, "Player State: ENDED")
                    }
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                handlePlayerError(error)
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) {
                    Log.d(TAG, "Player is playing")
                } else {
                    Log.d(TAG, "Player is paused")
                }
            }
        })

        exoPlayer.addAnalyticsListener(object : AnalyticsListener {
            override fun onPlayerError(eventTime: AnalyticsListener.EventTime, error: PlaybackException) {
                Log.e(TAG, "Analytics Error: ${error.message}")
                error.printStackTrace()
            }

            override fun onVideoSizeChanged(eventTime: AnalyticsListener.EventTime, videoSize: VideoSize) {
                Log.d(TAG, "Video size changed: ${videoSize.width}x${videoSize.height}")
            }

            override fun onDroppedVideoFrames(eventTime: AnalyticsListener.EventTime, droppedFrames: Int, elapsedMs: Long) {
                Log.w(TAG, "Dropped frames: $droppedFrames in $elapsedMs ms")
            }
        })
    }

    private fun prepareMedia(exoPlayer: ExoPlayer) {
        try {
            val mediaItem = MediaItem.Builder()
                .setUri(DEMO_VIDEO_URL)
                .setMimeType(MimeTypes.APPLICATION_MP4)
                .build()

            val cacheDataSourceFactory = try {
                CacheDataSource.Factory()
                    .setCache(ExoPlayerApp.cache)
                    .setUpstreamDataSourceFactory(
                        DefaultHttpDataSource.Factory()
                            .setAllowCrossProtocolRedirects(true)
                            .setConnectTimeoutMs(15000)
                            .setReadTimeoutMs(15000)
                            .setUserAgent("ResearchExo/1.0 (Android ${android.os.Build.VERSION.RELEASE})")
                    )
                    .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
            } catch (e: Exception) {
                Log.e(TAG, "Error creating cache data source", e)
                DefaultHttpDataSource.Factory()
                    .setAllowCrossProtocolRedirects(true)
                    .setConnectTimeoutMs(15000)
                    .setReadTimeoutMs(15000)
                    .setUserAgent("ResearchExo/1.0 (Android ${android.os.Build.VERSION.RELEASE})")
            }

            val mediaSource = ProgressiveMediaSource.Factory(cacheDataSourceFactory)
                .setContinueLoadingCheckIntervalBytes(1024 * 1024)
                .createMediaSource(mediaItem)

            exoPlayer.apply {
                setMediaSource(mediaSource)
                videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT
                repeatMode = Player.REPEAT_MODE_OFF
                volume = 1f
                playWhenReady = true
                prepare()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error preparing media", e)
            Toast.makeText(this, "Error preparing media", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handlePlayerError(error: PlaybackException) {
        val errorMessage = when (error.errorCode) {
            PlaybackException.ERROR_CODE_BEHIND_LIVE_WINDOW -> {
                player?.seekToDefaultPosition()
                player?.prepare()
                "Playing from live window"
            }
            PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED -> {
                retryConnection()
                "Network connection failed"
            }
            PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS,
            PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND -> {
                Log.e(TAG, "Source error: ${error.message}")
                "Video source error"
            }
            PlaybackException.ERROR_CODE_DECODER_INIT_FAILED -> {
                Log.e(TAG, "Decoder error: ${error.message}")
                "Decoder initialization failed"
            }
            PlaybackException.ERROR_CODE_IO_UNSPECIFIED -> {
                retryConnection()
                "IO error: ${error.message}"
            }
            else -> {
                Log.e(TAG, "Playback error: ${error.message}")
                error.printStackTrace()
                "Playback error: ${error.message}"
            }
        }

        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
    }

    private fun retryConnection() {
        Handler(Looper.getMainLooper()).postDelayed({
            try {
                player?.prepare()
            } catch (e: Exception) {
                Log.e(TAG, "Error retrying connection", e)
            }
        }, 5000)
    }

    private fun savePlaybackState() {
        player?.let { exoPlayer ->
            try {
                playbackPosition = exoPlayer.currentPosition
                mediaItemIndex = exoPlayer.currentMediaItemIndex
                playWhenReady = exoPlayer.playWhenReady
            } catch (e: Exception) {
                Log.e(TAG, "cek Error playback state gaaannnnnnnn", e)
            }
        }
    }

    private fun releasePlayer() {
        try {
            player?.let { exoPlayer ->
                savePlaybackState()
                exoPlayer.release()
                player = null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error mulai video", e)
        }
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val DEMO_VIDEO_URL = "https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4"
        private const val BACKUP_VIDEO_URL = "https://media.geeksforgeeks.org/wp-content/uploads/20201217163353/Screenrecorder-2020-12-17-16-32-03-350.mp4"
    }
}