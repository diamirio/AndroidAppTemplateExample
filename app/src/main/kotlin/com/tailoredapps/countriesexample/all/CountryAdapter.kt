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

package com.tailoredapps.countriesexample.all

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import com.tailoredapps.androidutil.ui.extensions.inflate
import com.tailoredapps.countriesexample.R
import com.tailoredapps.countriesexample.core.model.Country
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_country.view.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow

sealed class CountryAdapterInteractionType {
    data class DetailClick(val id: String) : CountryAdapterInteractionType()
    data class FavoriteClick(val country: Country) : CountryAdapterInteractionType()
}

class CountryAdapter : ListAdapter<Country, CountryViewHolder>(countryDiff) {
    private val _interaction: BroadcastChannel<CountryAdapterInteractionType> = BroadcastChannel(1)
    val interaction: Flow<CountryAdapterInteractionType> get() = _interaction.asFlow()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountryViewHolder =
        CountryViewHolder(parent.inflate(R.layout.item_country))

    override fun onBindViewHolder(holder: CountryViewHolder, position: Int) =
        holder.bind(getItem(position)) { _interaction.offer(it) }
}

private val countryDiff = object : DiffUtil.ItemCallback<Country>() {
    override fun areItemsTheSame(oldItem: Country, newItem: Country): Boolean =
        oldItem.alpha2Code == newItem.alpha2Code

    override fun areContentsTheSame(oldItem: Country, newItem: Country): Boolean =
        oldItem == newItem
}

class CountryViewHolder(
    override val containerView: View
) : RecyclerView.ViewHolder(containerView), LayoutContainer {

    fun bind(item: Country, interaction: (CountryAdapterInteractionType) -> Unit) {
        itemView.tvName.text = item.name
        itemView.ivFlag.load(item.flagPngUrl) {
            crossfade(200)
            error(R.drawable.ic_help_outline)
        }

        itemView.container.setOnClickListener {
            interaction(CountryAdapterInteractionType.DetailClick(item.alpha2Code))
        }
        itemView.btnFavorite.setOnClickListener {
            interaction(CountryAdapterInteractionType.FavoriteClick(item))
        }

        val favoriteRes =
            if (item.favorite) R.drawable.ic_favorite else R.drawable.ic_favorite_border
        itemView.btnFavorite.setImageResource(favoriteRes)
    }
}