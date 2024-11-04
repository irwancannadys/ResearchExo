package com.example.researchexo.nbs_player

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import com.example.researchexo.nbs_player.base.PlayerControl

@UnstableApi
class CustomPlayerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val surfaceView = SurfaceView(context)
    private var player: Player? = null
    private var controllerContainer: View? = null
    private var controllerTimeout = 3000L
    private var isControllerVisible = true
    private val hideHandler = Handler(Looper.getMainLooper())
    private val controls = mutableListOf<PlayerControl>()

    init {
        addView(surfaceView, LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT
        ))
        setupSurface()

        surfaceView.setOnClickListener {
            toggleControllerVisibility()
        }
    }

    private fun setupSurface() {
        surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                player?.setVideoSurface(holder.surface)
            }

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                player?.setVideoSurface(null)
            }
        })
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        if (childCount > 1) {
            controllerContainer = getChildAt(1)
        }
        findAndRegisterControls(this)
    }

    private fun findAndRegisterControls(view: View) {
        if (view is PlayerControl) {
            controls.add(view)
            player?.let { view.attachPlayer(it) }
        }
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                findAndRegisterControls(view.getChildAt(i))
            }
        }
    }

    fun setPlayer(newPlayer: Player) {
        // Detach current player
        player?.let { oldPlayer ->
            controls.forEach { it.detachPlayer() }
            if (surfaceView.holder.surface != null) {
                oldPlayer.setVideoSurface(null)
            }
        }

        // Attach new player
        player = newPlayer.also { player ->
            if (surfaceView.holder.surface != null) {
                player.setVideoSurface(surfaceView.holder.surface)
            }
            controls.forEach { it.attachPlayer(player) }
            updateControllerVisibility()
        }
    }

    fun setControllerTimeout(timeoutMs: Long) {
        controllerTimeout = timeoutMs
    }

    private fun toggleControllerVisibility() {
        if (isControllerVisible) {
            hideController()
        } else {
            showController()
        }
    }

    private fun showController() {
        controllerContainer?.visibility = View.VISIBLE
        isControllerVisible = true
        scheduleHideController()
    }

    private fun hideController() {
        controllerContainer?.visibility = View.GONE
        isControllerVisible = false
    }

    private fun scheduleHideController() {
        hideHandler.removeCallbacksAndMessages(null)
        if (player?.isPlaying == true) {
            hideHandler.postDelayed({ hideController() }, controllerTimeout)
        }
    }

    private fun updateControllerVisibility() {
        controllerContainer?.let {
            it.visibility = if (isControllerVisible) View.VISIBLE else View.GONE
        }
    }
//
//    override fun onDetachedFromWindow() {
//        super.onDetachedFromWindow()
//        hideHandler.removeCallbacksAndMessages(null)
//        controls.forEach { it.detachPlayer() }
//        player?.setVideoSurface(null)
//        player = null
//    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        hideHandler.removeCallbacksAndMessages(null)

        controls.forEach { control ->
            control.detachPlayer()
        }
        controls.clear()

        player?.setVideoSurface(null)
        player = null

        controllerContainer = null
    }
}