/*
 * Copyright 2020 Tailored Media GmbH.
 * Created by Florian Schuster.
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
import androidx.navigation.fragment.findNavController
import at.florianschuster.control.bind
import com.tailoredapps.androidapptemplate.base.ui.viewBinding
import com.tailoredapps.countriesexample.R
import com.tailoredapps.countriesexample.all.CountryAdapter
import com.tailoredapps.countriesexample.all.CountryAdapterInteractionType
import com.tailoredapps.countriesexample.databinding.FragmentFavoritesBinding
import com.tailoredapps.countriesexample.liftsAppBarWith
import com.tailoredapps.countriesexample.removeLiftsAppBarWith
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class FavoritesView : Fragment(R.layout.fragment_favorites) {

    private val binding by viewBinding(FragmentFavoritesBinding::bind)
    private val navController by lazy(::findNavController)
    private val adapter by inject<CountryAdapter>()
    private val viewModel by viewModel<FavoritesViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvFavorites.adapter = adapter

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
            .bind(to = binding.emptyLayout.emptyLayout::isVisible::set)
            .launchIn(viewLifecycleOwner.lifecycleScope)
        binding.emptyLayout
    }

    override fun onStart() {
        super.onStart()
        liftsAppBarWith(binding.rvFavorites)
    }

    override fun onStop() {
        super.onStop()
        removeLiftsAppBarWith(binding.rvFavorites)
    }
}