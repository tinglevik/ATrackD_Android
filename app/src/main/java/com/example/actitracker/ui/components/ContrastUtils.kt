package com.example.actitracker.ui.components

import androidx.compose.ui.graphics.Color
import kotlin.math.pow

object ContrastUtils {

    const val MIN_CONTRAST_RATIO = 4.5

    // ── Luminance & contrast ──────────────────────────────────────────────

    fun relativeLuminance(color: Color): Double {
        fun linearize(c: Float): Double =
            if (c <= 0.03928f) c / 12.92
            else ((c + 0.055) / 1.055).pow(2.4)

        return 0.2126 * linearize(color.red) +
                0.7152 * linearize(color.green) +
                0.0722 * linearize(color.blue)
    }

    fun contrastRatio(a: Color, b: Color): Double {
        val la = relativeLuminance(a)
        val lb = relativeLuminance(b)
        val light = maxOf(la, lb)
        val dark = minOf(la, lb)
        return (light + 0.05) / (dark + 0.05)
    }

    fun isReadable(
        text: Color,
        background: Color,
        minRatio: Double = MIN_CONTRAST_RATIO
    ): Boolean = contrastRatio(text, background) >= minRatio

    // ── HSL helpers ───────────────────────────────────────────────────────

    /** Color → FloatArray(hue 0-360, sat 0-1, light 0-1) */
    fun toHsl(color: Color): FloatArray {
        val r = color.red; val g = color.green; val b = color.blue
        val max = maxOf(r, g, b); val min = minOf(r, g, b)
        val l = (max + min) / 2f
        if (max == min) return floatArrayOf(0f, 0f, l)

        val d = max - min
        val s = if (l > 0.5f) d / (2f - max - min) else d / (max + min)
        val h = when (max) {
            r -> 60f * ((g - b) / d + (if (g < b) 6f else 0f))
            g -> 60f * ((b - r) / d + 2f)
            else -> 60f * ((r - g) / d + 4f)
        }
        return floatArrayOf(h, s, l)
    }

    /** FloatArray(hue 0-360, sat 0-1, light 0-1) → Color */
    fun fromHsl(h: Float, s: Float, l: Float): Color {
        if (s == 0f) return Color(l, l, l)
        val q = if (l < 0.5f) l * (1f + s) else l + s - l * s
        val p = 2f * l - q
        fun hue2rgb(t0: Float): Float {
            var t = t0
            if (t < 0f) t += 1f; if (t > 1f) t -= 1f
            return when {
                t < 1 / 6f -> p + (q - p) * 6f * t
                t < 1 / 2f -> q
                t < 2 / 3f -> p + (q - p) * (2f / 3f - t) * 6f
                else -> p
            }
        }
        val hn = h / 360f
        return Color(hue2rgb(hn + 1 / 3f), hue2rgb(hn), hue2rgb(hn - 1 / 3f))
    }

    /**
     * Calculates the lightness in HSL at which the luminance provides the required contrast
     * with the given background luminance. Returns null if impossible.
     *
     * Formula: target_lum = ratio * (bgLum + 0.05) - 0.05
     *          or         = (bgLum + 0.05) / ratio - 0.05
     * Then we reverse sRGB linearization to get channel -> lightness.
     */
    private fun luminanceForContrast(bgLuminance: Double, ratio: Double, dark: Boolean): Double? {
        val targetLum = if (dark) {
            // text is darker than background
            (bgLuminance + 0.05) / ratio - 0.05
        } else {
            // text is lighter than background
            ratio * (bgLuminance + 0.05) - 0.05
        }
        return if (targetLum in 0.0..1.0) targetLum else null
    }

    /**
     * Approximately converts relative luminance to HSL lightness.
     * We use a numerical search since the analytical formula is complex.
     */
    private fun luminanceToLightness(targetLuminance: Double, hue: Float, sat: Float): Float {
        var lo = 0f; var hi = 1f
        repeat(20) {
            val mid = (lo + hi) / 2f
            val lum = relativeLuminance(fromHsl(hue, sat, mid))
            if (lum < targetLuminance) lo = mid else hi = mid
        }
        return (lo + hi) / 2f
    }

    // ── Public API ────────────────────────────────────────────────────────

