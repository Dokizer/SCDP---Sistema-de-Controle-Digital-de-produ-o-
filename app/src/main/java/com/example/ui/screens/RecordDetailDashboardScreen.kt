package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import com.example.ui.theme.materialYouPress
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.TableChart
import com.example.data.model.DatingCheckEntity
import com.example.data.model.HistoryType
import com.example.data.model.MachineDowntimeEntity
import com.example.data.model.PackagingControlEntity
import com.example.ui.AppScreen
import com.example.ui.ProductionViewModel
import com.example.util.SpreadsheetExportUtil
import com.example.ui.theme.PdfAmber
import com.example.ui.theme.PdfCardBackground
import com.example.ui.theme.PdfCardBorder
import com.example.ui.theme.PdfCyanPrimary
import com.example.ui.theme.PdfDarkBackground
import com.example.ui.theme.PdfGreen
import com.example.ui.theme.PdfInputBorder
import com.example.ui.theme.PdfRed
import com.example.ui.theme.PdfTextMuted
import com.example.ui.theme.PdfTextPlaceholder
import com.example.ui.theme.PdfTextWhite
import com.example.util.PdfExportUtil
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ZoomIn
import com.example.util.ImageStorageUtil
import java.io.File

// Data holder models for parsed values
private data class DashboardHourlyProd(val hour: String, val real: String, val meta: String, val nTrans: String, val outros: String)
private data class DashboardWeightFaixa(val faixa: String, val count: Int)
private data class DashboardBobinaRow(val index: Int, val lot: String, val supplier: String, val filmWaste: String, val defect: String)
private data class DashboardDowntimeItem(val code: String, val desc: String, val pStart: String, val pEnd: String, val mStart: String, val mEnd: String, val obs: String)
private data class DashboardDatingItem(val hour: String, val lot: String, val time: String, val exp: String, val isCompliant: Boolean, val photoUri: String = "")

