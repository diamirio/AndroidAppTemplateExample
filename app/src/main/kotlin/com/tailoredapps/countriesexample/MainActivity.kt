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

package com.tailoredapps.countriesexample

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.tailoredapps.androidutil.ui.extensions.liftWith
import com.tailoredapps.androidutil.ui.extensions.removeLiftWith
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main_appbar.*

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private val navController: NavController by lazy { findNavController(R.id.navHost) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bnv.setupWithNavController(navController)
        toolbar.setupWithNavController(
            navController,
            AppBarConfiguration.Builder(R.id.overview, R.id.favorites).build()
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
    activity.appBar.setLiftable(true)
    activity.appBar.liftWith(view)
}

fun Fragment.removeLiftsAppBarWith(view: View) {
    val activity = activity as? MainActivity ?: return
    activity.appBar.setLiftable(true)
    activity.appBar.removeLiftWith(view)
}