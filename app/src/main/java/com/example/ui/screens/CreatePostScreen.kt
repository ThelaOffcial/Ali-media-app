package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.locale.AppLanguage
import com.example.data.locale.AppStrings
import com.example.data.model.Elephant
import com.example.data.model.ElephantType
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.ForestGreenDark
import com.example.ui.theme.ForestGreenPrimary

@Composable
fun CreatePostScreen(
    elephants: List<Elephant>,
    onPublish: (photoUrl: String, drawableRes: Int?, caption: String, elephantId: String?, isStoryOnly: Boolean, aspectRatio: String) -> Unit,
    onCancel: () -> Unit,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    var step by remember { mutableIntStateOf(1) }

    // Form State
    var selectedDrawableRes by remember { mutableStateOf<Int?>(R.drawable.img_hero_tuskers) }
    var photoUrl by remember { mutableStateOf("") }
    var isPastingUrl by remember { mutableStateOf(false) }
    var selectedElephantId by remember { mutableStateOf<String?>(null) }
    var caption by remember { mutableStateOf("") }
    var aspectRatio by remember { mutableStateOf("4:3") }
    var isStoryOnly by remember { mutableStateOf(false) }

    // Quick elephant presets for local images
    val presetDrawables = listOf(
        R.drawable.img_hero_tuskers,
        R.drawable.img_nadungamuwa_raja,
        R.drawable.img_kandula_bath
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("create_post_flow")
    ) {
        // Step Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (step > 1) {
                IconButton(
                    onClick = { step-- },
                    modifier = Modifier.testTag("composer_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = AppStrings.backBtn(language),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            } else {
                IconButton(
                    onClick = onCancel,
                    modifier = Modifier.testTag("composer_cancel_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancel",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // 3-Step Progress Dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 1..3) {
                    val isActive = i <= step
                    val isCurrent = i == step
                    Box(
                        modifier = Modifier
                            .size(if (isCurrent) 12.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (isActive) EmeraldAccent else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                            )
                    )
                }
            }

            // Step Label
            Text(
                text = "$step / 3",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Animated Content for 3 Steps
        AnimatedContent(
            targetState = step,
            transitionSpec = {
                if (targetState > initialState) {
                    slideInHorizontally { width -> width } + fadeIn() togetherWith
                            slideOutHorizontally { width -> -width } + fadeOut()
                } else {
                    slideInHorizontally { width -> -width } + fadeIn() togetherWith
                            slideOutHorizontally { width -> width } + fadeOut()
                }
            },
            label = "composerStepTransition",
            modifier = Modifier.weight(1f)
        ) { currentStep ->
            when (currentStep) {
                1 -> Step1ChoosePhoto(
                    selectedDrawableRes = selectedDrawableRes,
                    onSelectDrawable = {
                        selectedDrawableRes = it
                        photoUrl = ""
                    },
                    photoUrl = photoUrl,
                    onPhotoUrlChange = {
                        photoUrl = it
                        selectedDrawableRes = null
                    },
                    isPastingUrl = isPastingUrl,
                    onTogglePasteUrl = { isPastingUrl = !isPastingUrl },
                    presetDrawables = presetDrawables,
                    onContinue = { step = 2 },
                    language = language
                )
                2 -> Step2TagElephant(
                    elephants = elephants,
                    selectedElephantId = selectedElephantId,
                    onSelectElephant = { selectedElephantId = it },
                    onSkip = {
                        selectedElephantId = null
                        step = 3
                    },
                    onContinue = { step = 3 },
                    language = language
                )
                3 -> Step3Details(
                    selectedDrawableRes = selectedDrawableRes,
                    photoUrl = photoUrl,
                    aspectRatio = aspectRatio,
                    onAspectRatioChange = { aspectRatio = it },
                    caption = caption,
                    onCaptionChange = { caption = it },
                    isStoryOnly = isStoryOnly,
                    onStoryOnlyChange = { isStoryOnly = it },
                    selectedElephant = elephants.find { it.id == selectedElephantId },
                    onPublish = {
                        onPublish(
                            photoUrl,
                            selectedDrawableRes,
                            caption,
                            selectedElephantId,
                            isStoryOnly,
                            aspectRatio
                        )
                    },
                    language = language
                )
            }
        }
    }
}

// STEP 1: CHOOSE PHOTO
@Composable
private fun Step1ChoosePhoto(
    selectedDrawableRes: Int?,
    onSelectDrawable: (Int) -> Unit,
    photoUrl: String,
    onPhotoUrlChange: (String) -> Unit,
    isPastingUrl: Boolean,
    onTogglePasteUrl: () -> Unit,
    presetDrawables: List<Int>,
    onContinue: () -> Unit,
    language: AppLanguage
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = AppStrings.step1Title(language),
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        )
        Text(
            text = AppStrings.tapToChoosePhoto(language),
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Big Main Photo Preview / Drag & Drop Target
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .border(
                    2.dp,
                    Brush.linearGradient(listOf(EmeraldAccent, ForestGreenPrimary)),
                    RoundedCornerShape(24.dp)
                )
                .clickable { /* Tap to choose image */ }
                .testTag("photo_picker_box"),
            contentAlignment = Alignment.Center
        ) {
            if (selectedDrawableRes != null) {
                Image(
                    painter = painterResource(id = selectedDrawableRes),
                    contentDescription = "Selected Photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else if (photoUrl.isNotBlank()) {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = "Selected URL Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = "Choose Photo",
                        tint = EmeraldAccent,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tap to pick an elephant photo",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Preset Elephants Selection Row
        Text(
            text = if (language == AppLanguage.SINHALA) "හෝ අපගේ ඡායාරූප එකතුවෙන් තෝරන්න:" else "Or choose from featured elephant photos:",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            presetDrawables.forEach { resId ->
                val isSelected = selectedDrawableRes == resId
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(70.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(
                            width = if (isSelected) 3.dp else 1.dp,
                            color = if (isSelected) EmeraldAccent else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { onSelectDrawable(resId) }
                ) {
                    Image(
                        painter = painterResource(id = resId),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // URL Input Toggle
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .clickable { onTogglePasteUrl() }
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Link,
                contentDescription = null,
                tint = EmeraldAccent,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = AppStrings.orPasteUrl(language),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = EmeraldAccent
            )
        }

        if (isPastingUrl) {
            OutlinedTextField(
                value = photoUrl,
                onValueChange = onPhotoUrlChange,
                placeholder = { Text(AppStrings.imageUrlPlaceholder(language), fontSize = 12.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .testTag("image_url_input"),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Continue Button
        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("step1_continue_button"),
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ForestGreenPrimary
            )
        ) {
            Text(
                text = AppStrings.continueBtn(language),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

// STEP 2: TAG ELEPHANT (OPTIONAL & SKIPPABLE)
@Composable
private fun Step2TagElephant(
    elephants: List<Elephant>,
    selectedElephantId: String?,
    onSelectElephant: (String) -> Unit,
    onSkip: () -> Unit,
    onContinue: () -> Unit,
    language: AppLanguage
) {
    var searchQuery by remember { mutableStateOf("") }

    val filtered = remember(elephants, searchQuery) {
        val q = searchQuery.trim().lowercase()
        if (q.isEmpty()) elephants else elephants.filter {
            it.name.lowercase().contains(q) || it.sinhalaName.lowercase().contains(q)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = AppStrings.step2Title(language),
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        )
        Text(
            text = AppStrings.tagElephantPrompt(language),
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Search Input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text(AppStrings.searchElephants(language), fontSize = 12.sp) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("tag_elephant_search"),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Elephants List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filtered, key = { it.id }) { elephant ->
                val isSelected = selectedElephantId == elephant.id
                val name = if (language == AppLanguage.SINHALA) elephant.sinhalaName else elephant.name

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                    ),
                    border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, EmeraldAccent) else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectElephant(elephant.id) }
                        .testTag("select_elephant_${elephant.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                        ) {
                            if (elephant.drawableResId != null) {
                                Image(
                                    painter = painterResource(id = elephant.drawableResId),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = name,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (elephant.type == ElephantType.TUSKER) "👑 Tusker" else "🐘 Elephant",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = EmeraldAccent
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Action Buttons: "Skip for now" alongside "Continue"
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onSkip,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag("step2_skip_button"),
                shape = RoundedCornerShape(25.dp)
            ) {
                Text(
                    text = AppStrings.skipForNow(language),
                    fontWeight = FontWeight.SemiBold
                )
            }

            Button(
                onClick = onContinue,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag("step2_continue_button"),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ForestGreenPrimary
                )
            ) {
                Text(
                    text = AppStrings.continueBtn(language),
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

// STEP 3: DETAILS & PUBLISH
@Composable
private fun Step3Details(
    selectedDrawableRes: Int?,
    photoUrl: String,
    aspectRatio: String,
    onAspectRatioChange: (String) -> Unit,
    caption: String,
    onCaptionChange: (String) -> Unit,
    isStoryOnly: Boolean,
    onStoryOnlyChange: (Boolean) -> Unit,
    selectedElephant: Elephant?,
    onPublish: () -> Unit,
    language: AppLanguage
) {
    val emojis = listOf("🐘", "👑", "✨", "🛕", "🌿", "❤️", "🇱🇰", "🙏")

    // Count words (up to 5,000 words supported)
    val wordCount = remember(caption) {
        if (caption.isBlank()) 0 else caption.trim().split("\\s+".toRegex()).size
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Photo with aspect ratio switcher overlay
        item {
            val aspect = when (aspectRatio) {
                "1:1" -> 1.0f
                "9:16" -> 9f / 16f
                "3:4" -> 3f / 4f
                else -> 4f / 3f
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(aspect)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black.copy(alpha = 0.1f))
            ) {
                if (selectedDrawableRes != null) {
                    Image(
                        painter = painterResource(id = selectedDrawableRes),
                        contentDescription = "Post Preview",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else if (photoUrl.isNotBlank()) {
                    AsyncImage(
                        model = photoUrl,
                        contentDescription = "Post Preview",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                // Aspect ratio switcher chips overlaid on top
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("4:3", "1:1", "3:4", "9:16").forEach { ratio ->
                        val isSelected = aspectRatio == ratio
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) EmeraldAccent else Color.Transparent)
                                .clickable { onAspectRatioChange(ratio) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = ratio,
                                color = if (isSelected) Color.Black else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Tagged Elephant Badge (if selected)
                if (selectedElephant != null) {
                    val elName = if (language == AppLanguage.SINHALA) selectedElephant.sinhalaName else selectedElephant.name
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(10.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = ForestGreenDark.copy(alpha = 0.85f)
                    ) {
                        Text(
                            text = "🐘 Tagged: $elName",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Caption Input with live word counter (up to 5,000 words)
        item {
            Column {
                OutlinedTextField(
                    value = caption,
                    onValueChange = { onCaptionChange(it) },
                    placeholder = { Text(AppStrings.captionPlaceholder(language)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .testTag("caption_input_textarea"),
                    shape = RoundedCornerShape(16.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Emoji Picker row
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        emojis.forEach { emoji ->
                            Text(
                                text = emoji,
                                fontSize = 18.sp,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable { onCaptionChange(caption + emoji) }
                                    .padding(4.dp)
                            )
                        }
                    }

                    // Word count indicator
                    Text(
                        text = AppStrings.wordCount(language, wordCount),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (wordCount > 5000) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Two large tappable cards: "Feed + Story" vs "Story only"
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Option 1: Feed + Story
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onStoryOnlyChange(false) }
                        .testTag("option_feed_and_story"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (!isStoryOnly) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                    ),
                    border = if (!isStoryOnly) androidx.compose.foundation.BorderStroke(2.dp, EmeraldAccent) else null
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = AppStrings.feedAndStoryOption(language),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = AppStrings.feedAndStoryDesc(language),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Option 2: Story only (24h auto delete)
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onStoryOnlyChange(true) }
                        .testTag("option_story_only"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isStoryOnly) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                    ),
                    border = if (isStoryOnly) androidx.compose.foundation.BorderStroke(2.dp, EmeraldAccent) else null
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = AppStrings.storyOnlyOption(language),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = AppStrings.storyOnlyDesc(language),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Publish Button
        item {
            Button(
                onClick = onPublish,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("publish_post_button"),
                shape = RoundedCornerShape(27.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ForestGreenPrimary
                )
            ) {
                Text(
                    text = AppStrings.publishPost(language),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
