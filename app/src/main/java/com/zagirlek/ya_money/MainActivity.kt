package com.zagirlek.ya_money

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.zagirlek.ya_money.navigation.DefaultRootComponent
import com.zagirlek.ya_money.navigation.RootScreen
import com.zagirlek.systemdesign.theme.YaMoneyTheme

class MainActivity : ComponentActivity() {
    private val dependencies
        get() = (application as YaMoneyApplication).dependencies
    private val rootComponent by lazy {
        DefaultRootComponent(
            componentContext = DefaultComponentContext(LifecycleRegistry()),
            accountsRepository = dependencies.accountsRepository,
            categoriesRepository = dependencies.categoriesRepository,
            transactionsRepository = dependencies.transactionsRepository,
            isOnline = (application as YaMoneyApplication).networkMonitor.isOnline,
            syncStatusRepository = dependencies.syncStatusRepository,
            onRetrySyncRequested = dependencies::retrySync,
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            YaMoneyTheme {
                RootScreen(component = rootComponent)
            }
        }
    }
}
