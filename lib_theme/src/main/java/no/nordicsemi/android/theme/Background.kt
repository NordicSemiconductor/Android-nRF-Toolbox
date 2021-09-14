package no.nordicsemi.android.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object Background {

    @Composable
    fun whiteRoundedCorners(): Modifier {
        return Modifier
            .background(Color(0xffffffff))
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .clip(RoundedCornerShape(10.dp))
    }
}