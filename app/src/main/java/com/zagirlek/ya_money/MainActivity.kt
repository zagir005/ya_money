package com.zagirlek.ya_money

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.zagirlek.ya_money.di.AppDependencies
import com.zagirlek.ya_money.navigation.DefaultMainComponent
import com.zagirlek.ya_money.navigation.MainScreen
import com.zagirlek.systemdesign.theme.YaMoneyTheme

class MainActivity : ComponentActivity() {
    private val dependencies = AppDependencies()
    private val mainComponent by lazy {
        DefaultMainComponent(
            componentContext = DefaultComponentContext(LifecycleRegistry()),
            expensesRepository = dependencies.expensesRepository,
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            YaMoneyTheme {
                MainScreen(component = mainComponent)
            }
        }
    }
}
