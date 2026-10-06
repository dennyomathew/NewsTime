package com.dennymathew.newstime.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Brand colors carried over from the original app.
private val Indigo = Color(0xFF3F51B5)
private val IndigoLight = Color(0xFF9FA8DA)
private val Pink = Color(0xFFFF4081)
private val PinkLight = Color(0xFFFF80AB)

private val LightColors = lightColorScheme(primary = Indigo, secondary = Pink)
private val DarkColors = darkColorScheme(primary = IndigoLight, secondary = PinkLight)

@Composable
fun NewsTimeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
