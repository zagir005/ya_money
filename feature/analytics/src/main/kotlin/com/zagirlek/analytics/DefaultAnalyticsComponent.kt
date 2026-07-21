package com.zagirlek.analytics

import com.arkivanov.decompose.ComponentContext

class DefaultAnalyticsComponent(
    componentContext: ComponentContext,
) : AnalyticsComponent, ComponentContext by componentContext
