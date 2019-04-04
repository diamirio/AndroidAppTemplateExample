/* Copyright 2018 Tailored Media GmbH
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
 * limitations under the License.*/

package com.tailoredapps.countriesexample.detail.recyclerview

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.tailoredapps.countriesexample.R
import com.tailoredapps.countriesexample.core.model.Country
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

data class DetailAdapterItem(
    @DrawableRes val icon: Int,
    val title: String,
    @StringRes val subtitle: Int,
    val interaction: DetailAdapterInteraction? = null,
    val id: String = "$icon,$title,$subtitle"
)

fun Country.convertToDetailAdapterItems(): List<DetailAdapterItem> {
    return listOfNotNull(
        DetailAdapterItem(R.drawable.ic_person_outline, population.formatThousands(), R.string.detail_tv_population),
        DetailAdapterItem(
            R.drawable.ic_adjust,
            capital,
            R.string.detail_tv_capital,
            DetailAdapterInteraction.CapitalClick(capital)
        ),
        DetailAdapterItem(R.drawable.ic_terrain, "$subRegion, $region", R.string.detail_tv_region),
        DetailAdapterItem(R.drawable.ic_map, "${area.formatThousands()} km²", R.string.detail_tv_area),
        languages.ifEmpty { null }?.let { languages ->
            DetailAdapterItem(
                R.drawable.ic_language,
                languages.joinToString(", ") { "${it.name} (${it.nativeName})" },
                R.string.detail_tv_languages
            )
        },
        location?.let {
            DetailAdapterItem(
                R.drawable.ic_location_on,
                "(${it.first.formatTwoDecimals()}, ${it.second.formatTwoDecimals()})",
                R.string.detail_tv_location,
                DetailAdapterInteraction.LocationClick(it)
            )
        }
    )
}

private fun Number.formatTwoDecimals(): String =
    DecimalFormat("#.##").apply { roundingMode = RoundingMode.CEILING }.format(this)

private fun Number.formatThousands(): String =
    NumberFormat.getNumberInstance(Locale.US).format(this)