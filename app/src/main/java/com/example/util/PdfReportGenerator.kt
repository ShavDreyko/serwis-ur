package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.db.Converters
import com.example.data.model.ExtraWork
import com.example.data.model.MaintenanceTask
import com.example.data.model.TaskNote
import com.example.data.model.TaskType
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfReportGenerator {

    fun generateMonthlyPdf(
        context: Context,
        monthYearLabel: String,
        technicianName: String,
        tasks: List<MaintenanceTask>,
        extraWorks: List<ExtraWork>,
        includePhotos: Boolean = false
    ): File? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Standard A4 points
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        val paint = Paint().apply { isAntiAlias = true }
        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        val dateOnlyFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

        // Header Background Banner
        paint.color = Color.rgb(15, 23, 42) // Slate 900
        canvas.drawRect(0f, 0f, 595f, 90f, paint)

        // Accent strip
        paint.color = Color.rgb(245, 158, 11) // Amber 500
        canvas.drawRect(0f, 86f, 595f, 90f, paint)

        // Header Title
        paint.color = Color.WHITE
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 18f
        canvas.drawText("ASYSYSTENT SERWISU UR - RAPORT MIESIĘCZNY", 30f, 40f, paint)

        paint.textSize = 11f
        paint.color = Color.rgb(203, 213, 225)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Okres rozliczeniowy: $monthYearLabel | Technik: $technicianName", 30f, 62f, paint)
        canvas.drawText("Wygenerowano: ${dateFormat.format(Date())}", 30f, 78f, paint)

        var y = 115f

        // Section 1: Summary KPI Boxes
        val totalFailures = tasks.count { it.type == TaskType.AWARIA }
        val solvedFailures = tasks.count { it.type == TaskType.AWARIA && it.status.name == "ZAKONCZONE" }
        val totalExtraHours = extraWorks.sumOf { it.hours }
        val emergencyCost = totalFailures * 250

        paint.color = Color.rgb(241, 245, 249) // Slate 100
        canvas.drawRoundRect(30f, y, 565f, y + 62f, 6f, 6f, paint)

        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("PODSUMOWANIE MIESIĄCA I ROZLICZENIE NALEŻNOŚCI:", 45f, y + 18f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 9f
        canvas.drawText("• Przyjazdy awaryjne (sytuacje krytyczne): $totalFailures szt. × 250,00 PLN = $emergencyCost,00 PLN", 45f, y + 32f, paint)
        canvas.drawText("• Usunięte awarie: $solvedFailures / $totalFailures (Skuteczność: ${if (totalFailures > 0) (solvedFailures * 100 / totalFailures) else 100}%)", 45f, y + 44f, paint)
        canvas.drawText("• Prace dodatkowe: ${extraWorks.size} pozycji | Łącznie: ${String.format(Locale.US, "%.1f", totalExtraHours)} h (rozliczane indywidualnie, brak sztywnej stawki)", 45f, y + 56f, paint)

        y += 82f

        // Section 2: Breakdowns & Incidents Log
        paint.color = Color.rgb(30, 41, 59)
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("1. DZIENNIK ZDARZEŃ I PRZYJAZDÓW AWARYJNYCH (250 zł / przyjazd)", 30f, y, paint)

        y += 15f
        // Table Header
        paint.color = Color.rgb(226, 232, 240)
        canvas.drawRect(30f, y, 565f, y + 18f, paint)

        paint.color = Color.rgb(30, 41, 59)
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Data i godzina", 35f, y + 12f, paint)
        canvas.drawText("Maszyna / Stanowisko", 125f, y + 12f, paint)
        canvas.drawText("Opis usterki / Co robiono", 250f, y + 12f, paint)
        canvas.drawText("Priorytet / Stawka", 415f, y + 12f, paint)
        canvas.drawText("Status", 505f, y + 12f, paint)

        y += 18f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 8f

        val maxTasksToShow = tasks.take(10)
        for ((index, task) in maxTasksToShow.withIndex()) {
            val bg = if (index % 2 == 0) Color.rgb(248, 250, 252) else Color.WHITE
            paint.color = bg
            canvas.drawRect(30f, y, 565f, y + 18f, paint)

            paint.color = Color.rgb(51, 65, 85)
            val dateStr = dateFormat.format(Date(task.reportedAt))
            canvas.drawText(dateStr, 35f, y + 12f, paint)

            val machineTrunc = if (task.machineId.length > 22) task.machineId.take(20) + "..." else task.machineId
            canvas.drawText(machineTrunc, 125f, y + 12f, paint)

            val descTrunc = if (task.title.length > 30) task.title.take(28) + "..." else task.title
            canvas.drawText(descTrunc, 250f, y + 12f, paint)

            val feeInfo = if (task.type == TaskType.AWARIA) "Krytyczny • 250zł" else task.priority.name
            paint.color = if (task.type == TaskType.AWARIA) Color.rgb(220, 38, 38) else Color.rgb(71, 85, 105)
            canvas.drawText(feeInfo, 415f, y + 12f, paint)

            paint.color = if (task.status.name == "ZAKONCZONE") Color.rgb(22, 163, 74) else Color.rgb(2, 132, 199)
            canvas.drawText(task.status.label, 505f, y + 12f, paint)

            y += 18f
        }

        if (tasks.size > 10) {
            paint.color = Color.rgb(100, 116, 139)
            canvas.drawText("... oraz ${tasks.size - 10} pozostałych zgłoszeń w rejestrze", 35f, y + 12f, paint)
            y += 16f
        }

        y += 20f

        // Section 3: Extra Work (Prace Dodatkowe do Rozliczenia Indywidualnego)
        paint.color = Color.rgb(30, 41, 59)
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("2. ZESTAWIENIE PRAC DODATKOWYCH (Rozliczane indywidualnie)", 30f, y, paint)

        y += 15f
        // Table Header
        paint.color = Color.rgb(226, 232, 240)
        canvas.drawRect(30f, y, 565f, y + 18f, paint)

        paint.color = Color.rgb(30, 41, 59)
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Data", 35f, y + 12f, paint)
        canvas.drawText("Zakres prac dodatkowych (co wykonano)", 95f, y + 12f, paint)
        canvas.drawText("Maszyna / Dział", 310f, y + 12f, paint)
        canvas.drawText("Godziny", 435f, y + 12f, paint)
        canvas.drawText("Rozliczenie", 490f, y + 12f, paint)

        y += 18f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 8f

        for ((index, extra) in extraWorks.withIndex()) {
            if (y > 750f) break // Protect page margin

            val bg = if (index % 2 == 0) Color.rgb(248, 250, 252) else Color.WHITE
            paint.color = bg
            canvas.drawRect(30f, y, 565f, y + 18f, paint)

            paint.color = Color.rgb(51, 65, 85)
            canvas.drawText(dateOnlyFormat.format(Date(extra.dateMillis)), 35f, y + 12f, paint)

            val titleTrunc = if (extra.title.length > 40) extra.title.take(38) + "..." else extra.title
            canvas.drawText(titleTrunc, 95f, y + 12f, paint)

            val clientTrunc = if (extra.machineOrClient.length > 22) extra.machineOrClient.take(20) + "..." else extra.machineOrClient
            canvas.drawText(clientTrunc, 310f, y + 12f, paint)

            canvas.drawText("${extra.hours} h", 435f, y + 12f, paint)

            val statusText = if (extra.isSettled) "Rozliczone" else "Indywidualne"
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.color = if (extra.isSettled) Color.rgb(22, 163, 74) else Color.rgb(217, 119, 6)
            canvas.drawText(statusText, 490f, y + 12f, paint)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

            y += 18f
        }

        // Summary Total Row
        paint.color = Color.rgb(241, 245, 249)
        canvas.drawRect(30f, y, 565f, y + 22f, paint)
        paint.color = Color.rgb(15, 23, 42)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 9f
        canvas.drawText("PRZYJAZDY AWARYJNE: $emergencyCost,00 PLN  |  PRACE DODATKOWE: ${String.format(Locale.US, "%.1f", totalExtraHours)} h (rozliczane indywidualnie)", 45f, y + 15f, paint)

        // Signatures at bottom of first page
        paint.color = Color.rgb(100, 116, 139)
        paint.textSize = 8f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawLine(40f, 790f, 200f, 790f, paint)
        canvas.drawText("Podpis Technika UR", 70f, 804f, paint)

        canvas.drawLine(395f, 790f, 555f, 790f, paint)
        canvas.drawText("Zatwierdzenie Kierownika Produkcji/UR", 400f, 804f, paint)

        pdfDocument.finishPage(page)

        // Optional photo appendix pages
        if (includePhotos) {
            val photoNotes = mutableListOf<Pair<MaintenanceTask, TaskNote>>()
            for (task in tasks) {
                val notes = Converters.deserializeNotes(task.notesJson)
                for (note in notes) {
                    if (!note.photoUri.isNullOrBlank()) {
                        photoNotes.add(task to note)
                    }
                }
            }

            if (photoNotes.isNotEmpty()) {
                var photoIndex = 0
                var pageNum = 2
                while (photoIndex < photoNotes.size) {
                    val photoPageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNum).create()
                    val photoPage = pdfDocument.startPage(photoPageInfo)
                    val photoCanvas = photoPage.canvas

                    // Header for photo page
                    paint.color = Color.rgb(15, 23, 42)
                    photoCanvas.drawRect(0f, 0f, 595f, 50f, paint)
                    paint.color = Color.WHITE
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    paint.textSize = 14f
                    photoCanvas.drawText("ZAŁĄCZNIK: ZDJĘCIA Z NOTATEK ($monthYearLabel)", 30f, 32f, paint)

                    var py = 70f
                    var photosOnPage = 0
                    val maxPhotosPerPage = 2

                    while (photoIndex < photoNotes.size && photosOnPage < maxPhotosPerPage && py < 750f) {
                        val (task, note) = photoNotes[photoIndex]
                        try {
                            val uri = Uri.parse(note.photoUri)
                            context.contentResolver.openInputStream(uri)?.use { input ->
                                val bitmap = BitmapFactory.decodeStream(input)
                                if (bitmap != null) {
                                    // Scale to fit width ~250-300pt, keep aspect
                                    val maxWidth = 280f
                                    val scale = maxWidth / bitmap.width
                                    val scaledHeight = bitmap.height * scale
                                    val scaledBitmap = Bitmap.createScaledBitmap(
                                        bitmap,
                                        maxWidth.toInt(),
                                        scaledHeight.toInt().coerceAtMost(220),
                                        true
                                    )

                                    photoCanvas.drawBitmap(scaledBitmap, 40f, py, paint)

                                    paint.color = Color.rgb(30, 41, 59)
                                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                                    paint.textSize = 9f
                                    val captionY = py + scaledBitmap.height + 16f
                                    photoCanvas.drawText(
                                        "${task.machineId} • ${dateFormat.format(Date(note.timestamp))}",
                                        40f,
                                        captionY,
                                        paint
                                    )
                                    if (note.text.isNotBlank()) {
                                        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                                        val textTrunc = if (note.text.length > 70) note.text.take(67) + "..." else note.text
                                        photoCanvas.drawText(textTrunc, 40f, captionY + 14f, paint)
                                    }

                                    py = captionY + 30f
                                    photosOnPage++
                                    bitmap.recycle()
                                    scaledBitmap.recycle()
                                }
                            }
                        } catch (_: Exception) {
                            // Skip broken photo
                        }
                        photoIndex++
                    }

                    pdfDocument.finishPage(photoPage)
                    pageNum++
                }
            }
        }

        return try {
            val dir = File(context.cacheDir, "reports")
            if (!dir.exists()) dir.mkdirs()
            val fileName = "Raport_UR_${monthYearLabel.replace(" ", "_").replace("/", "_")}.pdf"
            val file = File(dir, fileName)
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            outputStream.close()
            pdfDocument.close()
            file
        } catch (e: Exception) {
            pdfDocument.close()
            null
        }
    }

    fun sharePdfViaEmail(
        context: Context,
        pdfFile: File,
        recipientEmail: String = "",
        monthYearLabel: String
    ) {
        val authority = "${context.packageName}.fileprovider"
        val contentUri = FileProvider.getUriForFile(context, authority, pdfFile)

        val emailIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            if (recipientEmail.isNotBlank()) {
                putExtra(Intent.EXTRA_EMAIL, arrayOf(recipientEmail))
            }
            putExtra(Intent.EXTRA_SUBJECT, "Raport miesięczny Serwis UR - $monthYearLabel")
            putExtra(
                Intent.EXTRA_TEXT,
                "Dzień dobry,\n\nw załączeniu przesyłam raport miesięczny zdarzeń, awarii oraz zestawienie prac dodatkowych do rozliczenia za okres $monthYearLabel.\n\nRaport wygenerowany automatycznie przez aplikację Serwis UR.\n\nPozdrawiam,\nTechnik Serwisu UR"
            )
            putExtra(Intent.EXTRA_STREAM, contentUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(emailIntent, "Wyślij raport PDF e-mailem")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}