package me.june8th.speakez.ui.navigation.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import kotlinx.coroutines.flow.collectLatest
import me.june8th.speakez.data.word.WordAssetType
import me.june8th.speakez.ui.settings.CustomWordEvent
import me.june8th.speakez.ui.settings.SettingsViewModel

@Composable
fun AddCustomWordScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val draft by viewModel.customWordDraft.collectAsStateWithLifecycle()
    val symbols by viewModel.mulberrySymbols.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }
    val filteredSymbols = symbols.filter { symbol ->
        query.isBlank() ||
            symbol.symbolVi.contains(query, ignoreCase = true) ||
            symbol.symbolEn.contains(query, ignoreCase = true) ||
            symbol.tags.contains(query, ignoreCase = true)
    }

    LaunchedEffect(viewModel) {
        viewModel.customWordEvents.collectLatest { event ->
            if (event == CustomWordEvent.Saved) onBackClick()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                }
                Text(
                    text = "Thêm từ của tôi",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
        },
        bottomBar = {
            Button(
                onClick = viewModel::saveCustomWord,
                enabled = !draft.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                Text(if (draft.isSaving) "Đang lưu..." else "Lưu từ")
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = draft.wordText,
                onValueChange = viewModel::updateCustomWordText,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Nội dung từ") },
                singleLine = true,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssetTypeButton(
                    text = "Mulberry Icons",
                    selected = draft.assetType == WordAssetType.MULBERRY,
                    onClick = { viewModel.selectCustomWordAssetType(WordAssetType.MULBERRY) },
                )
                AssetTypeButton(
                    text = "Emoji",
                    selected = draft.assetType == WordAssetType.EMOJI,
                    onClick = { viewModel.selectCustomWordAssetType(WordAssetType.EMOJI) },
                )
            }
            draft.errorMessage?.let { error ->
                Text(text = error, color = MaterialTheme.colorScheme.error)
            }

            if (draft.assetType == WordAssetType.EMOJI) {
                OutlinedTextField(
                    value = draft.assetValue,
                    onValueChange = viewModel::selectCustomWordAsset,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Nhập một emoji") },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.headlineMedium,
                )
            } else {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Tìm biểu tượng Mulberry") },
                    singleLine = true,
                )
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 96.dp),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(filteredSymbols, key = { it.id }) { symbol ->
                        Card(
                            onClick = { viewModel.selectCustomWordAsset(symbol.assetPath) },
                            border = BorderStroke(
                                width = if (draft.assetValue == symbol.assetPath) 3.dp else 1.dp,
                                color = if (draft.assetValue == symbol.assetPath) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.outlineVariant
                                },
                            ),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                            ),
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                AsyncImage(
                                    model = symbol.assetPath,
                                    contentDescription = symbol.symbolVi,
                                    modifier = Modifier.size(48.dp),
                                    contentScale = ContentScale.Fit,
                                )
                                Text(
                                    text = symbol.symbolVi,
                                    maxLines = 1,
                                    style = MaterialTheme.typography.labelMedium,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AssetTypeButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    if (selected) {
        Button(onClick = onClick) { Text(text) }
    } else {
        TextButton(onClick = onClick) { Text(text) }
    }
}
