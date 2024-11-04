package com.example.researchexo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.util.UnstableApi
import com.example.researchexo.databinding.ActivityMainBinding
import com.example.researchexo.nbs_player.base.NBSPlayerManager

@UnstableApi
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initializePlayer()
    }

    private fun initializePlayer() {
        val player = NBSPlayerManager.build(this) {
            setUrl(DEMO_VIDEO_URL)
        }
        binding.playerView.setPlayer(player)
    }

    override fun onDestroy() {
        super.onDestroy()
        NBSPlayerManager.release()
    }

    companion object {
        private const val DEMO_VIDEO_URL = "https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4"
    }
}