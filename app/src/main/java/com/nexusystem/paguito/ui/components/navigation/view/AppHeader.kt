package com.nexusystem.paguito.ui.components.navigation.view

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AppHeader(
    onBack: () -> Unit,
    title: String = "",
    showTitle: Boolean = title.isNotBlank(),
    rightIcon: ImageVector? = null,
    rightIconTint: Color = MaterialTheme.colorScheme.primary,
    onRightClick: (() -> Unit)? = null,
    rightActionText: String = "",
    rightActionTextColor: Color = MaterialTheme.colorScheme.primary,
    onRightActionClick: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(88.dp)
            .wrapContentHeight(unbounded = true)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(88.dp)
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.00f to MaterialTheme.colorScheme.background,
                            0.55f to MaterialTheme.colorScheme.background,
                            0.82f to MaterialTheme.colorScheme.background.copy(alpha = 0.55f),
                            1.00f to Color.Transparent
                        )
                    )
                )
        )

        if (showTitle) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 5.dp)
            )
        }

        HeaderIconButton(
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(start = 20.dp)
                .offset(y = (-8).dp),
            onClick = onBack
        ) {
            Icon(
                imageVector = Icons.Outlined.ArrowBackIosNew,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(17.dp)
            )
        }

        when {
            rightActionText.isNotBlank() -> {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .statusBarsPadding()
                        .padding(end = 20.dp)
                        .offset(y = (-8).dp)
                        .height(42.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.96f))
                        .clickable { onRightActionClick?.invoke() }
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = rightActionText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = rightActionTextColor
                    )
                }
            }

            rightIcon != null -> {
                HeaderIconButton(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .statusBarsPadding()
                        .padding(end = 20.dp)
                        .offset(y = (-8).dp),
                    onClick = { onRightClick?.invoke() }
                ) {
                    Icon(
                        imageVector = rightIcon,
                        contentDescription = null,
                        tint = rightIconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun HeaderIconButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .size(42.dp)
            .clip(CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(CircleShape)
                .then(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        Modifier.graphicsLayer {
                            renderEffect =
                                android.graphics.RenderEffect
                                    .createBlurEffect(
                                        24f,
                                        24f,
                                        android.graphics.Shader.TileMode.CLAMP
                                    )
                                    .asComposeRenderEffect()
                        }
                    } else {
                        Modifier
                    }
                )
                .background(
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
                )
        )

        content()
    }
}