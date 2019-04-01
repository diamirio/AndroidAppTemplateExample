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

package com.tailoredapps.countriesexample.core.model

data class Country(
    val alpha2Code: String,
    val name: String,
    val flag: String,
    val location: Pair<Double, Double>?,
    val favorite: Boolean,
    val population: Long,
    val area: Double,
    val capital: String,
    val region: String,
    val subRegion: String,
    val nativeName: String,
    var languages: List<Language>
) {

    data class Language(
        val name: String,
        val nativeName: String
    )

    val flagPngUrl: String
        get() = "https://flagpedia.net/data/flags/normal/${alpha2Code.toLowerCase()}.png"

    val infoUrl: String
        get() = "https://de.wikipedia.org/wiki/$name"
}