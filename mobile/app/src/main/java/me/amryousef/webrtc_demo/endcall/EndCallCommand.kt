package me.amryousef.webrtc_demo.endcall

const val END_CALL_COMMAND_ID = "ENDCALL"

data class EndCallCommand(val type: String = END_CALL_COMMAND_ID)
