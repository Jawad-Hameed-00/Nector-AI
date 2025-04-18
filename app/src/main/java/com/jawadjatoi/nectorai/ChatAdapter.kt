package com.jawadjatoi.nectorai

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.jawadjatoi.nectorai.network.ChatMessage
import io.noties.markwon.Markwon
import java.io.File

class ChatAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val messages = mutableListOf<ChatMessage>()
    private lateinit var markwon: Markwon

    fun submitList(newMessages: List<ChatMessage>) {
        messages.clear()
        messages.addAll(newMessages)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            messages[position].isTyping -> VIEW_TYPE_TYPING
            messages[position].isImage && messages[position].isUserMessage -> VIEW_TYPE_IMAGE_SENT
            messages[position].isImage -> VIEW_TYPE_IMAGE_RECEIVED
            messages[position].isUserMessage -> VIEW_TYPE_SENT
            else -> VIEW_TYPE_RECEIVED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        markwon = Markwon.create(parent.context)

        return when (viewType) {
            VIEW_TYPE_SENT -> SentMessageViewHolder(inflater.inflate(R.layout.chat_item_sent, parent, false))
            VIEW_TYPE_RECEIVED -> ReceivedMessageViewHolder(inflater.inflate(R.layout.chat_item_received, parent, false), markwon)
            VIEW_TYPE_IMAGE_SENT -> ImageSentViewHolder(inflater.inflate(R.layout.chat_item_image_send, parent, false))
            VIEW_TYPE_IMAGE_RECEIVED -> ImageReceivedViewHolder(inflater.inflate(R.layout.chat_item_image_received, parent, false))
            else -> TypingViewHolder(inflater.inflate(R.layout.chat_item_typing, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        when (holder) {
            is SentMessageViewHolder -> holder.bind(message)
            is ReceivedMessageViewHolder -> holder.bind(message)
            is ImageSentViewHolder -> holder.bind(message)
            is ImageReceivedViewHolder -> holder.bind(message)
        }
    }

    override fun getItemCount(): Int = messages.size

    class SentMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val sendText: TextView = itemView.findViewById(R.id.send_text)
        fun bind(message: ChatMessage) {
            sendText.text = message.content
        }
    }

    class ReceivedMessageViewHolder(itemView: View, private val markwon: Markwon) : RecyclerView.ViewHolder(itemView) {
        private val receiveText: TextView = itemView.findViewById(R.id.receive_text)
        fun bind(message: ChatMessage) {
            markwon.setMarkdown(receiveText, message.content)
        }
    }

    class ImageSentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val userImageView: ImageView = itemView.findViewById(R.id.imageView)
        fun bind(message: ChatMessage) {
            Glide.with(itemView.context)
                .load(message.imageUrl)
                .into(userImageView)
        }
    }

    class ImageReceivedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val botImageView: ImageView = itemView.findViewById(R.id.imageView)

        fun bind(message: ChatMessage) {
            Log.d("ImageReceived", "Image URL: ${message.imageUrl}")
            if (!message.imageUrl.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(File(message.imageUrl))
                    .into(botImageView)
            } else {
                Log.e("ImageReceived", "Received image URL is null or empty")
            }
        }
    }


    class TypingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val typingAnimation: LottieAnimationView = itemView.findViewById(R.id.typingAnimation)
    }

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
        private const val VIEW_TYPE_TYPING = 3
        private const val VIEW_TYPE_IMAGE_SENT = 4
        private const val VIEW_TYPE_IMAGE_RECEIVED = 5
    }
}
