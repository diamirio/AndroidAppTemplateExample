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

package com.tailoredapps.countriesexample

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxrelay2.PublishRelay
import com.tailoredapps.androidutil.core.extensions.inflate
import com.tailoredapps.countriesexample.core.model.Country
import com.tailoredapps.countriesexample.util.source
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_country.view.*

sealed class CountryAdapterInteractionType {
    data class DetailClick(val id: String) : CountryAdapterInteractionType()
    data class FavoriteClick(val country: Country) : CountryAdapterInteractionType()
}

typealias CountryAdapterInteraction = (CountryAdapterInteractionType) -> Unit

class CountryAdapter : ListAdapter<Country, CountryViewHolder>(countryDiff) {
    val interaction = PublishRelay.create<CountryAdapterInteractionType>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountryViewHolder =
        CountryViewHolder(parent.inflate(R.layout.item_country))

    override fun onBindViewHolder(holder: CountryViewHolder, position: Int) =
        holder.bind(getItem(position), interaction::accept)
}

private val countryDiff: DiffUtil.ItemCallback<Country> = object : DiffUtil.ItemCallback<Country>() {
    override fun areItemsTheSame(oldItem: Country, newItem: Country): Boolean = oldItem.alpha2Code == newItem.alpha2Code
    override fun areContentsTheSame(oldItem: Country, newItem: Country): Boolean = oldItem == newItem
}

class CountryViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {
    fun bind(item: Country, interaction: CountryAdapterInteraction) {
        itemView.tvName.text = item.name
        itemView.ivFlag.source(R.drawable.ic_help_outline).accept(item.flagPngUrl)

        itemView.container.setOnClickListener {
            interaction(CountryAdapterInteractionType.DetailClick(item.alpha2Code))
        }
        itemView.btnFavorite.setOnClickListener {
            interaction(CountryAdapterInteractionType.FavoriteClick(item))
        }

        val favoriteRes = if (item.favorite) R.drawable.ic_favorite else R.drawable.ic_favorite_border
        itemView.btnFavorite.setImageResource(favoriteRes)
    }
}