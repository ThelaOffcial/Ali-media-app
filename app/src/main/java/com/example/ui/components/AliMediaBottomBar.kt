package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.locale.AppLanguage
import com.example.data.locale.AppStrings
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.ForestGreenDark
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.LightBg
import com.example.ui.theme.LightBorder

enum class AppTab {
    FEED,
    ELEPHANTS,
    CREATE,
    NOTICES,
    PROFILE
}

@Composable
fun AliMediaBottomBar(
    selectedTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
    language: AppLanguage,
    isDarkMode: Boolean
) {
    val barBg = if (isDarkMode) DarkBg else LightBg
    val borderColor = if (isDarkMode) DarkBorder else LightBorder
    val activeColor = if (isDarkMode) EmeraldAccent else ForestGreenPrimary
    val inactiveColor = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF71717A)

    Surface(
        color = barBg,
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            HorizontalDivider(
                thickness = 1.dp,
                color = borderColor
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(62.dp)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                // Tab 1: Feed
                BottomBarItem(
                    title = AppStrings.feed(language),
                    selectedIcon = Icons.Filled.Home,
                    unselectedIcon = Icons.Outlined.Home,
                    isSelected = selectedTab == AppTab.FEED,
                    activeColor = activeColor,
                    inactiveColor = inactiveColor,
                    onClick = { onTabSelected(AppTab.FEED) },
                    testTag = "tab_feed"
                )

                // Tab 2: Elephants
                BottomBarItem(
                    title = AppStrings.elephants(language),
                    selectedIcon = Icons.Filled.Pets,
                    unselectedIcon = Icons.Outlined.Pets,
                    isSelected = selectedTab == AppTab.ELEPHANTS,
                    activeColor = activeColor,
                    inactiveColor = inactiveColor,
                    onClick = { onTabSelected(AppTab.ELEPHANTS) },
                    testTag = "tab_elephants"
                )

                // Tab 3: Center Elevated Create Button with smooth spring feedback
                var createButtonPressed by remember { mutableStateOf(false) }
                val createButtonScale by animateFloatAsState(
                    targetValue = if (createButtonPressed) 0.90f else 1.0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    ),
                    label = "create_scale"
                )

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .scale(createButtonScale)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(ForestGreenPrimary, ForestGreenDark)
                            )
                        )
                        .clickable {
                            createButtonPressed = true
                            onTabSelected(AppTab.CREATE)
                        }
                        .testTag("tab_create"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = AppStrings.create(language),
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }

                // Tab 4: Notices
                BottomBarItem(
                    title = AppStrings.notices(language),
                    selectedIcon = Icons.Filled.Campaign,
                    unselectedIcon = Icons.Outlined.Campaign,
                    isSelected = selectedTab == AppTab.NOTICES,
                    activeColor = activeColor,
                    inactiveColor = inactiveColor,
                    onClick = { onTabSelected(AppTab.NOTICES) },
                    testTag = "tab_notices"
                )

                // Tab 5: Profile
                BottomBarItem(
                    title = AppStrings.profile(language),
                    selectedIcon = Icons.Filled.Person,
                    unselectedIcon = Icons.Outlined.Person,
                    isSelected = selectedTab == AppTab.PROFILE,
                    activeColor = activeColor,
                    inactiveColor = inactiveColor,
                    onClick = { onTabSelected(AppTab.PROFILE) },
                    testTag = "tab_profile"
                )
            }
        }
    }
}

@Composable
private fun BottomBarItem(
    title: String,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    isSelected: Boolean,
    activeColor: Color,
    inactiveColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    val tabScale by animateFloatAsState(
        targetValue = if (isSelected) 1.12f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "tab_scale"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) activeColor else inactiveColor,
        animationSpec = tween(220),
        label = "tab_color"
    )

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (isSelected) selectedIcon else unselectedIcon,
            contentDescription = title,
            tint = contentColor,
            modifier = Modifier
                .size(22.dp)
                .scale(tabScale)
        )
        Text(
            text = title,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = contentColor
        )
        AnimatedVisibility(
            visible = isSelected,
            enter = scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
            exit = scaleOut() + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(activeColor)
            )
        }
    }
}
