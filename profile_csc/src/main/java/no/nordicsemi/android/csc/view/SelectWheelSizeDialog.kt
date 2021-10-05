package no.nordicsemi.android.csc.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.TabRowDefaults.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import no.nordicsemi.android.csc.R
import no.nordicsemi.android.theme.NordicColors
import no.nordicsemi.android.theme.NordicColors.NordicLightGray
import no.nordicsemi.android.theme.TestTheme

@Composable
internal fun SelectWheelSizeDialog(onEvent: (OnWheelSizeSelected) -> Unit) {
    Dialog(onDismissRequest = {}) {
        SelectWheelSizeView(onEvent)
    }
}

@Composable
private fun SelectWheelSizeView(onEvent: (OnWheelSizeSelected) -> Unit) {
    val wheelEntries = stringArrayResource(R.array.wheel_entries)
    val wheelValues = stringArrayResource(R.array.wheel_values)

    Card(
        modifier = Modifier.height(300.dp),
        backgroundColor = NordicColors.NordicGray4.value(),
        shape = RoundedCornerShape(10.dp),
        elevation = 0.dp
    ) {
        Column {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Wheel size",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {

                wheelEntries.forEachIndexed { i, entry ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = entry,
                        fontSize = 16.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onEvent(OnWheelSizeSelected(wheelValues[i].toInt(), entry))
                            }
                    )

                    if (i != wheelEntries.size - 1) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Divider(color = NordicLightGray.value(), thickness = 1.dp/2)
                    }
                }
            }
        }
    }
}

@Preview
@Composable
internal fun DefaultPreview() {
    TestTheme {
        SelectWheelSizeView { }
    }
}
