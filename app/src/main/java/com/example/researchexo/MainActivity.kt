package com.example.researchexo

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.example.researchexo.databinding.ActivityMainBinding

@UnstableApi
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var player: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onStart() {
        super.onStart()
        initializePlayer()
    }

    override fun onStop() {
        super.onStop()
        releasePlayer()
    }

    private fun initializePlayer() {
        try {
            // Create ExoPlayer instance
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
                    // Set up player with custom view
                    binding.playerView.setPlayer(exoPlayer)

                    // Create and set up media source
                    val mediaItem = MediaItem.Builder()
                        .setUri(DEMO_VIDEO_URL)
                        .setMimeType(MimeTypes.APPLICATION_MP4)
                        .build()

                    val dataSourceFactory = DefaultHttpDataSource.Factory()
                        .setAllowCrossProtocolRedirects(true)
                        .setConnectTimeoutMs(15000)
                        .setReadTimeoutMs(15000)
                        .setUserAgent("ResearchExo/1.0 (Android ${android.os.Build.VERSION.RELEASE})")

                    val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(mediaItem)

                    // Prepare player
                    exoPlayer.apply {
                        setMediaSource(mediaSource)
                        videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT
                        repeatMode = Player.REPEAT_MODE_OFF
                        volume = 1f
                        playWhenReady = true
                        prepare()
                    }
                }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error initializing player", e)
            Toast.makeText(this, "Error initializing player", Toast.LENGTH_SHORT).show()
        }
    }

    private fun releasePlayer() {
        try {
            player?.release()
            player = null
        } catch (e: Exception) {
            Log.e("MainActivity", "Error releasing player", e)
        }
    }

    companion object {
        private const val DEMO_VIDEO_URL = "https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4"
    }
}