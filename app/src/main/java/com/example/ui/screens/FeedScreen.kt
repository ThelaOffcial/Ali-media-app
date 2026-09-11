package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.locale.AppLanguage
import com.example.data.locale.AppStrings
import com.example.data.model.ElephantPost
import com.example.data.model.Story
import com.example.ui.components.PostCard
import com.example.ui.components.StoryTray

@Composable
fun FeedScreen(
    posts: List<ElephantPost>,
    stories: List<Story>,
    onLikePost: (String) -> Unit,
    onCommentSubmit: (String, String) -> Unit,
    onStoryClick: (Story) -> Unit,
    onAddStoryClick: () -> Unit,
    onElephantTagClick: (String) -> Unit,
    language: AppLanguage,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState()
) {
    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .testTag("feed_screen_list"),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Stories Tray at top
        item {
            StoryTray(
                stories = stories,
                onStoryClick = onStoryClick,
                onAddStoryClick = onAddStoryClick,
                language = language
            )
        }

        // Posts list
        if (posts.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "🐘", fontSize = 48.sp)
                        Text(
                            text = AppStrings.noPostsYet(language),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = AppStrings.beFirstToPost(language),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(posts, key = { it.id }) { post ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 14.dp)
                        .animateItem()
                ) {
                    PostCard(
                        post = post,
                        onLikeClick = { onLikePost(post.id) },
                        onCommentSubmit = { text -> onCommentSubmit(post.id, text) },
                        onElephantTagClick = onElephantTagClick,
                        language = language
                    )
                }
            }
        }
    }
}
