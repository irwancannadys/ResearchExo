package com.example.researchexo.component

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageButton
import androidx.media3.common.Player
import com.example.researchexo.R
import com.example.researchexo.base.PlayerControl

class ForwardRewindButtonComponent @JvmOverloads constructor(
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