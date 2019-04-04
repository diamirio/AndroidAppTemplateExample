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
import com.jakewharton.rxrelay2.PublishRelay
import com.tailoredapps.androidutil.ui.extensions.inflate
import com.tailoredapps.countriesexample.R

sealed class DetailAdapterInteraction {
    data class LocationClick(val latLng: Pair<Double, Double>) : DetailAdapterInteraction()
    data class CapitalClick(val capital: String) : DetailAdapterInteraction()
}

class DetailAdapter : ListAdapter<DetailAdapterItem, DetailViewHolder>(detailAdapterItemDiff) {
    val interaction: PublishRelay<DetailAdapterInteraction> = PublishRelay.create()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailViewHolder =
        DetailViewHolder(parent.inflate(R.layout.item_detail))

    override fun onBindViewHolder(holder: DetailViewHolder, position: Int) =
        holder.bind(getItem(position), interaction::accept)
}

private val detailAdapterItemDiff = object : DiffUtil.ItemCallback<DetailAdapterItem>() {
    override fun areItemsTheSame(oldItem: DetailAdapterItem, newItem: DetailAdapterItem): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: DetailAdapterItem, newItem: DetailAdapterItem): Boolean =
        oldItem == newItem
}