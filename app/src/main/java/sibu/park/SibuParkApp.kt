package sibu.park

import android.app.Application
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SibuParkApp : Application() {
    
    override fun onCreate() {
        super.onCreate()
        // 初始化Firebase
        FirebaseApp.initializeApp(this)
    }
} 