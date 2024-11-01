package com.example.researchexo

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import java.io.File

@UnstableApi
object ResearchExoApp {
    private var _cache: SimpleCache? = null
    val cache: SimpleCache
        get() = _cache ?: throw IllegalStateException("Cache not initialized")

    fun initialized(context: Context) {
        if (_cache == null) {
            val cacheSize: Long = 90 * 1024 * 1024 // 90MB cache
            val cacheDir = File(context.cacheDir, "media")
            if (!cacheDir.exists()) {
                cacheDir.mkdirs()
            }
            _cache = SimpleCache(
                cacheDir,
                LeastRecentlyUsedCacheEvictor(cacheSize),
                StandaloneDatabaseProvider(context)
            )
        }
    }
}