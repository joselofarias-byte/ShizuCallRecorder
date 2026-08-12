/*
 * ShizuCallRecorder: FOSS Call recording powered through ADB/Shizuku!
 * Copyright (C) 2026-present kitsumed (Med)
 * This software is licensed under the GNU General Public License v3 or later, with additional terms as permitted under Section 7.
 * The full license text is available in the LICENSE file at the root of this project.
 * This software is distributed WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.kitsumed.shizucallrecorder.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

private const val JOSELOFARIAS_EVM_ADDRESS = "0x5d580ac4f1eabff84379fa8e217df4684ad30934"
private const val JOSELOFARIAS_BINANCE_PAY_URL = "https://app.binance.com/uni-qr/Dhzk73AN"

@Composable
fun SponsorScreen(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    Surface(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Apoyar el desarrollo",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )

            Text(
                text = "Edición mantenida por JoseloFarias. El apoyo es opcional y se destina al mantenimiento y desarrollo de esta edición.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            SupportCard(
                title = "Dirección EVM multicadena",
                body = JOSELOFARIAS_EVM_ADDRESS,
            ) {
                OutlinedButton(
                    onClick = { copyToClipboard(context, "Dirección EVM", JOSELOFARIAS_EVM_ADDRESS) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Copiar dirección EVM")
                }
            }

            SupportCard(
                title = "Activos preferidos",
                body = "USDT · USDC · FDUSD\nTambién se aceptan otros tokens compatibles con la dirección y la red EVM seleccionada.",
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "Verificá que el token y la red sean compatibles antes de enviar. No envíes activos por una red incompatible.",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
            }

            SupportCard(
                title = "Binance Pay · CriptoUy",
                body = "Pago directo mediante Binance Pay.",
            ) {
                Button(
                    onClick = { openUrl(context, JOSELOFARIAS_BINANCE_PAY_URL) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Default.OpenInNew, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Abrir Binance Pay")
                }
            }

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "JoseloFarias",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                TextButton(onClick = onDismiss) {
                    Text("Cerrar")
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SupportCard(
    title: String,
    body: String,
    action: (@Composable () -> Unit)? = null,
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 6,
                overflow = TextOverflow.Ellipsis,
            )
            action?.invoke()
        }
    }
}

private fun copyToClipboard(context: Context, label: String, value: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText(label, value))
}

private fun openUrl(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    runCatching { context.startActivity(intent) }
}
