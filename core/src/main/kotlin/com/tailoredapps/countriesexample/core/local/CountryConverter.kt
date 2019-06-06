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

package com.tailoredapps.countriesexample.core.local

import com.tailoredapps.countriesexample.core.model.Country
import com.tailoredapps.countriesexample.core.local.model.LocalCountry
import com.tailoredapps.countriesexample.core.local.model.LocalCountryWithFavorite
import com.tailoredapps.countriesexample.core.local.model.LocalLanguage
import com.tailoredapps.countriesexample.core.remote.model.RemoteCountry

val RemoteCountry.asLocalCountry: LocalCountry
    get() = LocalCountry(alpha2Code, name, flag, latLng.firstOrNull(), latLng.getOrNull(1), population, area, capital, region, subRegion, nativeName)

val RemoteCountry.Language.asLocalLanguage: LocalLanguage
    get() = LocalLanguage(name, nativeName)

val List<RemoteCountry.Language>.asLocalLanguageList: List<LocalLanguage>
    get() = map(RemoteCountry.Language::asLocalLanguage)

val LocalLanguage.asLanguage: Country.Language
    get() = Country.Language(name, nativeName)

val List<LocalLanguage>.asLanguageList: List<Country.Language>
    get() = map(LocalLanguage::asLanguage)

fun LocalCountryWithFavorite.asCountryWithLanguages(languages: List<Country.Language>): Country =
        Country(country.alpha2Code, country.name, country.flag, if (country.lat != null && country.lng != null) country.lat to country.lng else null, favoriteCountry != null, country.population, country.area, country.capital, country.region, country.subregion, country.nativeName, languages)
