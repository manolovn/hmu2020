package me.amryousef.webrtc_demo.home

class HomePresenter(
    private val args: Args
) {

    interface Factory {
        fun create(args: Args): HomePresenter
    }

    data class Args(
        val isAdmin: Boolean
    )
}