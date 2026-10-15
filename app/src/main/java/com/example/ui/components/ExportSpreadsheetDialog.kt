package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.ProductionViewModel
import com.example.ui.theme.materialYouPress
import com.example.util.SpreadsheetDateFilter
import com.example.util.SpreadsheetExportType
import com.example.util.SpreadsheetExportUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ExportSpreadsheetDialog(
    viewModel: ProductionViewModel,
    initialType: SpreadsheetExportType = SpreadsheetExportType.CONSOLIDATED,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var selectedType by remember { mutableStateOf(initialType) }
    var selectedFilter by remember { mutableStateOf(SpreadsheetDateFilter.ALL) }
    var isGenerating by remember { mutableStateOf(false) }

    val historyItems by viewModel.historyItems.collectAsState()

    AlertDialog(
        onDismissRequest = {
            if (!isGenerating) onDismissRequest()
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.TableChart,
                            contentDescription = "Planilha",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "EXPORTAR PLANILHA",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = "Excel & Google Planilhas",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(
                    onClick = onDismissRequest,
                    enabled = !isGenerating,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Fechar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Seção: Tipo de Planilha
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Selecione o tipo de planilha:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    TypeOptionItem(
                        title = "Relatório Geral Consolidado",
                        subtitle = "Visão executiva com todos os registros juntos",
                        icon = Icons.Default.TableChart,
                        isSelected = selectedType == SpreadsheetExportType.CONSOLIDATED,
                        onClick = { selectedType = SpreadsheetExportType.CONSOLIDATED }
                    )

                    TypeOptionItem(
                        title = "Controle de Empacotamento",
                        subtitle = "Lotes, gramaturas, fardos, bobinas e inspeções",
                        icon = Icons.Default.Inventory2,
                        isSelected = selectedType == SpreadsheetExportType.PACKAGING,
                        onClick = { selectedType = SpreadsheetExportType.PACKAGING }
                    )

                    TypeOptionItem(
                        title = "Apontamento de Paradas",
                        subtitle = "Lista linha a linha de ocorrências e minutos parados",
                        icon = Icons.Default.PrecisionManufacturing,
                        isSelected = selectedType == SpreadsheetExportType.DOWNTIME,
                        onClick = { selectedType = SpreadsheetExportType.DOWNTIME }
                    )

                    TypeOptionItem(
                        title = "Checagem de Datagem",
                        subtitle = "Validação de datas impressas, lotes e conformidade",
                        icon = Icons.Default.AssignmentTurnedIn,
                        isSelected = selectedType == SpreadsheetExportType.DATING,
                        onClick = { selectedType = SpreadsheetExportType.DATING }
                    )
                }

                // Seção: Filtro de Período
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Filtrar por período:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SpreadsheetDateFilter.values().forEach { filterOption ->
                            FilterChip(
                                selected = selectedFilter == filterOption,
                                onClick = { selectedFilter = filterOption },
                                label = {
                                    Text(
                                        text = filterOption.label,
                                        fontSize = 11.sp,
                                        fontWeight = if (selectedFilter == filterOption) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }
                }

                // Nota informativa
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "ℹ Arquivo .CSV com codificação UTF-8 (compatível com acentuação e separadores brasileiros para abrir sem formatação manual no Excel).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                if (isGenerating) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Compilando dados da planilha...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Compartilhar
                FilledTonalButton(
                    onClick = {
                        if (isGenerating) return@FilledTonalButton
                        isGenerating = true
                        coroutineScope.launch {
                            val csvData = withContext(Dispatchers.IO) {
                                generateContent(viewModel, historyItems, selectedType, selectedFilter)
                            }
                            val dateTag = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
                            val filename = "${selectedType.filenamePrefix}_$dateTag.csv"
                            SpreadsheetExportUtil.shareCsv(context, filename, csvData)
                            isGenerating = false
                            onDismissRequest()
                        }
                    },
                    enabled = !isGenerating,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .materialYouPress(scaleFactor = 0.97f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Compartilhar",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Compartilhar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // Baixar no Aparelho
                Button(
                    onClick = {
                        if (isGenerating) return@Button
                        isGenerating = true
                        coroutineScope.launch {
                            val csvData = withContext(Dispatchers.IO) {
                                generateContent(viewModel, historyItems, selectedType, selectedFilter)
                            }
                            val dateTag = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
                            val filename = "${selectedType.filenamePrefix}_$dateTag.csv"
                            SpreadsheetExportUtil.downloadCsv(context, filename, csvData)
                            isGenerating = false
                            onDismissRequest()
                        }
                    },
                    enabled = !isGenerating,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .materialYouPress(scaleFactor = 0.97f)
                ) {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = "Baixar",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Baixar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {}
    )
}

@Composable
private fun TypeOptionItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
    } else {
        MaterialTheme.colorScheme.surfaceContainer
    }
    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
        }
    }
}

private suspend fun generateContent(
    viewModel: ProductionViewModel,
    historyItems: List<com.example.data.model.UnifiedHistoryItem>,
    type: SpreadsheetExportType,
    filter: SpreadsheetDateFilter
): String {
    return when (type) {
        SpreadsheetExportType.CONSOLIDATED -> {
            SpreadsheetExportUtil.generateConsolidatedCsv(historyItems, filter)
        }
        SpreadsheetExportType.PACKAGING -> {
            val list = viewModel.getAllPackagingControls()
            SpreadsheetExportUtil.generatePackagingCsv(list, filter)
        }
        SpreadsheetExportType.DOWNTIME -> {
            val list = viewModel.getAllMachineDowntimes()
            SpreadsheetExportUtil.generateDowntimeCsv(list, filter)
        }
        SpreadsheetExportType.DATING -> {
            val list = viewModel.getAllDatingChecks()
            SpreadsheetExportUtil.generateDatingCsv(list, filter)
        }
    }
}
