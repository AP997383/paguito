package com.nexusystem.paguito.ui.components

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.nexusystem.paguito.utils.findActivity

fun requestedAppOrientation(widthDp: Int, heightDp: Int): Int =
    if (widthDp < 600 || heightDp < 600) ActivityInfo.SCREEN_ORIENTATION_PORTRAIT else ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

/** Android's compact-screen recipe: keep phones upright, let large windows follow the system. */
@SuppressLint("SourceLockedOrientationActivity")
@Composable
fun CompactPortraitPolicy() {
    val activity = LocalContext.current.findActivity()
    val configuration = LocalConfiguration.current
    val target = requestedAppOrientation(configuration.screenWidthDp, configuration.screenHeightDp)
    LaunchedEffect(activity, target) {
        if (activity != null && activity.requestedOrientation != target) activity.requestedOrientation = target
    }
}
