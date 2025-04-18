package com.jawadjatoi.nectorai.network

import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

interface ChatApiService {

    @Headers(
        "Authorization: Bearer YOUR_OPEN_ROUTER_API",
        "Content-Type: application/json"
    )
    @POST("chat/completions")
    fun sendMessage(@Body request: ChatRequest): Call<ChatResponse>

    @Headers(
        "Authorization: Bearer YOUR_HUGGING_FACE_API",
        "Content-Type: application/json"
    )
    @POST("hf-inference/models/black-forest-labs/FLUX.1-dev/")
    fun textToImage(@Body request: TextToImageRequest): Call<ResponseBody>

    companion object {
        private const val BASE_URL_CHAT = "https://openrouter.ai/api/v1/"
        private const val BASE_URL_IMAGE = "https://router.huggingface.co/"

        private val client = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()

        fun create(): ChatApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL_CHAT)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
                .create(ChatApiService::class.java)
        }

        fun createImage(): ChatApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL_IMAGE)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
                .create(ChatApiService::class.java)
        }
    }
}
