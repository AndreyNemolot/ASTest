package com.andrey.test.presentation.router

import ru.terrakok.cicerone.Router
import ru.terrakok.cicerone.Screen
import javax.inject.Inject

class AppRouterImpl @Inject constructor(
    private val router: Router
) : AppRouter {

    override fun navigateTo(screen: Screen) {
        router.navigateTo(screen)
    }

    override fun replace(screen: Screen) {
        router.replaceScreen(screen)
    }

    override fun newRootChain(screen: Screen) {
        router.newRootChain(screen)
    }
}