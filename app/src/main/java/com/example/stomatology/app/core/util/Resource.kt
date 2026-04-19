package com.example.stomatology.app.core.util

sealed class Resource<out T> {

    data class Success<T>(val data: T) : Resource<T>()

    data class Error(
        val message: String,
        val throwable: Throwable? = null
    ) : Resource<Nothing>()

    object Loading : Resource<Nothing>()
}