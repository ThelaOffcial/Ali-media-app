package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.locale.AppLanguage
import com.example.data.locale.AppStrings
import com.example.data.model.Elephant
import com.example.data.model.ElephantPost
import com.example.data.model.ElephantType
import com.example.ui.components.PostCard
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.ForestGreenDark
import com.example.ui.theme.ForestGreenPrimary

@Composable
fun ElephantDetailScreen(
    elephant: Elephant,
    taggedPosts: List<ElephantPost>,
    onBack: () -> Unit,
    onFollowClick: () -> Unit,
    onLikePost: (String) -> Unit,
    onCommentSubmit: (String, String) -> Unit,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val primaryName = if (language == AppLanguage.SINHALA) elephant.sinhalaName else elephant.name
    val secondaryName = if (language == AppLanguage.SINHALA) elephant.name else elephant.sinhalaName

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("elephant_detail_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Bar & Hero Photo
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f)
            ) {
                if (elephant.drawableResId != null) {
                    Image(
                        painter = painterResource(id = elephant.drawableResId),
                        contentDescription = elephant.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else if (elephant.photos.isNotEmpty()) {
                    AsyncImage(
                        model = elephant.photos.first(),
                        contentDescription = elephant.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                // Back Button
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .padding(16.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                        .align(Alignment.TopStart)
                        .testTag("elephant_detail_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            }
        }

        // Title and Follow Row
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = primaryName,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )
                        Text(
                            text = secondaryName,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }

                    AnimatedContent(
                        targetState = elephant.isFollowedByMe,
                        transitionSpec = {
                            (scaleIn(initialScale = 0.85f) + fadeIn()) togetherWith (scaleOut(targetScale = 0.85f) + fadeOut())
                        },
                        label = "detail_follow_anim"
                    ) { isFollowed ->
                        if (isFollowed) {
                            OutlinedButton(
                                onClick = onFollowClick,
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.testTag("detail_unfollow_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = EmeraldAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = AppStrings.following(language),
                                    color = EmeraldAccent,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Button(
                                onClick = onFollowClick,
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ForestGreenPrimary
                                ),
                                modifier = Modifier.testTag("detail_follow_button")
                            ) {
                                Text(
                                    text = "+ ${AppStrings.follow(language)}",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Stat Chips Row: Age, Type, Followers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DetailStatBadge(
                        title = if (language == AppLanguage.SINHALA) "වර්ගය" else "Type",
                        value = if (elephant.type == ElephantType.TUSKER) "👑 ඇතා" else "🐘 අලියා",
                        modifier = Modifier.weight(1f)
                    )
                    DetailStatBadge(
                        title = if (language == AppLanguage.SINHALA) "වයස" else "Age",
                        value = AppStrings.yearsOld(language, elephant.age),
                        modifier = Modifier.weight(1f)
                    )
                    DetailStatBadge(
                        title = AppStrings.followers(language),
                        value = "${elephant.followerCount}",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Temple & Custodian Info Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp)),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccountBalance,
                                contentDescription = "Temple",
                                tint = EmeraldAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (language == AppLanguage.SINHALA) elephant.templeOwnerSinhala else elephant.templeOwner,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Location",
                                tint = AmberAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (language == AppLanguage.SINHALA) elephant.locationSinhala else elephant.location,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Biography Section
                Text(
                    text = AppStrings.elephantBio(language),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (language == AppLanguage.SINHALA) elephant.descriptionSinhala else elephant.description,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        lineHeight = 22.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Tagged Posts Header
                Text(
                    text = "${AppStrings.taggedPosts(language)} (${taggedPosts.size})",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
            }
        }

        // Tagged Posts
        if (taggedPosts.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (language == AppLanguage.SINHALA) "මෙම ඇතා ටැග් කළ සටහන් නොමැත" else "No posts tagged to this elephant yet",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(taggedPosts, key = { it.id }) { post ->
                Box(
                    modifier = Modifier
                        .animateItem()
                        .padding(horizontal = 16.dp)
                ) {
                    PostCard(
                        post = post,
                        onLikeClick = { onLikePost(post.id) },
                        onCommentSubmit = { text -> onCommentSubmit(post.id, text) },
                        onElephantTagClick = {},
                        language = language
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

@Composable
private fun DetailStatBadge(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
