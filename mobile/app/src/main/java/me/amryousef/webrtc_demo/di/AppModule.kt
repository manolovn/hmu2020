package me.amryousef.webrtc_demo.di

import com.squareup.inject.assisted.dagger2.AssistedModule
import dagger.Module

@AssistedModule
@Module(includes = [
    AssistedInject_AppModule::class
])
object AppModule {

}