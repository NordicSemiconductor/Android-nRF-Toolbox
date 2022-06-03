package no.nordicsemi.android.theme.view.dialog

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
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
import no.nordicsemi.android.theme.R

@Composable
fun StringListDialog(config: StringListDialogConfig) {
    AlertDialog(
        onDismissRequest = { config.onResult(FlowCanceled) },
        title = { Text(text = config.title ?: stringResource(id = R.string.dialog).toAnnotatedString()) },
        text = {
            Column(
                modifier = Modifier
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
        },
        confirmButton = {
            TextButton(onClick = { config.onResult(FlowCanceled) }) {
                Text(
                    text = stringResource(id = R.string.cancel),
                )
            }
        }
    )
}
