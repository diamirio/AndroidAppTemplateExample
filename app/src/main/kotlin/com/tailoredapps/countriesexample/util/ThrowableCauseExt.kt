/*
 * Copyright 2019 Florian Schuster. All rights reserved.
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

package com.tailoredapps.countriesexample.util

import android.content.res.Resources
import androidx.annotation.StringRes
import com.tailoredapps.countriesexample.R
import retrofit2.HttpException

fun Throwable.asCause(@StringRes customOnOtherStringResource: Int? = null): Cause {
    return Cause.fromThrowable(this, customOnOtherStringResource)
}

sealed class Cause(@StringRes val default: Int = R.string.error_other) {
    data class Other(private val otherRes: Int?) : Cause() {
        override fun translation(resources: Resources): String = when {
            otherRes != null -> resources.getString(otherRes)
            else -> super.translation(resources)
        }
    }

    class Http(private val code: Int) : Cause() {
        override fun translation(resources: Resources): String = when (code) {
            401 -> resources.getString(R.string.error_not_authenticated)
            else -> resources.getString(R.string.error_server_error, code)
        }
    }

    // override this to show different translation per Cause
    open fun translation(resources: Resources): String = resources.getString(default)

    companion object {
        fun fromThrowable(throwable: Throwable, @StringRes otherRes: Int? = null): Cause = when (throwable) {
            is HttpException -> Http(throwable.code())
            else -> Other(otherRes)
        }
    }
}