package com.tailoredapps.countriesexample

import com.tailoredapps.androidutil.async.Async
import com.tailoredapps.countriesexample.core.CountriesRepo
import com.tailoredapps.countriesexample.core.model.Country
import com.tailoredapps.countriesexample.overview.OverviewReactor
import com.tailoredapps.countriesexample.overview.overviewModule
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.subjects.BehaviorSubject
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.inject

class OverviewReactorTest : AutoCloseKoinTest() {
    @get:Rule
    val rxSchedulersOverrideRule = RxSchedulersOverrideRule()

    private val reactor: OverviewReactor by inject()

    @MockK
    private lateinit var repo: CountriesRepo

    @Before
    fun before() {
        MockKAnnotations.init(this)
        startKoin {
            modules(overviewModule, module { single { repo } })
        }
    }

    @Test
    fun testLoadAllCountriesOnStart() {
        every { repo.allCountries } returns Flowable.just(listOf(mockCountry))

        reactor.state.subscribe()

        verify { repo.allCountries }
    }

    @Test
    fun testReloadActionTriggersRefreshCountriesCall() {
        every { repo.allCountries } returns Flowable.just(listOf(mockCountry))
        every { repo.refreshCountries() } returns Completable.complete()

        val reloadAction = OverviewReactor.Action.Reload

        reactor.state.subscribe()
        reactor.action.accept(reloadAction)

        verify { repo.refreshCountries() }
    }

    @Test
    fun testReloadActionSetsLoadingAndSuccessState() {
        val subject = BehaviorSubject.createDefault(listOf(mockCountry))

        every { repo.allCountries } returns subject.toFlowable(BackpressureStrategy.LATEST)
        every { repo.refreshCountries() } returns Completable.complete()

        val testObserver = reactor.state.test()

        reactor.action.accept(OverviewReactor.Action.Reload)

        subject.onNext(listOf(mockCountry, mockCountry))

        val stateValues = testObserver.values().map { it.countriesAsync }
        Assert.assertEquals(Async.Success(listOf(mockCountry)), stateValues[0])
        Assert.assertEquals(Async.Loading, stateValues[1])
        Assert.assertEquals(Async.Success(listOf(mockCountry, mockCountry)), stateValues[2])
        testObserver.assertValueCount(3)
    }

    @Test
    fun testReloadActionSetsErrorStateOnApiFailure() {
        val errorToThrow = Throwable()

        every { repo.allCountries } returns Flowable.just(listOf(mockCountry))
        every { repo.refreshCountries() } returns Completable.error(errorToThrow)

        val testObserver = reactor.state.test()

        reactor.action.accept(OverviewReactor.Action.Reload)

        val stateValues = testObserver.values().map { it.countriesAsync }
        Assert.assertEquals(Async.Success(listOf(mockCountry)), stateValues[0])
        Assert.assertEquals(Async.Loading, stateValues[1])
        Assert.assertEquals(Async.Error(errorToThrow), stateValues[2])
        testObserver.assertValueCount(3)
    }

    @Test
    fun testToggleFavoriteActionTriggersToggleFavoriteInRepo() {
        every { repo.allCountries } returns Flowable.just(listOf(mockCountry))

        reactor.state.subscribe()
        reactor.action.accept(OverviewReactor.Action.ToggleFavorite(mockCountry))

        verify { repo.toggleFavorite(mockCountry) }
    }

    @Test
    fun testToggleFavoriteActionSetsSuccessStateWithToggledFavorite() {
        val initialMockCountry = mockCountry
        val favoriteToggledMockCountry = initialMockCountry.copy(favorite = !initialMockCountry.favorite)

        val subject = BehaviorSubject.createDefault(listOf(initialMockCountry))

        every { repo.allCountries } returns subject.toFlowable(BackpressureStrategy.LATEST)

        val testObserver = reactor.state.test()

        reactor.action.accept(OverviewReactor.Action.ToggleFavorite(initialMockCountry))

        subject.onNext(listOf(favoriteToggledMockCountry))

        val stateValues = testObserver.values().map { it.countriesAsync }
        Assert.assertEquals(Async.Success(listOf(initialMockCountry)), stateValues[0])
        Assert.assertEquals(Async.Success(listOf(favoriteToggledMockCountry)), stateValues[1])
        testObserver.assertValueCount(2)
    }

    companion object {
        val mockCountry = Country("", "", "", null, false, 0, 0.0, "", "", "", "", emptyList())
    }
}