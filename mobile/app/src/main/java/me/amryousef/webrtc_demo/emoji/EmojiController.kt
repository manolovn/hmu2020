package me.amryousef.webrtc_demo.emoji

import androidx.annotation.DrawableRes
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import me.amryousef.webrtc_demo.R
import me.amryousef.webrtc_demo.SignallingClient

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
class EmojiController(private val signallingClient: SignallingClient) {

    private val emojis = listOf(
        Emoji(0, "hearth", R.drawable.emoji_hearth) { signallingClient.sendEmoji(it) },
        Emoji(1, "fingerup", R.drawable.emoji_fingerup) { signallingClient.sendEmoji(it) },
        Emoji(2, "happy", R.drawable.emoji_happy) { signallingClient.sendEmoji(it) },
        Emoji(3, "meh", R.drawable.emoji_meh) { signallingClient.sendEmoji(it) },
        Emoji(4, "sad", R.drawable.emoji_sad) { signallingClient.sendEmoji(it) }
    )

    fun start() {

    }

    fun getEmojis(): List<Emoji> = emojis

    fun getEmojiById(id: Int): Emoji {
        return emojis[id]
    }
}

data class Emoji(
    val id: Int,
    val emojiCode: String,
    @DrawableRes val image: Int,
    val onPress: (id: Int) -> Unit
)
