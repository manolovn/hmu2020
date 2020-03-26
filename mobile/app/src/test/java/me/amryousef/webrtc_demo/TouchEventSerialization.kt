package me.amryousef.webrtc_demo

import com.google.gson.GsonBuilder
import junit.framework.TestCase.assertEquals
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class TouchEventSerialization {

    private val gson = GsonBuilder().apply {
        registerTypeAdapter(TouchEvent::class.java, TouchEventAdapter())
    }.create()

    @Test
    fun testCustomSerialization() {
        val anyTouchEvent = TouchEvent.ActionMove(214142F, 21412F)
        val serializedTouchEvent = gson.toJson(anyTouchEvent)

        assertEquals("", serializedTouchEvent)
    }

    @Test
    fun testCustomDeserialization() {
        val anyJson = "{\"x\":\"214142.0\",\"y\":\"21412.0\",\"type\":\"actionMove\"}"
        val deserializedTouchEvent = gson.fromJson(anyJson, TouchEvent::class.java)

        assertEquals(TouchEvent.ActionMove(214142F, 21412F), deserializedTouchEvent)
    }

    @Test
    fun testFloatDeserialization() {

        val anyFloat = gson.fromJson("5.0", Float::class.java)

        assertEquals(5F, anyFloat)
    }
}
