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

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_detail.view.*

class DetailViewHolder(
    override val containerView: View
) : RecyclerView.ViewHolder(containerView), LayoutContainer {

    fun bind(item: DetailAdapterItem, interaction: ((DetailAdapterInteraction) -> Unit)) {
        val resources = itemView.context.resources
        itemView.tvTitle.text = item.title
        itemView.tvSubtitle.text = resources.getString(item.subtitle)
        itemView.ivIndicator.setImageResource(item.icon)
        itemView.setOnClickListener { item.interaction?.let(interaction) }
    }
}