@Composable
fun RecordDetailDashboardScreen(
    id: String,
    type: HistoryType,
    viewModel: ProductionViewModel
) {
    val context = LocalContext.current
    var packagingEntity by remember { mutableStateOf<PackagingControlEntity?>(null) }
    var downtimeEntity by remember { mutableStateOf<MachineDowntimeEntity?>(null) }
    var datingEntity by remember { mutableStateOf<DatingCheckEntity?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(id, type) {
        isLoading = true
        when (type) {
            HistoryType.PACKAGING -> {
                packagingEntity = viewModel.getPackagingControl(id)
            }
            HistoryType.DOWNTIME -> {
                downtimeEntity = viewModel.getMachineDowntime(id)
            }
            HistoryType.DATING -> {
                datingEntity = viewModel.getDatingCheck(id)
            }
        }
        isLoading = false
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainer,
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.navigateTo(AppScreen.History) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when (type) {
                                HistoryType.PACKAGING -> "DASHBOARD DE EMPACOTAMENTO"
                                HistoryType.DOWNTIME -> "DASHBOARD DE PARADAS"
                                HistoryType.DATING -> "DASHBOARD DE DATAGEM"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = when (type) {
                                HistoryType.PACKAGING -> "${packagingEntity?.product ?: ""} • Linha ${packagingEntity?.line ?: ""}"
                                HistoryType.DOWNTIME -> "${downtimeEntity?.product ?: ""} • Linha ${downtimeEntity?.line ?: ""}"
                                HistoryType.DATING -> "Máquina ${datingEntity?.machine ?: ""} • ${datingEntity?.shift ?: ""}"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // PDF Export Quick Action
                    IconButton(
                        onClick = {
                            val dateTag = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
                            when (type) {
                                HistoryType.PACKAGING -> packagingEntity?.let {
                                    val csv = SpreadsheetExportUtil.generateSinglePackagingCsv(it)
                                    SpreadsheetExportUtil.downloadCsv(context, "SCDP_Empacotamento_${it.line}_$dateTag.csv", csv)
                                }
                                HistoryType.DOWNTIME -> downtimeEntity?.let {
                                    val csv = SpreadsheetExportUtil.generateSingleDowntimeCsv(it)
                                    SpreadsheetExportUtil.downloadCsv(context, "SCDP_Paradas_${it.line}_$dateTag.csv", csv)
                                }
                                HistoryType.DATING -> datingEntity?.let {
                                    val csv = SpreadsheetExportUtil.generateSingleDatingCsv(it)
                                    SpreadsheetExportUtil.downloadCsv(context, "SCDP_Datagem_${it.machine}_$dateTag.csv", csv)
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.TableChart,
                            contentDescription = "Exportar Planilha CSV",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = {
                            when (type) {
                                HistoryType.PACKAGING -> packagingEntity?.let { PdfExportUtil.exportPackagingPdf(context, it) }
                                HistoryType.DOWNTIME -> downtimeEntity?.let { PdfExportUtil.exportDowntimePdf(context, it) }
                                HistoryType.DATING -> datingEntity?.let { PdfExportUtil.exportDatingPdf(context, it) }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = "Exportar PDF",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainer,
                tonalElevation = 3.dp,
                shadowElevation = 8.dp
            ) {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    val isWideScreen = maxWidth >= 480.dp

                    if (isWideScreen) {
                        // Layout lado a lado com espaço generoso para tablets e modo paisagem
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    when (type) {
                                        HistoryType.PACKAGING -> packagingEntity?.let { PdfExportUtil.downloadPackagingPdf(context, it) }
                                        HistoryType.DOWNTIME -> downtimeEntity?.let { PdfExportUtil.downloadDowntimePdf(context, it) }
                                        HistoryType.DATING -> datingEntity?.let { PdfExportUtil.downloadDatingPdf(context, it) }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                shape = RoundedCornerShape(14.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .materialYouPress(scaleFactor = 0.97f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PictureAsPdf,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Baixar PDF",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    maxLines = 1
                                )
                            }

                            FilledTonalButton(
                                onClick = {
                                    val dateTag = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
                                    when (type) {
                                        HistoryType.PACKAGING -> packagingEntity?.let {
                                            val csv = SpreadsheetExportUtil.generateSinglePackagingCsv(it)
                                            SpreadsheetExportUtil.downloadCsv(context, "SCDP_Empacotamento_${it.line}_$dateTag.csv", csv)
                                        }
                                        HistoryType.DOWNTIME -> downtimeEntity?.let {
                                            val csv = SpreadsheetExportUtil.generateSingleDowntimeCsv(it)
                                            SpreadsheetExportUtil.downloadCsv(context, "SCDP_Paradas_${it.line}_$dateTag.csv", csv)
                                        }
                                        HistoryType.DATING -> datingEntity?.let {
                                            val csv = SpreadsheetExportUtil.generateSingleDatingCsv(it)
                                            SpreadsheetExportUtil.downloadCsv(context, "SCDP_Datagem_${it.machine}_$dateTag.csv", csv)
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(14.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .materialYouPress(scaleFactor = 0.97f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TableChart,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Baixar Planilha",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    maxLines = 1
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    when (type) {
                                        HistoryType.PACKAGING -> packagingEntity?.let { PdfExportUtil.sharePackagingPdf(context, it) }
                                        HistoryType.DOWNTIME -> downtimeEntity?.let { PdfExportUtil.shareDowntimePdf(context, it) }
                                        HistoryType.DATING -> datingEntity?.let { PdfExportUtil.shareDatingPdf(context, it) }
                                    }
                                },
                                shape = RoundedCornerShape(14.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .materialYouPress(scaleFactor = 0.97f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Compartilhar",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    maxLines = 1
                                )
                            }
                        }
                    } else {
                        // Layout dinâmico em 2 níveis para celulares (garante legibilidade total e zero cortes)
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Linha 1: Os dois botões principais de Download lado a lado com largura total
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Botão 1: BAIXAR PDF
                                Button(
                                    onClick = {
                                        when (type) {
                                            HistoryType.PACKAGING -> packagingEntity?.let { PdfExportUtil.downloadPackagingPdf(context, it) }
                                            HistoryType.DOWNTIME -> downtimeEntity?.let { PdfExportUtil.downloadDowntimePdf(context, it) }
                                            HistoryType.DATING -> datingEntity?.let { PdfExportUtil.downloadDatingPdf(context, it) }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(46.dp)
                                        .materialYouPress(scaleFactor = 0.97f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PictureAsPdf,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Baixar PDF",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        maxLines = 1
                                    )
                                }

                                // Botão 2: BAIXAR PLANILHA
                                FilledTonalButton(
                                    onClick = {
                                        val dateTag = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
                                        when (type) {
                                            HistoryType.PACKAGING -> packagingEntity?.let {
                                                val csv = SpreadsheetExportUtil.generateSinglePackagingCsv(it)
                                                SpreadsheetExportUtil.downloadCsv(context, "SCDP_Empacotamento_${it.line}_$dateTag.csv", csv)
                                            }
                                            HistoryType.DOWNTIME -> downtimeEntity?.let {
                                                val csv = SpreadsheetExportUtil.generateSingleDowntimeCsv(it)
                                                SpreadsheetExportUtil.downloadCsv(context, "SCDP_Paradas_${it.line}_$dateTag.csv", csv)
                                            }
                                            HistoryType.DATING -> datingEntity?.let {
                                                val csv = SpreadsheetExportUtil.generateSingleDatingCsv(it)
                                                SpreadsheetExportUtil.downloadCsv(context, "SCDP_Datagem_${it.machine}_$dateTag.csv", csv)
                                            }
                                        }
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(46.dp)
                                        .materialYouPress(scaleFactor = 0.97f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.TableChart,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Baixar Planilha",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        maxLines = 1
                                    )
                                }
                            }

                            // Linha 2: Compartilhar com largura total
                            OutlinedButton(
                                onClick = {
                                    when (type) {
                                        HistoryType.PACKAGING -> packagingEntity?.let { PdfExportUtil.sharePackagingPdf(context, it) }
                                        HistoryType.DOWNTIME -> downtimeEntity?.let { PdfExportUtil.shareDowntimePdf(context, it) }
                                        HistoryType.DATING -> datingEntity?.let { PdfExportUtil.shareDatingPdf(context, it) }
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp)
                                    .materialYouPress(scaleFactor = 0.98f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Compartilhar Relatório (WhatsApp / E-mail)",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                when (type) {
                    HistoryType.PACKAGING -> {
                        packagingEntity?.let { entity ->
                            PackagingDashboardContent(entity)
                        } ?: RecordNotFoundView()
                    }
                    HistoryType.DOWNTIME -> {
                        downtimeEntity?.let { entity ->
                            DowntimeDashboardContent(entity)
                        } ?: RecordNotFoundView()
                    }
                    HistoryType.DATING -> {
                        datingEntity?.let { entity ->
                            DatingDashboardContent(entity)
                        } ?: RecordNotFoundView()
                    }
                }
            }
        }
    }
}

// ==========================================
// 1. PACKAGING DETAIL DASHBOARD
// ==========================================
@Composable
private fun PackagingDashboardContent(entity: PackagingControlEntity) {
    val hourlyItems = remember(entity.hourlyProductionJson) {
        val list = mutableListOf<DashboardHourlyProd>()
        try {
            val arr = JSONArray(entity.hourlyProductionJson.ifBlank { "[]" })
            for (i in 0 until arr.length()) {
                val item = arr.getJSONObject(i)
                list.add(
                    DashboardHourlyProd(
                        hour = item.optString("hourLabel", ""),
                        real = item.optString("real", "-"),
                        meta = item.optString("meta", "-"),
                        nTrans = item.optString("nTransg", ""),
                        outros = item.optString("outros", "")
                    )
                )
            }
        } catch (_: Exception) {}
        list
    }

    val (gramatura, weightFaixas) = remember(entity.weightsJson) {
        var g = "400g"
        val faixas = mutableListOf<DashboardWeightFaixa>()
        try {
            val obj = JSONObject(entity.weightsJson.ifBlank { "{}" })
            g = obj.optString("gramatura", "400g")
            val gridObj = obj.optJSONObject("grid")
            if (gridObj != null) {
                gridObj.keys().forEach { faixa ->
                    val arr = gridObj.optJSONArray(faixa)
                    var count = 0
                    if (arr != null) {
                        for (i in 0 until arr.length()) {
                            if (arr.optString(i).isNotBlank()) count++
                        }
                    }
                    if (count > 0) {
                        faixas.add(DashboardWeightFaixa(faixa, count))
                    }
                }
            }
        } catch (_: Exception) {}
        Pair(g, faixas)
    }

    val bobinaRows = remember(entity.empacotadoraRowsJson) {
        val list = mutableListOf<DashboardBobinaRow>()
        try {
            val arr = JSONArray(entity.empacotadoraRowsJson.ifBlank { "[]" })
            for (i in 0 until arr.length()) {
                val row = arr.getJSONObject(i)
                val lot = row.optString("lot", "")
                val supplier = row.optString("supplier", "")
                val qty = row.optString("quantity", "")
                if (lot.isNotBlank() || supplier.isNotBlank() || qty.isNotBlank()) {
                    list.add(
                        DashboardBobinaRow(
                            index = i + 1,
                            lot = if (lot.isNotBlank()) lot else "-",
                            supplier = if (supplier.isNotBlank()) supplier else "-",
                            filmWaste = row.optString("filmWasteKg", "-"),
                            defect = row.optString("qualityDefectWeight", "-")
                        )
                    )
                }
            }
        } catch (_: Exception) {}
        list
    }

    val fardoRows = remember(entity.enfardadeiraRowsJson) {
        val list = mutableListOf<DashboardBobinaRow>()
        try {
            val arr = JSONArray(entity.enfardadeiraRowsJson.ifBlank { "[]" })
            for (i in 0 until arr.length()) {
                val row = arr.getJSONObject(i)
                val lot = row.optString("lot", "")
                val supplier = row.optString("supplier", "")
                val qty = row.optString("quantity", "")
                if (lot.isNotBlank() || supplier.isNotBlank() || qty.isNotBlank()) {
                    list.add(
                        DashboardBobinaRow(
                            index = i + 1,
                            lot = if (lot.isNotBlank()) lot else "-",
                            supplier = if (supplier.isNotBlank()) supplier else "-",
                            filmWaste = row.optString("filmWasteKg", "-"),
                            defect = row.optString("qualityDefectWeight", "-")
                        )
                    )
                }
            }
        } catch (_: Exception) {}
        list
    }

    // Top KPI Metrics
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MetricKpiCard(
            title = "TOTAL REALIZADO",
            value = "${entity.totalRealizedFds} fds",
            icon = Icons.Default.Inventory2,
            iconTint = PdfCyanPrimary,
            modifier = Modifier.weight(1f)
        )
        MetricKpiCard(
            title = "VELOCIDADE",
            value = "${entity.initialSpeed} bpm",
            icon = Icons.Default.Speed,
            iconTint = PdfAmber,
            modifier = Modifier.weight(1f)
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MetricKpiCard(
            title = "MÉDIA TURNO",
            value = if (entity.shiftAverageWeight.isNotBlank()) "${entity.shiftAverageWeight} g" else "-",
            icon = Icons.Default.Assignment,
            iconTint = PdfGreen,
            modifier = Modifier.weight(1f)
        )
        MetricKpiCard(
            title = "STATUS GERAL",
            value = if (entity.machineCondition == "C" && entity.setorCondition == "C") "CONFORME" else "ALERTA",
            icon = if (entity.machineCondition == "C") Icons.Default.CheckCircle else Icons.Default.Warning,
            iconTint = if (entity.machineCondition == "C") PdfGreen else PdfRed,
            modifier = Modifier.weight(1f)
        )
    }

    // Identification Card
    DashboardCard(title = "1. IDENTIFICAÇÃO E TURNO") {
        DashboardRow("Data", entity.date)
        DashboardRow("Turno", entity.shift)
        DashboardRow("Linha", entity.line)
        DashboardRow("Produto", entity.product)
        DashboardRow("Operador Responsável", entity.responsible)
        val formattedTime = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(entity.createdAt))
        DashboardRow("Registro Criado Em", formattedTime)
    }

    // Machine & Sector Inspection Card
    DashboardCard(title = "2. STATUS DE CONFORMIDADE & LIMPEZA") {
        ConformityBadgeRow("Empacotadora Parte Interna", entity.empacotadoraInternaC)
        ConformityBadgeRow("Empacotadora Parte Externa", entity.empacotadoraExternaC)
        ConformityBadgeRow("Enfardadeira Parte Interna", entity.enfardadeiraInternaC)
        ConformityBadgeRow("Enfardadeira Parte Externa", entity.enfardadeiraExternaC)
        ConformityBadgeRow("Máquina Limpa e Operacional", entity.machineCondition == "C")
        ConformityBadgeRow("Setor Limpo e Organizado", entity.setorCondition == "C")
        ConformityBadgeRow("Caixas de Lixo c/ Plástico", entity.trashCondition == "C")
        ConformityBadgeRow("Setor Abastecido", entity.supplyCondition == "C")
    }

    // Hourly Production Card
    DashboardCard(title = "3. PRODUÇÃO HORÁRIA REALIZADA") {
        if (hourlyItems.isEmpty()) {
            Text("Sem registros horários salvos", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                hourlyItems.forEach { item ->
                    Surface(
                        color = MaterialTheme.colorScheme.background,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = item.hour, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                                Text(
                                    text = "Realizado: ${item.real} fds",
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = "Meta: ${item.meta}", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), style = MaterialTheme.typography.labelSmall)
                                if (item.nTrans.isNotBlank() || item.outros.isNotBlank()) {
                                    Text(
                                        text = "N.Tr: ${item.nTrans} | Out: ${item.outros}",
                                        color = MaterialTheme.colorScheme.tertiary,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Metrology & Weights Card
    DashboardCard(title = "4. PESO LÍQUIDO E METROLOGIA") {
        DashboardRow("Média do Turno", if (entity.shiftAverageWeight.isNotBlank()) "${entity.shiftAverageWeight} g" else "-")
        DashboardRow("Tara da Embalagem", if (entity.packageTareWeight.isNotBlank()) "${entity.packageTareWeight} g" else "-")
        DashboardRow("Gramatura Monitorada", gramatura)

        if (weightFaixas.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Amostragens Registradas na Curva:",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            weightFaixas.forEach { wf ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Faixa ${wf.faixa} g:", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = "${wf.count} marcações",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    // Packaging & Lots Card
    DashboardCard(title = "5. BOBINAS, APARAS & DEFEITOS") {
        DashboardRow("Sobra Laminado", entity.leftoverLaminated.ifBlank { "0 kg" })
        DashboardRow("Sobra Enfardadeira", entity.leftoverBaler.ifBlank { "0 kg" })
        DashboardRow("Embalado Mesmo Produto", if (entity.packagingSameProduct) "Sim" else "Não")

        if (bobinaRows.isNotEmpty()) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Text(text = "Planilha de Bobinas (Empacotadora):", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            bobinaRows.forEach { b ->
                DashboardRow("Bobina ${b.index} (${b.lot})", "${b.supplier} | Apara: ${b.filmWaste} kg | Def: ${b.defect} kg")
            }
        }

        if (fardoRows.isNotEmpty()) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Text(text = "Planilha de Fardos (Enfardadeira):", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            fardoRows.forEach { f ->
                DashboardRow("Fardo ${f.index} (${f.lot})", "${f.supplier} | Desp: ${f.filmWaste} kg | Def: ${f.defect} kg")
            }
        }
    }

    // Observations
    if (entity.generalObservations.isNotBlank()) {
        DashboardCard(title = "6. OBSERVAÇÕES DO TURNO") {
            Text(
                text = entity.generalObservations,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 18.sp
            )
        }
    }
}

// ==========================================
// 2. DOWNTIME DETAIL DASHBOARD
// ==========================================
@Composable
private fun DowntimeDashboardContent(entity: MachineDowntimeEntity) {
    val downtimeEntries = remember(entity.entriesJson) {
        val list = mutableListOf<DashboardDowntimeItem>()
        try {
            val arr = JSONArray(entity.entriesJson.ifBlank { "[]" })
            for (i in 0 until arr.length()) {
                val item = arr.getJSONObject(i)
                list.add(
                    DashboardDowntimeItem(
                        code = item.optString("stopCode", "-"),
                        desc = item.optString("stopDescription", "-"),
                        pStart = item.optString("startParada", "--:--"),
                        pEnd = item.optString("endParada", "--:--"),
                        mStart = item.optString("startManutencao", "--:--"),
                        mEnd = item.optString("endManutencao", "--:--"),
                        obs = item.optString("comments", "")
                    )
                )
            }
        } catch (_: Exception) {}
        list
    }

    // Top KPI Metrics
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MetricKpiCard(
            title = "TOTAL DE PARADAS",
            value = "${entity.totalStopsCount}",
            icon = Icons.Default.PrecisionManufacturing,
            iconTint = PdfAmber,
            modifier = Modifier.weight(1f)
        )
        MetricKpiCard(
            title = "TRANSGÊNICO",
            value = if (entity.isTransgenic) "SIM (Transg.)" else "NÃO (Conv.)",
            icon = Icons.Default.Assignment,
            iconTint = if (entity.isTransgenic) PdfAmber else PdfGreen,
            modifier = Modifier.weight(1f)
        )
    }

    DashboardCard(title = "1. IDENTIFICAÇÃO DO TURNO") {
        DashboardRow("Data", entity.date)
        DashboardRow("Turno", entity.shift)
        DashboardRow("Linha", entity.line)
        DashboardRow("Produto", entity.product)
        DashboardRow("Operador Responsável", entity.responsible)
        val formattedTime = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(entity.createdAt))
        DashboardRow("Registro Criado Em", formattedTime)
    }

    DashboardCard(title = "2. OCORRÊNCIAS DE PARADA DETALHADAS") {
        if (downtimeEntries.isEmpty()) {
            Text("Nenhuma parada registrada", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                downtimeEntries.forEach { item ->
                    Surface(
                        color = MaterialTheme.colorScheme.background,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = MaterialTheme.colorScheme.tertiaryContainer,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = if (item.code.isNotBlank() && item.code != "-") "CÓD. ${item.code}" else "SEM CÓDIGO",
                                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }

                                Text(
                                    text = "Parada: ${item.pStart} às ${item.pEnd}",
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = item.desc.ifBlank { "Motivo não especificado" },
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )

                            if (item.mStart.isNotBlank() && item.mStart != "--:--" || item.mEnd.isNotBlank() && item.mEnd != "--:--") {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Atuação Manutenção: ${item.mStart} às ${item.mEnd}",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }

                            if (item.obs.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Obs: ${item.obs}",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 3. DATING DETAIL DASHBOARD
// ==========================================
@Composable
private fun DatingDashboardContent(entity: DatingCheckEntity) {
    val context = LocalContext.current
    val total = entity.compliantCount + entity.nonCompliantCount
    val complianceRate = if (total > 0) (entity.compliantCount * 100) / total else 100
    var previewPhotoUri by remember { mutableStateOf<String?>(null) }
    var previewPhotoTitle by remember { mutableStateOf("") }

    val datingItems = remember(entity.checksJson) {
        val list = mutableListOf<DashboardDatingItem>()
        try {
            val arr = JSONArray(entity.checksJson.ifBlank { "[]" })
            for (i in 0 until arr.length()) {
                val item = arr.getJSONObject(i)
                list.add(
                    DashboardDatingItem(
                        hour = item.optString("hourLabel", "Hora ${i + 1}"),
                        lot = item.optString("lotNumber", "-"),
                        time = item.optString("printedTime", "-"),
                        exp = item.optString("printedExpiration", "-"),
                        isCompliant = item.optBoolean("isCompliant", true),
                        photoUri = item.optString("photoUri", "")
                    )
                )
            }
        } catch (_: Exception) {}
        list
    }

    val itemsWithPhotos = remember(datingItems) {
        datingItems.filter { it.photoUri.isNotBlank() }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MetricKpiCard(
            title = "CONFORMIDADE",
            value = "$complianceRate%",
            icon = Icons.Default.CheckCircle,
            iconTint = if (complianceRate >= 90) PdfGreen else PdfAmber,
            modifier = Modifier.weight(1f)
        )
        MetricKpiCard(
            title = "TOTAL AMOSTRAS",
            value = "$total avaliadas",
            icon = Icons.Default.Assignment,
            iconTint = PdfCyanPrimary,
            modifier = Modifier.weight(1f)
        )
    }

    DashboardCard(title = "1. IDENTIFICAÇÃO DA CHECAGEM") {
        DashboardRow("Data", entity.date)
        DashboardRow("Turno", entity.shift)
        DashboardRow("Máquina", entity.machine)
        DashboardRow("Inspetor de Qualidade", entity.responsible)
        DashboardRow("Amostras Conformes", "${entity.compliantCount}")
        DashboardRow("Não Conformes", "${entity.nonCompliantCount}")
        DashboardRow("Fotos Anexadas", "${itemsWithPhotos.size} registro(s)")
    }

    DashboardCard(title = "2. AMOSTRAGEM HORÁRIA DE DATAGEM") {
        if (datingItems.isEmpty()) {
            Text("Nenhuma checagem horária salva", color = PdfTextMuted, fontSize = 12.sp)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                datingItems.forEach { item ->
                    Surface(
                        color = PdfDarkBackground,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, if (item.isCompliant) PdfInputBorder else PdfRed.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = item.hour, color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "Lote: ${item.lot} | Val: ${item.exp} | Hora: ${item.time}",
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Surface(
                                    color = if (item.isCompliant) PdfGreen.copy(alpha = 0.15f) else PdfRed.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(4.dp),
                                    border = BorderStroke(1.dp, if (item.isCompliant) PdfGreen else PdfRed)
                                ) {
                                    Text(
                                        text = if (item.isCompliant) "CONFORME" else "NÃO CONF.",
                                        color = if (item.isCompliant) PdfGreen else PdfRed,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            if (item.photoUri.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                val bitmap = remember(item.photoUri) {
                                    ImageStorageUtil.loadBitmap(context, item.photoUri)?.asImageBitmap()
                                }

                                if (bitmap != null) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .clickable {
                                                previewPhotoUri = item.photoUri
                                                previewPhotoTitle = "${item.hour} • Lote: ${item.lot}"
                                            }
                                    ) {
                                        Box {
                                            Image(
                                                bitmap = bitmap,
                                                contentDescription = "Foto da etiqueta",
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(130.dp),
                                                contentScale = ContentScale.Crop
                                            )
                                            Surface(
                                                color = Color.Black.copy(alpha = 0.65f),
                                                shape = RoundedCornerShape(bottomEnd = 6.dp),
                                                modifier = Modifier.align(Alignment.TopStart)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.CameraAlt,
                                                        contentDescription = null,
                                                        tint = PdfGreen,
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                    Text(
                                                        text = "Foto da Etiqueta Anexada",
                                                        color = Color.White,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // 3. FOTOS DAS ETIQUETAS COLETADAS
    if (itemsWithPhotos.isNotEmpty()) {
        DashboardCard(title = "3. REGISTROS FOTOGRÁFICOS DAS ETIQUETAS (${itemsWithPhotos.size})") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                itemsWithPhotos.forEach { photoItem ->
                    val bitmap = remember(photoItem.photoUri) {
                        ImageStorageUtil.loadBitmap(context, photoItem.photoUri)?.asImageBitmap()
                    }

                    if (bitmap != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    previewPhotoUri = photoItem.photoUri
                                    previewPhotoTitle = "${photoItem.hour} • Lote ${photoItem.lot}"
                                }
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = photoItem.hour,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.ZoomIn,
                                            contentDescription = "Ampliar foto",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Toque p/ ampliar",
                                            color = MaterialTheme.colorScheme.primary,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Image(
                                    bitmap = bitmap,
                                    contentDescription = "Foto da etiqueta ${photoItem.hour}",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .clip(RoundedCornerShape(6.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Lote: ${photoItem.lot} | Validade: ${photoItem.exp} | Horário: ${photoItem.time}",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (entity.correctiveActions.isNotBlank()) {
        DashboardCard(title = "4. AÇÕES CORRETIVAS ADOTADAS") {
            Text(
                text = entity.correctiveActions,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
    }

    // Modal de Zoom da Foto
    if (previewPhotoUri != null) {
        Dialog(
            onDismissRequest = { previewPhotoUri = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.92f))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = previewPhotoTitle,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { previewPhotoUri = null }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Fechar",
                                tint = Color.White
                            )
                        }
                    }

                    val fullBitmap = remember(previewPhotoUri) {
                        ImageStorageUtil.loadBitmap(context, previewPhotoUri!!)?.asImageBitmap()
                    }

                    if (fullBitmap != null) {
                        Image(
                            bitmap = fullBitmap,
                            contentDescription = "Foto da etiqueta em tamanho ampliado",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// REUSABLE DASHBOARD UI COMPONENTS
// ==========================================
@Composable
private fun MetricKpiCard(
    title: String,
    value: String,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun DashboardCard(
    title: String,
    content: @Composable () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.6.sp
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            content()
        }
    }
}

@Composable
private fun DashboardRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f, fill = false)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = value,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f, fill = false)
        )
    }
}

@Composable
private fun ConformityBadgeRow(label: String, isCompliant: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f, fill = false)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = if (isCompliant) Icons.Default.CheckCircle else Icons.Default.Cancel,
                contentDescription = null,
                tint = if (isCompliant) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = if (isCompliant) "Conforme (C)" else "Não Conforme (NC)",
                color = if (isCompliant) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun RecordNotFoundView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Registro não encontrado no banco de dados.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
