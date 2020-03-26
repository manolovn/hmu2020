package me.amryousef.webrtc_demo

import com.google.gson.*
import java.lang.reflect.Type

object TouchEventType {
    const val None = "none"
    const val ActionDown = "actionDown"
    const val ActionMove = "actionMove"
    const val ActionUp = "actionUp"
    const val Clear = "clear"
}

class TouchEventAdapter : JsonSerializer<TouchEvent>, JsonDeserializer<TouchEvent> {

    override fun serialize(src: TouchEvent, typeOfSrc: Type, context: JsonSerializationContext): JsonElement =
        when (src.type) {
            TouchEventType.None -> context.serialize(src, TouchEventType.None::class.java)
            TouchEventType.ActionDown -> context.serialize(src, TouchEventType.ActionDown::class.java)
            TouchEventType.ActionMove -> context.serialize(src, TouchEventType.ActionMove::class.java)
            TouchEventType.ActionUp -> context.serialize(src, TouchEventType.ActionUp::class.java)
            TouchEventType.Clear -> context.serialize(src, TouchEventType.Clear::class.java)
            else -> error("unknown TouchEventType $src")
        }


    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): TouchEvent {
        var touchEvent: TouchEvent = TouchEvent.None()
        if (json.isJsonObject) {
            val jsonObject = json.asJsonObject
            if (jsonObject.has("type")) {
                val touchEventType = jsonObject.get("type").asString
                touchEvent = when (touchEventType) {
                    TouchEventType.None -> TouchEvent.None()
                    TouchEventType.ActionDown -> context.deserialize(json, TouchEvent.ActionDown::class.java)
                    TouchEventType.ActionMove -> context.deserialize(json, TouchEvent.ActionMove::class.java)
                    TouchEventType.ActionUp -> TouchEvent.ActionUp()
                    TouchEventType.Clear -> TouchEvent.Clear()
                    else -> TouchEvent.None()
                }
            }
        }
        return touchEvent
    }
}