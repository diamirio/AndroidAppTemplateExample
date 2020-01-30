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
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding3.swiperefreshlayout.refreshes
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.view.visibility
import com.tailoredapps.androidutil.async.Async
import com.tailoredapps.androidutil.ui.extensions.snack
import com.tailoredapps.countriesexample.all.CountryAdapter
import com.tailoredapps.countriesexample.all.CountryAdapterInteractionType
import com.tailoredapps.countriesexample.R
import com.tailoredapps.countriesexample.util.asCause
import com.tailoredapps.countriesexample.core.CountriesProvider
import com.tailoredapps.countriesexample.core.model.Country
import com.tailoredapps.countriesexample.main.liftsAppBarWith
import at.florianschuster.reaktor.android.koin.reactor
import com.tailoredapps.androidapptemplate.base.ui.BaseFragment
import com.tailoredapps.androidapptemplate.base.ui.BaseReactor
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
        Observable.merge(srlOverView.refreshes(), emptyLayout.btnLoad.clicks())
            .map { OverviewReactor.Action.Reload }
            .bind(to = reactor.action)
            .addTo(disposables)

        adapter.interaction.ofType<CountryAdapterInteractionType.FavoriteClick>()
            .map { it.country }
            .map { OverviewReactor.Action.ToggleFavorite(it) }
            .bind(to = reactor.action)
            .addTo(disposables)

        // state
        reactor.state.changesFrom { !it.hasCountriesAndNotLoading }
            .bind(to = emptyLayout.visibility())
            .addTo(disposables)

        reactor.state.changesFrom { it.countriesAsync }
            .bind { countryAsync ->
                srlOverView.isRefreshing = countryAsync is Async.Loading
                when (countryAsync) {
                    is Async.Success -> adapter.submitList(countryAsync.element)
                    is Async.Error -> errorSnack(countryAsync.error)
                }
            }
            .addTo(disposables)
    }

    private fun errorSnack(throwable: Throwable) {
        Timber.e(throwable)
        val message = throwable.asCause(R.string.overview_error_message).translation(resources)
        root.snack(message, Snackbar.LENGTH_INDEFINITE, getString(R.string.overview_error_retry)) {
            reactor.action.accept(OverviewReactor.Action.Reload)
        }
    }
}

class OverviewReactor(
    private val countriesProvider: CountriesProvider
) : BaseReactor<OverviewReactor.Action, OverviewReactor.Mutation, OverviewReactor.State>(State()) {

    sealed class Action {
        object Reload : Action()
        data class ToggleFavorite(val country: Country) : Action()
    }

    sealed class Mutation {
        data class SetCountries(val countries: Async<List<Country>>) : Mutation()
    }

    data class State(
        val hasCountriesAndNotLoading: Boolean = true,
        val countriesAsync: Async<List<Country>> = Async.Uninitialized
    )

    override fun transformMutation(mutation: Observable<Mutation>): Observable<out Mutation> {
        val storedCountriesMutation = countriesProvider
            .getCountries()
            .map { Mutation.SetCountries(Async.Success(it)) }
            .toObservable()
        return Observable.merge(mutation, storedCountriesMutation)
    }

    override fun mutate(action: Action): Observable<out Mutation> = when (action) {
        is Action.Reload -> {
            val startLoading = Observable.just(Mutation.SetCountries(Async.Loading))
            val refreshCountries = countriesProvider.refreshCountries()
                .toObservable<Mutation>()
                .onErrorReturn { Mutation.SetCountries(Async.Error(it)) }
            Observable.concat(startLoading, refreshCountries)
        }
        is Action.ToggleFavorite -> {
            Single.just(action.country)
                .flatMapCompletable(countriesProvider::toggleFavorite)
                .toObservable()
        }
    }

    override fun reduce(previousState: State, mutation: Mutation): State = when (mutation) {
        is Mutation.SetCountries -> previousState.copy(
            countriesAsync = mutation.countries,
            hasCountriesAndNotLoading = mutation.countries is Async.Loading ||
                    mutation.countries is Async.Success && mutation.countries.element.isNotEmpty()
        )
    }
}