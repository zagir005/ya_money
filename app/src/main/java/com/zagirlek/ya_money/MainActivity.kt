package com.zagirlek.ya_money

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.zagirlek.ya_money.di.AppGraph
import com.zagirlek.ya_money.navigation.DefaultMainComponent
import com.zagirlek.ya_money.navigation.MainScreen
import com.zagirlek.systemdesign.theme.FinanceTheme
import dev.zacsweers.metro.createGraph

class MainActivity : ComponentActivity() {
    private val graph: AppGraph by lazy { createGraph<AppGraph>() }
    private val mainComponent by lazy {
        DefaultMainComponent(
            componentContext = DefaultComponentContext(LifecycleRegistry()),
            expensesRepository = graph.expensesRepository,
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinanceTheme {
                MainScreen(component = mainComponent)
            }
        }
    }
}
