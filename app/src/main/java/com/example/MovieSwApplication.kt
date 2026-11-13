package com.example

import android.app.Application
import com.example.data.MovieRepository

class MovieSwApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        MovieRepository.initialize(this)
    }
}
