package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import com.example.ui.theme.materialYouTabTransitionSpec
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.isSystemInDarkTheme
import com.example.ui.theme.ThemeMode
import com.example.data.model.StopCode
import com.example.ui.AppScreen
import com.example.ui.DetailedDowntimeEntry
import com.example.ui.ProductionViewModel
import com.example.ui.components.PdfCard
import com.example.ui.components.PdfDateField
import com.example.ui.components.PdfTimeField
import com.example.ui.components.PdfSaveButton
import com.example.ui.components.PdfSectionHeader
import com.example.ui.components.PdfSegmentedButton
import com.example.ui.components.SegmentedColorVariant
import com.example.ui.components.PdfStepBottomBar
import com.example.ui.components.PdfTabRow
import com.example.ui.components.PdfTextField
import com.example.ui.components.PdfTopBar
import com.example.ui.theme.PdfAmber
import com.example.ui.theme.PdfBrandBadgeBg
import com.example.ui.theme.PdfBrandBadgeBorder
import com.example.ui.theme.PdfButtonSelectedBg
import com.example.ui.theme.PdfButtonUnselectedBg
import com.example.ui.theme.PdfButtonUnselectedBorder
import com.example.ui.theme.PdfCardBackground
import com.example.ui.theme.PdfCardBorder
import com.example.ui.theme.PdfCyanPrimary
import com.example.ui.theme.PdfDarkBackground
import com.example.ui.theme.PdfInputBackground
import com.example.ui.theme.PdfInputBorder
import com.example.ui.theme.PdfRed
import com.example.ui.theme.PdfTextMuted
import com.example.ui.theme.PdfTextPlaceholder
import com.example.ui.theme.PdfTextWhite

