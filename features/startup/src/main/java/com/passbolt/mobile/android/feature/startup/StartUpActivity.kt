package com.passbolt.mobile.android.feature.startup

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.passbolt.mobile.android.core.ui.orientation.LockCompactScreenOrientation
import org.koin.android.ext.android.inject

// NOTE: When changing name or package read core/navigation/README.md
class StartUpActivity : ComponentActivity() {
    private val accountSetupModelCreator: AccountSetupModelCreator by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LockCompactScreenOrientation()
            StartUpScreen(
                accountSetupDataModel = accountSetupModelCreator.createFromIntent(intent),
            )
        }
    }
}
