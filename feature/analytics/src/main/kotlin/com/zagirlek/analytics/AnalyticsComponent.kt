package com.zagirlek.analytics

import com.zagirlek.ui.mvi.MviStore

interface AnalyticsComponent : MviStore<AnalyticsIntent, AnalyticsState, AnalyticsEffect>
