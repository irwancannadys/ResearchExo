package com.example.researchexo.component

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageButton
import androidx.media3.common.Player
import com.example.researchexo.R
import com.example.researchexo.base.PlayerControl


class PlayPauseButtonComponent @JvmOverloads constructor(
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
