package com.tailoredapps.countriesexample.detail

import androidx.lifecycle.viewModelScope
import at.florianschuster.control.Controller
import at.florianschuster.control.createController
import com.tailoredapps.androidapptemplate.base.ui.ControllerViewModel
import com.tailoredapps.countriesexample.core.CountriesProvider
import com.tailoredapps.countriesexample.core.model.Country
import kotlinx.coroutines.flow.flattenMerge
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class DetailViewModel(
    private val alpha2Code: String,
    private val countriesProvider: CountriesProvider
) : ControllerViewModel<Nothing, DetailViewModel.State>() {

    data class Mutation(val country: Country)

    data class State(val country: Country? = null)

    override val controller: Controller<Nothing, Mutation, State> = viewModelScope.createController(
        tag = "DetailViewModel",
        initialState = State(),
        mutationsTransformer = { mutations ->
            flowOf(
                mutations,
                countriesProvider.getCountry(alpha2Code).map { Mutation(it) }
            ).flattenMerge()
        },
        reducer = { previousState, mutation -> previousState.copy(country = mutation.country) }
    )
}
