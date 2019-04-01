package com.tailoredapps.countriesexample.core.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.tailoredapps.countriesexample.core.local.dao.CountryDao
import com.tailoredapps.countriesexample.core.local.dao.CountryLanguageDao
import com.tailoredapps.countriesexample.core.local.dao.FavoriteDao
import com.tailoredapps.countriesexample.core.local.dao.LanguageDao
import com.tailoredapps.countriesexample.core.local.model.CountryLanguageJoin
import com.tailoredapps.countriesexample.core.local.model.LocalCountry
import com.tailoredapps.countriesexample.core.local.model.LocalFavoriteCountry
import com.tailoredapps.countriesexample.core.local.model.LocalLanguage

/**
 * Created by Florian Schuster
 * florian.schuster@tailored-apps.com
 */

@Database(
        entities = [
            LocalCountry::class,
            LocalFavoriteCountry::class,
            LocalLanguage::class,
            CountryLanguageJoin::class
        ],
        version = CountriesDatabase.VERSION,
        exportSchema = true)
abstract class CountriesDatabase : RoomDatabase() {
    abstract fun countryDao(): CountryDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun languageDao(): LanguageDao
    abstract fun countryLanguageDao(): CountryLanguageDao

    companion object {
        const val VERSION = 1
        const val NAME = "countries_database"
    }
}
