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
import at.florianschuster.reaktor.ReactorView
import at.florianschuster.reaktor.android.bind
import at.florianschuster.reaktor.changesFrom
import com.jakewharton.rxbinding3.view.visibility
import com.tailoredapps.countriesexample.all.CountryAdapter
import com.tailoredapps.countriesexample.all.CountryAdapterInteractionType
import com.tailoredapps.countriesexample.R
import com.tailoredapps.countriesexample.uibase.BaseFragment
import com.tailoredapps.countriesexample.uibase.BaseReactor
import com.tailoredapps.countriesexample.core.CountriesRepo
import com.tailoredapps.countriesexample.core.model.Country
import com.tailoredapps.countriesexample.main.liftsAppBarWith
import com.tailoredapps.reaktor.android.koin.reactor
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.ofType
import kotlinx.android.synthetic.main.fragment_favorites.*
import org.koin.android.ext.android.inject

class FavoritesFragment : BaseFragment(R.layout.fragment_favorites), ReactorView<FavoritesReactor> {
    override val reactor: FavoritesReactor by reactor()
    private val adapter: CountryAdapter by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bind(reactor)
    }

    override fun bind(reactor: FavoritesReactor) {
        rvFavorites.adapter = adapter
        liftsAppBarWith(rvFavorites)

        adapter.interaction.ofType<CountryAdapterInteractionType.DetailClick>()
            .map { FavoritesFragmentDirections.actionFavoritesToDetail(it.id) }
            .bind(to = navController::navigate)
            .addTo(disposables)

        // action
        adapter.interaction.ofType<CountryAdapterInteractionType.FavoriteClick>()
            .map { it.country }
            .map { FavoritesReactor.Action.RemoveFavorite(it) }
            .bind(to = reactor.action)
            .addTo(disposables)

        // state
        reactor.state.changesFrom { it.countries }
            .bind(to = adapter::submitList)
            .addTo(disposables)

        reactor.state.changesFrom { it.countries.isEmpty() }
            .bind(to = emptyLayout.visibility())
            .addTo(disposables)
    }
}

class FavoritesReactor(
    private val countriesRepo: CountriesRepo
) : BaseReactor<FavoritesReactor.Action, FavoritesReactor.Mutation, FavoritesReactor.State>(State()) {

    sealed class Action {
        data class RemoveFavorite(val country: Country) : Action()
    }

    sealed class Mutation {
        data class SetCountries(val countries: List<Country>) : Mutation()
    }

    data class State(
        val countries: List<Country> = emptyList()
    )

    override fun transformMutation(mutation: Observable<Mutation>): Observable<out Mutation> =
        Observable.merge(mutation, favoritesObservable)

    override fun mutate(action: Action): Observable<out Mutation> = when (action) {
        is Action.RemoveFavorite -> {
            Single.just(action.country)
                .flatMapCompletable(countriesRepo::toggleFavorite)
                .toObservable()
        }
    }

    override fun reduce(previousState: State, mutation: Mutation): State = when (mutation) {
        is Mutation.SetCountries -> previousState.copy(countries = mutation.countries)
    }

    private val favoritesObservable: Observable<out Mutation>
        get() = countriesRepo.allFavorites
            .map { Mutation.SetCountries(it) }
            .toObservable()
}