package com.example.researchexo.nbs_player.component

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageButton
import androidx.media3.common.Player
import com.example.researchexo.nbs_player.R
import com.example.researchexo.nbs_player.base.PlayerControl

class PlayPauseButtonComponent @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageButton(context, attrs, defStyleAttr), PlayerControl {

    private var player: Player? = null

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            updateState()
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            updateState()
        }
    }

    init {
        setOnClickListener {
            player?.let {
                if (it.isPlaying) {
                    val currentPosition = it.currentPosition
                    it.pause()
                    it.seekTo(currentPosition)
                } else {
                    val currentPosition = it.currentPosition
                    it.play()
                    it.seekTo(currentPosition)
                }
                updateState()
            }
        }
    }

    override fun attachPlayer(player: Player) {
        this.player = player
        player.addListener(playerListener)
        updateState()
    }

    override fun detachPlayer() {
        player?.removeListener(playerListener)
        player = null
    }

    override fun updateState() {
        post {
            setImageResource(
                if (player?.isPlaying == true) R.drawable.ic_pause
                else R.drawable.ic_play
            )
        }
    }
}