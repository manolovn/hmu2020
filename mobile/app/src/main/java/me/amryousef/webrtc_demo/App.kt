package me.amryousef.webrtc_demo

import android.app.Application
import android.os.Looper
import io.reactivex.rxjava3.android.plugins.RxAndroidPlugins
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import me.amryousef.webrtc_demo.di.AppComponent
import me.amryousef.webrtc_demo.di.DaggerAppComponent

class App: Application() {

    companion object {
        lateinit var component: AppComponent
    }

    override fun onCreate() {
        super.onCreate()
        component = buildDependencyGraph()

        RxAndroidPlugins.setInitMainThreadSchedulerHandler {
            AndroidSchedulers.from(Looper.getMainLooper(), true)
        }
    }

    private fun buildDependencyGraph(): AppComponent = DaggerAppComponent.builder().build()

}