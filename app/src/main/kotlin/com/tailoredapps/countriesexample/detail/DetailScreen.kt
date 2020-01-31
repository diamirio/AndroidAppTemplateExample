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
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import at.florianschuster.control.Controller
import at.florianschuster.control.bind
import coil.api.load
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tailoredapps.androidapptemplate.base.ui.DelegateViewModel
import com.tailoredapps.androidutil.ui.IntentUtil
import com.tailoredapps.countriesexample.R
import com.tailoredapps.countriesexample.core.CountriesProvider
import com.tailoredapps.countriesexample.core.model.Country
import com.tailoredapps.countriesexample.detail.recyclerview.DetailAdapter
import com.tailoredapps.countriesexample.detail.recyclerview.DetailAdapterInteraction
import com.tailoredapps.countriesexample.detail.recyclerview.convertToDetailAdapterItems
import com.tailoredapps.countriesexample.main.liftsAppBarWith
import kotlinx.android.synthetic.main.fragment_detail.*
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flattenMerge
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import reactivecircus.flowbinding.android.view.clicks
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class DetailFragment : Fragment(R.layout.fragment_detail) {

    private val args: DetailFragmentArgs by navArgs()
    private val viewModel: DetailViewModel by viewModel { parametersOf(args.alpha2code) }
    private val adapter: DetailAdapter by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvDetail.adapter = adapter
        liftsAppBarWith(rvDetail)
        DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
            .apply {
                ContextCompat.getDrawable(requireContext(), R.drawable.bg_divider)
                    ?.let(::setDrawable)
            }
            .also(rvDetail::addItemDecoration)

        val locationFlow = adapter.interaction
            .filterIsInstance<DetailAdapterInteraction.LocationClick>()
            .map { "${it.latLng.first}, ${it.latLng.second}" }

        val capitalFlow = adapter.interaction
            .filterIsInstance<DetailAdapterInteraction.CapitalClick>()
            .map { it.capital }

        flowOf(locationFlow, capitalFlow)
            .flattenMerge()
            .map { IntentUtil.maps(it) }
            .bind(to = requireContext()::startActivity)
            .launchIn(viewLifecycleOwner.lifecycleScope)

        // action
        btnMore.clicks()
            .map { viewModel.currentState.country }
            .filterNotNull()
            .map { it.infoUrl }
            .map { url ->
                val answer = yesNoDialog(
                    title = getString(R.string.detail_dialog_title),
                    message = getString(R.string.detail_dialog_message, url),
                    positiveButtonText = getString(R.string.detail_dialog_positive),
                    negativeButtonText = getString(R.string.detail_dialog_negative)
                )
                if (answer) url else null
            }
            .filterNotNull()
            .bind(to = this::openChromeTab)
            .launchIn(viewLifecycleOwner.lifecycleScope)

        // state
        viewModel.state.map { it.country }
            .distinctUntilChanged()
            .filterNotNull()
            .bind { country ->
                ivFlag.load(country.flagPngUrl) {
                    crossfade(200)
                    error(R.drawable.ic_help_outline)
                }
                tvName.text = country.name
                adapter.submitList(country.convertToDetailAdapterItems())
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun openChromeTab(url: String) {
        if (url.isEmpty()) return

        try {
            CustomTabsIntent.Builder().build().launchUrl(requireContext(), Uri.parse(url))
        } catch (throwable: Throwable) {
            Timber.i(throwable)
            startActivity(IntentUtil.web(url)) // fallback of chrome not installed -> open default browser
        }
    }

    private suspend fun Fragment.yesNoDialog(
        title: String,
        message: String,
        positiveButtonText: String,
        negativeButtonText: String
    ): Boolean = suspendCoroutine { continuation ->
        MaterialAlertDialogBuilder(requireContext()).apply {
            setTitle(title)
            setMessage(message)
            setPositiveButton(positiveButtonText) { _, _ -> continuation.resume(true) }
            setNegativeButton(negativeButtonText) { _, _ -> continuation.resume(false) }
        }.show()
    }
}

class DetailViewModel(
    private val alpha2Code: String,
    private val countriesProvider: CountriesProvider
) : DelegateViewModel<Nothing, DetailViewModel.State>() {

    data class Mutation(val country: Country)

    data class State(val country: Country? = null)

    override val controller: Controller<Nothing, Mutation, State> = Controller(
        initialState = State(),
        mutationsTransformer = { mutations ->
            flowOf(
                mutations,
                countriesProvider.getCountry(alpha2Code).map { Mutation(it) }
            ).flattenMerge()
        },
        reducer = { previousState, mutation ->
            previousState.copy(country = mutation.country)
        }
    )
}
