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

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RemoteCountry(
    val name: String,
    val alpha2Code: String,
    val capital: String,
    val region: String,
    val subregion: String,
    val population: Long,
    val latlng: List<Double>,
    val area: Double?,
    val nativeName: String,
    val languages: List<Language>,
    val flag: String
) {

    @Serializable
    data class Language(
        @SerialName("iso639_1") val iso6391: String?,
        @SerialName("iso639_2") val iso6392: String,
        val name: String,
        val nativeName: String
    )
}
