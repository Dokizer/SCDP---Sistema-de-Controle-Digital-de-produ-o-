package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.DatingCheckEntity
import com.example.data.model.HistoryType
import com.example.data.model.MachineDowntimeEntity
import com.example.data.model.PackagingControlEntity
import com.example.data.model.UnifiedHistoryItem
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class SpreadsheetExportType(val label: String, val filenamePrefix: String) {
    CONSOLIDATED("Relatório Geral Consolidado", "SCDP_Geral_Consolidado"),
    PACKAGING("Controle de Empacotamento", "SCDP_Empacotamento"),
    DOWNTIME("Apontamento de Paradas de Máquinas", "SCDP_Paradas_Maquinas"),
    DATING("Checagem de Datagem dos Pacotes", "SCDP_Checagem_Datagem")
}

enum class SpreadsheetDateFilter(val label: String) {
    ALL("Todos os Registros"),
    TODAY("Hoje"),
    LAST_7_DAYS("Últimos 7 Dias"),
    CURRENT_MONTH("Este Mês")
}

object SpreadsheetExportUtil {

    private const val CSV_SEPARATOR = ";"
    private const val UTF8_BOM = "\uFEFF"

    /**
     * Escapes a single CSV value according to standard CSV RFC 4180 rules,
     * replacing internal quotes with double quotes and wrapping in quotes if needed.
     */
    private fun escapeCsv(value: Any?): String {
        if (value == null) return ""
        val str = value.toString().trim()
        val containsSpecial = str.contains(CSV_SEPARATOR) || str.contains("\"") || str.contains("\n") || str.contains("\r")
        return if (containsSpecial) {
            "\"${str.replace("\"", "\"\"")}\""
        } else {
            str
        }
    }

    private fun formatTimestamp(timestamp: Long): String {
        return try {
            SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date(timestamp))
        } catch (_: Exception) {
            "-"
        }
    }

    private fun isWithinDateFilter(createdAt: Long, dateString: String, filter: SpreadsheetDateFilter): Boolean {
        if (filter == SpreadsheetDateFilter.ALL) return true

        val now = Calendar.getInstance()
        val recordCal = Calendar.getInstance().apply { timeInMillis = createdAt }

        return when (filter) {
            SpreadsheetDateFilter.ALL -> true
            SpreadsheetDateFilter.TODAY -> {
                val todayStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(now.time)
                dateString.trim() == todayStr ||
                        (recordCal.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                                recordCal.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR))
            }
            SpreadsheetDateFilter.LAST_7_DAYS -> {
                val sevenDaysAgo = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000)
                createdAt >= sevenDaysAgo
            }
            SpreadsheetDateFilter.CURRENT_MONTH -> {
                recordCal.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                        recordCal.get(Calendar.MONTH) == now.get(Calendar.MONTH)
            }
        }
    }

    // ==========================================
    // 1. GERAÇÃO: RELATÓRIO GERAL CONSOLIDADO
    // ==========================================
    fun generateConsolidatedCsv(
        history: List<UnifiedHistoryItem>,
        filter: SpreadsheetDateFilter
    ): String {
        val sb = StringBuilder()
        sb.append(UTF8_BOM)

        // Metadata Header
        sb.append("# FARINHAS CJ — SISTEMA DE CONTROLE DIGITAL DE PRODUÇÃO (SCDP)\n")
        sb.append("# RELATÓRIO GERAL CONSOLIDADO DE PRODUÇÃO\n")
        sb.append("# Gerado em: ${formatTimestamp(System.currentTimeMillis())} | Filtro: ${filter.label}\n\n")

        // Table Header
        val headers = listOf(
            "Tipo de Controle",
            "Título do Registro",
            "Data",
            "Turno",
            "Linha / Máquina",
            "Produto / Identificação",
            "Resumo de Métricas",
            "Data e Hora do Registro"
        )
        sb.append(headers.joinToString(CSV_SEPARATOR) { escapeCsv(it) }).append("\n")

        // Rows
        val filtered = history.filter { isWithinDateFilter(it.timestamp, it.date, filter) }
        for (item in filtered) {
            val typeName = when (item.type) {
                HistoryType.PACKAGING -> "Controle de Empacotamento"
                HistoryType.DOWNTIME -> "Apontamento de Paradas"
                HistoryType.DATING -> "Checagem de Datagem"
            }
            val row = listOf(
                typeName,
                item.title,
                item.date,
                item.shift,
                item.line,
                item.subtitle,
                item.metricsSummary,
                formatTimestamp(item.timestamp)
            )
            sb.append(row.joinToString(CSV_SEPARATOR) { escapeCsv(it) }).append("\n")
        }

        return sb.toString()
    }

    // ==========================================
    // 2. GERAÇÃO: CONTROLE DE EMPACOTAMENTO
    // ==========================================
    fun generatePackagingCsv(
        items: List<PackagingControlEntity>,
        filter: SpreadsheetDateFilter
    ): String {
        val sb = StringBuilder()
        sb.append(UTF8_BOM)

        sb.append("# FARINHAS CJ — SCDP\n")
        sb.append("# PLANILHA DE CONTROLE DIÁRIO DE EMPACOTAMENTO (FORM.003.FAR.CJ)\n")
        sb.append("# Gerado em: ${formatTimestamp(System.currentTimeMillis())} | Filtro: ${filter.label}\n\n")

        val headers = listOf(
            "Data",
            "Turno",
            "Linha",
            "Produto",
            "Mudança para",
            "Horário Mudança",
            "Responsável",
            "Fardos Realizados (fds)",
            "Velocidade Inicial (bpm)",
            "Velocidade Alterada",
            "Motivo Mudança Velocidade",
            "Empacotadora Interna",
            "Empacotadora Externa",
            "Enfardadeira Interna",
            "Enfardadeira Externa",
            "Condição Máquina",
            "Condição Setor",
            "Lixeiras",
            "Abastecido",
            "Média Peso Turno (g)",
            "Tara Pacote (g)",
            "Bobinas Empacotadora",
            "Bobinas Enfardadeira",
            "Peso Inicial Empacotadora (g)",
            "Peso Inicial Enfardadeira (g)",
            "Devolução Empacotadora (kg)",
            "Defeito Devolução Empacotadora",
            "Devolução Enfardadeira (kg)",
            "Defeito Devolução Enfardadeira",
            "Sobra Laminado (kg)",
            "Sobra Fardo (kg)",
            "Observações Gerais",
            "Data e Hora do Registro"
        )
        sb.append(headers.joinToString(CSV_SEPARATOR) { escapeCsv(it) }).append("\n")

        val filtered = items.filter { isWithinDateFilter(it.createdAt, it.date, filter) }
        for (it in filtered) {
            val empBobinas = try {
                val arr = JSONArray(it.empacotadoraRowsJson.ifBlank { "[]" })
                val list = mutableListOf<String>()
                for (b in 0 until arr.length()) {
                    val obj = arr.getJSONObject(b)
                    val lot = obj.optString("lot", "")
                    val sup = obj.optString("supplier", "")
                    if (lot.isNotBlank() || sup.isNotBlank()) list.add("$lot ($sup)")
                }
                if (list.isEmpty()) "-" else list.joinToString(", ")
            } catch (_: Exception) { "-" }

            val enfBobinas = try {
                val arr = JSONArray(it.enfardadeiraRowsJson.ifBlank { "[]" })
                val list = mutableListOf<String>()
                for (b in 0 until arr.length()) {
                    val obj = arr.getJSONObject(b)
                    val lot = obj.optString("lot", "")
                    val sup = obj.optString("supplier", "")
                    if (lot.isNotBlank() || sup.isNotBlank()) list.add("$lot ($sup)")
                }
                if (list.isEmpty()) "-" else list.joinToString(", ")
            } catch (_: Exception) { "-" }

            val row = listOf(
                it.date,
                it.shift,
                it.line,
                it.product,
                it.productChangedTo.ifBlank { "-" },
                it.productChangeTime.ifBlank { "-" },
                it.responsible,
                it.totalRealizedFds.toString(),
                it.initialSpeed.ifBlank { "-" },
                if (it.speedChanged) "Sim" else "Não",
                it.speedChangeReason.ifBlank { "-" },
                if (it.empacotadoraInternaC) "Conforme" else "Não Conforme",
                if (it.empacotadoraExternaC) "Conforme" else "Não Conforme",
                if (it.enfardadeiraInternaC) "Conforme" else "Não Conforme",
                if (it.enfardadeiraExternaC) "Conforme" else "Não Conforme",
                if (it.machineCondition == "C") "Conforme" else "Não Conforme",
                if (it.setorCondition == "C") "Conforme" else "Não Conforme",
                if (it.trashCondition == "C") "Conforme" else "Não Conforme",
                if (it.supplyCondition == "C") "Conforme" else "Não Conforme",
                it.shiftAverageWeight.ifBlank { "-" },
                it.packageTareWeight.ifBlank { "-" },
                empBobinas,
                enfBobinas,
                it.empacotadoraInitialWeight.ifBlank { "-" },
                it.enfardadeiraInitialWeight.ifBlank { "-" },
                it.empDevolucaoPeso.ifBlank { "0" },
                it.empDevolucaoDefeito.ifBlank { "-" },
                it.enfDevolucaoPeso.ifBlank { "0" },
                it.enfDevolucaoDefeito.ifBlank { "-" },
                it.leftoverLaminated.ifBlank { "0" },
                it.leftoverBaler.ifBlank { "0" },
                it.generalObservations.ifBlank { "-" },
                formatTimestamp(it.createdAt)
            )
            sb.append(row.joinToString(CSV_SEPARATOR) { escapeCsv(it) }).append("\n")
        }

        return sb.toString()
    }

    // ==========================================
    // 3. GERAÇÃO: APONTAMENTO DE PARADAS
    // ==========================================
    fun generateDowntimeCsv(
        items: List<MachineDowntimeEntity>,
        filter: SpreadsheetDateFilter
    ): String {
        val sb = StringBuilder()
        sb.append(UTF8_BOM)

        sb.append("# FARINHAS CJ — SCDP\n")
        sb.append("# PLANILHA DETALHADA DE APONTAMENTO DE PARADA DE MÁQUINAS\n")
        sb.append("# Gerado em: ${formatTimestamp(System.currentTimeMillis())} | Filtro: ${filter.label}\n\n")

        val headers = listOf(
            "Data",
            "Turno",
            "Linha",
            "Produto",
            "Transgênico",
            "Responsável",
            "Código Parada",
            "Descrição da Parada",
            "Início Parada",
            "Fim Parada",
            "Duração (minutos)",
            "Início Manutenção",
            "Fim Manutenção",
            "Observações / Motivo",
            "Total Paradas do Turno",
            "Total Minutos Parados",
            "Data e Hora do Registro"
        )
        sb.append(headers.joinToString(CSV_SEPARATOR) { escapeCsv(it) }).append("\n")

        val filtered = items.filter { isWithinDateFilter(it.createdAt, it.date, filter) }
        for (entity in filtered) {
            val entries = try {
                val array = JSONArray(entity.entriesJson.ifBlank { "[]" })
                val list = mutableListOf<JSONObject>()
                for (i in 0 until array.length()) {
                    list.add(array.getJSONObject(i))
                }
                list
            } catch (_: Exception) {
                emptyList<JSONObject>()
            }

            if (entries.isEmpty()) {
                val row = listOf(
                    entity.date,
                    entity.shift,
                    entity.line,
                    entity.product,
                    if (entity.isTransgenic) "Sim" else "Não",
                    entity.responsible,
                    "-",
                    "Sem paradas detalhadas",
                    "-",
                    "-",
                    "0",
                    "-",
                    "-",
                    "-",
                    entity.totalStopsCount.toString(),
                    entity.totalDowntimeMinutes.toString(),
                    formatTimestamp(entity.createdAt)
                )
                sb.append(row.joinToString(CSV_SEPARATOR) { escapeCsv(it) }).append("\n")
            } else {
                for (entry in entries) {
                    val code = entry.optString("stopCode", "-")
                    val desc = entry.optString("stopDescription", "-")
                    val pStart = entry.optString("startParada", entry.optString("startTime", "-"))
                    val pEnd = entry.optString("endParada", entry.optString("endTime", "-"))
                    val mStart = entry.optString("startManutencao", "-")
                    val mEnd = entry.optString("endManutencao", "-")
                    val dur = entry.optInt("durationMinutes", 0)
                    val obs = entry.optString("comments", "-")

                    val row = listOf(
                        entity.date,
                        entity.shift,
                        entity.line,
                        entity.product,
                        if (entity.isTransgenic) "Sim" else "Não",
                        entity.responsible,
                        code,
                        desc,
                        pStart,
                        pEnd,
                        dur.toString(),
                        mStart,
                        mEnd,
                        obs,
                        entity.totalStopsCount.toString(),
                        entity.totalDowntimeMinutes.toString(),
                        formatTimestamp(entity.createdAt)
                    )
                    sb.append(row.joinToString(CSV_SEPARATOR) { escapeCsv(it) }).append("\n")
                }
            }
        }

        return sb.toString()
    }

    // ==========================================
    // 4. GERAÇÃO: CHECAGEM DE DATAGEM
    // ==========================================
    fun generateDatingCsv(
        items: List<DatingCheckEntity>,
        filter: SpreadsheetDateFilter
    ): String {
        val sb = StringBuilder()
        sb.append(UTF8_BOM)

        sb.append("# FARINHAS CJ — SCDP\n")
        sb.append("# PLANILHA DE CHECAGEM DE DATAGEM DOS PACOTES\n")
        sb.append("# Gerado em: ${formatTimestamp(System.currentTimeMillis())} | Filtro: ${filter.label}\n\n")

        val headers = listOf(
            "Data",
            "Turno",
            "Máquina",
            "Responsável",
            "Horário Checagem",
            "Lote Cadastrado",
            "Hora Impressa",
            "Validade Impressa",
            "Status Conformidade",
            "Foto da Etiqueta Anexada",
            "Total Conformes no Turno",
            "Total Não Conformes no Turno",
            "Ações Corretivas Registradas",
            "Data e Hora do Registro"
        )
        sb.append(headers.joinToString(CSV_SEPARATOR) { escapeCsv(it) }).append("\n")

        val filtered = items.filter { isWithinDateFilter(it.createdAt, it.date, filter) }
        for (entity in filtered) {
            val checks = try {
                val array = JSONArray(entity.checksJson.ifBlank { "[]" })
                val list = mutableListOf<JSONObject>()
                for (i in 0 until array.length()) {
                    list.add(array.getJSONObject(i))
                }
                list
            } catch (_: Exception) {
                emptyList<JSONObject>()
            }

            if (checks.isEmpty()) {
                val row = listOf(
                    entity.date,
                    entity.shift,
                    entity.machine,
                    entity.responsible,
                    "-",
                    "-",
                    "-",
                    "-",
                    if (entity.nonCompliantCount == 0) "Conforme" else "Não Conforme",
                    "-",
                    entity.compliantCount.toString(),
                    entity.nonCompliantCount.toString(),
                    entity.correctiveActions.ifBlank { "-" },
                    formatTimestamp(entity.createdAt)
                )
                sb.append(row.joinToString(CSV_SEPARATOR) { escapeCsv(it) }).append("\n")
            } else {
                for (chk in checks) {
                    val hour = chk.optString("hourLabel", chk.optString("hour", "-"))
                    val lot = chk.optString("lotNumber", chk.optString("lotDate", "-"))
                    val printTime = chk.optString("printedTime", "-")
                    val printExp = chk.optString("printedExpiration", chk.optString("expirationDate", "-"))
                    val isC = chk.optBoolean("isCompliant", true)
                    val photoUri = chk.optString("photoUri", "")
                    val photoStatus = if (photoUri.isNotBlank()) "Sim (Registrada no App/PDF)" else "Não"

                    val row = listOf(
                        entity.date,
                        entity.shift,
                        entity.machine,
                        entity.responsible,
                        hour,
                        lot,
                        printTime,
                        printExp,
                        if (isC) "Conforme" else "NÃO CONFORME",
                        photoStatus,
                        entity.compliantCount.toString(),
                        entity.nonCompliantCount.toString(),
                        entity.correctiveActions.ifBlank { "-" },
                        formatTimestamp(entity.createdAt)
                    )
                    sb.append(row.joinToString(CSV_SEPARATOR) { escapeCsv(it) }).append("\n")
                }
            }
        }

        return sb.toString()
    }

    // ==========================================
    // EXPORTAÇÃO INDIVIDUAL (1 REGISTRO)
    // ==========================================
    fun generateSinglePackagingCsv(entity: PackagingControlEntity): String {
        return generatePackagingCsv(listOf(entity), SpreadsheetDateFilter.ALL)
    }

    fun generateSingleDowntimeCsv(entity: MachineDowntimeEntity): String {
        return generateDowntimeCsv(listOf(entity), SpreadsheetDateFilter.ALL)
    }

    fun generateSingleDatingCsv(entity: DatingCheckEntity): String {
        return generateDatingCsv(listOf(entity), SpreadsheetDateFilter.ALL)
    }

    // ==========================================
    // GRAVAÇÃO E DOWNLOAD LOCAL (Downloads/FarinhasCJ/Planilhas)
    // ==========================================
    fun downloadCsv(context: Context, filename: String, csvContent: String) {
        val safeFilename = if (filename.endsWith(".csv", ignoreCase = true)) filename else "$filename.csv"

        try {
            // 1. Salvar no cache interno para garantir URI do FileProvider
            val cacheFolder = File(context.cacheDir, "spreadsheets").apply { mkdirs() }
            val cacheFile = File(cacheFolder, safeFilename)
            FileOutputStream(cacheFile).use { it.write(csvContent.toByteArray(Charsets.UTF_8)) }

            // 2. Gravação oficial na pasta pública Downloads
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, safeFilename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/FarinhasCJ/Planilhas")
                }
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { it.write(csvContent.toByteArray(Charsets.UTF_8)) }
                }
            } else {
                @Suppress("DEPRECATION")
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val appDir = File(downloadsDir, "FarinhasCJ/Planilhas").apply { mkdirs() }
                val targetFile = File(appDir, safeFilename)
                FileOutputStream(targetFile).use { it.write(csvContent.toByteArray(Charsets.UTF_8)) }
                MediaScannerConnection.scanFile(context, arrayOf(targetFile.absolutePath), arrayOf("text/csv"), null)
            }

            Toast.makeText(
                context,
                "✓ Planilha salva no aparelho!\nPasta: Downloads/FarinhasCJ/Planilhas/$safeFilename",
                Toast.LENGTH_LONG
            ).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Erro ao baixar planilha: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // ==========================================
    // COMPARTILHAMENTO NATIVO (WhatsApp, E-mail, Drive, etc.)
    // ==========================================
    fun shareCsv(context: Context, filename: String, csvContent: String) {
        val safeFilename = if (filename.endsWith(".csv", ignoreCase = true)) filename else "$filename.csv"

        try {
            val cacheFolder = File(context.cacheDir, "spreadsheets").apply { mkdirs() }
            val cacheFile = File(cacheFolder, safeFilename)
            FileOutputStream(cacheFile).use { it.write(csvContent.toByteArray(Charsets.UTF_8)) }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                cacheFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Planilha de Produção Farinhas CJ — $safeFilename")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Segue anexo a planilha de produção gerada pelo Sistema de Controle Digital de Produção (SCDP) — Farinhas CJ."
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(shareIntent, "Compartilhar Planilha via...").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
            Toast.makeText(context, "Planilha pronta para envio!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Erro ao compartilhar planilha: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
