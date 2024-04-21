package com.aacharya.prashant.common

import android.app.Application
import android.graphics.Bitmap
import com.jakewharton.disklrucache.DiskLruCache
import java.io.File
import java.util.concurrent.ConcurrentHashMap


    class MyApplication : Application() {

        companion object {
            lateinit var memoryCache: ConcurrentHashMap<String, Bitmap>
            lateinit var diskCache: DiskLruCache
        }

        override fun onCreate() {
            super.onCreate()
            memoryCache = ConcurrentHashMap<String, Bitmap>(100)
            val cacheDirectory = File(cacheDir, "image_cache")
            diskCache = DiskLruCache.open(cacheDirectory,0, 1, 300 * 1024 * 1024)
        }
    }