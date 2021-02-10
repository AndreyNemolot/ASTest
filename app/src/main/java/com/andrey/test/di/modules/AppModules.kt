package com.andrey.test.di.modules

import android.app.Application
import com.andrey.test.data.network.HttpClientProvider
import com.andrey.test.data.network.Network
import com.andrey.test.data.network.NetworkImpl
import com.andrey.test.data.repository.CityRepository
import com.andrey.test.data.repository.CityRepositoryImpl
import com.andrey.test.di.ViewModelFactory
import com.andrey.test.domain.CityInteractor
import com.andrey.test.domain.CityInteractorImpl
import com.andrey.test.domain.NetworkConnectivityManager
import com.andrey.test.domain.NetworkConnectivityManagerImpl
import com.google.gson.Gson
import toothpick.config.Module
import toothpick.ktp.binding.bind

class AppModules(application: Application) : Module() {

    init {
        bind(Application::class.java).toInstance(application)
        bind<ViewModelFactory>().singleton()
        bind<Gson>().toInstance(Gson())
        bind<HttpClientProvider>().toInstance(HttpClientProvider())
        bind(Network::class.java).to(NetworkImpl::class.java).toString()
        bind(CityRepository::class.java).to(CityRepositoryImpl::class.java).toString()
        bind(CityInteractor::class.java).to(CityInteractorImpl::class.java).toString()
        bind(NetworkConnectivityManager::class.java).to(NetworkConnectivityManagerImpl::class.java)
            .toString()
    }
}