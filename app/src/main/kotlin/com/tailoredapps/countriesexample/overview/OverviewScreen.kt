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

import android.os.Bundle
import android.view.View
import at.florianschuster.reaktor.ReactorView
import at.florianschuster.reaktor.android.bind
import at.florianschuster.reaktor.changesFrom
import com.jakewharton.rxbinding3.swiperefreshlayout.refreshes
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.view.visibility
import com.tailoredapps.androidutil.async.Async
import com.tailoredapps.androidutil.ui.extensions.snack
import com.tailoredapps.countriesexample.CountryAdapter
import com.tailoredapps.countriesexample.CountryAdapterInteractionType
import com.tailoredapps.countriesexample.R
import com.tailoredapps.countriesexample.base.BaseFragment
import com.tailoredapps.countriesexample.base.BaseReactor
import com.tailoredapps.countriesexample.core.CountriesRepo
import com.tailoredapps.countriesexample.core.model.Country
import com.tailoredapps.countriesexample.main.liftsAppBarWith
import com.tailoredapps.reaktor.android.koin.reactor
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.ofType
import kotlinx.android.synthetic.main.fragment_overview.*
import kotlinx.android.synthetic.main.fragment_overview_empty.view.*
import org.koin.android.ext.android.inject
import timber.log.Timber

class OverviewFragment : BaseFragment(R.layout.fragment_overview), ReactorView<OverviewReactor> {
    override val reactor: OverviewReactor by reactor()
    private val adapter: CountryAdapter by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bind(reactor)
    }

    override fun bind(reactor: OverviewReactor) {
        rvOverView.adapter = adapter
        liftsAppBarWith(rvOverView)

        adapter.interaction.ofType<CountryAdapterInteractionType.DetailClick>()
            .map { OverviewFragmentDirections.actionOverviewToDetail(it.id) }
            .bind(to = navController::navigate)
            .addTo(disposables)

        // action
        emptyLayout.btnLoad.clicks()
            .map { OverviewReactor.Action.Reload }
            .bind(to = reactor.action)
            .addTo(disposables)

        adapter.interaction.ofType<CountryAdapterInteractionType.FavoriteClick>()
            .map { it.country }
            .map { OverviewReactor.Action.ToggleFavorite(it) }
            .bind(to = reactor.action)
            .addTo(disposables)

        swipeRefresh.refreshes()
            .map { OverviewReactor.Action.Reload }
            .bind(to = reactor.action)
            .addTo(disposables)

        // state
        reactor.state.changesFrom { it.hasCountries }
            .bind(to = emptyLayout.visibility())
            .addTo(disposables)

        reactor.state.changesFrom { it.countriesAsync }
            .bind {
                swipeRefresh.isRefreshing = it is Async.Loading
                when (it) {
                    is Async.Success -> adapter.submitList(it.element)
                    is Async.Error -> errorSnack(it.error)
                }
            }
            .addTo(disposables)
    }

    private fun errorSnack(throwable: Throwable) {
        Timber.e(throwable)
        root.snack(R.string.overview_error_message, R.string.overview_error_retry) {
            reactor.action.accept(OverviewReactor.Action.Reload)
        }
    }
}

class OverviewReactor(
    private val countriesRepo: CountriesRepo
) : BaseReactor<OverviewReactor.Action, OverviewReactor.Mutation, OverviewReactor.State>(State()) {

    sealed class Action {
        object Reload : Action()
        data class ToggleFavorite(val country: Country) : Action()
    }

    sealed class Mutation {
        data class SetCountries(val countries: Async<List<Country>>) : Mutation()
    }

    data class State(
        val hasCountries: Boolean = false,
        val countriesAsync: Async<List<Country>> = Async.Uninitialized
    )

    override fun transformMutation(mutation: Observable<Mutation>): Observable<out Mutation> =
        Observable.merge(mutation, storedCountriesMutation)

    override fun mutate(action: Action): Observable<out Mutation> = when (action) {
        is Action.Reload -> {
            val startLoading = Observable.just(Mutation.SetCountries(Async.Loading))
            val refreshCountries = countriesRepo.refreshCountries()
                .toObservable<Mutation>()
                .onErrorReturn { Mutation.SetCountries(Async.Error(it)) }
            Observable.concat(startLoading, refreshCountries)
        }
        is Action.ToggleFavorite -> {
            Single.just(action.country)
                .flatMapCompletable(countriesRepo::toggleFavorite)
                .toObservable()
        }
    }

    override fun reduce(previousState: State, mutation: Mutation): State = when (mutation) {
        is Mutation.SetCountries -> previousState.copy(
            countriesAsync = mutation.countries,
            hasCountries = mutation.countries !is Async.Loading && mutation.countries()?.isEmpty() == true
        )
    }

    private val storedCountriesMutation: Observable<out Mutation>
        get() = countriesRepo.all
            .map { Mutation.SetCountries(Async.Success(it)) }
            .toObservable()
}