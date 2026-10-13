package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.DatingCheckEntity
import com.example.data.model.MachineDowntimeEntity
import com.example.data.model.PackagingControlEntity
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfExportUtil {

    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 36f

    fun exportPackagingPdf(context: Context, entity: PackagingControlEntity, isShare: Boolean = false) {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        var y = MARGIN

        // Header Background
        paint.color = Color.rgb(15, 23, 42) // Dark Navy
        canvas.drawRoundRect(RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + 54f), 6f, 6f, paint)

        // Header Title
        textPaint.color = Color.rgb(6, 182, 212) // Cyan
        textPaint.textSize = 12f
        textPaint.isFakeBoldText = true
        canvas.drawText("FARINHAS CJ — CONTROLE DIÁRIO DE PRODUÇÃO", MARGIN + 12f, y + 20f, textPaint)

        textPaint.color = Color.WHITE
        textPaint.textSize = 9f
        textPaint.isFakeBoldText = false
        canvas.drawText("FORM.003.FAR.CJ | REV. 02 — CONTROLE DE EMPACOTAMENTO", MARGIN + 12f, y + 36f, textPaint)

        val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(entity.createdAt))
        textPaint.color = Color.rgb(148, 163, 184)
        textPaint.textSize = 8f
        canvas.drawText("Gerado em: $dateStr", PAGE_WIDTH - MARGIN - 130f, y + 20f, textPaint)

        y += 66f

        // Info Grid Box
        drawSectionBox(canvas, MARGIN, y, PAGE_WIDTH - MARGIN, y + 60f, "DADOS DE IDENTIFICAÇÃO")
        val infoY = y + 22f
        drawKeyValue(canvas, "Data:", entity.date, MARGIN + 10f, infoY)
        drawKeyValue(canvas, "Turno:", entity.shift, MARGIN + 110f, infoY)
        drawKeyValue(canvas, "Linha:", entity.line, MARGIN + 210f, infoY)
        drawKeyValue(canvas, "Produto:", entity.product, MARGIN + 300f, infoY)

        val infoY2 = y + 42f
        drawKeyValue(canvas, "Responsável:", entity.responsible, MARGIN + 10f, infoY2)
        if (entity.productChangedTo.isNotBlank()) {
            drawKeyValue(canvas, "Mudou para:", "${entity.productChangedTo} (${entity.productChangeTime})", MARGIN + 160f, infoY2)
            drawKeyValue(canvas, "Fardos Realizados:", "${entity.totalRealizedFds} fds", MARGIN + 340f, infoY2)
            drawKeyValue(canvas, "Velocidade:", "${entity.initialSpeed} bpm", MARGIN + 460f, infoY2)
        } else {
            drawKeyValue(canvas, "Fardos Realizados:", "${entity.totalRealizedFds} fds", MARGIN + 210f, infoY2)
            drawKeyValue(canvas, "Velocidade:", "${entity.initialSpeed} bpm", MARGIN + 360f, infoY2)
        }

        y += 70f

        // Checklist de Condição e Limpeza
        drawSectionBox(canvas, MARGIN, y, PAGE_WIDTH - MARGIN, y + 50f, "STATUS DE LIMPEZA E MÁQUINA")
        val statusY = y + 22f
        drawStatusBadge(canvas, "Empacotadora Interna:", entity.empacotadoraInternaC, MARGIN + 10f, statusY)
        drawStatusBadge(canvas, "Empacotadora Externa:", entity.empacotadoraExternaC, MARGIN + 155f, statusY)
        drawStatusBadge(canvas, "Enfardadeira Interna:", entity.enfardadeiraInternaC, MARGIN + 300f, statusY)
        drawStatusBadge(canvas, "Enfardadeira Externa:", entity.enfardadeiraExternaC, MARGIN + 435f, statusY)

        val statusY2 = y + 38f
        drawStatusBadge(canvas, "Condição Máquina:", entity.machineCondition == "C", MARGIN + 10f, statusY2)
        drawStatusBadge(canvas, "Condição Setor:", entity.setorCondition == "C", MARGIN + 155f, statusY2)
        drawStatusBadge(canvas, "Caixas de Lixo:", entity.trashCondition == "C", MARGIN + 300f, statusY2)
        drawStatusBadge(canvas, "Abastecido:", entity.supplyCondition == "C", MARGIN + 435f, statusY2)

        y += 60f

        // Produção Horária Resumida
        drawSectionBox(canvas, MARGIN, y, PAGE_WIDTH - MARGIN, y + 84f, "PRODUÇÃO HORÁRIA REALIZADA")
        try {
            val prodArray = JSONArray(entity.hourlyProductionJson.ifBlank { "[]" })
            var rowX = MARGIN + 10f
            var rowY = y + 24f
            for (i in 0 until minOf(prodArray.length(), 8)) {
                val item = prodArray.getJSONObject(i)
                val hour = item.optString("hourLabel", "H${i + 1}").split(" / ").firstOrNull() ?: "H"
                val real = item.optString("real", "-").ifBlank { "-" }
                val meta = item.optString("meta", "0")
                
                // Draw pill
                paint.color = Color.rgb(241, 245, 249)
                canvas.drawRoundRect(RectF(rowX, rowY, rowX + 58f, rowY + 44f), 4f, 4f, paint)
                
                textPaint.color = Color.rgb(71, 85, 105)
                textPaint.textSize = 7f
                textPaint.isFakeBoldText = true
                canvas.drawText(hour, rowX + 4f, rowY + 12f, textPaint)
                
                textPaint.color = Color.rgb(15, 23, 42)
                textPaint.textSize = 9f
                textPaint.isFakeBoldText = true
                canvas.drawText("$real fds", rowX + 4f, rowY + 26f, textPaint)

                textPaint.color = Color.rgb(100, 116, 139)
                textPaint.textSize = 7f
                textPaint.isFakeBoldText = false
                canvas.drawText("Meta: $meta", rowX + 4f, rowY + 38f, textPaint)

                rowX += 65f
            }
        } catch (_: Exception) {}

        y += 94f

        // Pesagem e Tolerância
        var gramatura = "400g"
        val faixasSummary = mutableListOf<String>()
        try {
            val obj = JSONObject(entity.weightsJson.ifBlank { "{}" })
            gramatura = obj.optString("gramatura", "400g")
            val gridObj = obj.optJSONObject("grid")
            if (gridObj != null) {
                gridObj.keys().forEach { faixaKey ->
                    val arr = gridObj.optJSONArray(faixaKey)
                    var count = 0
                    if (arr != null) {
                        for (i in 0 until arr.length()) {
                            if (arr.optString(i).isNotBlank()) count++
                        }
                    }
                    if (count > 0) {
                        faixasSummary.add("${faixaKey}g ($count)")
                    }
                }
            }
        } catch (_: Exception) {}

        drawSectionBox(canvas, MARGIN, y, PAGE_WIDTH - MARGIN, y + 66f, "CONTROLE DE PESAGEM E METROLOGIA")
        val pesY = y + 22f
        drawKeyValue(canvas, "Gramatura:", gramatura, MARGIN + 10f, pesY)
        drawKeyValue(canvas, "Média do Turno:", if (entity.shiftAverageWeight.isNotBlank()) "${entity.shiftAverageWeight} g" else "-", MARGIN + 120f, pesY)
        drawKeyValue(canvas, "Tara Embalagem:", if (entity.packageTareWeight.isNotBlank()) "${entity.packageTareWeight} g" else "-", MARGIN + 260f, pesY)
        drawKeyValue(canvas, "Tolerância Padrão:", if (gramatura == "400g") "398 a 402 g" else "497,5 a 502,5 g", MARGIN + 400f, pesY)

        val pesY2 = y + 44f
        val faixasStr = if (faixasSummary.isNotEmpty()) faixasSummary.joinToString(" | ") else "Nenhuma amostragem de faixa registrada no grid"
        drawKeyValue(canvas, "Pesagens por Faixa:", faixasStr.take(75), MARGIN + 10f, pesY2)

        y += 76f

        // Embalagens e Bobinas
        drawSectionBox(canvas, MARGIN, y, PAGE_WIDTH - MARGIN, y + 80f, "CONTROLE DE BOBINAS E APARAS")
        val embY = y + 24f
        drawKeyValue(canvas, "Sobra Laminado (kg):", entity.leftoverLaminated.ifBlank { "0 kg" }, MARGIN + 10f, embY)
        drawKeyValue(canvas, "Sobra Enfardadeira (kg):", entity.leftoverBaler.ifBlank { "0 kg" }, MARGIN + 220f, embY)
        drawKeyValue(canvas, "Mesmo Produto:", if (entity.packagingSameProduct) "Sim" else "Não", MARGIN + 420f, embY)

        try {
            val empArray = JSONArray(entity.empacotadoraRowsJson.ifBlank { "[]" })
            if (empArray.length() > 0) {
                val first = empArray.getJSONObject(0)
                val lot = first.optString("lot", "-")
                val forn = first.optString("supplier", "-")
                val apara = first.optString("filmWasteKg", "-")
                val embY2 = y + 46f
                drawKeyValue(canvas, "Lote Bobina 1:", lot, MARGIN + 10f, embY2)
                drawKeyValue(canvas, "Fornecedor:", forn, MARGIN + 170f, embY2)
                drawKeyValue(canvas, "Apara Filme:", "$apara kg", MARGIN + 350f, embY2)
            }
        } catch (_: Exception) {}

        y += 90f

        // Observações Gerais
        drawSectionBox(canvas, MARGIN, y, PAGE_WIDTH - MARGIN, y + 60f, "OBSERVAÇÕES GERAIS")
        textPaint.color = Color.rgb(30, 41, 59)
        textPaint.textSize = 8.5f
        textPaint.isFakeBoldText = false
        val obs = entity.generalObservations.ifBlank { "Nenhuma observação registrada neste turno." }
        canvas.drawText(obs.take(120), MARGIN + 10f, y + 26f, textPaint)

        y += 76f

        // Assinaturas
        drawSignatures(canvas, y)

        document.finishPage(page)
        val filename = "controle_empacotamento_${entity.date.replace("/", "-")}_${entity.line}.pdf"
        if (isShare) saveAndSharePdf(context, document, filename) else saveAndDownloadPdf(context, document, filename)
    }

    fun downloadPackagingPdf(context: Context, entity: PackagingControlEntity) = exportPackagingPdf(context, entity, isShare = false)
    fun sharePackagingPdf(context: Context, entity: PackagingControlEntity) = exportPackagingPdf(context, entity, isShare = true)

    fun exportDowntimePdf(context: Context, entity: MachineDowntimeEntity, isShare: Boolean = false) {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        var y = MARGIN

        // Header Background
        paint.color = Color.rgb(15, 23, 42)
        canvas.drawRoundRect(RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + 54f), 6f, 6f, paint)

        textPaint.color = Color.rgb(245, 158, 11) // Amber
        textPaint.textSize = 12f
        textPaint.isFakeBoldText = true
        canvas.drawText("FARINHAS CJ — APONTAMENTO DE PARADA DE MÁQUINAS", MARGIN + 12f, y + 20f, textPaint)

        textPaint.color = Color.WHITE
        textPaint.textSize = 9f
        textPaint.isFakeBoldText = false
        canvas.drawText("FORM.002.FAR.CJ | REV. 01 — APONTAMENTO PARADA DE MÁQUINAS", MARGIN + 12f, y + 36f, textPaint)

        val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(entity.createdAt))
        textPaint.color = Color.rgb(148, 163, 184)
        textPaint.textSize = 8f
        canvas.drawText("Gerado em: $dateStr", PAGE_WIDTH - MARGIN - 130f, y + 20f, textPaint)

        y += 66f

        // Info Box
        drawSectionBox(canvas, MARGIN, y, PAGE_WIDTH - MARGIN, y + 50f, "DADOS DA LINHA E TURNO")
        val infoY = y + 22f
        drawKeyValue(canvas, "Data:", entity.date, MARGIN + 10f, infoY)
        drawKeyValue(canvas, "Turno:", entity.shift, MARGIN + 110f, infoY)
        if (entity.shiftStart.isNotBlank() || entity.shiftEnd.isNotBlank()) {
            drawKeyValue(canvas, "Horários:", "${entity.shiftStart} às ${entity.shiftEnd}", MARGIN + 210f, infoY)
            drawKeyValue(canvas, "Linha:", entity.line, MARGIN + 340f, infoY)
            drawKeyValue(canvas, "Produto:", entity.product, MARGIN + 430f, infoY)
        } else {
            drawKeyValue(canvas, "Linha:", entity.line, MARGIN + 210f, infoY)
            drawKeyValue(canvas, "Produto:", entity.product, MARGIN + 300f, infoY)
            drawKeyValue(canvas, "Transgênico:", if (entity.isTransgenic) "Sim" else "Não", MARGIN + 430f, infoY)
        }

        val infoY2 = y + 38f
        drawKeyValue(canvas, "Operador Responsável:", entity.responsible, MARGIN + 10f, infoY2)
        drawKeyValue(canvas, "Total Paradas:", "${entity.totalStopsCount} ocorrências", MARGIN + 250f, infoY2)
        drawKeyValue(canvas, "Tempo Total:", "${entity.totalDowntimeMinutes} min", MARGIN + 400f, infoY2)

        y += 60f

        // Tabela de Paradas
        drawSectionBox(canvas, MARGIN, y, PAGE_WIDTH - MARGIN, y + 380f, "OCORRÊNCIAS DE PARADA REGISTRADAS")
        
        // Table Header
        val tableHeaderY = y + 22f
        paint.color = Color.rgb(241, 245, 249)
        canvas.drawRect(MARGIN + 6f, tableHeaderY - 10f, PAGE_WIDTH - MARGIN - 6f, tableHeaderY + 8f, paint)

        textPaint.color = Color.rgb(15, 23, 42)
        textPaint.textSize = 8f
        textPaint.isFakeBoldText = true
        canvas.drawText("INÍCIO/FIM PARADA", MARGIN + 10f, tableHeaderY, textPaint)
        canvas.drawText("INÍCIO/FIM MANUT.", MARGIN + 110f, tableHeaderY, textPaint)
        canvas.drawText("CÓDIGO", MARGIN + 210f, tableHeaderY, textPaint)
        canvas.drawText("DESCRIÇÃO DO MOTIVO", MARGIN + 270f, tableHeaderY, textPaint)
        canvas.drawText("OBSERVAÇÃO", MARGIN + 420f, tableHeaderY, textPaint)

        var rowY = tableHeaderY + 18f
        try {
            val entries = JSONArray(entity.entriesJson.ifBlank { "[]" })
            for (i in 0 until minOf(entries.length(), 14)) {
                val item = entries.getJSONObject(i)
                val pStart = item.optString("startParada", "--:--")
                val pEnd = item.optString("endParada", "--:--")
                val mStart = item.optString("startManutencao", "--:--")
                val mEnd = item.optString("endManutencao", "--:--")
                val code = item.optString("stopCode", "-")
                val desc = item.optString("stopDescription", "-")
                val obs = item.optString("comments", "-")

                textPaint.color = Color.rgb(51, 65, 85)
                textPaint.textSize = 7.5f
                textPaint.isFakeBoldText = false

                canvas.drawText("$pStart - $pEnd", MARGIN + 10f, rowY, textPaint)
                canvas.drawText("$mStart - $mEnd", MARGIN + 110f, rowY, textPaint)
                
                textPaint.isFakeBoldText = true
                textPaint.color = Color.rgb(180, 83, 9)
                canvas.drawText(code, MARGIN + 210f, rowY, textPaint)
                
                textPaint.isFakeBoldText = false
                textPaint.color = Color.rgb(15, 23, 42)
                canvas.drawText(desc.take(28), MARGIN + 270f, rowY, textPaint)
                canvas.drawText(obs.take(20), MARGIN + 420f, rowY, textPaint)

                // Divider line
                paint.color = Color.rgb(226, 232, 240)
                canvas.drawLine(MARGIN + 6f, rowY + 4f, PAGE_WIDTH - MARGIN - 6f, rowY + 4f, paint)

                rowY += 18f
            }
        } catch (_: Exception) {}

        y += 390f

        // Assinaturas
        drawSignatures(canvas, y)

        document.finishPage(page)
        val filename = "paradas_${entity.date.replace("/", "-")}_${entity.line}.pdf"
        if (isShare) saveAndSharePdf(context, document, filename) else saveAndDownloadPdf(context, document, filename)
    }

    fun downloadDowntimePdf(context: Context, entity: MachineDowntimeEntity) = exportDowntimePdf(context, entity, isShare = false)
    fun shareDowntimePdf(context: Context, entity: MachineDowntimeEntity) = exportDowntimePdf(context, entity, isShare = true)

    fun exportDatingPdf(context: Context, entity: DatingCheckEntity, isShare: Boolean = false) {
        val document = PdfDocument()
        var pageNumber = 1
        val pageInfo1 = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
        val page1 = document.startPage(pageInfo1)
        val canvas = page1.canvas

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        var y = MARGIN

        // Header Background
        paint.color = Color.rgb(15, 23, 42)
        canvas.drawRoundRect(RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + 54f), 6f, 6f, paint)

        textPaint.color = Color.rgb(16, 185, 129) // Green
        textPaint.textSize = 12f
        textPaint.isFakeBoldText = true
        canvas.drawText("FARINHAS CJ — CHECAGEM DE DATAGEM DOS PACOTES", MARGIN + 12f, y + 20f, textPaint)

        textPaint.color = Color.WHITE
        textPaint.textSize = 9f
        textPaint.isFakeBoldText = false
        canvas.drawText("FORM.005.FAR.CJ | REV. 02 — CONTROLE DE QUALIDADE", MARGIN + 12f, y + 36f, textPaint)

        val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(entity.createdAt))
        textPaint.color = Color.rgb(148, 163, 184)
        textPaint.textSize = 8f
        canvas.drawText("Gerado em: $dateStr", PAGE_WIDTH - MARGIN - 130f, y + 20f, textPaint)

        y += 66f

        // Info Box
        drawSectionBox(canvas, MARGIN, y, PAGE_WIDTH - MARGIN, y + 50f, "IDENTIFICAÇÃO DA AMOSTRAGEM")
        val infoY = y + 22f
        drawKeyValue(canvas, "Data:", entity.date, MARGIN + 10f, infoY)
        drawKeyValue(canvas, "Turno:", entity.shift, MARGIN + 110f, infoY)
        drawKeyValue(canvas, "Máquina:", entity.machine, MARGIN + 220f, infoY)
        drawKeyValue(canvas, "Inspetor:", entity.responsible, MARGIN + 330f, infoY)

        val infoY2 = y + 38f
        drawKeyValue(canvas, "Amostras Conformes:", "${entity.compliantCount}", MARGIN + 10f, infoY2)
        drawKeyValue(canvas, "Não Conformes:", "${entity.nonCompliantCount}", MARGIN + 150f, infoY2)
        val rate = if (entity.compliantCount + entity.nonCompliantCount > 0) {
            "${(entity.compliantCount * 100) / (entity.compliantCount + entity.nonCompliantCount)}%"
        } else "100%"
        drawKeyValue(canvas, "Taxa Conformidade:", rate, MARGIN + 300f, infoY2)

        y += 60f

        // Coletar itens e fotos
        data class PdfDatingRow(
            val hourLabel: String,
            val lot: String,
            val time: String,
            val exp: String,
            val isComp: Boolean,
            val photoUri: String
        )
        val checksList = mutableListOf<PdfDatingRow>()
        try {
            val checks = JSONArray(entity.checksJson.ifBlank { "[]" })
            for (i in 0 until checks.length()) {
                val item = checks.getJSONObject(i)
                checksList.add(
                    PdfDatingRow(
                        hourLabel = item.optString("hourLabel", "Hora ${i + 1}").take(24),
                        lot = item.optString("lotNumber", "-").ifBlank { "-" },
                        time = item.optString("printedTime", "-").ifBlank { "-" },
                        exp = item.optString("printedExpiration", "-").ifBlank { "-" },
                        isComp = item.optBoolean("isCompliant", true),
                        photoUri = item.optString("photoUri", "")
                    )
                )
            }
        } catch (_: Exception) {}

        // Tabela de Checagem
        drawSectionBox(canvas, MARGIN, y, PAGE_WIDTH - MARGIN, y + 260f, "AMOSTRAGEM HORÁRIA DE DATAS E LOTES")
        
        val tableHeaderY = y + 22f
        paint.color = Color.rgb(241, 245, 249)
        canvas.drawRect(MARGIN + 6f, tableHeaderY - 10f, PAGE_WIDTH - MARGIN - 6f, tableHeaderY + 8f, paint)

        textPaint.color = Color.rgb(15, 23, 42)
        textPaint.textSize = 8f
        textPaint.isFakeBoldText = true
        canvas.drawText("HORÁRIO / ETAPA", MARGIN + 10f, tableHeaderY, textPaint)
        canvas.drawText("Nº LOTE", MARGIN + 140f, tableHeaderY, textPaint)
        canvas.drawText("HORA", MARGIN + 210f, tableHeaderY, textPaint)
        canvas.drawText("VALIDADE", MARGIN + 280f, tableHeaderY, textPaint)
        canvas.drawText("FOTO", MARGIN + 370f, tableHeaderY, textPaint)
        canvas.drawText("STATUS", MARGIN + 450f, tableHeaderY, textPaint)

        var rowY = tableHeaderY + 18f
        for (i in 0 until minOf(checksList.size, 9)) {
            val item = checksList[i]

            textPaint.color = Color.rgb(51, 65, 85)
            textPaint.textSize = 7.5f
            textPaint.isFakeBoldText = false

            canvas.drawText(item.hourLabel, MARGIN + 10f, rowY, textPaint)
            canvas.drawText(item.lot, MARGIN + 140f, rowY, textPaint)
            canvas.drawText(item.time, MARGIN + 210f, rowY, textPaint)
            canvas.drawText(item.exp, MARGIN + 280f, rowY, textPaint)

            // Indicador de foto anexada
            if (item.photoUri.isNotBlank()) {
                textPaint.color = Color.rgb(16, 185, 129)
                textPaint.isFakeBoldText = true
                canvas.drawText("📷 ANEXADA", MARGIN + 370f, rowY, textPaint)
            } else {
                textPaint.color = Color.rgb(148, 163, 184)
                textPaint.isFakeBoldText = false
                canvas.drawText("—", MARGIN + 370f, rowY, textPaint)
            }

            // Status tag
            paint.color = if (item.isComp) Color.rgb(220, 252, 231) else Color.rgb(254, 226, 226)
            canvas.drawRoundRect(RectF(MARGIN + 450f, rowY - 8f, MARGIN + 510f, rowY + 3f), 3f, 3f, paint)
            
            textPaint.color = if (item.isComp) Color.rgb(22, 101, 52) else Color.rgb(153, 27, 27)
            textPaint.textSize = 7f
            textPaint.isFakeBoldText = true
            canvas.drawText(if (item.isComp) "CONFORME" else "NÃO CONF.", MARGIN + 454f, rowY, textPaint)

            // Divider
            paint.color = Color.rgb(226, 232, 240)
            canvas.drawLine(MARGIN + 6f, rowY + 5f, PAGE_WIDTH - MARGIN - 6f, rowY + 5f, paint)

            rowY += 20f
        }

        y += 270f

        // Ações Corretivas e Assinaturas
        drawSectionBox(canvas, MARGIN, y, PAGE_WIDTH - MARGIN, y + 60f, "AÇÕES CORRETIVAS")
        textPaint.color = Color.rgb(30, 41, 59)
        textPaint.textSize = 8.5f
        textPaint.isFakeBoldText = false
        val corr = entity.correctiveActions.ifBlank { "Nenhuma não-conformidade detectada no lote." }
        canvas.drawText(corr.take(120), MARGIN + 10f, y + 26f, textPaint)

        y += 76f

        // Assinaturas de Inspeção
        drawSignatures(canvas, y)
        document.finishPage(page1)

        // ==========================================
        // PÁGINA 2: ANEXO FOTOGRÁFICO (Se houver fotos)
        // ==========================================
        val photosToPrint = checksList.filter { it.photoUri.isNotBlank() }
        if (photosToPrint.isNotEmpty()) {
            val chunks = photosToPrint.chunked(4) // 4 fotos por página
            chunks.forEachIndexed { pageIdx, photoChunk ->
                pageNumber++
                val pageInfoPhotos = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                val photoPage = document.startPage(pageInfoPhotos)
                val photoCanvas = photoPage.canvas

                var py = MARGIN

                // Header da página de fotos
                paint.color = Color.rgb(15, 23, 42)
                photoCanvas.drawRoundRect(RectF(MARGIN, py, PAGE_WIDTH - MARGIN, py + 46f), 6f, 6f, paint)

                textPaint.color = Color.rgb(16, 185, 129)
                textPaint.textSize = 11f
                textPaint.isFakeBoldText = true
                photoCanvas.drawText("ANEXO FOTOGRÁFICO — REGISTRO DAS ETIQUETAS DATADAS", MARGIN + 12f, py + 18f, textPaint)

                textPaint.color = Color.rgb(203, 213, 225)
                textPaint.textSize = 8.5f
                textPaint.isFakeBoldText = false
                photoCanvas.drawText("Máquina: ${entity.machine} | Data: ${entity.date} | Turno: ${entity.shift} (Página $pageNumber)", MARGIN + 12f, py + 34f, textPaint)

                py += 58f

                // Grade 2x2 para as 4 fotos
                val colWidth = (PAGE_WIDTH - (MARGIN * 2) - 16f) / 2f
                val rowHeight = 310f

                photoChunk.forEachIndexed { idx, pItem ->
                    val col = idx % 2
                    val row = idx / 2

                    val photoLeft = MARGIN + col * (colWidth + 16f)
                    val photoTop = py + row * rowHeight
                    val photoRight = photoLeft + colWidth
                    val photoBottom = photoTop + rowHeight - 16f

                    // Card Container
                    paint.color = Color.rgb(248, 250, 252)
                    photoCanvas.drawRoundRect(RectF(photoLeft, photoTop, photoRight, photoBottom), 6f, 6f, paint)

                    paint.color = Color.rgb(203, 213, 225)
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = 0.8f
                    photoCanvas.drawRoundRect(RectF(photoLeft, photoTop, photoRight, photoBottom), 6f, 6f, paint)
                    paint.style = Paint.Style.FILL

                    // Card Header info
                    textPaint.color = Color.rgb(15, 23, 42)
                    textPaint.textSize = 9f
                    textPaint.isFakeBoldText = true
                    photoCanvas.drawText(pItem.hourLabel, photoLeft + 8f, photoTop + 16f, textPaint)

                    textPaint.color = Color.rgb(71, 85, 105)
                    textPaint.textSize = 7.5f
                    textPaint.isFakeBoldText = false
                    photoCanvas.drawText("Lote: ${pItem.lot} | Val: ${pItem.exp} | Hora: ${pItem.time}", photoLeft + 8f, photoTop + 28f, textPaint)

                    // Draw Bitmap
                    val bitmap = ImageStorageUtil.loadBitmap(context, pItem.photoUri)
                    if (bitmap != null) {
                        val imgLeft = photoLeft + 8f
                        val imgTop = photoTop + 34f
                        val imgRight = photoRight - 8f
                        val imgBottom = photoBottom - 26f
                        val dstRect = RectF(imgLeft, imgTop, imgRight, imgBottom)

                        photoCanvas.drawBitmap(bitmap, null, dstRect, null)

                        // Status Tag no rodapé do card da foto
                        paint.color = if (pItem.isComp) Color.rgb(220, 252, 231) else Color.rgb(254, 226, 226)
                        photoCanvas.drawRoundRect(RectF(photoLeft + 8f, photoBottom - 20f, photoRight - 8f, photoBottom - 6f), 3f, 3f, paint)

                        textPaint.color = if (pItem.isComp) Color.rgb(22, 101, 52) else Color.rgb(153, 27, 27)
                        textPaint.textSize = 7.5f
                        textPaint.isFakeBoldText = true
                        val statusStr = if (pItem.isComp) "✓ ETIQUETA CONFORME" else "✕ ETIQUETA NÃO CONFORME"
                        photoCanvas.drawText(statusStr, photoLeft + 14f, photoBottom - 10f, textPaint)
                    } else {
                        textPaint.color = Color.rgb(148, 163, 184)
                        textPaint.textSize = 8f
                        photoCanvas.drawText("Foto não disponível para renderização", photoLeft + 12f, photoTop + 80f, textPaint)
                    }
                }

                document.finishPage(photoPage)
            }
        }

        val filename = "checagem_datagem_${entity.date.replace("/", "-")}_${entity.machine}.pdf"
        if (isShare) saveAndSharePdf(context, document, filename) else saveAndDownloadPdf(context, document, filename)
    }

    fun downloadDatingPdf(context: Context, entity: DatingCheckEntity) = exportDatingPdf(context, entity, isShare = false)
    fun shareDatingPdf(context: Context, entity: DatingCheckEntity) = exportDatingPdf(context, entity, isShare = true)

    private fun drawSectionBox(canvas: Canvas, left: Float, top: Float, right: Float, bottom: Float, title: String) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = Color.rgb(248, 250, 252) // Slate 50
        canvas.drawRoundRect(RectF(left, top, right, bottom), 4f, 4f, paint)

        paint.color = Color.rgb(203, 213, 225) // Border
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0.8f
        canvas.drawRoundRect(RectF(left, top, right, bottom), 4f, 4f, paint)
        paint.style = Paint.Style.FILL

        // Section Title
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        textPaint.color = Color.rgb(15, 23, 42)
        textPaint.textSize = 8.5f
        textPaint.isFakeBoldText = true
        canvas.drawText(title, left + 10f, top + 12f, textPaint)

        paint.color = Color.rgb(226, 232, 240)
        canvas.drawLine(left + 6f, top + 15f, right - 6f, top + 15f, paint)
    }

    private fun drawKeyValue(canvas: Canvas, label: String, value: String, x: Float, y: Float) {
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        textPaint.color = Color.rgb(100, 116, 139)
        textPaint.textSize = 8f
        textPaint.isFakeBoldText = true
        canvas.drawText(label, x, y, textPaint)

        val labelWidth = textPaint.measureText(label)
        textPaint.color = Color.rgb(15, 23, 42)
        textPaint.isFakeBoldText = false
        canvas.drawText(" $value", x + labelWidth, y, textPaint)
    }

    private fun drawStatusBadge(canvas: Canvas, label: String, isOk: Boolean, x: Float, y: Float) {
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        textPaint.color = Color.rgb(71, 85, 105)
        textPaint.textSize = 7f
        textPaint.isFakeBoldText = false
        canvas.drawText(label, x, y, textPaint)

        val labelWidth = textPaint.measureText(label)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = if (isOk) Color.rgb(22, 163, 74) else Color.rgb(220, 38, 38)
        canvas.drawCircle(x + labelWidth + 7f, y - 2.5f, 3.5f, paint)
    }

    private fun drawSignatures(canvas: Canvas, y: Float) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = Color.rgb(148, 163, 184)
        paint.strokeWidth = 0.8f

        val colWidth = 140f
        val sig1X = MARGIN + 20f
        val sig2X = MARGIN + 180f
        val sig3X = MARGIN + 340f

        canvas.drawLine(sig1X, y + 25f, sig1X + colWidth, y + 25f, paint)
        canvas.drawLine(sig2X, y + 25f, sig2X + colWidth, y + 25f, paint)
        canvas.drawLine(sig3X, y + 25f, sig3X + colWidth, y + 25f, paint)

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        textPaint.color = Color.rgb(71, 85, 105)
        textPaint.textSize = 7.5f
        textPaint.isFakeBoldText = true
        canvas.drawText("OPERADOR / INSPETOR", sig1X + 15f, y + 36f, textPaint)
        canvas.drawText("LÍDER DE PRODUÇÃO", sig2X + 22f, y + 36f, textPaint)
        canvas.drawText("GARANTIA DA QUALIDADE", sig3X + 12f, y + 36f, textPaint)
    }

    private fun saveAndDownloadPdf(context: Context, document: PdfDocument, filename: String) {
        try {
            // 1. Android 10+ (API 29+): Salva diretamente na pasta pública de Downloads do aparelho via MediaStore
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/FarinhasCJ")
                }
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { out ->
                        document.writeTo(out)
                        out.flush()
                    }
                }
            } else {
                // 2. Android 9 e anteriores: Salva na pasta pública de Downloads do armazenamento
                val publicDownloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val targetDir = File(publicDownloads, "FarinhasCJ")
                if (!targetDir.exists()) targetDir.mkdirs()
                val targetFile = File(targetDir, filename)
                FileOutputStream(targetFile).use { out ->
                    document.writeTo(out)
                    out.flush()
                }
                MediaScannerConnection.scanFile(context, arrayOf(targetFile.absolutePath), arrayOf("application/pdf"), null)
            }

            // 3. Salva também cópia no cache interno do app para visualização imediata segura via FileProvider
            val cacheFile = File(context.cacheDir, filename)
            FileOutputStream(cacheFile).use { out ->
                document.writeTo(out)
                out.flush()
            }

            document.close()

            // 4. Toast de confirmação do download direto no hardware
            Toast.makeText(
                context,
                "✓ Download concluído com sucesso!\nSalvo em: Downloads/FarinhasCJ/$filename",
                Toast.LENGTH_LONG
            ).show()

            // 5. Abre automaticamente o PDF baixado no leitor do tablet/celular
            try {
                val viewUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    cacheFile
                )
                val openIntent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(viewUri, "application/pdf")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                val chooser = Intent.createChooser(openIntent, "Abrir PDF Baixado").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(chooser)
            } catch (_: Exception) {
                // Se não houver leitor de PDF instalado, o arquivo já está baixado e seguro na pasta Downloads
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Erro ao baixar PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun saveAndSharePdf(context: Context, document: PdfDocument, filename: String) {
        try {
            val file = File(context.cacheDir, filename)
            val outputStream = FileOutputStream(file)
            document.writeTo(outputStream)
            document.close()
            outputStream.flush()
            outputStream.close()

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Relatório Oficial de Produção - Farinhas CJ")
                putExtra(Intent.EXTRA_TEXT, "Segue anexo o relatório oficial em PDF gerado pelo sistema de produção Farinhas CJ.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(shareIntent, "Compartilhar PDF do Relatório").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
            Toast.makeText(context, "PDF gerado com sucesso!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Erro ao gerar PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
