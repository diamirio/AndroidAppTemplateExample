/* Copyright 2018 Florian Schuster
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. */

package com.tailoredapps.countriesexample.core.remote.model

import com.google.gson.annotations.SerializedName

data class RemoteCountry(
    val name: String,
    val topLevelDomain: List<String>,
    val alpha2Code: String,
    val alpha3Code: String,
    val callingCodes: List<String>,
    val capital: String,
    val altSpellings: List<String>,
    val region: String,
    val subregion: String,
    val population: Long,
    val latlng: List<Double>,
    val demonym: String,
    val area: Double,
    val gini: Double,
    val timezones: List<String>,
    val borders: List<String>,
    val nativeName: String,
    val numericCode: String,
    val currencies: List<RemoteCurrency>,
    val languages: List<RemoteLanguage>,
    val translations: RemoteTranslations,
    val flag: String,
    val regionalBlocs: List<RemoteRegionalBloc>,
    val cioc: String
)

data class RemoteCurrency(
    val code: String,
    val name: String,
    val symbol: String
)

data class RemoteLanguage(
    @SerializedName("iso639_1") val iso6391: String,
    @SerializedName("iso639_2") val iso6392: String,
    val name: String,
    val nativeName: String
)

data class RemoteRegionalBloc(
    val acronym: String,
    val name: String,
    val otherAcronyms: List<String>,
    val otherNames: List<String>
)

data class RemoteTranslations(
    val de: String,
    val es: String,
    val fr: String,
    val ja: String,
    val it: String,
    val br: String,
    val pt: String
)
