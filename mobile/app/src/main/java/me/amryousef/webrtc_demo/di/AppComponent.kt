package me.amryousef.webrtc_demo.di

import dagger.Component
import kotlinx.coroutines.ExperimentalCoroutinesApi
import me.amryousef.webrtc_demo.home.HomeActivity

@ExperimentalCoroutinesApi
@Component(modules = [AppModule::class])
interface AppComponent {
    fun inject(target: HomeActivity)
}
