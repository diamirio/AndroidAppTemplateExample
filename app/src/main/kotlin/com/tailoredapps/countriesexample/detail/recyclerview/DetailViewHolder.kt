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

import androidx.recyclerview.widget.RecyclerView
import com.tailoredapps.countriesexample.databinding.ItemDetailBinding

class DetailViewHolder(
    private val binding: ItemDetailBinding,
    private val interaction: ((DetailAdapterInteraction) -> Unit)
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: DetailAdapterItem) {
        binding.tvTitle.text = item.title
        binding.tvSubtitle.text = itemView.context.resources.getString(item.subtitle)
        binding.ivIndicator.setImageResource(item.icon)
        binding.root.setOnClickListener { item.interaction?.let(interaction) }
    }
}