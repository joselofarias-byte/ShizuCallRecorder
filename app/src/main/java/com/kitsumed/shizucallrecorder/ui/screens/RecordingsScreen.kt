/*
 * ShizuCallRecorder: FOSS Call recording powered through ADB/Shizuku!
 *  Copyright (C) 2026-present kitsumed (Med)
 *  This software is licensed under the GNU General Public License v3 or later, with additional terms as permitted under Section 7.
 *  The full license text is available in the LICENSE file at the root of this project.
 *  This software is distributed WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.kitsumed.shizucallrecorder.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.automirrored.rounded.CallMade
import androidx.compose.material.icons.automirrored.rounded.CallReceived
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.MicNone
import androidx.compose.material.icons.automirrored.rounded.Notes
import androidx.compose.material.icons.rounded.SelectAll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kitsumed.shizucallrecorder.R
import com.kitsumed.shizucallrecorder.ui.viewmodels.RecordingItem
import com.kitsumed.shizucallrecorder.ui.viewmodels.RecordingsFilterTab
import com.kitsumed.shizucallrecorder.ui.viewmodels.RecordingsSortConfig
import com.kitsumed.shizucallrecorder.ui.viewmodels.RecordingsSortField
import com.kitsumed.shizucallrecorder.ui.viewmodels.RecordingsSortOrder
import com.kitsumed.shizucallrecorder.ui.viewmodels.RecordingsViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordingsScreen(
    onBack: () -> Unit,
    onRecordingClick: (RecordingItem) -> Unit,
    viewModel: RecordingsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val isSelectionMode = uiState.selectedUris.isNotEmpty()

    BackHandler(enabled = isSelectionMode) {
        viewModel.clearSelection()
    }

    BackHandler(enabled = !isSelectionMode && uiState.searchQuery.isNotBlank()) {
        viewModel.clearSearch()
        keyboardController?.hide()
        focusManager.clearFocus()
    }

    LaunchedEffect(Unit) {
        viewModel.loadRecordings()
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    var showBulkDeleteConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.recordings_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text(text = stringResource(R.string.general_back))
                    }
                },
                actions = {
                    if (!isSelectionMode) {
                        TextButton(onClick = { viewModel.loadRecordings() }) {
                            Text(text = stringResource(R.string.recordings_refresh))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(bottom = 110.dp)
            ) {
                item {
                    RecordingSearchBar(
                        query = uiState.searchQuery,
                        onQueryChange = viewModel::setSearchQuery,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                item {
                    FilterPillRow(
                        filterTab = uiState.filterTab,
                        sortConfig = uiState.sortConfig,
                        onFilterChange = viewModel::setFilterTab,
                        onSortChange = viewModel::setSortConfig,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }

                when {
                    uiState.isLoading -> item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    !uiState.folderConfigured -> item {
                        EmptyState(
                            icon = Icons.Rounded.MicNone,
                            title = stringResource(R.string.recordings_folder_missing),
                            body = stringResource(R.string.recordings_empty_body)
                        )
                    }

                    uiState.recordings.isEmpty() -> item {
                        val isFavourites = uiState.filterTab == RecordingsFilterTab.FAVOURITES
                        val hasQuery = uiState.searchQuery.isNotBlank()

                        EmptyState(
                            icon = when {
                                hasQuery -> Icons.Outlined.SearchOff
                                isFavourites -> Icons.Outlined.FavoriteBorder
                                else -> Icons.Rounded.MicNone
                            },
                            title = when {
                                hasQuery -> "Sin resultados"
                                isFavourites -> "No hay favoritas"
                                else -> stringResource(R.string.recordings_empty_title)
                            },
                            body = when {
                                hasQuery -> "Prueba con otro numero, contacto o nota."
                                isFavourites -> "Toca el corazón de una grabación para guardarla aquí."
                                else -> stringResource(R.string.recordings_empty_body)
                            }
                        )
                    }

                    else -> {
                        val grouped = uiState.recordings.groupBy { groupLabel(it.date) }
                        grouped.forEach { (dateLabel, items) ->
                            item(key = "header_$dateLabel") {
                                DateGroupHeader(label = dateLabel)
                            }

                            item(key = "group_$dateLabel") {
                                RecordingGroupCard(
                                    items = items,
                                    searchQuery = uiState.searchQuery,
                                    selectedUris = uiState.selectedUris,
                                    isSelectionMode = isSelectionMode,
                                    onFavouriteToggle = viewModel::toggleFavourite,
                                    onRecordingClick = { item ->
                                        if (isSelectionMode) viewModel.toggleSelection(item.uri) else onRecordingClick(item)
                                    },
                                    onToggleSelect = { item -> viewModel.toggleSelection(item.uri) },
                                    onShare = { item -> shareRecording(context, item) },
                                    onDelete = { item -> viewModel.deleteRecording(item) },
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (isSelectionMode) {
                SelectionBar(
                    count = uiState.selectedUris.size,
                    total = uiState.recordings.size,
                    onCancel = viewModel::clearSelection,
                    onSelectAll = viewModel::selectAllVisible,
                    onShare = {
                        val selected = uiState.recordings.filter { it.uri in uiState.selectedUris }
                        shareRecordings(context, selected)
                    },
                    onDelete = { showBulkDeleteConfirm = true },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 20.dp, vertical = 36.dp)
                )
            }
        }
    }

    if (showBulkDeleteConfirm) {
        val count = uiState.selectedUris.size
        AlertDialog(
            onDismissRequest = { showBulkDeleteConfirm = false },
            icon = { Icon(Icons.Outlined.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Eliminar $count grabación${if (count == 1) "" else "es"}?") },
            text = { Text("Esto eliminará permanentemente los archivos selecciónados. Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = {
                        showBulkDeleteConfirm = false
                        viewModel.deleteSelected()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBulkDeleteConfirm = false }) {
                    Text(stringResource(R.string.general_cancel))
                }
            }
        )
    }
}

@Composable
private fun RecordingSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = { Text("Buscar grabaciónes...") },
        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
        trailingIcon = {
            AnimatedVisibility(
                visible = query.isNotBlank(),
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Rounded.Close, contentDescription = "Limpiar búsqueda")
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
        )
    )
}

@Composable
private fun FilterPillRow(
    filterTab: RecordingsFilterTab,
    sortConfig: RecordingsSortConfig,
    onFilterChange: (RecordingsFilterTab) -> Unit,
    onSortChange: (RecordingsSortConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    var showSortMenu by remember { mutableStateOf(false) }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilterPill(
            label = "Todas",
            selected = filterTab == RecordingsFilterTab.ALL,
            icon = Icons.AutoMirrored.Outlined.List,
            onClick = { onFilterChange(RecordingsFilterTab.ALL) }
        )

        FilterPill(
            label = "Favoritas",
            selected = filterTab == RecordingsFilterTab.FAVOURITES,
            icon = Icons.Outlined.FavoriteBorder,
            onClick = { onFilterChange(RecordingsFilterTab.FAVOURITES) }
        )

        Spacer(Modifier.weight(1f))

        Box {
            val sortLabel = when (sortConfig.field) {
                RecordingsSortField.TIME -> "Fecha"
                RecordingsSortField.NAME -> "Nombre"
                RecordingsSortField.SIZE -> "Tamaño"
            }

            FilterPill(
                label = sortLabel,
                selected = false,
                icon = Icons.AutoMirrored.Outlined.Sort,
                trailingIcon = if (sortConfig.order == RecordingsSortOrder.DESC) Icons.Rounded.ArrowDownward else Icons.Rounded.ArrowUpward,
                onClick = { showSortMenu = true }
            )

            DropdownMenu(
                expanded = showSortMenu,
                onDismissRequest = { showSortMenu = false },
                shape = RoundedCornerShape(16.dp)
            ) {
                SortOption.entries.forEach { option ->
                    val selected = sortConfig.field == option.field && sortConfig.order == option.order
                    DropdownMenuItem(
                        text = { Text(option.label, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal) },
                        leadingIcon = { Icon(option.icon, contentDescription = null) },
                        trailingIcon = if (selected) {
                            { Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null,
                        onClick = {
                            onSortChange(RecordingsSortConfig(option.field, option.order))
                            showSortMenu = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterPill(
    label: String,
    selected: Boolean,
    icon: ImageVector,
    trailingIcon: ImageVector? = null,
    onClick: () -> Unit
) {
    val containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh
    val contentColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = containerColor,
        contentColor = contentColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
            if (trailingIcon != null) {
                Icon(trailingIcon, contentDescription = null, modifier = Modifier.size(14.dp))
            }
        }
    }
}

private enum class SortOption(
    val label: String,
    val field: RecordingsSortField,
    val order: RecordingsSortOrder,
    val icon: ImageVector
) {
    DATE_DESC("Más recientes", RecordingsSortField.TIME, RecordingsSortOrder.DESC, Icons.Rounded.ArrowDownward),
    DATE_ASC("Más antiguas", RecordingsSortField.TIME, RecordingsSortOrder.ASC, Icons.Rounded.ArrowUpward),
    NAME_ASC("Nombre A-Z", RecordingsSortField.NAME, RecordingsSortOrder.ASC, Icons.Rounded.ArrowDownward),
    NAME_DESC("Nombre Z-A", RecordingsSortField.NAME, RecordingsSortOrder.DESC, Icons.Rounded.ArrowUpward),
    SIZE_DESC("Más grandes", RecordingsSortField.SIZE, RecordingsSortOrder.DESC, Icons.Rounded.ArrowDownward)
}

@Composable
private fun DateGroupHeader(label: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
    )
}

@Composable
private fun RecordingGroupCard(
    items: List<RecordingItem>,
    searchQuery: String,
    selectedUris: Set<Uri>,
    isSelectionMode: Boolean,
    onFavouriteToggle: (RecordingItem) -> Unit,
    onRecordingClick: (RecordingItem) -> Unit,
    onToggleSelect: (RecordingItem) -> Unit,
    onShare: (RecordingItem) -> Unit,
    onDelete: (RecordingItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            items.forEachIndexed { index, item ->
                RecordingRow(
                    item = item,
                    searchQuery = searchQuery,
                    isSelectionMode = isSelectionMode,
                    isSelected = item.uri in selectedUris,
                    onFavouriteToggle = { onFavouriteToggle(item) },
                    onClick = { onRecordingClick(item) },
                    onEnterSelectionMode = { onToggleSelect(item) },
                    onShare = { onShare(item) },
                    onDelete = { onDelete(item) }
                )

                if (index < items.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RecordingRow(
    item: RecordingItem,
    searchQuery: String,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onFavouriteToggle: () -> Unit,
    onClick: () -> Unit,
    onEnterSelectionMode: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    val viewModel: RecordingsViewModel = viewModel()
    val context = LocalContext.current
    val isIncoming = item.direction == "in"
    val accentColor = if (isIncoming) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
    val directionIcon = if (isIncoming) Icons.AutoMirrored.Rounded.CallReceived else Icons.AutoMirrored.Rounded.CallMade
    val directionLabel = if (isIncoming) "Entrante" else "Saliente"
    val timeStr = item.date?.let { SimpleDateFormat("hh:mm a", Locale.getDefault()).format(it) }.orEmpty()
    val displayName = item.contactName ?: item.phoneNumber
    val lowerQuery = searchQuery.trim().lowercase(Locale.getDefault())

    var showMenu by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var photoBitmap by remember(item.phoneNumber) { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(item.phoneNumber) {
        photoBitmap = viewModel.loadContactPhoto(context, item.phoneNumber)
    }

    val rowBackground by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.28f) else Color.Transparent,
        animationSpec = tween(220),
        label = "rowBackground"
    )

    Box(modifier = Modifier.background(rowBackground)) {
        ListItem(
            modifier = Modifier.combinedClickable(
                onClick = onClick,
                onLongClick = {
                    if (isSelectionMode) showMenu = true else onEnterSelectionMode()
                }
            ),
            leadingContent = {
                RecordingAvatar(
                    item = item,
                    photoBitmap = photoBitmap,
                    accentColor = accentColor,
                    isSelectionMode = isSelectionMode,
                    isSelected = isSelected
                )
            },
            headlineContent = {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            supportingContent = {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(directionIcon, contentDescription = null, tint = accentColor, modifier = Modifier.size(12.dp))
                        Text(directionLabel, style = MaterialTheme.typography.labelSmall, color = accentColor)

                        if (timeStr.isNotBlank()) {
                            Text(" - ", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(timeStr, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Text(" - ", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(formatSize(item.sizeBytes), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        if (item.durationMs > 0L) {
                            Text(" - ", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(formatDurationMs(item.durationMs), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    if (lowerQuery.isNotBlank() && item.noteText.lowercase(Locale.getDefault()).contains(lowerQuery)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Rounded.Notes, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                            Text(
                                text = item.noteText.take(80),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            },
            trailingContent = {
                if (!isSelectionMode) {
                    IconButton(onClick = onFavouriteToggle, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = if (item.isFavourite) Icons.Rounded.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = null,
                            tint = if (item.isFavourite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    Spacer(Modifier.size(36.dp))
                }
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
        )

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            shape = RoundedCornerShape(16.dp)
        ) {
            DropdownMenuItem(
                text = { Text("Seleccionar") },
                leadingIcon = { Icon(Icons.Outlined.CheckCircle, contentDescription = null) },
                onClick = {
                    showMenu = false
                    onEnterSelectionMode()
                }
            )

            DropdownMenuItem(
                text = { Text("Compartir") },
                leadingIcon = { Icon(Icons.Outlined.Share, contentDescription = null) },
                onClick = {
                    showMenu = false
                    onShare()
                }
            )

            DropdownMenuItem(
                text = { Text("Información") },
                leadingIcon = { Icon(Icons.Outlined.Info, contentDescription = null) },
                onClick = {
                    showMenu = false
                    showInfoDialog = true
                }
            )

            DropdownMenuItem(
                text = { Text(if (item.isFavourite) "Quitar favorita" else "Agregar favorita") },
                leadingIcon = { Icon(if (item.isFavourite) Icons.Rounded.Favorite else Icons.Outlined.FavoriteBorder, contentDescription = null) },
                onClick = {
                    showMenu = false
                    onFavouriteToggle()
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            DropdownMenuItem(
                text = { Text("Eliminar", color = MaterialTheme.colorScheme.error) },
                leadingIcon = { Icon(Icons.Outlined.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                onClick = {
                    showMenu = false
                    showDeleteConfirm = true
                }
            )
        }
    }

    if (showInfoDialog) {
        RecordingInfoDialog(item = item, onDismiss = { showInfoDialog = false })
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            icon = { Icon(Icons.Outlined.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("¿Eliminar grabación?") },
            text = { Text("Esto eliminará permanentemente la grabación de ${item.contactName ?: item.phoneNumber}.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.general_cancel))
                }
            }
        )
    }
}

@Composable
private fun RecordingAvatar(
    item: RecordingItem,
    photoBitmap: ImageBitmap?,
    accentColor: Color,
    isSelectionMode: Boolean,
    isSelected: Boolean
) {
    Box(modifier = Modifier.size(44.dp), contentAlignment = Alignment.Center) {
        val avatarAlpha by animateFloatAsState(
            targetValue = if (isSelectionMode) 0f else 1f,
            animationSpec = tween(260, easing = FastOutSlowInEasing),
            label = "avatarAlpha"
        )

        val checkLayerAlpha by animateFloatAsState(
            targetValue = if (isSelectionMode) 1f else 0f,
            animationSpec = tween(260, easing = FastOutSlowInEasing),
            label = "checkLayerAlpha"
        )

        val checkLayerScale by animateFloatAsState(
            targetValue = if (isSelectionMode) 1f else 0.65f,
            animationSpec = spring(),
            label = "checkLayerScale"
        )

        val tickAlpha by animateFloatAsState(
            targetValue = if (isSelected) 1f else 0f,
            animationSpec = tween(200),
            label = "tickAlpha"
        )

        Box(
            modifier = Modifier
                .size(44.dp)
                .graphicsLayer { alpha = avatarAlpha }
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            if (photoBitmap != null) {
                Image(
                    bitmap = photoBitmap,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                val initial = item.contactName?.firstOrNull()?.uppercaseChar()?.toString()
                    ?: item.phoneNumber.firstOrNull { it.isDigit() }?.toString()
                    ?: "?"
                Text(initial, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = accentColor)
            }
        }

        Box(
            modifier = Modifier
                .size(44.dp)
                .graphicsLayer {
                    alpha = checkLayerAlpha
                    scaleX = checkLayerScale
                    scaleY = checkLayerScale
                }
                .scale(if (isSelected) 1f else 0.88f)
                .clip(CircleShape)
                .background(if (isSelected) accentColor.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceContainerHigh)
                .then(
                    if (isSelected) {
                        Modifier.border(2.dp, accentColor, CircleShape)
                    } else {
                        Modifier.border(1.5.dp, MaterialTheme.colorScheme.outline, CircleShape)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.Check,
                contentDescription = "Seleccionado",
                tint = accentColor,
                modifier = Modifier
                    .size(22.dp)
                    .graphicsLayer { alpha = tickAlpha }
            )
        }
    }
}

@Composable
private fun SelectionBar(
    count: Int,
    total: Int,
    onCancel: () -> Unit,
    onSelectAll: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        tonalElevation = 6.dp,
        border = androidx.compose.foundation.BorderStroke(
            0.8.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onCancel) {
                Icon(Icons.Rounded.Close, contentDescription = "Cancelar selección")
            }

            Text(
                text = "$count selecciónada${if (count == 1) "" else "s"}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp)
            )

            if (count < total) {
                FilledTonalIconButton(onClick = onSelectAll, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Rounded.SelectAll, contentDescription = "Seleccionar todo", modifier = Modifier.size(20.dp))
                }
            }

            IconButton(onClick = onShare, enabled = count > 0) {
                Icon(Icons.Outlined.Share, contentDescription = "Compartir")
            }

            IconButton(onClick = onDelete, enabled = count > 0) {
                Icon(Icons.Outlined.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun RecordingInfoDialog(item: RecordingItem, onDismiss: () -> Unit) {
    val isIncoming = item.direction == "in"
    val accentColor = if (isIncoming) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        icon = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isIncoming) Icons.AutoMirrored.Rounded.CallReceived else Icons.AutoMirrored.Rounded.CallMade,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        title = { Text("Información", fontWeight = FontWeight.SemiBold) },
        text = {
            Column {
                InfoRow("Contacto", item.contactName ?: "—")
                InfoRow("Número", item.phoneNumber)
                InfoRow("Dirección", if (isIncoming) "Entrante" else "Saliente")
                InfoRow("Fecha", item.date?.let { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it) } ?: "—")
                InfoRow("Hora", item.date?.let { SimpleDateFormat("hh:mm a", Locale.getDefault()).format(it) } ?: "—")
                InfoRow("Duración", if (item.durationMs > 0L) formatDurationMs(item.durationMs) else "—")
                InfoRow("Tamaño", formatSize(item.sizeBytes))
                InfoRow("Formato", item.extension.uppercase(Locale.getDefault()).ifBlank { "—" })
                InfoRow("Favorita", if (item.isFavourite) "Sí" else "No")
                if (item.noteText.isNotBlank()) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)
                    Text("Nota", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    Text(item.noteText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(0.4f))
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(0.6f)
        )
    }
}

@Composable
private fun EmptyState(
    icon: ImageVector,
    title: String,
    body: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(32.dp))
            }

            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
    }
}

private fun shareRecording(context: android.content.Context, item: RecordingItem) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "audio/*"
        putExtra(Intent.EXTRA_STREAM, item.uri)
        // Attach a human-readable card so the receiver app (WhatsApp, email, ...) can show
        // who/when alongside the audio, since the file itself carries no embedded tags.
        putExtra(Intent.EXTRA_TEXT, buildShareCard(item))
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Compartir grabación"))
}

private fun shareRecordings(context: android.content.Context, items: List<RecordingItem>) {
    if (items.isEmpty()) return

    val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
        type = "audio/*"
        putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(items.map { it.uri }))
        putExtra(Intent.EXTRA_TEXT, items.joinToString("\n\n") { buildShareCard(it) })
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Compartir grabaciones"))
}

/**
 * Builds a short, plain-text description of a recording to attach to share intents.
 * Mirrors the fields shown in RecordingInfoDialog so the share matches what the user sees.
 */
