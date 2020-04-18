/*
 * Copyright 2020 Tailored Media GmbH.
 * Created by Florian Schuster.
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

package com.tailoredapps.countriesexample

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.tailoredapps.androidapptemplate.base.ui.viewBinding
import com.tailoredapps.androidutil.ui.extensions.liftWith
import com.tailoredapps.androidutil.ui.extensions.removeLiftWith
import com.tailoredapps.countriesexample.databinding.ActivityMainBinding
import org.koin.androidx.fragment.android.setupKoinFragmentFactory

class MainActivity : AppCompatActivity() {

    private val binding by viewBinding(ActivityMainBinding::inflate)
    private val navController by lazy { findNavController(R.id.navHost) }

    internal val appBar get() = binding.mainAppBar.mainAppBar

    override fun onCreate(savedInstanceState: Bundle?) {
        setupKoinFragmentFactory()
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.bnv.setupWithNavController(navController)
        binding.mainAppBar.toolbar.setupWithNavController(
            navController,
            AppBarConfiguration(setOf(R.id.overview, R.id.favorites))
        )
    }

    override fun onSupportNavigateUp(): Boolean = navController.navigateUp()

    override fun onBackPressed() {
        if (!navController.popBackStack()) {
            super.onBackPressed()
        }
    }
}

fun Fragment.liftsAppBarWith(view: View) {
    val activity = activity as? MainActivity ?: return
    with(activity.appBar) {
        setLiftable(true)
        liftWith(view)
    }
}

fun Fragment.removeLiftsAppBarWith(view: View) {
    val activity = activity as? MainActivity ?: return
    with(activity.appBar) {
        setLiftable(true)
        removeLiftWith(view)
    }
}