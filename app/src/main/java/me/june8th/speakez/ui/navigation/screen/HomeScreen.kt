package me.june8th.speakez.ui.navigation.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items as lazyRowItems
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import me.june8th.speakez.R
import me.june8th.speakez.domain.model.MulberryCategory
import me.june8th.speakez.domain.model.MulberrySymbol
import me.june8th.speakez.ui.home.HomeViewModel

private val categoryPalette = listOf(
    Color(0xFFDDF7F4),
    Color(0xFFFFE8D6),
    Color(0xFFDDE8FF),
    Color(0xFFF3E0F8),
    Color(0xFFFFF0C2),
    Color(0xFFE8F5E9),
    Color(0xFFFFE0E6),
    Color(0xFFE0F2F1),
)

private fun categoryColor(categoryId: String): Color {
    val index = (categoryId.toIntOrNull() ?: categoryId.sumOf { it.code }) % categoryPalette.size
    return categoryPalette[index]
}

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    val viewModel: HomeViewModel = hiltViewModel()

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        val isLandscape = maxWidth > maxHeight

        if (isLandscape) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Column(
                    modifier = Modifier.weight(0.42f),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    SentenceBar(
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    SymbolSearchBar(
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    CategoryRow(
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                SymbolGrid(
                    viewModel = viewModel,
                    modifier = Modifier.weight(0.58f),
                    columns = 3,
                )
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                SentenceBar(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxWidth(),
                )
                SymbolSearchBar(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxWidth(),
                )
                CategoryRow(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxWidth(),
                )
                SymbolGrid(
                    viewModel = viewModel,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    columns = 2,
                )
            }
        }
    }
}

@Composable
private fun SentenceBar(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier,
) {
    val sentenceWords = viewModel.sentenceWords.collectAsState()
    val displayText = if (sentenceWords.value.isEmpty()) {
        stringResource(R.string.sentence_placeholder)
    } else {
        sentenceWords.value.joinToString(" ")
    }

    ElevatedCard(
        modifier = modifier,
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            LazyRow(modifier = Modifier.weight(1f)) {
                item {
                    Text(
                        text = displayText,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
            IconButton(
                onClick = { viewModel.removeLastWord() },
                modifier = Modifier.size(64.dp),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = stringResource(R.string.delete_last_word),
                    modifier = Modifier.size(28.dp),
                )
            }
            IconButton(
                onClick = { viewModel.speakSentence() },
                modifier = Modifier.size(64.dp),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = stringResource(R.string.speak_sentence),
                    modifier = Modifier.size(28.dp),
                )
            }
        }
    }
}

@Composable
private fun SymbolSearchBar(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier,
) {
    val searchQuery = viewModel.searchQuery.collectAsState()

    OutlinedTextField(
        value = searchQuery.value,
        onValueChange = viewModel::updateSearchQuery,
        modifier = modifier,
        singleLine = true,
        label = { Text(text = stringResource(R.string.symbol_search_label)) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
            )
        },
        trailingIcon = {
            if (searchQuery.value.isNotBlank()) {
                IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.symbol_search_clear),
                    )
                }
            }
        },
    )
}

@Composable
private fun CategoryRow(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier,
) {
    val categories = viewModel.categories.collectAsState()
    val selectedCategory = viewModel.selectedCategory.collectAsState()
    val totalSymbols = categories.value.sumOf { it.symbolCount }

    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.categories_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item(key = "all") {
                CategoryChip(
                    title = stringResource(R.string.category_all),
                    count = totalSymbols,
                    selected = selectedCategory.value == null,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    onClick = { viewModel.selectCategory(null) },
                )
            }
            lazyRowItems(
                items = categories.value,
                key = { category -> category.id },
            ) { category ->
                CategoryChip(
                    category = category,
                    selected = selectedCategory.value == category.id,
                    onClick = {
                        viewModel.selectCategory(
                            if (selectedCategory.value == category.id) null else category.id,
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun CategoryChip(
    category: MulberryCategory,
    selected: Boolean,
    onClick: () -> Unit,
) {
    CategoryChip(
        title = category.title,
        count = category.symbolCount,
        selected = selected,
        containerColor = categoryColor(category.id),
        onClick = onClick,
    )
}

@Composable
private fun CategoryChip(
    title: String,
    count: Int,
    selected: Boolean,
    containerColor: Color,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.size(width = 140.dp, height = 84.dp),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.5f),
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) containerColor else containerColor.copy(alpha = 0.62f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 4.dp else 1.dp),
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SymbolGrid(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier,
    columns: Int,
) {
    val gridColumns = if (columns < 1) 1 else columns
    val symbols = viewModel.filteredSymbols.collectAsState()
    val isLoading = viewModel.isLoading.collectAsState()

    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.vocabulary_grid_title, symbols.value.size),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(12.dp))

        when {
            isLoading.value -> CenterMessage(text = stringResource(R.string.symbols_loading))
            symbols.value.isEmpty() -> CenterMessage(text = stringResource(R.string.empty_vocabulary))
            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(gridColumns),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(
                        items = symbols.value,
                        key = { symbol -> symbol.id },
                    ) { symbol ->
                        SymbolCard(
                            symbol = symbol,
                            onClick = { viewModel.addWord(symbol.symbolVi) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CenterMessage(text: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SymbolCard(
    symbol: MulberrySymbol,
    onClick: () -> Unit,
) {
    Surface(
        color = categoryColor(symbol.categoryId),
        shape = MaterialTheme.shapes.large,
        tonalElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(156.dp)
            .border(
                BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)),
                MaterialTheme.shapes.large,
            ),
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            AsyncImage(
                model = symbol.assetPath,
                contentDescription = symbol.symbolVi,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(58.dp),
            )
            Text(
                text = symbol.symbolVi,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = symbol.categoryVi,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
