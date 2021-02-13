package com.andrey.test.presentation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.andrey.test.R
import com.andrey.test.di.Scope
import com.andrey.test.di.modules.PresentationModules
import com.andrey.test.presentation.router.AppRouter
import com.andrey.test.presentation.searchScreen.SearchFragment
import ru.terrakok.cicerone.Navigator
import ru.terrakok.cicerone.NavigatorHolder
import ru.terrakok.cicerone.android.support.SupportAppNavigator
import toothpick.Toothpick
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var router: AppRouter

    @Inject
    lateinit var navigatorHolder: NavigatorHolder

    private val navigator: Navigator = SupportAppNavigator(this, R.id.navFragment)

    override fun onCreate(savedInstanceState: Bundle?) {
        val presentationScope = Toothpick.openScopes(Scope.PRESENTATION)
        presentationScope.installModules(PresentationModules())
        Toothpick.inject(this, presentationScope)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            router.newRootChain(SearchFragment.Screen())
        }
    }

    override fun onResume() {
        super.onResume()
        navigatorHolder.setNavigator(navigator)
    }

    override fun onPause() {
        super.onPause()
        navigatorHolder.removeNavigator()
    }

    override fun onDestroy() {
        Toothpick.closeScope(Scope.PRESENTATION)
        super.onDestroy()
    }
}
