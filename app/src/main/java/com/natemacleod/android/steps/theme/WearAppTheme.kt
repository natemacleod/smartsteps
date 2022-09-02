package com.natemacleod.android.steps.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material.MaterialTheme
import com.natemacleod.android.steps.theme.Typography
import com.natemacleod.android.steps.theme.wearColorPalette

@Composable
fun WearAppTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = wearColorPalette,
        typography = Typography,
        // For shapes, we generally recommend using the default Material Wear shapes which are
        // optimized for round and non-round devices.
        content = content
    )
}
