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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

@Composable
fun AppHeader(
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .wrapContentHeight(unbounded = true)
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.00f to MaterialTheme.colorScheme.background,
                            0.48f to MaterialTheme.colorScheme.background,
                            0.78f to MaterialTheme.colorScheme.background.copy(alpha = 0.45f),
                            1.00f to Color.Transparent
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(start = 20.dp)
                .offset(y = (-8).dp)
                .size(42.dp)
                .clip(CircleShape)
                .clickable { onBack() },
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

            Icon(
                imageVector = Icons.Outlined.ArrowBackIosNew,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(17.dp)
            )
        }
    }
}