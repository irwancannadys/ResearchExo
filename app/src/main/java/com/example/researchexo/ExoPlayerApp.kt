package com.example.researchexo

import android.app.Application
import androidx.media3.common.util.UnstableApi
import com.example.researchexo.nbs_player.ResearchExoApp

@UnstableApi
class ExoPlayerApp : Application() {

    override fun onCreate() {
        super.onCreate()
        ResearchExoApp.initialized(this)
    }
}