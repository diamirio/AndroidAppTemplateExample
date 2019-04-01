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
import com.tailoredapps.countriesexample.core.local.asCountryWithLanguages
import com.tailoredapps.countriesexample.core.local.asLanguageList
import com.tailoredapps.countriesexample.core.local.asLocalCountry
import com.tailoredapps.countriesexample.core.local.asLocalLanguageList
import com.tailoredapps.countriesexample.core.model.Country
import com.tailoredapps.countriesexample.core.remote.CountriesApi
import com.tailoredapps.countriesexample.core.local.CountriesDatabase
import com.tailoredapps.countriesexample.core.local.model.CountryLanguageJoin
import com.tailoredapps.countriesexample.core.local.model.LocalCountryWithFavorite
import com.tailoredapps.countriesexample.core.local.model.LocalFavoriteCountry
import com.tailoredapps.countriesexample.core.remote.model.RemoteCountry
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

interface CountriesRepo {
    val empty: Single<Boolean>
    val all: Flowable<List<Country>>
    val favorites: Flowable<List<Country>>

    fun getCountry(alpha2Code: String): Flowable<Country>
    fun refreshCountries(): Completable
    fun toggleFavorite(country: Country): Completable
}

class RetrofitRoomCountriesRepo(
    private val api: CountriesApi,
    private val db: CountriesDatabase
) : CountriesRepo {
    override val empty: Single<Boolean>
        get() = Single.fromCallable { db.countryDao().getNumberOfCountries() }
            .map { it == 0 }
            .subscribeOn(Schedulers.io())

    override val all: Flowable<List<Country>>
        get() = db.countryDao().getAll()
            .onBackpressureLatest()
            .flatMap(::combineWithLanguages)
            .subscribeOn(Schedulers.io())

    override val favorites: Flowable<List<Country>>
        get() = db.countryDao().getAllFavorites()
            .onBackpressureLatest()
            .flatMap(::combineWithLanguages)
            .subscribeOn(Schedulers.io())

    override fun getCountry(alpha2Code: String): Flowable<Country> =
        db.countryDao().get(alpha2Code)
            .flatMap(::combineWithLanguages)
            .subscribeOn(Schedulers.io())

    override fun refreshCountries(): Completable = api.all()
        .split()
        .flattenAsObservable { it }
        .flatMapCompletable(::updateOrInsertCountry)
        .subscribeOn(Schedulers.io())

    override fun toggleFavorite(country: Country): Completable =
        if (country.favorite) removeAsFavorite(country.alpha2Code)
        else setAsFavorite(country.alpha2Code)

    private fun setAsFavorite(alpha2Code: String): Completable = Completable
        .fromAction { db.favoriteDao().addFavorite(LocalFavoriteCountry(alpha2Code)) }
        .subscribeOn(Schedulers.io())

    private fun removeAsFavorite(alpha2Code: String): Completable = Completable
        .fromAction { db.favoriteDao().removeFavorite(LocalFavoriteCountry(alpha2Code)) }
        .subscribeOn(Schedulers.io())

    private fun updateOrInsertCountry(country: RemoteCountry): Completable = Completable
        .fromAction {
            // insert or update country
            db.countryDao().insertOrUpdate(country.asLocalCountry)

            // insert or update languages
            db.languageDao().insertOrUpdate(*country.languages.asLocalLanguageList.toTypedArray())

            // insert or update country language join
            country.languages.forEach { language ->
                db.countryLanguageDao().insertOrUpdate(CountryLanguageJoin(country.alpha2Code, language.name))
            }
        }
        .subscribeOn(Schedulers.io())

    private fun combineWithLanguages(countries: List<LocalCountryWithFavorite>): Flowable<List<Country>> =
        Flowable.fromIterable(countries)
            .flatMap(::combineWithLanguages)
            .toList()
            .toFlowable()

    private fun combineWithLanguages(country: LocalCountryWithFavorite): Flowable<Country> =
        db.countryLanguageDao()
            .getLanguagesByCountry(country.country.alpha2Code)
            .map { country.asCountryWithLanguages(it.asLanguageList) }
            .toFlowable()
}