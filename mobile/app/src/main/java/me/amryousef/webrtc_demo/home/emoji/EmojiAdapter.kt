package me.amryousef.webrtc_demo.home.emoji

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.view_holder_emoji.view.*
import me.amryousef.webrtc_demo.R

class EmojiAdapter(
    private val items: List<Emoji>,
    private val context: Context
) : RecyclerView.Adapter<EmojiViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmojiViewHolder {
        return EmojiViewHolder(LayoutInflater.from(context).inflate(R.layout.view_holder_emoji, parent, false))
    }

    override fun onBindViewHolder(holderEmoji: EmojiViewHolder, position: Int) {
        val current = items[position]
        holderEmoji.emojiImage.setImageResource(current.image)
        holderEmoji.emojiImage.setOnClickListener { current.onPress(current.id) }
    }

    override fun getItemCount(): Int = items.size
}

class EmojiViewHolder (view: View) : RecyclerView.ViewHolder(view) {
    val emojiImage: ImageView = view.emojiImage
}
