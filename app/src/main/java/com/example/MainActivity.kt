package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.data.locale.AppLanguage
import com.example.data.model.Elephant
import com.example.data.model.Story
import com.example.data.repository.AliMediaRepository
import com.example.ui.components.AliMediaBottomBar
import com.example.ui.components.AliMediaTopBar
import com.example.ui.components.AppTab
import com.example.ui.components.StoryViewerDialog
import com.example.ui.screens.CreatePostScreen
import com.example.ui.screens.ElephantDetailScreen
import com.example.ui.screens.ElephantsScreen
import com.example.ui.screens.FeedbackScreen
import com.example.ui.screens.FeedScreen
import com.example.ui.screens.NoticesScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.theme.AliMediaTheme
import kotlinx.coroutines.launch

sealed interface AppNavScreen {
    data class ElephantDetail(val id: String) : AppNavScreen
    object Feedback : AppNavScreen
    data class TabScreen(val tab: AppTab) : AppNavScreen
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AliMediaApp()
        }
    }
}

@Composable
fun AliMediaApp() {
    val repository = remember { AliMediaRepository.instance }
    val systemDark = isSystemInDarkTheme()

    var isDarkMode by remember { mutableStateOf(false) }
    var currentLanguage by remember { mutableStateOf(AppLanguage.ENGLISH) }
    var currentTab by remember { mutableStateOf(AppTab.FEED) }

    // Screen navigation state
    var viewingElephant by remember { mutableStateOf<Elephant?>(null) }
    var viewingStory by remember { mutableStateOf<Story?>(null) }
    var isViewingFeedback by remember { mutableStateOf(false) }

    // Scroll states for smooth scroll-to-top behavior
    val feedListState = rememberLazyListState()
    val elephantsListState = rememberLazyListState()

    // Coroutine & Snackbar
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // State flows from repository
    val posts by repository.posts.collectAsState()
    val elephants by repository.elephants.collectAsState()
    val stories by repository.stories.collectAsState()
    val notices by repository.notices.collectAsState()
    val feedbacks by repository.feedbacks.collectAsState()
    val currentUser by repository.currentUser.collectAsState()
    val isSyncing by repository.isSyncing.collectAsState()

    val currentNavScreen: AppNavScreen = when {
        viewingElephant != null -> AppNavScreen.ElephantDetail(viewingElephant!!.id)
        isViewingFeedback -> AppNavScreen.Feedback
        else -> AppNavScreen.TabScreen(currentTab)
    }

    AliMediaTheme(darkTheme = isDarkMode) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                // Hide top bar on fullscreen create screen or elephant detail
                if (currentTab != AppTab.CREATE && viewingElephant == null && !isViewingFeedback) {
                    AliMediaTopBar(
                        currentLanguage = currentLanguage,
                        onToggleLanguage = {
                            currentLanguage = if (currentLanguage == AppLanguage.ENGLISH) {
                                AppLanguage.SINHALA
                            } else {
                                AppLanguage.ENGLISH
                            }
                        },
                        isDarkMode = isDarkMode,
                        onToggleDarkMode = { isDarkMode = !isDarkMode },
                        onNotificationsClick = {
                            currentTab = AppTab.NOTICES
                        },
                        unreadNoticesCount = notices.count { it.isUrgent },
                        isSyncing = isSyncing,
                        onRefreshClick = {
                            repository.refreshFromDatabase()
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(
                                    if (currentLanguage == AppLanguage.SINHALA)
                                        "දත්ත සමමුහුර්ත කෙරේ..."
                                    else
                                        "Syncing with Realtime Database..."
                                )
                            }
                        }
                    )
                }
            },
            bottomBar = {
                // Show bottom bar on standard tabs when not in full screens
                if (viewingElephant == null && !isViewingFeedback && currentTab != AppTab.CREATE) {
                    AliMediaBottomBar(
                        selectedTab = currentTab,
                        onTabSelected = { newTab ->
                            if (newTab == currentTab && viewingElephant == null && !isViewingFeedback) {
                                coroutineScope.launch {
                                    when (newTab) {
                                        AppTab.FEED -> feedListState.animateScrollToItem(0)
                                        AppTab.ELEPHANTS -> elephantsListState.animateScrollToItem(0)
                                        else -> {}
                                    }
                                }
                            } else {
                                viewingElephant = null
                                isViewingFeedback = false
                                currentTab = newTab
                            }
                        },
                        language = currentLanguage,
                        isDarkMode = isDarkMode
                    )
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AnimatedContent(
                    targetState = currentNavScreen,
                    transitionSpec = {
                        when {
                            targetState is AppNavScreen.ElephantDetail || targetState is AppNavScreen.Feedback -> {
                                (slideInHorizontally(initialOffsetX = { it / 4 }, animationSpec = tween(280)) + fadeIn(tween(280))) togetherWith
                                        (slideOutHorizontally(targetOffsetX = { -it / 4 }, animationSpec = tween(240)) + fadeOut(tween(240)))
                            }
                            initialState is AppNavScreen.ElephantDetail || initialState is AppNavScreen.Feedback -> {
                                (slideInHorizontally(initialOffsetX = { -it / 4 }, animationSpec = tween(280)) + fadeIn(tween(280))) togetherWith
                                        (slideOutHorizontally(targetOffsetX = { it / 4 }, animationSpec = tween(240)) + fadeOut(tween(240)))
                            }
                            else -> {
                                (fadeIn(animationSpec = tween(220)) + scaleIn(initialScale = 0.98f, animationSpec = tween(220))) togetherWith
                                        fadeOut(animationSpec = tween(180))
                            }
                        }
                    },
                    label = "main_screen_nav",
                    modifier = Modifier.fillMaxSize()
                ) { navTarget ->
                    when (navTarget) {
                        // 1. Elephant Detail Screen
                        is AppNavScreen.ElephantDetail -> {
                            val selected = viewingElephant ?: elephants.find { it.id == navTarget.id }
                            if (selected != null) {
                                val currentElephantState = elephants.find { it.id == selected.id } ?: selected
                                val taggedPosts = posts.filter { it.elephantId == selected.id }

                                ElephantDetailScreen(
                                    elephant = currentElephantState,
                                    taggedPosts = taggedPosts,
                                    onBack = { viewingElephant = null },
                                    onFollowClick = {
                                        repository.toggleFollowElephant(selected.id)
                                    },
                                    onLikePost = { postId ->
                                        repository.toggleLikePost(postId)
                                    },
                                    onCommentSubmit = { postId, comment ->
                                        repository.addComment(postId, comment)
                                    },
                                    language = currentLanguage
                                )
                            }
                        }

                        // 2. Feedback & Suggestions Board
                        is AppNavScreen.Feedback -> {
                            FeedbackScreen(
                                feedbacks = feedbacks,
                                onLikeFeedback = { id ->
                                    repository.toggleLikeFeedback(id)
                                },
                                onSubmitFeedback = { type, msg ->
                                    repository.submitFeedback(type, msg)
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(
                                            if (currentLanguage == AppLanguage.SINHALA) "ඔබේ අදහස සාර්ථකව ඉදිරිපත් කරන ලදී! ස්තූතියි." else "Feedback submitted successfully! Thank you."
                                        )
                                    }
                                },
                                onBack = { isViewingFeedback = false },
                                language = currentLanguage
                            )
                        }

                        // 3. Tab Screens
                        is AppNavScreen.TabScreen -> {
                            when (navTarget.tab) {
                                AppTab.FEED -> {
                                    FeedScreen(
                                        posts = posts,
                                        stories = stories,
                                        onLikePost = { postId ->
                                            repository.toggleLikePost(postId)
                                        },
                                        onCommentSubmit = { postId, text ->
                                            repository.addComment(postId, text)
                                        },
                                        onStoryClick = { story ->
                                            viewingStory = story
                                        },
                                        onAddStoryClick = {
                                            currentTab = AppTab.CREATE
                                        },
                                        onElephantTagClick = { elephantId ->
                                            val el = elephants.find { it.id == elephantId }
                                            if (el != null) viewingElephant = el
                                        },
                                        language = currentLanguage,
                                        listState = feedListState
                                    )
                                }

                                AppTab.ELEPHANTS -> {
                                    ElephantsScreen(
                                        elephants = elephants,
                                        onFollowElephant = { elephantId ->
                                            repository.toggleFollowElephant(elephantId)
                                        },
                                        onElephantClick = { elephant ->
                                            viewingElephant = elephant
                                        },
                                        language = currentLanguage,
                                        listState = elephantsListState
                                    )
                                }

                                AppTab.CREATE -> {
                                    CreatePostScreen(
                                        elephants = elephants,
                                        onPublish = { photoUrl, caption, elephantId, isStoryOnly, aspect ->
                                            repository.createPost(
                                                photoUrl = photoUrl,
                                                caption = caption,
                                                elephantId = elephantId,
                                                isStoryOnly = isStoryOnly,
                                                aspectRatio = aspect
                                            )
                                            currentTab = AppTab.FEED
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar(
                                                    if (currentLanguage == AppLanguage.SINHALA) "ඡායාරූපය සාර්ථකව බෙදාගන්නා ලදී! 🐘" else "Post published successfully! 🐘"
                                                )
                                            }
                                        },
                                        onCancel = {
                                            currentTab = AppTab.FEED
                                        },
                                        language = currentLanguage
                                    )
                                }

                                AppTab.NOTICES -> {
                                    NoticesScreen(
                                        notices = notices,
                                        language = currentLanguage
                                    )
                                }

                                AppTab.PROFILE -> {
                                    val userPosts = posts.filter { it.authorUid == currentUser.uid }
                                    val followedElephants = elephants.filter { currentUser.followedElephants.contains(it.id) }

                                    ProfileScreen(
                                        userProfile = currentUser,
                                        userPosts = userPosts,
                                        followedElephants = followedElephants,
                                        onLikePost = { id -> repository.toggleLikePost(id) },
                                        onCommentSubmit = { id, text -> repository.addComment(id, text) },
                                        onFollowElephant = { id -> repository.toggleFollowElephant(id) },
                                        onElephantClick = { elephant -> viewingElephant = elephant },
                                        onOpenFeedback = { isViewingFeedback = true },
                                        language = currentLanguage,
                                        onToggleLanguage = {
                                            currentLanguage = if (currentLanguage == AppLanguage.ENGLISH) AppLanguage.SINHALA else AppLanguage.ENGLISH
                                        },
                                        isDarkMode = isDarkMode,
                                        onToggleDarkMode = { isDarkMode = !isDarkMode }
                                    )
                                }
                            }
                        }
                    }
                }

                // Fullscreen 24-hour Story Viewer
                if (viewingStory != null) {
                    val currentStoryIndex = stories.indexOfFirst { it.id == viewingStory!!.id }

                    StoryViewerDialog(
                        story = viewingStory!!,
                        onDismiss = { viewingStory = null },
                        onNext = {
                            if (currentStoryIndex in 0 until stories.size - 1) {
                                viewingStory = stories[currentStoryIndex + 1]
                            } else {
                                viewingStory = null
                            }
                        },
                        onPrevious = {
                            if (currentStoryIndex > 0) {
                                viewingStory = stories[currentStoryIndex - 1]
                            }
                        },
                        language = currentLanguage
                    )
                }
            }
        }
    }
}
