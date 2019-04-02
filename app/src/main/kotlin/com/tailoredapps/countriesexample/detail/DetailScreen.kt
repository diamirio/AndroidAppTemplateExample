/*
 * Copyright 2019 Florian Schuster.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tailoredapps.countriesexample.detail

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import at.florianschuster.reaktor.ReactorView
import at.florianschuster.reaktor.android.bind
import at.florianschuster.reaktor.changesFrom
import com.jakewharton.rxbinding3.view.clicks
import com.tailoredapps.androidutil.core.IntentUtil
import com.tailoredapps.androidutil.core.extensions.RxDialogAction
import com.tailoredapps.androidutil.core.extensions.rxDialog
import com.tailoredapps.androidutil.optional.asOptional
import com.tailoredapps.androidutil.optional.filterSome
import com.tailoredapps.androidutil.optional.ofType
import com.tailoredapps.countriesexample.R
import com.tailoredapps.countriesexample.base.BaseReactor
import com.tailoredapps.countriesexample.base.BaseFragment
import com.tailoredapps.countriesexample.core.CountriesRepo
import com.tailoredapps.countriesexample.core.model.Country
import com.tailoredapps.countriesexample.main.liftsAppBarWith
import com.tailoredapps.countriesexample.util.source
import com.tailoredapps.reaktor.koin.reactor
import io.reactivex.Observable
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.fragment_detail.*
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import timber.log.Timber

class DetailFragment : BaseFragment(R.layout.fragment_detail), ReactorView<DetailReactor> {
    private val args: DetailFragmentArgs by navArgs()
    override val reactor: DetailReactor by reactor { parametersOf(args.alpha2code) }
    private val adapter: DetailAdapter by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bind(reactor)
    }

    override fun bind(reactor: DetailReactor) {
        rvDetail.adapter = adapter
        liftsAppBarWith(rvDetail)
        DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
            .apply { ContextCompat.getDrawable(requireContext(), R.drawable.bg_divider)?.let(::setDrawable) }
            .also(rvDetail::addItemDecoration)

        // action
        btnMore.clicks()
            .flatMapMaybe { url ->
                rxDialog {
                    titleResource = R.string.detail_dialog_title
                    message = getString(R.string.detail_dialog_message, url)
                    positiveButtonResource = R.string.detail_dialog_positive
                    negativeButtonResource = R.string.detail_dialog_negative
                }.ofType<RxDialogAction.Positive>()
            }
            .map { reactor.currentState.country.asOptional }
            .filterSome()
            .map { it.infoUrl }
            .bind(to = this::openChromeTab)
            .addTo(disposables)

        // state
        reactor.state.changesFrom { it.country.asOptional }
            .filterSome()
            .bind {
                ivFlag.source(R.drawable.ic_help_outline).accept(it.flagPngUrl)
                tvName.text = it.name
                adapter.submitCountry(it)
            }
            .addTo(disposables)
    }

    private fun openChromeTab(url: String) {
        if (!url.isEmpty()) {
            try {
                CustomTabsIntent.Builder().build().launchUrl(requireContext(), Uri.parse(url))
            } catch (throwable: Throwable) {
                Timber.i(throwable)
                startActivity(IntentUtil.web(url)) // fallback of chrome not installed -> open default browser
            }
        }
    }
}

class DetailReactor(
    private val alpha2Code: String,
    private val countriesRepo: CountriesRepo
) : BaseReactor<DetailReactor.Action, DetailReactor.Mutation, DetailReactor.State>(
    initialState = State(),
    initialAction = Action.InitialLoad
) {
    enum class Action {
        InitialLoad
    }

    sealed class Mutation {
        data class SetCountry(val country: Country) : Mutation()
    }

    data class State(
        val country: Country? = null
    )

    override fun mutate(action: Action): Observable<out Mutation> = when (action) {
        Action.InitialLoad -> {
            countriesRepo.getCountry(alpha2Code)
                .map { Mutation.SetCountry(it) }
                .toObservable()
        }
    }

    override fun reduce(previousState: State, mutation: Mutation): State = when (mutation) {
        is Mutation.SetCountry -> previousState.copy(country = mutation.country)
    }
}