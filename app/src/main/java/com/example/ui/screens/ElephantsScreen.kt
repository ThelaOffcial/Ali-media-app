package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.locale.AppLanguage
import com.example.data.locale.AppStrings
import com.example.data.model.Elephant
import com.example.data.model.ElephantType
import com.example.ui.components.ElephantCard
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.ForestGreenPrimary

enum class ElephantFilter {
    ALL,
    TUSKERS,
    ELEPHANTS
}

@Composable
fun ElephantsScreen(
    elephants: List<Elephant>,
    onFollowElephant: (String) -> Unit,
    onElephantClick: (Elephant) -> Unit,
    language: AppLanguage,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState()
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(ElephantFilter.ALL) }

    val filteredElephants = remember(elephants, searchQuery, selectedFilter) {
        elephants.filter { elephant ->
            val matchesFilter = when (selectedFilter) {
                ElephantFilter.ALL -> true
                ElephantFilter.TUSKERS -> elephant.type == ElephantType.TUSKER
                ElephantFilter.ELEPHANTS -> elephant.type == ElephantType.ELEPHANT
            }

            val query = searchQuery.trim().lowercase()
            val matchesSearch = query.isEmpty() ||
                    elephant.name.lowercase().contains(query) ||
                    elephant.sinhalaName.lowercase().contains(query) ||
                    elephant.location.lowercase().contains(query) ||
                    elephant.locationSinhala.lowercase().contains(query) ||
                    elephant.templeOwner.lowercase().contains(query) ||
                    elephant.templeOwnerSinhala.lowercase().contains(query)

            matchesFilter && matchesSearch
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("elephants_screen")
    ) {
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("elephants_search_bar"),
            placeholder = {
                Text(
                    text = AppStrings.searchElephants(language),
                    fontSize = 13.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            shape = RoundedCornerShape(24.dp),
            singleLine = true
        )

        // Filter Pills Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedFilter == ElephantFilter.ALL,
                onClick = { selectedFilter = ElephantFilter.ALL },
                label = { Text(AppStrings.allFilter(language)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = ForestGreenPrimary,
                    selectedLabelColor = androidx.compose.ui.graphics.Color.White
                )
            )

            FilterChip(
                selected = selectedFilter == ElephantFilter.TUSKERS,
                onClick = { selectedFilter = ElephantFilter.TUSKERS },
                label = { Text("👑 " + AppStrings.tuskersFilter(language)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = ForestGreenPrimary,
                    selectedLabelColor = androidx.compose.ui.graphics.Color.White
                )
            )

            FilterChip(
                selected = selectedFilter == ElephantFilter.ELEPHANTS,
                onClick = { selectedFilter = ElephantFilter.ELEPHANTS },
                label = { Text("🐘 " + AppStrings.elephantsFilter(language)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = ForestGreenPrimary,
                    selectedLabelColor = androidx.compose.ui.graphics.Color.White
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Elephants List
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (filteredElephants.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (language == AppLanguage.SINHALA) "කිසිදු ඇතෙකු හමු නොවීය" else "No elephants found",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(filteredElephants, key = { it.id }) { elephant ->
                    ElephantCard(
                        elephant = elephant,
                        onFollowClick = { onFollowElephant(elephant.id) },
                        onClick = { onElephantClick(elephant) },
                        language = language,
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }
    }
}
