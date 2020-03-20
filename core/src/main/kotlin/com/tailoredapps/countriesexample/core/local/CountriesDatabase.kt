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

package com.tailoredapps.countriesexample.core.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.tailoredapps.countriesexample.core.local.dao.CountryDao
import com.tailoredapps.countriesexample.core.local.dao.CountryLanguageDao
import com.tailoredapps.countriesexample.core.local.dao.FavoriteDao
import com.tailoredapps.countriesexample.core.local.dao.LanguageDao
import com.tailoredapps.countriesexample.core.local.model.CountryLanguageJoin
import com.tailoredapps.countriesexample.core.local.model.LocalCountry
import com.tailoredapps.countriesexample.core.local.model.LocalFavoriteCountry
import com.tailoredapps.countriesexample.core.local.model.LocalLanguage

@Database(
        entities = [
            LocalCountry::class,
            LocalFavoriteCountry::class,
            LocalLanguage::class,
            CountryLanguageJoin::class
        ],
        version = CountriesDatabase.VERSION,
        exportSchema = true)
abstract class CountriesDatabase : RoomDatabase() {
    abstract fun countryDao(): CountryDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun languageDao(): LanguageDao
    abstract fun countryLanguageDao(): CountryLanguageDao

    companion object {
        const val VERSION = 1
        const val NAME = "countries_database"
    }
}
