package com.aacharya.prashant.common

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.ImageView
import android.widget.Toast
import com.aacharya.prashant.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.net.URL


fun Activity.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}


fun loadUrl(imageView: ImageView, url: String, coroutine: (Job) -> Unit) {


    fun loadFromMemory(key: String) = MyApplication.memoryCache[key]


    fun loadFromDisk(key: String): Bitmap? {
        return try {
            val snapShot = MyApplication.diskCache.get(key) ?: return null
            val inputStream = snapShot.getInputStream(0)
            val buffIn = BufferedInputStream(inputStream)
            val bitmap = BitmapFactory.decodeStream(buffIn)
            inputStream.close()
            buffIn.close()
            bitmap

        } catch (e: Exception) {
            null
        }
    }

    fun loadImageFromCache(key: String) = loadFromMemory(key) ?: loadFromDisk(key)



    suspend fun loadFromNetwork(): Bitmap? {

        return withContext(Dispatchers.IO) {
             try {
                val connection = URL(url).openConnection()
                connection.connect()
                val inputStream = connection.getInputStream()
                val originalBitmap =
                    withContext(Dispatchers.Default) {
                        if(isActive) {
                            BitmapFactory.decodeStream(inputStream)
                        }else{
                            null
                        }
                    }
                inputStream.close()
                async (Dispatchers.Default) {
                    if(originalBitmap != null && isActive) {
                        Bitmap.createScaledBitmap(
                            originalBitmap,
                            230,
                            230,
                            true
                        )
                    }else{
                        null
                    }
                }.await()
            } catch (e: Exception) {
                null
            }
        }
    }


    suspend fun ImageView.setBitmapOnMain(bitmap: Bitmap) {
        withContext(Dispatchers.Main) {
            if(isActive) {
                setImageBitmap(bitmap)
            }
        }
    }

    fun CoroutineScope.cacheThumbnail(key: String, thumb: Bitmap) {
        launch {
            withContext(Dispatchers.IO) {
                if (!isActive) return@withContext
                MyApplication.memoryCache[key] = thumb

                val editor = MyApplication.diskCache.edit(key)
                if (editor != null) {
                    try {
                        val outputStream =
                            BufferedOutputStream(
                                editor.newOutputStream(0)
                            )
                        thumb.compress(
                            Bitmap.CompressFormat.JPEG,
                            80,
                            outputStream
                        )
                        outputStream.flush()
                        editor.commit()
                        outputStream.close()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        editor.abort()
                    }
                }

            }
        }
    }

    imageView.setImageResource(R.drawable.placeholder)

    val loadingTask = GlobalScope.launch(Dispatchers.IO) {
        val key = url.split("/").getOrNull(4) ?: ""
        val bitmap = loadImageFromCache(key)
        if (bitmap != null) {
            imageView.setBitmapOnMain(bitmap)
        } else {
            val thumb = loadFromNetwork()
            if (thumb != null) {
                cacheThumbnail(key, thumb)
                imageView.setBitmapOnMain(thumb)
            } else {
                withContext(Dispatchers.Main) {
                    imageView.setImageResource(R.drawable.image_load_error)
                }
            }
        }
    }
    coroutine.invoke(loadingTask)
}

suspend fun <T> handleApi(
    block: suspend () -> Response<T>
): IOState<T> {
    return try {
        val response = block()
        val body: T?
        if (response.isSuccessful) {
            body = response.body()
            if (body != null) {
                IOState.Success(body)
            } else {
                IOState.Failure(response.message())
            }
        } else {
            IOState.Failure(response.message())
        }
    } catch (e: Exception) {
        IOState.Failure(e.message ?: "Something went wrong")
    }
}