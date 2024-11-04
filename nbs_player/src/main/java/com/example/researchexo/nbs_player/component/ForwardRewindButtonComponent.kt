package com.example.researchexo.nbs_player.component

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageButton
import androidx.media3.common.Player
import com.example.researchexo.nbs_player.R
import com.example.researchexo.nbs_player.base.PlayerControl

@SuppressLint("CustomViewStyleable")
class ForwardRewindButtonComponent @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageButton(context, attrs, defStyleAttr), PlayerControl {

    private var player: Player? = null
    private var isForward: Boolean = true

    companion object {
        private const val SEEK_TIME_MS = 10_000
    }

    init {
        context.obtainStyledAttributes(attrs, R.styleable.ForwardRewindButton).apply {
            try {
                isForward = getBoolean(R.styleable.ForwardRewindButton_isForward, true)
            } finally {
                recycle()
            }
        }

        setImageResource(if (isForward) R.drawable.ic_forwad else R.drawable.ic_rewind)

        setOnClickListener {
            player?.let {
                val wasPlaying = it.isPlaying
                val currentPosition = it.currentPosition

                if (isForward) {
                    val seekPosition = minOf(currentPosition + SEEK_TIME_MS, it.duration)
                    it.seekTo(seekPosition)
                } else {
                    val seekPosition = maxOf(0, currentPosition - SEEK_TIME_MS)
                    it.seekTo(seekPosition)
                }

                if (wasPlaying && !it.isPlaying) {
                    it.play()
                }
            }
        }
    }

    override fun attachPlayer(player: Player) {
        this.player = player
    }

    override fun detachPlayer() {
        player = null
    }

    override fun updateState() {
    }
}