package com.aacharya.prashant.common

import com.aacharya.prashant.model.imageUrl

class ImagesRepositoryImpl(private val apiService: ImagesApiService) : ImagesRepository {


    override suspend fun loadImages(
        onSuccess: suspend (images: List<String>) -> Unit,
        onFailure: suspend (reason: String) -> Unit
    ) {

        when (val response = handleApi { apiService.getImages() }) {
            is IOState.Success -> {
                val images = response.data.map { it.thumbnail.imageUrl }
                onSuccess.invoke(images)
            }

            is IOState.Failure -> {
                onFailure.invoke(response.reason)
            }
        }
    }

}