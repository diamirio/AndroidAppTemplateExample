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

package com.tailoredapps.countriesexample.core.local.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Embedded
import androidx.room.Index

@Entity(tableName = "country")
data class LocalCountry(
    @PrimaryKey
    @ColumnInfo(name = PRIMARY_KEY)
    val alpha2Code: String,
    val name: String,
    val flag: String,
    val lat: Double?,
    val lng: Double?,
    val population: Long,
    val area: Double?,
    val capital: String,
    val region: String,
    val subregion: String,
    val nativeName: String
) {
    companion object {
        const val PRIMARY_KEY = "c_alpha2Code"
    }
}

@Entity(tableName = "language")
data class LocalLanguage(
    @PrimaryKey
    @ColumnInfo(name = PRIMARY_KEY)
    val name: String,
    val nativeName: String
) {
    companion object {
        const val PRIMARY_KEY = "l_name"
    }
}

@Entity(tableName = "country_language_join",
        primaryKeys = [CountryLanguageJoin.FK_LANGUAGE, CountryLanguageJoin.FK_COUNTRY],
        foreignKeys = [
            ForeignKey(
                    entity = LocalCountry::class,
                    parentColumns = [LocalCountry.PRIMARY_KEY],
                    childColumns = [CountryLanguageJoin.FK_COUNTRY]
            ),
            ForeignKey(
                    entity = LocalLanguage::class,
                    parentColumns = [LocalLanguage.PRIMARY_KEY],
                    childColumns = [CountryLanguageJoin.FK_LANGUAGE]
            )
        ],
        indices = [Index(CountryLanguageJoin.FK_COUNTRY), Index(CountryLanguageJoin.FK_LANGUAGE)])
data class CountryLanguageJoin(
    @ColumnInfo(name = FK_COUNTRY)
    val alpha2Code: String,
    @ColumnInfo(name = FK_LANGUAGE)
    val name: String
) {
    companion object {
        const val FK_COUNTRY = "fk_country"
        const val FK_LANGUAGE = "fk_language"
    }
}

@Entity(tableName = "favorite")
data class LocalFavoriteCountry(
    @PrimaryKey
    @ColumnInfo(name = PRIMARY_KEY)
    val alpha2Code: String
) {
    companion object {
        const val PRIMARY_KEY = "f_alpha2Code"
    }
}

data class LocalCountryWithFavorite(
    @Embedded
    val country: LocalCountry,
    @Embedded
    val favoriteCountry: LocalFavoriteCountry?
)
