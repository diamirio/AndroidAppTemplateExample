/*
 * Copyright 2020 Tailored Media GmbH.
 * Created by Florian Schuster.
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

import com.google.gson.Gson
import com.tailoredapps.countriesexample.core.model.AppBuildInfo
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

internal val remoteModule = module {
    single { provideOkHttpClient(appBuildInfo = get()) }
    single { provideApi<CountriesApi>(okHttpClient = get(), gson = get(), appBuildInfo = get()) }
}

private fun provideOkHttpClient(
    appBuildInfo: AppBuildInfo
) =
    OkHttpClient().newBuilder().apply {
        if (appBuildInfo.debug) {
            val loggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.level = HttpLoggingInterceptor.Level.BASIC
            addInterceptor(loggingInterceptor)
        }
    }.build()

private inline fun <reified T> provideApi(
    okHttpClient: OkHttpClient,
    gson: Gson,
    appBuildInfo: AppBuildInfo
): T =
    Retrofit.Builder().apply {
        baseUrl(appBuildInfo.baseUrl)
        client(okHttpClient)
        addConverterFactory(GsonConverterFactory.create(gson))
    }.build().create(T::class.java)
