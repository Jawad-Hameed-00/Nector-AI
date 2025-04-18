package com.jawadjatoi.nectorai.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.jawadjatoi.nectorai.network.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val apiService = ChatApiService.create()
    private val apiServiceImage = ChatApiService.createImage()
    private val appContext = application.applicationContext

    fun sendMessage(userMessage: String, onResponse: (ChatMessage) -> Unit) {
        val request = ChatRequest(messages = listOf(Message(role = "user", content = userMessage)))

        apiService.sendMessage(request).enqueue(object : retrofit2.Callback<ChatResponse> {
            override fun onResponse(
                call: retrofit2.Call<ChatResponse>,
                response: retrofit2.Response<ChatResponse>
            ) {
                if (response.isSuccessful) {
                    val reply =
                        response.body()?.choices?.firstOrNull()?.message?.content ?: "No response"
                    onResponse(ChatMessage(content = reply))
                } else {
                    onResponse(
                        ChatMessage(
                            content = "Error: ${response.code()} - ${
                                response.errorBody()?.string()
                            }"
                        )
                    )
                }
            }

            override fun onFailure(call: retrofit2.Call<ChatResponse>, t: Throwable) {
                onResponse(ChatMessage(content = "API call failed: ${t.message}"))
            }
        })
    }


    fun generateImage(prompt: String, onResponse: (ChatMessage) -> Unit) {
        val request = TextToImageRequest(inputs = prompt)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiServiceImage.textToImage(request).execute()

                if (response.isSuccessful) {
                    val responseBody = response.body()?.bytes()

                    if (responseBody != null) {
                        Log.d("ImageGen", "Image response received: ${responseBody.size} bytes")
                        val bitmap = BitmapFactory.decodeByteArray(responseBody, 0, responseBody.size)

                        if (bitmap != null) {
                            val imageUrl = saveImageToCache(bitmap)
                            Log.d("ImageGen", "Saved image path: $imageUrl")

                            withContext(Dispatchers.Main) {
                                onResponse(ChatMessage(content = "Generated Image", imageUrl = imageUrl, isImage = true))
                            }
                        } else {
                            Log.e("ImageGen", "Bitmap decoding failed")
                            withContext(Dispatchers.Main) {
                                onResponse(ChatMessage(content = "Image Generation failed!"))
                            }
                        }
                    } else {
                        Log.e("ImageGen", "Response body is null")
                        withContext(Dispatchers.Main) {
                            onResponse(ChatMessage(content = "Image Generation failed!"))
                        }
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    Log.e("ImageGen", "Error: $errorBody")
                    withContext(Dispatchers.Main) {
                        onResponse(ChatMessage(content = "Error: $errorBody"))
                    }
                }
            } catch (e: Exception) {
                Log.e("ImageGen", "Exception: ${e.message}")
                withContext(Dispatchers.Main) {
                    onResponse(ChatMessage(content = "Exception: ${e.message}"))
                }
            }
        }
    }


    private fun saveImageToCache(bitmap: Bitmap): String {
        val file = File(appContext.cacheDir, "generated_${System.currentTimeMillis()}.jpg")

        return try {
            FileOutputStream(file).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            }
            Log.d("ImageGen", "Image saved successfully: ${file.absolutePath}")
            file.absolutePath
        } catch (e: Exception) {
            Log.e("ImageGen", "Error saving image: ${e.message}")
            ""
        }
    }

}