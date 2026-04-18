package com.example.atrackd.ui.components

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
     * Вычисляет lightness в HSL, при котором luminance даёт нужный контраст
     * с заданным фоновым luminance. Возвращает null, если невозможно.
     *
     * Формула: target_lum = ratio * (bgLum + 0.05) - 0.05
     *          или        = (bgLum + 0.05) / ratio - 0.05
     * Затем обращаем sRGB-linearization, чтобы получить channel → lightness.
     */
    private fun luminanceForContrast(bgLuminance: Double, ratio: Double, dark: Boolean): Double? {
        val targetLum = if (dark) {
            // текст темнее фона
            (bgLuminance + 0.05) / ratio - 0.05
        } else {
            // текст светлее фона
            ratio * (bgLuminance + 0.05) - 0.05
        }
        return if (targetLum in 0.0..1.0) targetLum else null
    }

    /**
     * Приближённо переводит relative luminance в HSL-lightness.
     * Используем численный поиск, т.к. аналитическая формула сложная.
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
     * Генерирует до 4 контрастных вариантов цвета текста для данного фона.
     * Все варианты вычисляются математически, а не берутся из хардкод-списка.
     */
    fun suggestTextColors(background: Color): List<Pair<String, Color>> {
        val bgLum = relativeLuminance(background)
        val bgHsl = toHsl(background)
        val hue = bgHsl[0]
        val sat = bgHsl[1]

        val results = mutableListOf<Pair<String, Color>>()

        // 1. Нейтральный (ахроматический): чёрный или белый — математически гарантирован
        if (bgLum > 0.5) {
            results += "Dark neutral" to computeNeutralColor(bgLum, dark = true)
        } else {
            results += "Light neutral" to computeNeutralColor(bgLum, dark = false)
        }

        // 2. Тот же оттенок (hue), но скорректированная яркость
        val sameHueColor = computeColorWithHue(hue, sat.coerceAtLeast(0.3f), bgLum)
        if (sameHueColor != null && isReadable(sameHueColor, background)) {
            results += "Same hue" to sameHueColor
        }

        // 3. Противоположный оттенок (complementary, +180°)
        val compHue = (hue + 180f) % 360f
        val compColor = computeColorWithHue(compHue, 0.6f, bgLum)
        if (compColor != null && isReadable(compColor, background)) {
            results += "Complementary" to compColor
        }

        // 4. Аналогичный оттенок (+60°)
        val analogHue = (hue + 60f) % 360f
        val analogColor = computeColorWithHue(analogHue, 0.5f, bgLum)
        if (analogColor != null && isReadable(analogColor, background)) {
            results += "Analogous" to analogColor
        }

        return results.distinctBy {
            // Убираем дубликаты по близости цветов
            (it.second.red * 10).toInt() * 1000 +
                    (it.second.green * 10).toInt() * 100 +
                    (it.second.blue * 10).toInt()
        }
    }

    /**
     * Генерирует контрастные варианты фона для данного цвета текста.
     */
    fun suggestBackgroundColors(text: Color): List<Pair<String, Color>> {
        // Логика симметрична: ищем фон, контрастный к тексту
        val textLum = relativeLuminance(text)
        val textHsl = toHsl(text)
        val hue = textHsl[0]
        val sat = textHsl[1]

        val results = mutableListOf<Pair<String, Color>>()

        // 1. Нейтральный
        if (textLum > 0.5) {
            results += "Dark neutral" to computeNeutralColor(textLum, dark = true)
        } else {
            results += "Light neutral" to computeNeutralColor(textLum, dark = false)
        }

        // 2. Тот же оттенок
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
     * Вычисляет нейтральный (серый) цвет, контрастный к bgLum.
     */
    private fun computeNeutralColor(bgLum: Double, dark: Boolean): Color {
        val targetLum = luminanceForContrast(bgLum, MIN_CONTRAST_RATIO, dark)
            ?: if (dark) 0.0 else 1.0
        // Для нейтрального hue=0, sat=0 → lightness ≈ sqrt(targetLum) через sRGB
        val lightness = luminanceToLightness(targetLum, 0f, 0f)
        return fromHsl(0f, 0f, lightness)
    }

    /**
     * Вычисляет цвет с нужным hue и saturation, скорректированный по яркости.
     */
    private fun computeColorWithHue(hue: Float, sat: Float, bgLum: Double): Color? {
        // Определяем, нужен тёмный или светлый цвет
        val needDark = bgLum > 0.5

        val targetLum = luminanceForContrast(bgLum, MIN_CONTRAST_RATIO, needDark)
            ?: return null

        val lightness = luminanceToLightness(targetLum, hue, sat)
        val candidate = fromHsl(hue, sat, lightness)

        // Финальная проверка
        return if (isReadable(candidate, Color(
                android.graphics.Color.HSVToColor(
                    floatArrayOf(0f, 0f, bgLum.toFloat())
                )
            ))
        ) candidate else null
    }
}