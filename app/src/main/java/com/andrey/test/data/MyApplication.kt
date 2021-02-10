package com.andrey.test.data

import android.app.Application
import com.andrey.test.di.Scope
import com.andrey.test.di.modules.AppModules
import toothpick.Toothpick

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        val appScope = Toothpick.openScope(Scope.APP)
        appScope.installModules(AppModules(this))
        Toothpick.inject(this, appScope)

    }
}