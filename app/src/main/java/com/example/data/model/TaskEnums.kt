package com.example.data.model

import androidx.compose.ui.graphics.Color

enum class Priority(val label: String, val level: Int) {
    KRYTYCZNY("Krytyczny", 1),
    WYSOKI("Wysoki", 2),
    SREDNI("Średni", 3),
    NISKI("Niski", 4);

    fun getColor(isDark: Boolean = true): Color {
        return when (this) {
            KRYTYCZNY -> if (isDark) Color(0xFFF87171) else Color(0xFFDC2626)
            WYSOKI -> if (isDark) Color(0xFFFB923C) else Color(0xFFEA580C)
            SREDNI -> if (isDark) Color(0xFFFBBF24) else Color(0xFFD97706)
            NISKI -> if (isDark) Color(0xFF34D399) else Color(0xFF059669)
        }
    }
}

enum class TaskStatus(val label: String) {
    NOWE("Nowe"),
    W_TRAKCIE("W trakcie"),
    WSTRZYMANE("Wstrzymane"),
    ZAKONCZONE("Zakończone");

    fun getColor(isDark: Boolean = true): Color {
        return when (this) {
            NOWE -> if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
            W_TRAKCIE -> if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)
            WSTRZYMANE -> if (isDark) Color(0xFFFBBF24) else Color(0xFFD97706)
            ZAKONCZONE -> if (isDark) Color(0xFF4ADE80) else Color(0xFF16A34A)
        }
    }
}

enum class TaskType(val label: String) {
    AWARIA("Przyjazd awaryjny (250 zł)"),
    PRZEGLAD("Przegląd okresowy"),
    KONSERWACJA("Konserwacja / Smarowanie"),
    MODYFIKACJA("Przezbrojenie / Modyfikacja"),
    NADGODZINY("Nadgodziny")
}
