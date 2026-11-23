package com.lrds.finner

import androidx.compose.ui.unit.dp
import com.lrds.finner.ui.theme.DarkFinnerBucketColors
import com.lrds.finner.ui.theme.DarkFinnerColors
import com.lrds.finner.ui.theme.DefaultFinnerElevation
import com.lrds.finner.ui.theme.DefaultFinnerNumericStyles
import com.lrds.finner.ui.theme.DefaultFinnerRadius
import com.lrds.finner.ui.theme.DefaultFinnerShapes
import com.lrds.finner.ui.theme.DefaultFinnerSizes
import com.lrds.finner.ui.theme.DefaultFinnerSpacing
import com.lrds.finner.ui.theme.DefaultFinnerTypography
import com.lrds.finner.ui.theme.FinnerRadius
import com.lrds.finner.ui.theme.FinnerShapeTokens
import com.lrds.finner.ui.theme.LightFinnerBucketColors
import com.lrds.finner.ui.theme.LightFinnerColors
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DesignSystemTokensTest {

    @Test
    fun `spacing scale is strictly monotonic`() {
        val s = DefaultFinnerSpacing
        assertTrue(s.xxs < s.xs)
        assertTrue(s.xs < s.sm)
        assertTrue(s.sm < s.md)
        assertTrue(s.md < s.lg)
        assertTrue(s.lg < s.xl)
        assertTrue(s.xl < s.xxl)
        assertTrue(s.xxl < s.xxxl)
    }

    @Test
    fun `sizes icon scale is strictly monotonic and touch target meets accessibility minimum`() {
        val sizes = DefaultFinnerSizes
        assertTrue(sizes.iconXs < sizes.iconSm)
        assertTrue(sizes.iconSm < sizes.iconMd)
        assertTrue(sizes.iconMd < sizes.iconLg)
        assertTrue(sizes.iconLg < sizes.iconXl)
        assertTrue(sizes.iconXl < sizes.iconXxl)
        assertTrue(sizes.minTouchTarget >= 48.dp)
        assertTrue(sizes.primaryButtonHeight >= sizes.minTouchTarget)
        assertTrue(sizes.quickActionButton >= sizes.minTouchTarget)
    }

    @Test
    fun `elevation scale is strictly monotonic`() {
        val e = DefaultFinnerElevation
        assertEquals(0.dp, e.none)
        assertTrue(e.none < e.sm)
        assertTrue(e.sm < e.md)
        assertTrue(e.md < e.lg)
        assertTrue(e.lg < e.floatingDock)
    }

    @Test
    fun `radius scale is strictly monotonic and maps cleanly to material shapes`() {
        val r = DefaultFinnerRadius
        assertTrue(r.xs < r.sm)
        assertTrue(r.sm < r.md)
        assertTrue(r.md < r.lg)
        assertTrue(r.lg < r.xl)
        assertTrue(r.xl < r.dock)

        val customShapes = FinnerShapeTokens.fromRadius(FinnerRadius())
        assertEquals(DefaultFinnerShapes.toMaterialShapes(), customShapes.toMaterialShapes())
    }

    @Test
    fun `all numeric and typography styles enforce tabular lining numerals`() {
        val expected = "tnum, lnum"
        val numeric = DefaultFinnerNumericStyles
        listOf(
            numeric.heroAmount,
            numeric.sectionAmount,
            numeric.cardAmount,
            numeric.rowAmount,
            numeric.rowAmountBold,
            numeric.metaAmount,
        ).forEach { style ->
            assertEquals(expected, style.fontFeatureSettings)
        }

        val t = DefaultFinnerTypography
        listOf(
            t.displayLarge, t.displayMedium, t.displaySmall,
            t.headlineLarge, t.headlineMedium, t.headlineSmall,
            t.titleLarge, t.titleMedium, t.titleMediumBold, t.titleSmall, t.titleSmallBold,
            t.bodyLarge, t.bodyMedium, t.bodyMediumSemiBold, t.bodyMediumBold, t.bodySmall, t.bodySmallSemiBold,
            t.labelLarge, t.labelLargeMedium, t.labelLargeBold,
            t.labelMedium, t.labelMediumMedium, t.labelMediumBold,
            t.labelSmall, t.labelSmallMedium, t.labelSmallBold,
        ).forEach { style ->
            assertEquals(expected, style.fontFeatureSettings)
        }
    }

    @Test
    fun `light and dark palettes expose distinct surfaces and stay synced with material color scheme`() {
        assertFalse(LightFinnerColors.isDark)
        assertTrue(DarkFinnerColors.isDark)
        assertNotEquals(LightFinnerColors.background, DarkFinnerColors.background)
        assertNotEquals(LightFinnerColors.surfaceContainer, DarkFinnerColors.surfaceContainer)
        assertNotEquals(LightFinnerColors.inflow, DarkFinnerColors.inflow)

        val darkMaterial = DarkFinnerColors.toMaterialColorScheme()
        assertEquals(DarkFinnerColors.primary, darkMaterial.primary)
        assertEquals(DarkFinnerColors.background, darkMaterial.background)
        assertEquals(DarkFinnerColors.surfaceContainer, darkMaterial.surfaceContainer)

        val lightMaterial = LightFinnerColors.toMaterialColorScheme()
        assertEquals(LightFinnerColors.primary, lightMaterial.primary)
        assertEquals(LightFinnerColors.background, lightMaterial.background)
        assertEquals(LightFinnerColors.surfaceContainer, lightMaterial.surfaceContainer)
    }

    @Test
    fun `bucket colors resolve known envelopes case-insensitively and hash custom names deterministically`() {
        listOf(DarkFinnerBucketColors, LightFinnerBucketColors).forEach { palette ->
            assertEquals(palette.fallback, palette.forName(null))
            assertEquals(palette.fallback, palette.forName("   "))
            assertEquals(palette.bills, palette.forName("Bills"))
            assertEquals(palette.bills, palette.forName(" bills "))
            assertEquals(palette.reserve, palette.forName("RESERVE"))
            assertEquals(palette.joint, palette.forName("Joint"))
            assertEquals(palette.personal, palette.forName("Personal"))
            assertEquals(palette.trip, palette.forName("trip"))

            val customFirst = palette.forName("Investments")
            val customSecond = palette.forName(" investments ")
            assertEquals(customFirst, customSecond)
            assertTrue(customFirst in palette.customPalette)
        }
        assertNotEquals(LightFinnerBucketColors.bills, DarkFinnerBucketColors.bills)
    }
}
