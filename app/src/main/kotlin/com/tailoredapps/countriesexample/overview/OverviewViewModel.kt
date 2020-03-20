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

package com.tailoredapps.countriesexample.overview

import androidx.lifecycle.viewModelScope
import at.florianschuster.control.Controller
import at.florianschuster.control.Mutator
import at.florianschuster.control.Reducer
import at.florianschuster.control.Transformer
import at.florianschuster.control.createController
import com.tailoredapps.androidapptemplate.base.ui.Async
import com.tailoredapps.androidapptemplate.base.ui.ControllerViewModel
import com.tailoredapps.androidapptemplate.base.ui.map
import com.tailoredapps.countriesexample.core.CountriesProvider
import com.tailoredapps.countriesexample.core.model.Country
import kotlinx.coroutines.flow.flattenMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import timber.log.Timber

class OverviewViewModel(
    private val countriesProvider: CountriesProvider
) : ControllerViewModel<OverviewViewModel.Action, OverviewViewModel.State>() {

    sealed class Action {
        object Reload : Action()
        data class ToggleFavorite(val country: Country) : Action()
    }

    sealed class Mutation {
        data class SetCountries(val countriesLoad: Async<List<Country>>) : Mutation()
    }

    data class State(
        val countries: List<Country> = emptyList(),
        val countriesLoad: Async<Unit> = Async.Uninitialized
    ) {
        val displayCountriesEmpty: Boolean get() = countriesLoad.complete && countries.isEmpty()
    }

    override val controller: Controller<Action, Mutation, State> = viewModelScope.createController(
        initialState = State(),
        mutationsTransformer = Transformer { mutations ->
            flowOf(
                mutations,
                countriesProvider.getCountries().map { Mutation.SetCountries(Async.Success(it)) }
            ).flattenMerge()
        },
        mutator = Mutator { action, _, _ ->
            when (action) {
                is Action.Reload -> flow {
                    emit(Mutation.SetCountries(Async.Loading))
                    try {
                        countriesProvider.refreshCountries()
                    } catch (throwable: Throwable) {
                        Timber.e(throwable)
                        emit(Mutation.SetCountries(Async.Error(throwable)))
                    }
                }
                is Action.ToggleFavorite -> flow { countriesProvider.toggleFavorite(action.country) }
            }
        },
        reducer = Reducer { mutation, previousState ->
            when (mutation) {
                is Mutation.SetCountries -> previousState.copy(
                    countries = mutation.countriesLoad() ?: previousState.countries,
                    countriesLoad = mutation.countriesLoad.map { Unit }
                )
            }
        }
    )
}