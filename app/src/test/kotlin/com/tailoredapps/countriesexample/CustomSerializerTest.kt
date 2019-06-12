package com.tailoredapps.countriesexample

import com.tailoredapps.countriesexample.core.coreModule
import com.tailoredapps.countriesexample.core.remote.model.RemoteCountry
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.parse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.test.AutoCloseKoinTest
import org.koin.test.inject
import org.threeten.bp.ZoneOffset

@ImplicitReflectionSerializer
class CustomSerializerTest : AutoCloseKoinTest() {

    private val json: Json by inject()

    @Before
    fun before() {
        startKoin {
            modules(coreModule)
        }
    }

    @Test
    fun testDeserializeCountryWithTimezone() {
        val country = json.parse<RemoteCountry>(countryJson)

        assertEquals(ZoneOffset.of("+04:30"), country.timezones.first())
    }

    @Test
    fun testDeserializeCountryWithNullTimezone() {
        val countryWithNoTimezone = json.parse<RemoteCountry>(countryWithNoTimezoneJson)

        assertNull(countryWithNoTimezone.timezones.first())
    }

    companion object {

        private const val countryJson =
            "{\"name\":\"Afghanistan\",\"topLevelDomain\":[\".af\"],\"alpha2Code\":\"AF\",\"alpha3Code\":\"AFG\",\"callingCodes\":[\"93\"],\"capital\":\"Kabul\",\"altSpellings\":[\"AF\",\"Afġānistān\"],\"region\":\"Asia\",\"subregion\":\"Southern Asia\",\"population\":27657145,\"latlng\":[33,65],\"demonym\":\"Afghan\",\"area\":652230,\"gini\":27.8,\"timezones\":[\"UTC+04:30\"],\"borders\":[\"IRN\",\"PAK\",\"TKM\",\"UZB\",\"TJK\",\"CHN\"],\"nativeName\":\"افغانستان\",\"numericCode\":\"004\",\"currencies\":[{\"code\":\"AFN\",\"name\":\"Afghan afghani\",\"symbol\":\"؋\"}],\"languages\":[{\"iso639_1\":\"ps\",\"iso639_2\":\"pus\",\"name\":\"Pashto\",\"nativeName\":\"پښتو\"},{\"iso639_1\":\"uz\",\"iso639_2\":\"uzb\",\"name\":\"Uzbek\",\"nativeName\":\"Oʻzbek\"},{\"iso639_1\":\"tk\",\"iso639_2\":\"tuk\",\"name\":\"Turkmen\",\"nativeName\":\"Türkmen\"}],\"translations\":{\"de\":\"Afghanistan\",\"es\":\"Afganistán\",\"fr\":\"Afghanistan\",\"ja\":\"アフガニスタン\",\"it\":\"Afghanistan\",\"br\":\"Afeganistão\",\"pt\":\"Afeganistão\",\"nl\":\"Afghanistan\",\"hr\":\"Afganistan\",\"fa\":\"افغانستان\"},\"flag\":\"https://restcountries.eu/data/afg.svg\",\"regionalBlocs\":[{\"acronym\":\"SAARC\",\"name\":\"South Asian Association for Regional Cooperation\",\"otherAcronyms\":[],\"otherNames\":[]}],\"cioc\":\"AFG\"}"

        private const val countryWithNoTimezoneJson =
            "{\"name\":\"Afghanistan\",\"topLevelDomain\":[\".af\"],\"alpha2Code\":\"AF\",\"alpha3Code\":\"AFG\",\"callingCodes\":[\"93\"],\"capital\":\"Kabul\",\"altSpellings\":[\"AF\",\"Afġānistān\"],\"region\":\"Asia\",\"subregion\":\"Southern Asia\",\"population\":27657145,\"latlng\":[33,65],\"demonym\":\"Afghan\",\"area\":652230,\"gini\":27.8,\"timezones\":[\"\"],\"borders\":[\"IRN\",\"PAK\",\"TKM\",\"UZB\",\"TJK\",\"CHN\"],\"nativeName\":\"افغانستان\",\"numericCode\":\"004\",\"currencies\":[{\"code\":\"AFN\",\"name\":\"Afghan afghani\",\"symbol\":\"؋\"}],\"languages\":[{\"iso639_1\":\"ps\",\"iso639_2\":\"pus\",\"name\":\"Pashto\",\"nativeName\":\"پښتو\"},{\"iso639_1\":\"uz\",\"iso639_2\":\"uzb\",\"name\":\"Uzbek\",\"nativeName\":\"Oʻzbek\"},{\"iso639_1\":\"tk\",\"iso639_2\":\"tuk\",\"name\":\"Turkmen\",\"nativeName\":\"Türkmen\"}],\"translations\":{\"de\":\"Afghanistan\",\"es\":\"Afganistán\",\"fr\":\"Afghanistan\",\"ja\":\"アフガニスタン\",\"it\":\"Afghanistan\",\"br\":\"Afeganistão\",\"pt\":\"Afeganistão\",\"nl\":\"Afghanistan\",\"hr\":\"Afganistan\",\"fa\":\"افغانستان\"},\"flag\":\"https://restcountries.eu/data/afg.svg\",\"regionalBlocs\":[{\"acronym\":\"SAARC\",\"name\":\"South Asian Association for Regional Cooperation\",\"otherAcronyms\":[],\"otherNames\":[]}],\"cioc\":\"AFG\"}"
    }
}