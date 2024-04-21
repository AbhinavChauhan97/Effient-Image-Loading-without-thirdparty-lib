package com.aacharya.prashant.common

import android.graphics.Bitmap

interface ImagesRepository {

    suspend fun loadImages(onSuccess: suspend (images:List<String>) -> Unit
                           ,onFailure: suspend (reason:String) -> Unit)

}