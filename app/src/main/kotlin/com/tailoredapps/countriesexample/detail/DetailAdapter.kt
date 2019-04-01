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

package com.tailoredapps.countriesexample.detail

import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import com.tailoredapps.androidutil.core.extensions.inflate
import com.tailoredapps.countriesexample.R
import com.tailoredapps.countriesexample.core.model.Country
import kotlinx.android.synthetic.main.item_detail.view.*
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

class DetailAdapter : RecyclerView.Adapter<DetailViewHolder>() {
    data class Item(@DrawableRes val icon: Int, val title: String, @StringRes val subtitle: Int)

    private var items = emptyList<Item>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    fun submitCountry(country: Country) {
        items = listOfNotNull(
            Item(R.drawable.ic_person_outline, country.population.formatThousands(), R.string.detail_population),
            Item(R.drawable.ic_adjust, country.capital, R.string.detail_capital),
            Item(R.drawable.ic_terrain, "${country.subRegion}, ${country.region}", R.string.detail_region),
            Item(R.drawable.ic_map, "${country.area.formatThousands()} km²", R.string.detail_area),
            country.languages.ifEmpty { null }?.let { languages ->
                Item(
                    R.drawable.ic_language,
                    languages.joinToString(", ") { "${it.name} (${it.nativeName})" },
                    R.string.detail_languages
                )
            },
            country.location?.let {
                Item(
                    R.drawable.ic_location_on,
                    "(${it.first.formatTwoDecimals()}, ${it.second.formatTwoDecimals()})",
                    R.string.detail_location
                )
            }
        )
    }

    override fun getItemCount(): Int =
        items.count()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailViewHolder =
        DetailViewHolder(parent.inflate(R.layout.item_detail))

    override fun onBindViewHolder(holder: DetailViewHolder, position: Int) =
        holder.bind(items[position])
}

class DetailViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(item: DetailAdapter.Item) {
        val resources = itemView.context.resources
        itemView.tvTitle.text = item.title
        itemView.tvSubtitle.text = resources.getString(item.subtitle)
        itemView.ivIndicator.setImageResource(item.icon)
    }
}

private fun Number.formatTwoDecimals(): String =
    DecimalFormat("#.##").apply { roundingMode = RoundingMode.CEILING }.format(this)

private fun Number.formatThousands(): String = NumberFormat.getNumberInstance(Locale.US).format(this)