package com.aacharya.prashant.common

import com.aacharya.prashant.model.Coverage
import retrofit2.Response
import retrofit2.http.GET

interface ImagesApiService {

    @GET("media-coverages?limit=100")
    suspend fun getImages() : Response<List<Coverage>>


}