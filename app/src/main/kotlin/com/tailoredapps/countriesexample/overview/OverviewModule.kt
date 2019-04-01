package com.tailoredapps.countriesexample.overview

import com.tailoredapps.reaktor.koin.reactor
import org.koin.dsl.module

val overviewModule = module {
    reactor { OverviewReactor(countriesRepo = get()) }
}