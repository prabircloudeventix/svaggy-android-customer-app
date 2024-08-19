package com.svaggy.utils

sealed class ResponseLoad<T>(val data:T?=null,val message:String?=null){
    class Success<T>(data: T): ResponseLoad<T>(data)
    class Error<T>(message: String?,data: T?=null): ResponseLoad<T>(data,message)
    class Loading<T>: ResponseLoad<T>()
}
