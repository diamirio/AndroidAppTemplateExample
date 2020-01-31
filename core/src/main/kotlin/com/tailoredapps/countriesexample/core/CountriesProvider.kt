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

import com.tailoredapps.countriesexample.core.local.CountriesDatabase
import com.tailoredapps.countriesexample.core.local.asCountryWithLanguages
import com.tailoredapps.countriesexample.core.local.asLanguageList
import com.tailoredapps.countriesexample.core.local.asLocalCountry
import com.tailoredapps.countriesexample.core.local.asLocalLanguageList
import com.tailoredapps.countriesexample.core.local.model.CountryLanguageJoin
import com.tailoredapps.countriesexample.core.local.model.LocalCountryWithFavorite
import com.tailoredapps.countriesexample.core.local.model.LocalFavoriteCountry
import com.tailoredapps.countriesexample.core.model.Country
import com.tailoredapps.countriesexample.core.remote.CountriesApi
import com.tailoredapps.countriesexample.core.remote.model.RemoteCountry
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

interface CountriesProvider {
    suspend fun refreshCountries()

    fun getCountries(): Flow<List<Country>>
    fun getFavoriteCountries(): Flow<List<Country>>
    fun getCountry(alpha2Code: String): Flow<Country>

    suspend fun toggleFavorite(country: Country)
}

class RetrofitRoomCountriesProvider(
    private val countriesApi: CountriesApi,
    private val countriesDb: CountriesDatabase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : CountriesProvider {

    override suspend fun refreshCountries() = withContext(ioDispatcher) {
        val countries = countriesApi.all()
        countries.forEach { updateOrInsertCountry(it) }
    }

    override fun getCountries(): Flow<List<Country>> =
        countriesDb.countryDao().getAll()
            .conflate()
            .map { it.combineWithLanguages() }
            .flowOn(ioDispatcher)

    override fun getFavoriteCountries(): Flow<List<Country>> =
        countriesDb.countryDao().getAllFavorites()
            .conflate()
            .map { it.combineWithLanguages() }
            .flowOn(ioDispatcher)

    override fun getCountry(alpha2Code: String): Flow<Country> =
        countriesDb.countryDao().get(alpha2Code)
            .map { it.combineWithLanguages() }
            .flowOn(ioDispatcher)

    override suspend fun toggleFavorite(country: Country) =
        if (country.favorite) removeAsFavorite(country.alpha2Code)
        else setAsFavorite(country.alpha2Code)

    private suspend fun removeAsFavorite(alpha2Code: String) = withContext(ioDispatcher) {
        countriesDb.favoriteDao().removeFavorite(LocalFavoriteCountry(alpha2Code))
    }

    private suspend fun setAsFavorite(alpha2Code: String) = withContext(ioDispatcher) {
        countriesDb.favoriteDao().addFavorite(LocalFavoriteCountry(alpha2Code))
    }

    private suspend fun updateOrInsertCountry(country: RemoteCountry) =
        withContext(ioDispatcher) {
            // insert or update country
            countriesDb.countryDao().insertOrUpdate(country.asLocalCountry)

            // insert or update languages
            countriesDb.languageDao()
                .insertOrUpdate(*country.languages.asLocalLanguageList.toTypedArray())

            // insert or update country language join
            country.languages.forEach { language ->
                countriesDb.countryLanguageDao()
                    .insertOrUpdate(CountryLanguageJoin(country.alpha2Code, language.name))
            }
        }

    private suspend fun List<LocalCountryWithFavorite>.combineWithLanguages(): List<Country> =
        map { it.combineWithLanguages() }

    private suspend fun LocalCountryWithFavorite.combineWithLanguages(): Country {
        val localLanguages = countriesDb.countryLanguageDao()
            .getLanguagesByCountry(country.alpha2Code)
        return asCountryWithLanguages(localLanguages.asLanguageList)
    }
}