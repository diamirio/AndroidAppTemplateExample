/*
 * Copyright 2019 Michael Gostner.
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

package com.tailoredapps.countriesexample.core

import com.tailoredapps.androidutil.network.networkresponse.split
import com.tailoredapps.countriesexample.core.local.*
import com.tailoredapps.countriesexample.core.local.model.CountryLanguageJoin
import com.tailoredapps.countriesexample.core.local.model.LocalCountryWithFavorite
import com.tailoredapps.countriesexample.core.local.model.LocalFavoriteCountry
import com.tailoredapps.countriesexample.core.model.Country
import com.tailoredapps.countriesexample.core.remote.CountriesApi
import com.tailoredapps.countriesexample.core.remote.model.RemoteCountry
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers

interface CountriesRepo {
    val allCountries: Flowable<List<Country>>
    val allFavorites: Flowable<List<Country>>

    fun refreshCountries(): Completable

    fun getCountry(alpha2Code: String): Flowable<Country>
    fun toggleFavorite(country: Country): Completable
}

class RetrofitRoomCountriesRepo(
    private val countriesApi: CountriesApi,
    private val countriesDb: CountriesDatabase
) : CountriesRepo {
    override val allCountries: Flowable<List<Country>>
        get() = countriesDb.countryDao().getAll()
            .onBackpressureLatest()
            .flatMap(::combineWithLanguages)
            .subscribeOn(Schedulers.io())

    override val allFavorites: Flowable<List<Country>>
        get() = countriesDb.countryDao().getAllFavorites()
            .onBackpressureLatest()
            .flatMap(::combineWithLanguages)
            .subscribeOn(Schedulers.io())

    override fun refreshCountries(): Completable = countriesApi.all()
        .split()
        .flattenAsObservable { it }
        .flatMapCompletable(::updateOrInsertCountry)
        .subscribeOn(Schedulers.io())

    override fun getCountry(alpha2Code: String): Flowable<Country> =
        countriesDb.countryDao().get(alpha2Code)
            .flatMap(::combineWithLanguages)
            .subscribeOn(Schedulers.io())

    override fun toggleFavorite(country: Country): Completable =
        if (country.favorite) removeAsFavorite(country.alpha2Code)
        else setAsFavorite(country.alpha2Code)

    private fun removeAsFavorite(alpha2Code: String): Completable =
        countriesDb.favoriteDao().removeFavorite(LocalFavoriteCountry(alpha2Code))
            .subscribeOn(Schedulers.io())

    private fun setAsFavorite(alpha2Code: String): Completable =
        countriesDb.favoriteDao().addFavorite(LocalFavoriteCountry(alpha2Code))
            .subscribeOn(Schedulers.io())

    private fun updateOrInsertCountry(country: RemoteCountry): Completable = Completable
        .fromAction {
            // insert or update country
            countriesDb.countryDao().insertOrUpdate(country.asLocalCountry)

            // insert or update languages
            countriesDb.languageDao().insertOrUpdate(*country.languages.asLocalLanguageList.toTypedArray())

            // insert or update country language join
            country.languages.forEach { language ->
                countriesDb.countryLanguageDao().insertOrUpdate(CountryLanguageJoin(country.alpha2Code, language.name))
            }
        }
        .subscribeOn(Schedulers.io())

    private fun combineWithLanguages(countries: List<LocalCountryWithFavorite>): Flowable<List<Country>> =
        Flowable.fromIterable(countries)
            .flatMap(::combineWithLanguages)
            .toList()
            .toFlowable()

    private fun combineWithLanguages(country: LocalCountryWithFavorite): Flowable<Country> =
        countriesDb.countryLanguageDao()
            .getLanguagesByCountry(country.country.alpha2Code)
            .map { country.asCountryWithLanguages(it.asLanguageList) }
            .toFlowable()
}