private fun buildShareCard(item: RecordingItem): String {
    val direction = if (item.direction == "in") "Entrante" else "Saliente"
    val date = item.date?.let { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(it) }.orEmpty()
    val duration = if (item.durationMs > 0L) formatDurationMs(item.durationMs) else ""
    val lines = buildList {
        add("Grabación de llamada")
        item.contactName?.takeIf { it.isNotBlank() }?.let { add("Contacto: $it") }
        add("Número: ${item.phoneNumber}")
        add("Dirección: $direction")
        if (date.isNotBlank()) add("Fecha: $date")
        if (duration.isNotBlank()) add("Duración: $duration")
        item.noteText.takeIf { it.isNotBlank() }?.let { add("Nota: $it") }
    }
    return lines.joinToString("\n")
}

private fun groupLabel(date: Date?): String {
    if (date == null) return "Sin fecha"

    val now = Calendar.getInstance()
    val calendar = Calendar.getInstance().apply { time = date }

    return when {
        isSameDay(now, calendar) -> "Hoy"
        isYesterday(now, calendar) -> "Ayer"
        isSameWeek(now, calendar) -> SimpleDateFormat("EEEE", Locale.getDefault()).format(date)
        isSameYear(now, calendar) -> SimpleDateFormat("d MMMM", Locale.getDefault()).format(date)
        else -> SimpleDateFormat("d MMMM yyyy", Locale.getDefault()).format(date)
    }
}

private fun isSameDay(a: Calendar, b: Calendar): Boolean {
    return a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
        a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)
}

private fun isYesterday(now: Calendar, other: Calendar): Boolean {
    val yesterday = (now.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -1) }
    return isSameDay(yesterday, other)
}

private fun isSameWeek(a: Calendar, b: Calendar): Boolean {
    return a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
        a.get(Calendar.WEEK_OF_YEAR) == b.get(Calendar.WEEK_OF_YEAR)
}

private fun isSameYear(a: Calendar, b: Calendar): Boolean {
    return a.get(Calendar.YEAR) == b.get(Calendar.YEAR)
}

private fun formatSize(bytes: Long): String {
    return when {
        bytes < 1024L -> "${bytes}B"
        bytes < 1024L * 1024L -> "${bytes / 1024L}KB"
        else -> "%.1fMB".format(Locale.getDefault(), bytes / (1024.0 * 1024.0))
    }
}

private fun formatDurationMs(ms: Long): String {
    val minutes = ms / 60_000L
    val seconds = (ms % 60_000L) / 1_000L
    return "%d:%02d".format(Locale.getDefault(), minutes, seconds)
}






