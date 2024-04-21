package com.aacharya.prashant.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aacharya.prashant.common.ImagesApiService
import com.aacharya.prashant.common.ImagesRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ImagesViewModel : ViewModel() {

    sealed class ImageLoadingEvents {
        data class ImagesLoaded(val images:List<String>) : ImageLoadingEvents()
        data class ImageLoadingFailure(val reason:String) : ImageLoadingEvents()
    }

    private val _imageLoadingEventsStateFlow = MutableSharedFlow<ImageLoadingEvents>()
    val imageLoadingEventsStateFlow:SharedFlow<ImageLoadingEvents> = _imageLoadingEventsStateFlow

    private val imagesApiService = Retrofit
        .Builder()
        .baseUrl("https://acharyaprashant.org/api/v2/content/misc/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(OkHttpClient.Builder().addInterceptor(
            HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
            .build())
        .build()
        .create(ImagesApiService::class.java)

    private val repository = ImagesRepositoryImpl(imagesApiService)



    fun loadImages() {
        viewModelScope.launch(Dispatchers.IO) {
            delay(100)
            repository.loadImages(
                onSuccess = { images ->
                    _imageLoadingEventsStateFlow.emit(ImageLoadingEvents.ImagesLoaded(images))
                },
                onFailure = { reason ->
                    _imageLoadingEventsStateFlow.emit(ImageLoadingEvents.ImageLoadingFailure(reason))
                }
            )
        }
    }


}