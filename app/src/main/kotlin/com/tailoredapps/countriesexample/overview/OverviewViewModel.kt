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

package com.tailoredapps.countriesexample.overview

import at.florianschuster.control.Controller
import at.florianschuster.control.ControllerScope
import com.tailoredapps.androidapptemplate.base.ui.Async
import com.tailoredapps.androidapptemplate.base.ui.DelegateViewModel
import com.tailoredapps.countriesexample.core.CountriesProvider
import com.tailoredapps.countriesexample.core.model.Country
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flattenMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import timber.log.Timber

class OverviewViewModel(
    private val countriesProvider: CountriesProvider,
    scope: CoroutineScope = ControllerScope()
) : DelegateViewModel<OverviewViewModel.Action, OverviewViewModel.State>() {

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

    override val controller: Controller<Action, Mutation, State> = Controller(
        initialState = State(),
        mutationsTransformer = { mutations ->
            val storedCountries = countriesProvider.getCountries()
                .map { Mutation.SetCountries(Async.Success(it)) }
            flowOf(mutations, storedCountries).flattenMerge()
        },
        mutator = { action ->
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
        reducer = { previousState, mutation ->
            when (mutation) {
                is Mutation.SetCountries -> previousState.copy(
                    countries = mutation.countriesLoad() ?: previousState.countries,
                    countriesLoad = mutation.countriesLoad.map { Unit }
                )
            }
        },
        scope = scope
    )
}

fun <T, O> Async<T>.map(mapper: (T) -> O): Async<O> {
    return when (this) {
        is Async.Success -> Async.Success(mapper.invoke(this.element))
        is Async.Error -> Async.Error(error)
        is Async.Loading -> Async.Loading
        else -> Async.Uninitialized
    }
}