// ExoPlayerApp.kt
package com.example.researchexo

import android.app.Application
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import java.io.File

@UnstableApi
class ExoPlayerApp : Application() {
    companion object {
        private var _cache: SimpleCache? = null
        val cache: SimpleCache
            get() = _cache ?: throw IllegalStateException("Cache not initialized")
    }

    override fun onCreate() {
        super.onCreate()
        initializeCache()
    }

    private fun initializeCache() {
        if (_cache == null) {
            val cacheSize: Long = 90 * 1024 * 1024 // 90MB cache
            val cacheDir = File(applicationContext.cacheDir, "media")
            if (!cacheDir.exists()) {
                cacheDir.mkdirs()
            }
            _cache = SimpleCache(
                cacheDir,
                LeastRecentlyUsedCacheEvictor(cacheSize),
                StandaloneDatabaseProvider(applicationContext)
            )
        }
    }
}