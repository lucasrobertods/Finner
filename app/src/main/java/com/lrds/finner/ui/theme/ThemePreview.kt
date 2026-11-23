package com.lrds.finner.ui.theme

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview

@Composable
private fun FinnerTokenShowcase() {
    val colors = FinnerTheme.colors
    val spacing = FinnerTheme.spacing
    val sizes = FinnerTheme.sizes
    val shapes = FinnerTheme.shapes
    val typography = FinnerTheme.typography
    val numeric = FinnerTheme.numericStyles

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.background)
            .padding(spacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(spacing.sectionGap),
    ) {
        Text(
            text = "ACTUAL SPARE CASH",
            style = typography.labelSmall,
            color = colors.onSurfaceVariant,
        )
        Text(
            text = "€ 1.245,80",
            style = numeric.heroAmount,
            color = colors.onSurface,
        )
        Surface(
            shape = shapes.sectionCard,
            color = colors.surfaceContainer,
            border = BorderStroke(sizes.borderThin, colors.cardBorder),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(spacing.cardPadding),
                verticalArrangement = Arrangement.spacedBy(spacing.sm),
            ) {
                Text(
                    text = "Semantic Status & Buckets",
                    style = typography.titleMediumBold,
                    color = colors.onSurface,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    listOf("Bills", "Reserve", "Joint", "Personal", "Trip").forEach { bucket ->
                        val bucketColor = colors.buckets.forName(bucket)
                        Surface(
                            shape = shapes.pill,
                            color = bucketColor.copy(alpha = FinnerAlpha.tintedContainer),
                        ) {
                            Row(
                                modifier = Modifier.padding(
                                    horizontal = spacing.badgeHorizontal,
                                    vertical = spacing.xs,
                                ),
                                horizontalArrangement = Arrangement.spacedBy(spacing.xs),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(sizes.statusDot)
                                        .clip(shapes.pill)
                                        .background(bucketColor),
                                )
                                Text(
                                    text = bucket,
                                    style = typography.labelSmall,
                                    color = bucketColor,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(name = "Finner Dark Theme", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun FinnerThemeDarkPreview() {
    FinnerTheme(darkTheme = true) {
        FinnerTokenShowcase()
    }
}

@Preview(name = "Finner Light Theme", uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun FinnerThemeLightPreview() {
    FinnerTheme(darkTheme = false) {
        FinnerTokenShowcase()
    }
}
