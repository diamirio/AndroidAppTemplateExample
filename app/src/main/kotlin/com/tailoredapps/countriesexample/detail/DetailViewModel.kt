package com.tailoredapps.countriesexample.detail

import androidx.lifecycle.viewModelScope
import at.florianschuster.control.Controller
import at.florianschuster.control.Reducer
import at.florianschuster.control.Transformer
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
) : ControllerViewModel<DetailViewModel.Action, DetailViewModel.State>() {

    sealed class Action

    sealed class Mutation {
        data class SetCountry(val country: Country) : Mutation()
    }

    data class State(val country: Country? = null)

    override val controller: Controller<Action, Mutation, State> = viewModelScope.createController(
        initialState = State(),
        mutationsTransformer = Transformer { mutations ->
            flowOf(
                mutations,
                countriesProvider.getCountry(alpha2Code).map { Mutation.SetCountry(it) }
            ).flattenMerge()
        },
        reducer = Reducer { mutation, previousState ->
            when (mutation) {
                is Mutation.SetCountry -> previousState.copy(country = mutation.country)
            }
        }
    )
}
