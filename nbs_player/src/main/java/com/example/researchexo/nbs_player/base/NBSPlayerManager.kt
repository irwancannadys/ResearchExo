
package com.example.researchexo.nbs_player.base

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory

@OptIn(UnstableApi::class)
class NBSPlayerManager private constructor() {
    private var exoPlayer: ExoPlayer? = null

    class Builder(private val context: Context) {
        private var url: String? = null
        private var mimeType: String = MimeTypes.APPLICATION_MP4
        private var autoPlay: Boolean = true
        private var userAgent: String = "NBSPlayer/1.0"
        private var connectTimeoutMs: Int = 15000
        private var readTimeoutMs: Int = 15000
        private var volume: Float = 1f

        // Audio attribute bisa langsung menggunakan DEFAULT_AUDIO_ATTRIBUTES

        fun setUrl(url: String) = apply { this.url = url }
        fun setMimeType(mimeType: String) = apply { this.mimeType = mimeType }
        fun setAutoPlay(autoPlay: Boolean) = apply { this.autoPlay = autoPlay }
        fun setUserAgent(userAgent: String) = apply { this.userAgent = userAgent }
        fun setTimeout(connectMs: Int, readMs: Int) = apply {
            this.connectTimeoutMs = connectMs
            this.readTimeoutMs = readMs
        }
        fun setVolume(volume: Float) = apply {
            this.volume = volume.coerceIn(0f, 1f)
        }

        fun build(): ExoPlayer {
            instance.releasePlayer()

            val dataSourceFactory = DefaultHttpDataSource.Factory()
                .setAllowCrossProtocolRedirects(true)
                .setConnectTimeoutMs(connectTimeoutMs)
                .setReadTimeoutMs(readTimeoutMs)
                .setUserAgent(userAgent)

            val loadControl = DefaultLoadControl.Builder()
                .setBufferDurationsMs(
                    DefaultLoadControl.DEFAULT_MIN_BUFFER_MS * 2,
                    DefaultLoadControl.DEFAULT_MAX_BUFFER_MS * 2,
                    DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_MS * 2,
                    DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS * 2
                )
                .setPrioritizeTimeOverSizeThresholds(true)
                .build()

            val mediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory)

            return ExoPlayer.Builder(context)
                .setLoadControl(loadControl)
                .setMediaSourceFactory(mediaSourceFactory)
                .build()
                .also { player ->
                    player.setAudioAttributes(DEFAULT_AUDIO_ATTRIBUTES, true)
                    url?.let {
                        val mediaItem = MediaItem.Builder()
                            .setUri(it)
                            .setMimeType(mimeType)
                            .build()
                        player.setMediaItem(mediaItem)
                    }

                    player.apply {
                        videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT
                        repeatMode = Player.REPEAT_MODE_OFF
                        setVolume(volume)
                        playWhenReady = autoPlay
                        if (url != null) prepare()
                    }

                    instance.exoPlayer = player
                }
        }
    }

    fun releasePlayer() {
        exoPlayer?.release()
        exoPlayer = null
    }

    companion object {
        private val instance = NBSPlayerManager()

        private val DEFAULT_AUDIO_ATTRIBUTES = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()

        fun build(context: Context, block: Builder.() -> Unit): ExoPlayer {
            return Builder(context).apply(block).build()
        }

        fun release() {
            instance.releasePlayer()
        }
    }
}