@Composable
fun MachineDowntimeScreen(viewModel: ProductionViewModel) {
    val selectedTab by viewModel.downtimeTab.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val systemInDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> systemInDark
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    val tabs = listOf(
        "Cabeçalho",
        "Registro",
        "Códigos"
    )

    Scaffold(
        topBar = {
            Column {
                PdfTopBar(
                    formCode = "FORM.002.FAR.CJ | REV. 01",
                    title = "Apontamento Parada de Máquinas",
                    onBackClick = { viewModel.navigateTo(AppScreen.Dashboard) },
                    onThemeToggle = { viewModel.toggleThemeMode() },
                    isDarkTheme = isDark
                )
                PdfTabRow(
                    tabs = tabs,
                    selectedTab = selectedTab,
                    onTabSelected = { viewModel.downtimeTab.value = it }
                )
            }
        },
        bottomBar = {
            PdfStepBottomBar(
                currentStep = selectedTab,
                totalSteps = tabs.size,
                onPrevious = {
                    if (selectedTab > 0) viewModel.downtimeTab.value = selectedTab - 1
                },
                onNext = {
                    if (selectedTab < tabs.size - 1) viewModel.downtimeTab.value = selectedTab + 1
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    materialYouTabTransitionSpec(targetState > initialState)
                },
                label = "DowntimeTabMotion"
            ) { currentTab ->
                Box(modifier = Modifier.fillMaxSize()) {
                    when (currentTab) {
                        0 -> HeaderTabContentDowntime(viewModel)
                        1 -> RegisterTabContentDowntime(viewModel)
                        2 -> CodesTabContentDowntime(viewModel)
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// TAB 1: CABEÇALHO (Page 8)
// ----------------------------------------------------
@Composable
private fun HeaderTabContentDowntime(viewModel: ProductionViewModel) {
    val date by viewModel.dtDate.collectAsState()
    val responsible by viewModel.dtResponsible.collectAsState()
    val shift by viewModel.dtShift.collectAsState()
    val shiftStart by viewModel.dtShiftStart.collectAsState()
    val shiftEnd by viewModel.dtShiftEnd.collectAsState()
    val line by viewModel.dtLine.collectAsState()
    val product by viewModel.dtProduct.collectAsState()
    val isTransgenic by viewModel.dtIsTransgenic.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. IDENTIFICAÇÃO
        PdfCard {
            PdfSectionHeader("IDENTIFICAÇÃO")
            PdfDateField(
                value = date,
                onValueChange = { viewModel.dtDate.value = it },
                label = "Data",
                placeholder = "DD/MM/AAAA"
            )
            Spacer(modifier = Modifier.height(10.dp))
            PdfTextField(
                value = responsible,
                onValueChange = { viewModel.dtResponsible.value = it },
                label = "Responsável",
                placeholder = "Nome do responsável"
            )
        }

        // 2. TURNO E HORÁRIOS
        PdfCard {
            PdfSectionHeader("TURNO E HORÁRIOS")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("1º Turno", "2º Turno", "3º Turno").forEach { item ->
                    PdfSegmentedButton(
                        text = item,
                        isSelected = shift == item,
                        onClick = { viewModel.dtShift.value = item },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PdfTimeField(
                    value = shiftStart,
                    onValueChange = { viewModel.dtShiftStart.value = it },
                    label = "Início do Turno",
                    placeholder = "06:00",
                    modifier = Modifier.weight(1f)
                )
                PdfTimeField(
                    value = shiftEnd,
                    onValueChange = { viewModel.dtShiftEnd.value = it },
                    label = "Fim do Turno",
                    placeholder = "14:20",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 3. Nº DA LINHA
        PdfCard {
            PdfSectionHeader("Nº DA LINHA")
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("M1", "M2", "M3").forEach { item ->
                        PdfSegmentedButton(
                            text = item,
                            isSelected = line == item,
                            onClick = { viewModel.dtLine.value = item },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("M4", "M5", "M6").forEach { item ->
                        PdfSegmentedButton(
                            text = item,
                            isSelected = line == item,
                            onClick = { viewModel.dtLine.value = item },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 4. PRODUTO
        PdfCard {
            PdfSectionHeader("PRODUTO")
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("São Braz", "Nordestino", "Novamilho", "Gostosin").forEach { item ->
                    PdfSegmentedButton(
                        text = item,
                        isSelected = product == item,
                        onClick = { viewModel.dtProduct.value = item },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Transgênico?",
                color = PdfTextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PdfSegmentedButton(
                    text = "Sim",
                    isSelected = isTransgenic,
                    onClick = { viewModel.dtIsTransgenic.value = true },
                    modifier = Modifier.weight(1f),
                    colorVariant = SegmentedColorVariant.POSITIVE
                )
                PdfSegmentedButton(
                    text = "Não Transgênico",
                    isSelected = !isTransgenic,
                    onClick = { viewModel.dtIsTransgenic.value = false },
                    modifier = Modifier.weight(1f),
                    colorVariant = SegmentedColorVariant.NEGATIVE
                )
            }
        }
    }
}

// ----------------------------------------------------
// TAB 2: REGISTRO (Page 9)
// ----------------------------------------------------
@Composable
private fun RegisterTabContentDowntime(viewModel: ProductionViewModel) {
    val entries by viewModel.dtDetailedEntries.collectAsState()
    val date by viewModel.dtDate.collectAsState()

    // Cálculo em tempo real do total de minutos de paradas
    val totalDowntimeMinutes = remember(entries) {
        var total = 0
        entries.forEach { item ->
            try {
                val pStart = item.startParada.trim()
                val pEnd = item.endParada.trim()
                if (pStart.contains(":") && pEnd.contains(":")) {
                    val sParts = pStart.split(":")
                    val eParts = pEnd.split(":")
                    val sMin = sParts[0].toInt() * 60 + sParts[1].toInt()
                    val eMin = eParts[0].toInt() * 60 + eParts[1].toInt()
                    val diff = if (eMin >= sMin) eMin - sMin else (1440 - sMin) + eMin
                    total += diff
                }
            } catch (_: Exception) {}
        }
        total
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Subheader Row com Total Calculado em Tempo Real
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Registros do turno (${entries.size}) — $date",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (totalDowntimeMinutes > 0) {
                    val hrs = totalDowntimeMinutes / 60
                    val mins = totalDowntimeMinutes % 60
                    val formatted = if (hrs > 0) "${hrs}h ${mins}min" else "${mins}min"
                    Text(
                        text = "Tempo total parado: $formatted ($totalDowntimeMinutes min)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            AssistChip(
                onClick = { viewModel.addDetailedDowntimeEntry() },
                label = { Text("Adicionar Parada", style = MaterialTheme.typography.labelMedium) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Adicionar",
                        modifier = Modifier.size(16.dp)
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    leadingIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                border = null,
                shape = RoundedCornerShape(12.dp)
            )
        }

        // Parada Entries Cards
        entries.forEachIndexed { index, entry ->
            DowntimeEntryCard(
                index = index,
                entry = entry,
                onUpdate = { updatedEntry ->
                    val list = entries.toMutableList()
                    list[index] = updatedEntry
                    viewModel.dtDetailedEntries.value = list
                },
                onSelectCodeClick = {
                    viewModel.activeStopEntryIndexForCode = index
                    viewModel.downtimeTab.value = 2 // Jump to Códigos tab
                },
                onRemove = {
                    viewModel.removeDetailedDowntimeEntry(index)
                }
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        PdfSaveButton(
            text = "✓ Salvar Apontamento de Paradas",
            onClick = { viewModel.saveMachineDowntime() }
        )
    }
}

@Composable
private fun DowntimeEntryCard(
    index: Int,
    entry: DetailedDowntimeEntry,
    onUpdate: (DetailedDowntimeEntry) -> Unit,
    onSelectCodeClick: () -> Unit,
    onRemove: () -> Unit
) {
    PdfCard {
        // Calcula duração individual em minutos
        val itemDuration = remember(entry.startParada, entry.endParada) {
            try {
                val pStart = entry.startParada.trim()
                val pEnd = entry.endParada.trim()
                if (pStart.contains(":") && pEnd.contains(":")) {
                    val sParts = pStart.split(":")
                    val eParts = pEnd.split(":")
                    val sMin = sParts[0].toInt() * 60 + sParts[1].toInt()
                    val eMin = eParts[0].toInt() * 60 + eParts[1].toInt()
                    val diff = if (eMin >= sMin) eMin - sMin else (1440 - sMin) + eMin
                    diff
                } else null
            } catch (_: Exception) { null }
        }

        // Card Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer
                ) {
                    Text(
                        text = "Parada #${index + 1}",
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                if (itemDuration != null && itemDuration > 0) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                    ) {
                        Text(
                            text = "$itemDuration min",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onRemove() }
            ) {
                Text(
                    text = "Remover",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Row 1: Início da parada / Fim da parada
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PdfTimeField(
                value = entry.startParada,
                onValueChange = { onUpdate(entry.copy(startParada = it)) },
                label = "Início da parada",
                placeholder = "--:--",
                modifier = Modifier.weight(1f)
            )
            PdfTimeField(
                value = entry.endParada,
                onValueChange = { onUpdate(entry.copy(endParada = it)) },
                label = "Fim da parada / turno",
                placeholder = "--:--",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Row 2: Início manutenção / Fim manutenção
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PdfTimeField(
                value = entry.startManutencao,
                onValueChange = { onUpdate(entry.copy(startManutencao = it)) },
                label = "Início manutenção",
                placeholder = "--:--",
                modifier = Modifier.weight(1f)
            )
            PdfTimeField(
                value = entry.endManutencao,
                onValueChange = { onUpdate(entry.copy(endManutencao = it)) },
                label = "Fim manutenção",
                placeholder = "--:--",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Código da parada
        Text(
            text = "Código da parada",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
            border = BorderStroke(
                1.dp,
                if (entry.stopCode.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable { onSelectCodeClick() }
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (entry.stopCode.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = entry.stopCode,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Text(
                        text = entry.stopDescription,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                } else {
                    Text(
                        text = "Toque para selecionar código...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Detalhamento da parada
        PdfTextField(
            value = entry.comments,
            onValueChange = { onUpdate(entry.copy(comments = it)) },
            label = "Detalhamento da parada",
            placeholder = "Descreva o ocorrido..."
        )
    }
}

// ----------------------------------------------------
// TAB 3: CÓDIGOS (Pages 10 to 13)
// ----------------------------------------------------
@Composable
private fun CodesTabContentDowntime(viewModel: ProductionViewModel) {
    val searchQuery by viewModel.stopCodeSearchQuery.collectAsState()
    val filteredCodes by viewModel.filteredStopCodes.collectAsState()

    val groupedCodes = filteredCodes.groupBy { it.category }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {
        // Search Bar
        PdfTextField(
            value = searchQuery,
            onValueChange = { viewModel.stopCodeSearchQuery.value = it },
            placeholder = "Buscar código ou descrição...",
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Buscar",
                    tint = PdfTextMuted,
                    modifier = Modifier.size(18.dp)
                )
            },
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            groupedCodes.forEach { (category, codes) ->
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = category,
                            color = PdfCyanPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        codes.forEach { codeItem ->
                            StopCodeListItem(
                                code = codeItem,
                                onClick = { viewModel.selectStopCode(codeItem) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StopCodeListItem(
    code: StopCode,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = PdfCardBackground,
        border = BorderStroke(1.dp, PdfCardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Code badge
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = Color(0xFF1E3A8A),
                modifier = Modifier.size(width = 34.dp, height = 24.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = code.code,
                        color = Color(0xFF60A5FA),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = code.description,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
