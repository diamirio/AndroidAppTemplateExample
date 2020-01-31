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

package com.tailoredapps.countriesexample.core.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.tailoredapps.countriesexample.core.local.model.LocalCountry
import com.tailoredapps.countriesexample.core.local.model.LocalCountryWithFavorite
import com.tailoredapps.countriesexample.core.local.model.LocalFavoriteCountry
import kotlinx.coroutines.flow.Flow

@Dao
interface CountryDao : BaseDao<LocalCountry> {

    @Query("SELECT * FROM country WHERE ${LocalCountry.PRIMARY_KEY} = :alpha2Code")
    fun get(alpha2Code: String): Flow<LocalCountryWithFavorite>

    @Query("SELECT * FROM country LEFT JOIN favorite ON ${LocalCountry.PRIMARY_KEY} = ${LocalFavoriteCountry.PRIMARY_KEY} ORDER BY name")
    fun getAll(): Flow<List<LocalCountryWithFavorite>>

    @Query("SELECT * FROM country INNER JOIN favorite ON ${LocalCountry.PRIMARY_KEY} = ${LocalFavoriteCountry.PRIMARY_KEY} ORDER BY name")
    fun getAllFavorites(): Flow<List<LocalCountryWithFavorite>>
}