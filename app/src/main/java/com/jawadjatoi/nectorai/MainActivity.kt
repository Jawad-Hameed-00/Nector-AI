package com.jawadjatoi.nectorai

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabel
import com.google.mlkit.vision.label.ImageLabeler
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.jawadjatoi.nectorai.network.ChatMessage
import com.jawadjatoi.nectorai.viewmodel.ChatViewModel
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var messageInput: EditText
    private lateinit var sendButton: ImageView
    private lateinit var captureImage: ImageView
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>

    private val chatAdapter = ChatAdapter()
    private val messages = mutableListOf<ChatMessage>()

    private val chatViewModel: ChatViewModel by viewModels()
    private lateinit var imageLabeler: ImageLabeler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.chatRecyclerView)
        messageInput = findViewById(R.id.messageInput)
        sendButton = findViewById(R.id.sendButton)
        captureImage = findViewById(R.id.captureImage)

        imageLabeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)

        cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    result.data?.extras?.getParcelable("data", Bitmap::class.java)
                } else {
                    result.data?.extras?.get("data") as? Bitmap
                }

                if (imageBitmap != null) {
                    labelImage(imageBitmap)
                } else {
                    Toast.makeText(this, "Unable to capture Image", Toast.LENGTH_SHORT).show()
                }
            }
        }

        captureImage.setOnClickListener {
            if (checkCameraPermission()) {
                val clickImage = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                if (clickImage.resolveActivity(packageManager) != null) {
                    cameraLauncher.launch(clickImage)
                }
            }
        }

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = chatAdapter
        }

        sendButton.setOnClickListener {
            val userMessage = messageInput.text.toString().trim()
            if (userMessage.isNotEmpty()) {
                addMessage(ChatMessage(userMessage, isUserMessage = true))
                messageInput.text.clear()

                showTypingIndicator()

                if (userMessage.startsWith("@imagine")) {
                    chatViewModel.generateImage(userMessage.removePrefix("@imagine").trim()) { response ->
                        removeTypingIndicator()
                        addMessage(response)
                    }
                } else {
                    chatViewModel.sendMessage(userMessage) { response ->
                        removeTypingIndicator()

                        val formattedResponse = formatAIResponse(response.content)
                        val finalResponse = replaceWords(formattedResponse)

                        addMessage(ChatMessage(finalResponse, isUserMessage = false))
                    }
                }
            }
        }
    }

    private fun addMessage(chatMessage: ChatMessage) {
        messages.add(chatMessage)
        chatAdapter.submitList(messages.toList())
        recyclerView.smoothScrollToPosition(messages.size - 1)
    }

    private fun showTypingIndicator() {
        messages.add(ChatMessage("Typing...", isTyping = true))
        chatAdapter.submitList(messages.toList())
        recyclerView.smoothScrollToPosition(messages.size - 1)
    }

    private fun removeTypingIndicator() {
        messages.removeAll { it.isTyping }
        chatAdapter.submitList(messages.toList())
    }

    private fun formatAIResponse(response: String): String {
        return response
            .replace(Regex("""\\boxed\s*\{([^}]*)\}"""), "$1")
            .replace("\n", "\n\n")
    }

    private fun replaceWords(response: String): String {
        val wordReplacements = mapOf(
            "I'm DeepSeek-R1," to "I'm Nector AI,",
            "created by DeepSeek" to "created by Jawad Hameed",
            "DeepSeek" to "Nector"
        )

        var modifiedResponse = response
        for ((oldWord, newWord) in wordReplacements) {
            modifiedResponse = modifiedResponse.replace(oldWord, newWord)
        }

        return modifiedResponse
    }

    private fun labelImage(bitmap: Bitmap) {
        val inputImage = InputImage.fromBitmap(bitmap, 0)
        imageLabeler.process(inputImage)
            .addOnSuccessListener { labels ->
                if (labels.isNotEmpty()) {
                    val mostConfidentLabel = labels[0].text

                    addMessage(ChatMessage("", isUserMessage = true, isImage = true, imageUrl = saveImageToCache(bitmap)))

                    sendImageLabelToChatbot(mostConfidentLabel)
                } else {
                    Toast.makeText(this, "No labels detected.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun sendImageLabelToChatbot(label: String) {

        showTypingIndicator()

        val prompt = "Print this line and give one line description about ${label}: This is a $label."

        chatViewModel.sendMessage(prompt) { response ->
            removeTypingIndicator()
            val formattedResponse = formatAIResponse(response.content)
            addMessage(ChatMessage(formattedResponse, isUserMessage = false))
        }
    }



    private fun checkCameraPermission(): Boolean {
        return if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
            false
        } else {
            true
        }
    }

    private fun saveImageToCache(bitmap: Bitmap): String {
        clearOldImages()

        val uniqueFilename = "generated_${System.currentTimeMillis()}.jpg"
        val file = File(cacheDir, uniqueFilename)

        FileOutputStream(file).use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        }

        return file.absolutePath
    }

    private fun clearOldImages() {
        val cacheDir = cacheDir
        cacheDir?.listFiles()?.forEach { file ->
            if (file.name.startsWith("generated_")) {
                file.delete()
            }
        }
    }

    companion object {
        private const val CAMERA_PERMISSION_CODE = 100
    }
}
