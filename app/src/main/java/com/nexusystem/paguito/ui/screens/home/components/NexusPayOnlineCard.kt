// Path:
// app/src/main/java/com/nexusystem/paguito/ui/screens/home/components/NexusPayOnlineCard.kt

package com.nexusystem.paguito.ui.screens.home.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.LocalActivity
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexusystem.paguito.ui.theme.WebsiteBorderDark
import com.nexusystem.paguito.ui.theme.WebsiteBorderLight
import com.nexusystem.paguito.ui.theme.WebsiteCardDark
import com.nexusystem.paguito.ui.theme.WebsiteCardLight
import com.nexusystem.paguito.ui.theme.WebsiteGreen
import com.nexusystem.paguito.ui.theme.WebsitePrimary
import com.nexusystem.paguito.ui.theme.WebsiteSurfaceDark
import com.nexusystem.paguito.ui.theme.WebsiteSurfaceLight
import com.nexusystem.paguito.ui.theme.WebsiteTextPrimaryDark
import com.nexusystem.paguito.ui.theme.WebsiteTextPrimaryLight
import com.nexusystem.paguito.ui.theme.WebsiteTextSecondaryDark
import com.nexusystem.paguito.ui.theme.WebsiteTextSecondaryLight

private val TicketOrange = Color(0xFFFFA313)

@Composable
fun NexusPayOnlineCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    val cardBackground = if (isDark) {
        WebsiteCardDark
    } else {
        WebsiteCardLight
    }

    val cardBorder = if (isDark) {
        WebsiteBorderDark
    } else {
        WebsiteBorderLight
    }

    val titleColor = if (isDark) {
        WebsiteTextPrimaryDark
    } else {
        WebsiteTextPrimaryLight
    }

    val subtitleColor = if (isDark) {
        WebsiteTextSecondaryDark
    } else {
        WebsiteTextSecondaryLight
    }

    val neutralBadgeBackground = if (isDark) {
        WebsiteSurfaceDark
    } else {
        WebsiteSurfaceLight
    }

    val floatingIconBackground = if (isDark) {
        WebsiteSurfaceDark
    } else {
        WebsiteCardLight
    }

    val centerOuterBackground = if (isDark) {
        WebsiteSurfaceDark
    } else {
        WebsiteSurfaceLight
    }

    val centerInnerBackground = if (isDark) {
        WebsiteCardDark
    } else {
        WebsiteCardLight
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardBackground
        ),
        border = BorderStroke(
            width = 1.dp,
            color = cardBorder
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(166.dp)
                .padding(
                    start = 24.dp,
                    end = 14.dp,
                    top = 22.dp,
                    bottom = 22.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Cobra en línea",
                    color = titleColor,
                    fontSize = 24.sp,
                    lineHeight = 29.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Recibe pagos al instante",
                    color = subtitleColor,
                    fontSize = 16.sp,
                    lineHeight = 21.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NexusPayBadge(
                        text = "RÁPIDO",
                        background = WebsiteGreen.copy(
                            alpha = if (isDark) {
                                0.18f
                            } else {
                                0.12f
                            }
                        ),
                        contentColor = WebsiteGreen
                    )

                    NexusPayBadge(
                        text = "SEGURO",
                        background = neutralBadgeBackground,
                        contentColor = subtitleColor
                    )
                }
            }

            NexusPayRippleGraphic(
                modifier = Modifier.size(132.dp),
                rippleColor = WebsitePrimary,
                outerBackground = centerOuterBackground,
                innerBackground = centerInnerBackground,
                floatingBackground = floatingIconBackground
            )
        }
    }
}

@Composable
private fun NexusPayBadge(
    text: String,
    background: Color,
    contentColor: Color
) {
    Box(
        modifier = Modifier
            .background(
                color = background,
                shape = RoundedCornerShape(50)
            )
            .padding(
                horizontal = 13.dp,
                vertical = 6.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = contentColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.6.sp
        )
    }
}

@Composable
private fun NexusPayRippleGraphic(
    rippleColor: Color,
    outerBackground: Color,
    innerBackground: Color,
    floatingBackground: Color,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(
        label = "NexusPayWaterRipple"
    )

    val rippleOneProgress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2_200,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "RippleOne"
    )

    val rippleTwoProgress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2_200,
                delayMillis = 720,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "RippleTwo"
    )

    val rippleThreeProgress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2_200,
                delayMillis = 1_440,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "RippleThree"
    )

    val floatingProgress by transition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1_600
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "FloatingIcons"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        NexusRippleCircle(
            progress = rippleOneProgress,
            baseSize = 66,
            color = rippleColor
        )

        NexusRippleCircle(
            progress = rippleTwoProgress,
            baseSize = 66,
            color = rippleColor
        )

        NexusRippleCircle(
            progress = rippleThreeProgress,
            baseSize = 66,
            color = rippleColor
        )

        Box(
            modifier = Modifier
                .size(68.dp)
                .background(
                    color = outerBackground,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(
                        color = innerBackground,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Link,
                    contentDescription = null,
                    tint = WebsiteGreen,
                    modifier = Modifier.size(29.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(
                    x = (-5).dp,
                    y = 7.dp + floatingProgress.dp
                )
                .size(37.dp)
                .background(
                    color = floatingBackground,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Shield,
                contentDescription = null,
                tint = WebsitePrimary,
                modifier = Modifier.size(20.dp)
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(
                    x = 5.dp,
                    y = (-6).dp - floatingProgress.dp
                )
                .size(37.dp)
                .background(
                    color = floatingBackground,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.LocalActivity,
                contentDescription = null,
                tint = TicketOrange,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun NexusRippleCircle(
    progress: Float,
    baseSize: Int,
    color: Color
) {
    val scale = 1f + progress * 0.85f
    val alpha = (1f - progress) * 0.22f

    Box(
        modifier = Modifier
            .size(baseSize.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .alpha(alpha)
            .background(
                color = color,
                shape = CircleShape
            )
    )
}

private fun Color.luminance(): Float {
    val red = red
    val green = green
    val blue = blue

    return (0.2126f * red) +
            (0.7152f * green) +
            (0.0722f * blue)
}