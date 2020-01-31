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

package com.tailoredapps.countriesexample.favorites

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import at.florianschuster.control.bind
import com.tailoredapps.countriesexample.R
import com.tailoredapps.countriesexample.all.CountryAdapter
import com.tailoredapps.countriesexample.all.CountryAdapterInteractionType
import com.tailoredapps.countriesexample.main.liftsAppBarWith
import kotlinx.android.synthetic.main.fragment_favorites.*
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class FavoritesView : Fragment(R.layout.fragment_favorites) {
    private val viewModel: FavoritesViewModel by viewModel()
    private val navController: NavController by lazy(::findNavController)
    private val adapter: CountryAdapter by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvFavorites.adapter = adapter
        liftsAppBarWith(rvFavorites)

        adapter.interaction.filterIsInstance<CountryAdapterInteractionType.DetailClick>()
            .map { FavoritesViewDirections.actionFavoritesToDetail(it.id) }
            .bind(to = navController::navigate)
            .launchIn(viewLifecycleOwner.lifecycleScope)

        // action
        adapter.interaction.filterIsInstance<CountryAdapterInteractionType.FavoriteClick>()
            .map { FavoritesViewModel.Action.RemoveFavorite(it.country) }
            .bind(to = viewModel::dispatch)
            .launchIn(viewLifecycleOwner.lifecycleScope)

        // state
        viewModel.state.map { it.countries }
            .distinctUntilChanged()
            .bind(to = adapter::submitList)
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.state.map { it.countries.isEmpty() }
            .distinctUntilChanged()
            .bind(to = emptyLayout::isVisible::set)
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }
}