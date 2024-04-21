package com.aacharya.prashant.model

data class Thumbnail(
    val id: String,
    val version: Int,
    val domain: String,
    val basePath: String,
    val key: String,
    val qualities: List<Int>,
    val aspectRatio: Int,
){
    companion object {
        fun empty() = Thumbnail(id = "", version = 1, domain = "", basePath = "", key = "", qualities = emptyList(),1)
    }
}

val Thumbnail.imageUrl : String
    get() = "$domain/$basePath/0/$key"