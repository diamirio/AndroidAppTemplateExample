package com.tailoredapps.countriesexample

import com.tailoredapps.androidapptemplate.base.ui.Async
import com.tailoredapps.countriesexample.core.CountriesProvider
import com.tailoredapps.countriesexample.core.model.Country
import com.tailoredapps.countriesexample.overview.OverviewViewModel
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

internal class OverviewViewModelTest {

    @get:Rule
    private val testScopeRule = TestCoroutineScopeRule()

    private lateinit var viewModel: OverviewViewModel

    private val provider: CountriesProvider = mockk()

    @Before
    fun before() {
        viewModel = OverviewViewModel(provider, testScopeRule)
    }

    @Test
    fun testLoadAllCountriesOnStart() {
        every { provider.getCountries() } returns flowOf(listOf(mockCountry))

        viewModel.state

        verify(exactly = 1) { provider.getCountries() }
    }

    @Test
    fun testReloadActionTriggersRefreshCountriesCall() {
        every { provider.getCountries() } returns flowOf(listOf(mockCountry))
        coEvery { provider.refreshCountries() } just Runs

        viewModel.dispatch(OverviewViewModel.Action.Reload)

        coVerify { provider.refreshCountries() }
    }

    @Test
    fun testReloadActionSetsLoadingAndSuccessState() {
        val channel = ConflatedBroadcastChannel(listOf(mockCountry))

        every { provider.getCountries() } returns channel.asFlow()
        coEvery { provider.refreshCountries() } just Runs

        val states = viewModel.state.testIn(testScopeRule)

        viewModel.dispatch(OverviewViewModel.Action.Reload)

        channel.offer(listOf(mockCountry, mockCountry))

        val stateLoads = states.map { it.countriesLoad }
        assertEquals(Async.Success(Unit), stateLoads[0])
        assertEquals(Async.Loading, stateLoads[1])
        assertEquals(Async.Success(Unit), stateLoads[2])

        val stateCountries = states.map { it.countries }
        assertEquals(listOf(mockCountry), stateCountries[0])
        assertEquals(listOf(mockCountry), stateCountries[1])
        assertEquals(listOf(mockCountry, mockCountry), stateCountries[2])

        assertEquals(3, states.count())
    }

    @Test
    fun testReloadActionSetsErrorStateOnApiFailure() {
        val errorToThrow = Throwable()

        every { provider.getCountries() } returns flowOf(listOf(mockCountry))
        coEvery { provider.refreshCountries() } just Runs

        val states = viewModel.state.testIn(testScopeRule)

        viewModel.dispatch(OverviewViewModel.Action.Reload)

        val stateLoads = states.map { it.countriesLoad }
        assertEquals(Async.Success(Unit), stateLoads[0])
        assertEquals(Async.Loading, stateLoads[1])
        assertEquals(Async.Error(errorToThrow), stateLoads[2])

        val stateCountries = states.map { it.countries }
        assertEquals(listOf(mockCountry), stateCountries[0])
        assertEquals(listOf(mockCountry), stateCountries[1])
        assertEquals(listOf(mockCountry), stateCountries[2])

        assertEquals(3, states.count())
    }

    @Test
    fun testToggleFavoriteActionTriggersToggleFavoriteInRepo() {
        every { provider.getCountries() } returns flowOf(listOf(mockCountry))

        viewModel.dispatch(OverviewViewModel.Action.ToggleFavorite(mockCountry))

        coVerify { provider.toggleFavorite(mockCountry) }
    }

    @Test
    fun testToggleFavoriteActionSetsSuccessStateWithToggledFavorite() {
        val initialMockCountry = mockCountry
        val favoriteToggledMockCountry =
            initialMockCountry.copy(favorite = !initialMockCountry.favorite)

        val channel = ConflatedBroadcastChannel(listOf(initialMockCountry))

        every { provider.getCountries() } returns channel.asFlow()

        val states = viewModel.state.testIn(testScopeRule)

        viewModel.dispatch(OverviewViewModel.Action.ToggleFavorite(initialMockCountry))

        channel.offer(listOf(favoriteToggledMockCountry))

        val stateCountries = states.map { it.countries }
        assertEquals(listOf(initialMockCountry), stateCountries[0])
        assertEquals(listOf(favoriteToggledMockCountry), stateCountries[1])
        assertEquals(2, stateCountries.count())
    }

    companion object {
        val mockCountry = Country(
            "", "", "", null,
            false, 0, 0.0, "",
            "", "", "", emptyList()
        )
    }
}

private fun <T> Flow<T>.testIn(scope: TestCoroutineScope): List<T> {
    val states = mutableListOf<T>()
    scope.launch { toList(states) }
    return states
}