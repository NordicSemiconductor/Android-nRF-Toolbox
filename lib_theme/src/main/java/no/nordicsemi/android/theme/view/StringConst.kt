package no.nordicsemi.android.theme.view

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import no.nordicsemi.android.theme.R
import javax.inject.Inject

class StringConst @Inject constructor(
    @ApplicationContext
    private val context: Context
) {

    val APP_NAME = context.getString(R.string.app_name)
}
