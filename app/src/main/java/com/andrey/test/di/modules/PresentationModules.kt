package com.andrey.test.di.modules

import com.andrey.test.presentation.router.AppRouter
import com.andrey.test.presentation.router.AppRouterImpl
import ru.terrakok.cicerone.Cicerone
import ru.terrakok.cicerone.NavigatorHolder
import ru.terrakok.cicerone.Router
import toothpick.config.Module
import toothpick.ktp.binding.bind

class PresentationModules : Module() {

    init {
        val cicerone = Cicerone.create()
        bind<Router>().toInstance(cicerone.router)
        bind<NavigatorHolder>().toInstance(cicerone.navigatorHolder)
        bind(AppRouter::class.java).to(AppRouterImpl::class.java).singleton()
    }
}