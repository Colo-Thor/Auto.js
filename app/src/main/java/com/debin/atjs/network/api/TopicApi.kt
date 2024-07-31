package com.debin.atjs.network.api

import kotlinx.coroutines.Deferred
import com.debin.atjs.network.entity.topic.Category
import com.debin.atjs.network.entity.topic.Topic
import retrofit2.http.GET
import retrofit2.http.Path

interface TopicApi {

    @GET("/api/category/{cid}")
    fun getCategory(@Path("cid") cid: Long): Deferred<Category>

    @GET("/api/topic/{tid}")
    fun getTopic(@Path("tid") pid: Long): Deferred<Topic>


}