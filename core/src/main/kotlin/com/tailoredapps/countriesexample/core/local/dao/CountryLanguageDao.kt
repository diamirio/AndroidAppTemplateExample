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
import com.tailoredapps.countriesexample.core.local.model.CountryLanguageJoin
import com.tailoredapps.countriesexample.core.local.model.LocalLanguage

@Dao
interface CountryLanguageDao : BaseDao<CountryLanguageJoin> {

    @Query("""SELECT * FROM language
        JOIN country_language_join ON l_name = fk_language
        WHERE  fk_country = :alpha2Code""")
    suspend fun getLanguagesByCountry(alpha2Code: String): List<LocalLanguage>
}