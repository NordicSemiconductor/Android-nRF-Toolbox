package no.nordicsemi.android.theme.view.dialog

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import no.nordicsemi.android.material.you.Card
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
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
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
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            Spacer(modifier = Modifier.size(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxHeight(0.8f)
                    .verticalScroll(rememberScrollState())
            ) {

                config.items.forEachIndexed { i, entry ->
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { config.onResult(ItemSelectedResult(i)) }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        config.leftIcon?.let {
                            Image(
                                modifier = Modifier.padding(horizontal = 4.dp),
                                painter = painterResource(it),
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant),
                                contentDescription = "Content image",
                            )
                        }
                        Text(
                            text = entry,
                            modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.titleLarge
                        )
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
