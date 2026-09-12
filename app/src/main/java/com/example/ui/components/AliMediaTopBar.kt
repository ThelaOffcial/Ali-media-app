package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.locale.AppLanguage
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCardBg
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.ForestGreenDark
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.LightBg
import com.example.ui.theme.LightBorder
import com.example.ui.theme.LightPillBg

@Composable
fun AliMediaTopBar(
    currentLanguage: AppLanguage,
    onToggleLanguage: () -> Unit,
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    onNotificationsClick: () -> Unit,
    unreadNoticesCount: Int = 2,
    isSyncing: Boolean = false,
    onRefreshClick: (() -> Unit)? = null
) {
    val barBg = if (isDarkMode) DarkBg else LightBg
    val borderColor = if (isDarkMode) DarkBorder else LightBorder
    val controlBg = if (isDarkMode) DarkCardBg else LightPillBg
    val primaryTextColor = if (isDarkMode) EmeraldAccent else ForestGreenPrimary

    Surface(
        color = barBg,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Brand Logo: Official AliMedia Ribbon Logo
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.testTag("brand_logo_header")
                ) {
                    Image(
                        painter = painterResource(
                            id = if (isDarkMode) R.drawable.ic_alimedia_logo_dark else R.drawable.ic_alimedia_logo
                        ),
                        contentDescription = "AliMedia Logo",
                        modifier = Modifier
                            .height(38.dp)
                            .padding(vertical = 1.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                // Action Controls: Language Switcher, Theme Switcher, RTDB Sync, Notifications
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Language Switcher Pill (EN / සිං)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(controlBg)
                            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
                            .clickable { onToggleLanguage() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("language_toggle_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (currentLanguage == AppLanguage.ENGLISH) "සිංහල" else "EN",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryTextColor
                        )
                    }

                    // Dark / Light Mode Switcher
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(controlBg)
                            .border(1.dp, borderColor, CircleShape)
                            .clickable { onToggleDarkMode() }
                            .testTag("theme_toggle_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Theme",
                            tint = if (isDarkMode) AmberAccent else ForestGreenPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // RTDB Sync Button
                    if (onRefreshClick != null) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(controlBg)
                                .border(1.dp, borderColor, CircleShape)
                                .clickable { onRefreshClick() }
                                .testTag("rtdb_sync_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSyncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = EmeraldAccent
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Sync with Realtime Database",
                                    tint = primaryTextColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // Notification Bell with badge
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(controlBg)
                            .border(1.dp, borderColor, CircleShape)
                            .clickable { onNotificationsClick() }
                            .testTag("notifications_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        BadgedBox(
                            badge = {
                                if (unreadNoticesCount > 0) {
                                    Badge(
                                        containerColor = EmeraldAccent,
                                        contentColor = Color.White
                                    ) {
                                        Text(
                                            text = "$unreadNoticesCount",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notices & Announcements",
                                tint = if (isDarkMode) Color.White else Color(0xFF27272A),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Crisp 1.dp bottom separator matching border-zinc-200
            HorizontalDivider(
                thickness = 1.dp,
                color = borderColor
            )
        }
    }
}
