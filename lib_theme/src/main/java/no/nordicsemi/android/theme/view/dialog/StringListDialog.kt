package no.nordicsemi.android.theme.view.dialog

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import no.nordicsemi.android.theme.NordicColors
import no.nordicsemi.android.theme.R

@Composable
fun StringListDialog(config: StringListDialogConfig) {
    Dialog(onDismissRequest = { config.onResult(FlowCanceled) }) {
        StringListView(config)
    }
}

@Composable
fun StringListView(config: StringListDialogConfig) {
    Card(
        modifier = Modifier.height(300.dp),
        backgroundColor = NordicColors.NordicGray4.value(),
        shape = RoundedCornerShape(10.dp),
        elevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = config.title ?: stringResource(id = R.string.dialog).toAnnotatedString(),
                    fontSize = 20.sp
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxHeight(0.8f)
                    .verticalScroll(rememberScrollState())
            ) {

                config.items.forEachIndexed { i, entry ->
                    Column(modifier = Modifier.clickable { config.onResult(ItemSelectedResult(i)) }) {
                        Spacer(modifier = Modifier.height(16.dp))

                        Row {
                            config.leftIcon?.let {
                                Image(
                                    modifier = Modifier.padding(horizontal = 4.dp),
                                    painter = painterResource(it),
                                    contentDescription = "Content image",
                                    colorFilter = ColorFilter.tint(
                                        NordicColors.NordicDarkGray.value()
                                    )
                                )
                            }
                            Text(
                                text = entry,
                                fontSize = 16.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                            )
                        }

                        if (i != config.items.size - 1) {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }

                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End
            ) {
                TextButton(onClick = { config.onResult(FlowCanceled) }) {
                    Text(
                        text = stringResource(id = R.string.cancel),
                    )
                }
            }
        }
    }
}