    /**
     * Generates up to 4 contrasting text color options for a given background.
     * All options are calculated mathematically, rather than taken from a hardcoded list.
     */
    fun suggestTextColors(background: Color): List<Pair<String, Color>> {
        val bgLum = relativeLuminance(background)
        val bgHsl = toHsl(background)
        val hue = bgHsl[0]
        val sat = bgHsl[1]

        val results = mutableListOf<Pair<String, Color>>()

        // 1. Neutral (achromatic): black or white — mathematically guaranteed
        if (bgLum > 0.5) {
            results += "Dark neutral" to computeNeutralColor(bgLum, dark = true)
        } else {
            results += "Light neutral" to computeNeutralColor(bgLum, dark = false)
        }

        // 2. Same hue, but adjusted brightness
        val sameHueColor = computeColorWithHue(hue, sat.coerceAtLeast(0.3f), bgLum)
        if (sameHueColor != null && isReadable(sameHueColor, background)) {
            results += "Same hue" to sameHueColor
        }

        // 3. Opposite hue (complementary, +180°)
        val compHue = (hue + 180f) % 360f
        val compColor = computeColorWithHue(compHue, 0.6f, bgLum)
        if (compColor != null && isReadable(compColor, background)) {
            results += "Complementary" to compColor
        }

        // 4. Analogous hue (+60°)
        val analogHue = (hue + 60f) % 360f
        val analogColor = computeColorWithHue(analogHue, 0.5f, bgLum)
        if (analogColor != null && isReadable(analogColor, background)) {
            results += "Analogous" to analogColor
        }

        return results.distinctBy {
            // Remove duplicates based on color proximity
            (it.second.red * 10).toInt() * 1000 +
                    (it.second.green * 10).toInt() * 100 +
                    (it.second.blue * 10).toInt()
        }
    }

    /**
     * Generates contrasting background options for a given text color.
     */
    fun suggestBackgroundColors(text: Color): List<Pair<String, Color>> {
        // The logic is symmetric: looking for a background contrasting with the text
        val textLum = relativeLuminance(text)
        val textHsl = toHsl(text)
        val hue = textHsl[0]
        val sat = textHsl[1]

        val results = mutableListOf<Pair<String, Color>>()

        // 1. Neutral
        if (textLum > 0.5) {
            results += "Dark neutral" to computeNeutralColor(textLum, dark = true)
        } else {
            results += "Light neutral" to computeNeutralColor(textLum, dark = false)
        }

        // 2. Same hue
        val sameHue = computeColorWithHue(hue, sat.coerceAtLeast(0.2f), textLum)
        if (sameHue != null && isReadable(text, sameHue)) {
            results += "Same hue" to sameHue
        }

        // 3. Complementary
        val compHue = (hue + 180f) % 360f
        val comp = computeColorWithHue(compHue, 0.5f, textLum)
        if (comp != null && isReadable(text, comp)) {
            results += "Complementary" to comp
        }

        // 4. Analogous
        val analogHue = (hue + 60f) % 360f
        val analog = computeColorWithHue(analogHue, 0.4f, textLum)
        if (analog != null && isReadable(text, analog)) {
            results += "Analogous" to analog
        }

        return results.distinctBy {
            (it.second.red * 10).toInt() * 1000 +
                    (it.second.green * 10).toInt() * 100 +
                    (it.second.blue * 10).toInt()
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────

    /**
     * Calculates a neutral (gray) color contrasting with bgLum.
     */
    private fun computeNeutralColor(bgLum: Double, dark: Boolean): Color {
        val targetLum = luminanceForContrast(bgLum, MIN_CONTRAST_RATIO, dark)
            ?: if (dark) 0.0 else 1.0
        // For neutral hue=0, sat=0 -> lightness ≈ sqrt(targetLum) via sRGB
        val lightness = luminanceToLightness(targetLum, 0f, 0f)
        return fromHsl(0f, 0f, lightness)
    }

    /**
     * Calculates a color with the desired hue and saturation, adjusted for brightness.
     */
    private fun computeColorWithHue(hue: Float, sat: Float, bgLum: Double): Color? {
        // Determine whether a dark or light color is needed
        val needDark = bgLum > 0.5

        val targetLum = luminanceForContrast(bgLum, MIN_CONTRAST_RATIO, needDark)
            ?: return null

        val lightness = luminanceToLightness(targetLum, hue, sat)
        val candidate = fromHsl(hue, sat, lightness)

        // Final check
        return if (isReadable(candidate, Color(
                android.graphics.Color.HSVToColor(
                    floatArrayOf(0f, 0f, bgLum.toFloat())
                )
            ))
        ) candidate else null
    }
}