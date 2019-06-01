/*
 * Copyright 2019 Florian Schuster.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tailoredapps.countriesexample.core.remote

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.tailoredapps.androidutil.network.networkresponse.NetworkResponseRxJava2CallAdapterFactory
import com.tailoredapps.countriesexample.core.BuildConfig
import io.reactivex.schedulers.Schedulers
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory

internal val remoteModule = module {
    factory { HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY } }
    single { provideOkHttpClient(loggingInterceptor = get()) }
    single { provideApi<CountriesApi>(okHttpClient = get(), json = get(), baseUrl = get()) }
}

data class BaseUrl(val url: String)

private fun provideOkHttpClient(loggingInterceptor: HttpLoggingInterceptor) =
    OkHttpClient().newBuilder().apply {
        if (BuildConfig.DEBUG) addInterceptor(loggingInterceptor)
    }.build()

private inline fun <reified T> provideApi(okHttpClient: OkHttpClient, json: Json, baseUrl: BaseUrl): T =
    Retrofit.Builder().apply {
        baseUrl(baseUrl.url)
        client(okHttpClient)
        addConverterFactory(json.asConverterFactory(MediaType.get("application/json")))
        addCallAdapterFactory(NetworkResponseRxJava2CallAdapterFactory.create())
        addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
    }.build().create(T::class.java)
