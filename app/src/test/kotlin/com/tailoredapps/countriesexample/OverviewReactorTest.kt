package com.tailoredapps.countriesexample

import com.tailoredapps.androidapptemplate.RxSchedulersOverrideRule
import com.tailoredapps.androidutil.async.Async
import com.tailoredapps.countriesexample.core.CountriesProvider
import com.tailoredapps.countriesexample.core.model.Country
import com.tailoredapps.countriesexample.overview.OverviewReactor
import com.tailoredapps.countriesexample.overview.overviewModule
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.subjects.BehaviorSubject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.inject
import kotlin.test.assertEquals

class OverviewReactorTest : AutoCloseKoinTest() {

    @get:Rule
    val rxSchedulersOverrideRule = RxSchedulersOverrideRule()

    private val reactor: OverviewReactor by inject()

    private val provider: CountriesProvider = mockk()

    @Before
    fun before() {
        MockKAnnotations.init(this)
        startKoin {
            modules(overviewModule + module { single { provider } })
        }
    }

    @Test
    fun testLoadAllCountriesOnStart() {
        every { provider.getCountries() } returns Flowable.just(listOf(mockCountry))

        reactor.state.subscribe()

        verify { provider.getCountries() }
    }

    @Test
    fun testReloadActionTriggersRefreshCountriesCall() {
        every { provider.getCountries() } returns Flowable.just(listOf(mockCountry))
        every { provider.refreshCountries() } returns Completable.complete()

        val reloadAction = OverviewReactor.Action.Reload

        reactor.state.subscribe()
        reactor.action.accept(reloadAction)

        verify { provider.refreshCountries() }
    }

    @Test
    fun testReloadActionSetsLoadingAndSuccessState() {
        val subject = BehaviorSubject.createDefault(listOf(mockCountry))

        every { provider.getCountries() } returns subject.toFlowable(BackpressureStrategy.LATEST)
        every { provider.refreshCountries() } returns Completable.complete()

        val testObserver = reactor.state.test()

        reactor.action.accept(OverviewReactor.Action.Reload)

        subject.onNext(listOf(mockCountry, mockCountry))

        val stateValues = testObserver.values().map { it.countriesAsync }
        assertEquals(Async.Success(listOf(mockCountry)), stateValues[0])
        assertEquals(Async.Loading, stateValues[1])
        assertEquals(Async.Success(listOf(mockCountry, mockCountry)), stateValues[2])
        testObserver.assertValueCount(3)
    }

    @Test
    fun testReloadActionSetsErrorStateOnApiFailure() {
        val errorToThrow = Throwable()

        every { provider.getCountries() } returns Flowable.just(listOf(mockCountry))
        every { provider.refreshCountries() } returns Completable.error(errorToThrow)

        val testObserver = reactor.state.test()

        reactor.action.accept(OverviewReactor.Action.Reload)

        val stateValues = testObserver.values().map { it.countriesAsync }
        assertEquals(Async.Success(listOf(mockCountry)), stateValues[0])
        assertEquals(Async.Loading, stateValues[1])
        assertEquals(Async.Error(errorToThrow), stateValues[2])
        testObserver.assertValueCount(3)
    }

    @Test
    fun testToggleFavoriteActionTriggersToggleFavoriteInRepo() {
        every { provider.getCountries() } returns Flowable.just(listOf(mockCountry))

        reactor.state.subscribe()
        reactor.action.accept(OverviewReactor.Action.ToggleFavorite(mockCountry))

        verify { provider.toggleFavorite(mockCountry) }
    }

    @Test
    fun testToggleFavoriteActionSetsSuccessStateWithToggledFavorite() {
        val initialMockCountry = mockCountry
        val favoriteToggledMockCountry =
            initialMockCountry.copy(favorite = !initialMockCountry.favorite)

        val subject = BehaviorSubject.createDefault(listOf(initialMockCountry))

        every { provider.getCountries() } returns subject.toFlowable(BackpressureStrategy.LATEST)

        val testObserver = reactor.state.test()

        reactor.action.accept(OverviewReactor.Action.ToggleFavorite(initialMockCountry))

        subject.onNext(listOf(favoriteToggledMockCountry))

        val stateValues = testObserver.values().map { it.countriesAsync }
        assertEquals(Async.Success(listOf(initialMockCountry)), stateValues[0])
        assertEquals(Async.Success(listOf(favoriteToggledMockCountry)), stateValues[1])
        testObserver.assertValueCount(2)
    }

    companion object {
        val mockCountry = Country("", "", "", null, false, 0, 0.0, "", "", "", "", emptyList())
    }
}