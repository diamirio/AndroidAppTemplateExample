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

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.tailoredapps.androidapptemplate.base.ui.viewBinding
import com.tailoredapps.countriesexample.databinding.ItemDetailBinding
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow

sealed class DetailAdapterInteraction {
    data class LocationClick(val latLng: Pair<Double, Double>) : DetailAdapterInteraction()
    data class CapitalClick(val capital: String) : DetailAdapterInteraction()
}

class DetailAdapter : ListAdapter<DetailAdapterItem, DetailViewHolder>(detailAdapterItemDiff) {

    private val _interaction = BroadcastChannel<DetailAdapterInteraction>(BUFFERED)
    val interaction: Flow<DetailAdapterInteraction> = _interaction.asFlow()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DetailViewHolder = DetailViewHolder(
        parent.viewBinding { ItemDetailBinding.inflate(it, parent, false) }
    ) { _interaction.offer(it) }

    override fun onBindViewHolder(
        holder: DetailViewHolder,
        position: Int
    ) = holder.bind(getItem(position))
}

private val detailAdapterItemDiff = object : DiffUtil.ItemCallback<DetailAdapterItem>() {

    override fun areItemsTheSame(
        oldItem: DetailAdapterItem,
        newItem: DetailAdapterItem
    ): Boolean = oldItem.id == newItem.id

    override fun areContentsTheSame(
        oldItem: DetailAdapterItem,
        newItem: DetailAdapterItem
    ): Boolean = oldItem == newItem
}