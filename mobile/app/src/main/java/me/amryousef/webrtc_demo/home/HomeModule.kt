package me.amryousef.webrtc_demo.home

import dagger.Module
import dagger.Provides

@Module
object HomeModule {

    @Provides
    fun presenter(): HomePresenter.Factory {
        return object : HomePresenter.Factory {
            override fun create(args: HomePresenter.Args) = HomePresenter(args)
        }
    }
}