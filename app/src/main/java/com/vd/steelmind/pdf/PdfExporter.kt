package com.vd.steelmind.pdf

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.vd.steelmind.logic.PlanResult
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfExporter {
    fun export(context: Context, projectName: String, plan: PlanResult, drawBars: Boolean = true): File {
        val pdf = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()

        var page = pdf.startPage(pageInfo)
        var c = page.canvas

        val titlePaint = Paint().apply { textSize = 18f; isAntiAlias = true; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }
        val textPaint = Paint().apply { textSize = 12f; isAntiAlias = true }
        val grayPaint = Paint().apply { color = Color.LTGRAY }
        val darkPaint = Paint().apply { color = Color.DKGRAY }

        var y = 40f
        val date = SimpleDateFormat("yyyy.MM.dd. HH:mm", Locale.getDefault()).format(Date())

        fun header() {
            c.drawText("VD SteelMind – Darabolási terv", 40f, y, titlePaint); y += 20f
            c.drawText("Projekt: $projectName    Dátum: $date", 40f, y, textPaint); y += 20f
        }
        fun newPage() {
            pdf.finishPage(page)
            page = pdf.startPage(pageInfo)
            c = page.canvas
            y = 40f
            header()
        }
        header()

        plan.bars.forEachIndexed { idx, bar ->
            if (y > 760f) newPage()
            val headerLine = "${idx + 1}. Szál (${bar.stockLength} mm): " + bar.cuts.joinToString("; ")
            c.drawText("$headerLine (maradék: ${bar.offcut})", 40f, y, textPaint); y += 14f

            if (drawBars) {
                val left = 40f; val right = 555f; val barWidth = right - left
                val scale = if (bar.stockLength > 0) barWidth / bar.stockLength else 0f
                val top = y; val height = 10f
                var x = left
                bar.cuts.forEach { cut ->
                    val w = cut * scale
                    c.drawRect(RectF(x, top, x + w, top + height), darkPaint)
                    x += w
                }
                c.drawRect(RectF(x, top, right, top + height), grayPaint)
                y += 20f
            }
        }

        if (y > 760f) newPage()
        c.drawText("Összes hulladék: ${plan.totalWaste} mm    Kihasználtság: ${plan.utilizationPercent}%", 40f, y, textPaint)

        pdf.finishPage(page)

        val outFile = File(context.cacheDir, "darabolasi_terv.pdf")
        FileOutputStream(outFile).use { pdf.writeTo(it) }
        pdf.close()
        return outFile
    }

    fun shareFile(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(context, context.packageName + ".provider", file)
    }
}