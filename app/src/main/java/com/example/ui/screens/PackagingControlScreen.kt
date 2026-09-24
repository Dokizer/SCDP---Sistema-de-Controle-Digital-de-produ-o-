package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import com.example.ui.theme.materialYouTabTransitionSpec
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.isSystemInDarkTheme
import com.example.ui.theme.ThemeMode
import com.example.data.model.PackagingControlItem
import com.example.ui.AppScreen
import com.example.ui.DetailedProductionRow
import com.example.ui.ProductionViewModel
import com.example.ui.components.PdfDateField
import com.example.ui.components.PdfTimeField
import com.example.ui.components.PdfCard
import com.example.ui.components.PdfConformityPair
import com.example.ui.components.PdfSaveButton
import com.example.ui.components.PdfSectionHeader
import com.example.ui.components.PdfSegmentedButton
import com.example.ui.components.SegmentedColorVariant
import com.example.ui.components.PdfStepBottomBar
import com.example.ui.components.PdfTabRow
import com.example.ui.components.PdfTextField
import com.example.ui.components.PdfTopBar
import com.example.ui.theme.PdfAmber
import com.example.ui.theme.PdfButtonSelectedBg
import com.example.ui.theme.PdfButtonUnselectedBg
import com.example.ui.theme.PdfButtonUnselectedBorder
import com.example.ui.theme.PdfCardBackground
import com.example.ui.theme.PdfCardBorder
import com.example.ui.theme.PdfCyanPrimary
import com.example.ui.theme.PdfDarkBackground
import com.example.ui.theme.PdfGreen
import com.example.ui.theme.PdfInputBackground
import com.example.ui.theme.PdfInputBorder
import com.example.ui.theme.PdfRed
import com.example.ui.theme.PdfTextMuted
import com.example.ui.theme.PdfTextPlaceholder
import com.example.ui.theme.PdfTextWhite

@Composable
fun PackagingControlScreen(viewModel: ProductionViewModel) {
    val selectedTab by viewModel.packagingTab.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val systemInDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> systemInDark
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    val tabs = listOf(
        "Cabeçalho",
        "Produção",
        "Máquina",
        "Pesos",
        "Embalagens",
        "Notas"
    )

    Scaffold(
        topBar = {
            Column {
                PdfTopBar(
                    formCode = "FORM.003.FAR.CJ | REV. 02",
                    title = "Controle Diário de Empacotamento",
                    onBackClick = { viewModel.navigateTo(AppScreen.Dashboard) },
                    onThemeToggle = { viewModel.toggleThemeMode() },
                    isDarkTheme = isDark
                )
                PdfTabRow(
                    tabs = tabs,
                    selectedTab = selectedTab,
                    onTabSelected = { viewModel.packagingTab.value = it }
                )
            }
        },
        bottomBar = {
            PdfStepBottomBar(
                currentStep = selectedTab,
                totalSteps = tabs.size,
                onPrevious = {
                    if (selectedTab > 0) viewModel.packagingTab.value = selectedTab - 1
                },
                onNext = {
                    if (selectedTab < tabs.size - 1) viewModel.packagingTab.value = selectedTab + 1
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    materialYouTabTransitionSpec(targetState > initialState)
                },
                label = "PackagingTabMotion"
            ) { currentTab ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    when (currentTab) {
                        0 -> HeaderTabContent(viewModel)
                        1 -> ProductionTabContent(viewModel)
                        2 -> MachineTabContent(viewModel)
                        3 -> WeightsTabContent(viewModel)
                        4 -> PackagingTabContent(viewModel)
                        5 -> NotesTabContent(viewModel)
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// TAB 1: CABEÇALHO (Page 2)
// ----------------------------------------------------
@Composable
private fun HeaderTabContent(viewModel: ProductionViewModel) {
    val date by viewModel.pkgDate.collectAsState()
    val product by viewModel.pkgProduct.collectAsState()
    val productChangedTo by viewModel.pkgProductChangedTo.collectAsState()
    val productChangeTime by viewModel.pkgProductChangeTime.collectAsState()
    val responsible by viewModel.pkgResponsible.collectAsState()
    val shift by viewModel.pkgShift.collectAsState()
    val line by viewModel.pkgLine.collectAsState()

    val empInterna by viewModel.pkgEmpacotadoraInternaC.collectAsState()
    val empExterna by viewModel.pkgEmpacotadoraExternaC.collectAsState()
    val enfInterna by viewModel.pkgEnfardadeiraInternaC.collectAsState()
    val enfExterna by viewModel.pkgEnfardadeiraExternaC.collectAsState()

    // 1. IDENTIFICAÇÃO
    PdfCard {
        PdfSectionHeader("IDENTIFICAÇÃO")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PdfDateField(
                value = date,
                onValueChange = { viewModel.pkgDate.value = it },
                label = "Data",
                placeholder = "DD/MM/AAAA",
                modifier = Modifier.weight(1f)
            )
            PdfTextField(
                value = product,
                onValueChange = { viewModel.pkgProduct.value = it },
                label = "Produto",
                placeholder = "Ex: Farinha Tipo 1",
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PdfTextField(
                value = productChangedTo,
                onValueChange = { viewModel.pkgProductChangedTo.value = it },
                label = "Mudou para (Produto)",
                placeholder = "Se trocou durante turno",
                modifier = Modifier.weight(1.3f)
            )
            PdfTimeField(
                value = productChangeTime,
                onValueChange = { viewModel.pkgProductChangeTime.value = it },
                label = "Horário",
                placeholder = "--:--",
                modifier = Modifier.weight(0.8f)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        PdfTextField(
            value = responsible,
            onValueChange = { viewModel.pkgResponsible.value = it },
            label = "Responsável",
            placeholder = "Nome do responsável"
        )
    }

    // 2. TURNO
    PdfCard {
        PdfSectionHeader("TURNO")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("1º Turno", "2º Turno", "3º Turno").forEach { item ->
                PdfSegmentedButton(
                    text = item,
                    isSelected = shift == item,
                    onClick = { viewModel.pkgShift.value = item },
                    modifier = Modifier.weight(1f)
                )
            }
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
                        onClick = { viewModel.pkgLine.value = item },
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
                        onClick = { viewModel.pkgLine.value = item },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    // 4. LIMPEZA — FREQUÊNCIA DIÁRIA
    PdfCard {
        PdfSectionHeader("LIMPEZA — FREQUÊNCIA DIÁRIA")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // EMPACOTADORA Column
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "EMPACOTADORA",
                    color = PdfTextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(text = "Interna", color = PdfTextPlaceholder, fontSize = 10.sp)
                PdfConformityPair(
                    isConform = empInterna,
                    onSelectConform = { viewModel.pkgEmpacotadoraInternaC.value = it }
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = "Externa", color = PdfTextPlaceholder, fontSize = 10.sp)
                PdfConformityPair(
                    isConform = empExterna,
                    onSelectConform = { viewModel.pkgEmpacotadoraExternaC.value = it }
                )
            }

            // ENFARDADEIRA Column
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "ENFARDADEIRA",
                    color = PdfTextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(text = "Interna", color = PdfTextPlaceholder, fontSize = 10.sp)
                PdfConformityPair(
                    isConform = enfInterna,
                    onSelectConform = { viewModel.pkgEnfardadeiraInternaC.value = it }
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = "Externa", color = PdfTextPlaceholder, fontSize = 10.sp)
                PdfConformityPair(
                    isConform = enfExterna,
                    onSelectConform = { viewModel.pkgEnfardadeiraExternaC.value = it }
                )
            }
        }
    }
}

// ----------------------------------------------------
// TAB 2: PRODUÇÃO (Page 3)
// ----------------------------------------------------
@Composable
private fun ProductionTabContent(viewModel: ProductionViewModel) {
    val initialSpeed by viewModel.pkgInitialSpeed.collectAsState()
    val changeTime by viewModel.pkgChangeTime.collectAsState()
    val speedChangeReason by viewModel.pkgSpeedChangeReason.collectAsState()
    val speedChanged by viewModel.pkgSpeedChanged.collectAsState()
    val rows by viewModel.pkgDetailedProductionRows.collectAsState()

    // 2. VELOCIDADE DA MÁQUINA
    PdfCard {
        PdfSectionHeader("VELOCIDADE DA MÁQUINA")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PdfTextField(
                value = initialSpeed,
                onValueChange = { viewModel.pkgInitialSpeed.value = it },
                label = "No início do turno (FD/min)",
                placeholder = "0",
                modifier = Modifier.weight(1f)
            )
            PdfTimeField(
                value = changeTime,
                onValueChange = { viewModel.pkgChangeTime.value = it },
                label = "Horário da mudança",
                placeholder = "--:--",
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        PdfTextField(
            value = speedChangeReason,
            onValueChange = { viewModel.pkgSpeedChangeReason.value = it },
            label = "Motivo da mudança de velocidade",
            placeholder = "Descreva o motivo"
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Mudou a velocidade?",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PdfSegmentedButton(
                text = "✓ Sim",
                isSelected = speedChanged,
                onClick = { viewModel.pkgSpeedChanged.value = true },
                modifier = Modifier.weight(1f),
                colorVariant = SegmentedColorVariant.POSITIVE
            )
            PdfSegmentedButton(
                text = "✕ Não",
                isSelected = !speedChanged,
                onClick = { viewModel.pkgSpeedChanged.value = false },
                modifier = Modifier.weight(1f),
                colorVariant = SegmentedColorVariant.NEGATIVE
            )
        }
    }

    // PRODUÇÃO (FD/MIN) — DENSIDADE E VELOCIDADE
    PdfCard {
        PdfSectionHeader("PRODUÇÃO (FD/MIN) — DENSIDADE E VELOCIDADE")

        // Table Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                .padding(vertical = 8.dp, horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "HORÁRIO",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1.3f)
            )
            Text(
                text = "META",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(0.8f)
            )
            Text(
                text = "REAL",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(0.9f)
            )
            Text(
                text = "N-TRANSG\n400-440 g/l",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                lineHeight = 11.sp,
                modifier = Modifier.weight(1.1f)
            )
            Text(
                text = "OUTROS\n410-440 g/l",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                lineHeight = 11.sp,
                modifier = Modifier.weight(1.1f)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Table Rows
        rows.forEachIndexed { index, rowItem ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = rowItem.hourLabel,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 10.sp,
                    lineHeight = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1.3f)
                )
                Text(
                    text = rowItem.meta,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(0.8f)
                )

                // Real
                MiniInputCell(
                    value = rowItem.real,
                    onValueChange = { newVal ->
                        val updated = rows.toMutableList()
                        updated[index] = updated[index].copy(real = newVal)
                        viewModel.pkgDetailedProductionRows.value = updated
                    },
                    modifier = Modifier.weight(0.9f)
                )

                // N-Transg
                MiniInputCell(
                    value = rowItem.nTransg,
                    onValueChange = { newVal ->
                        val updated = rows.toMutableList()
                        updated[index] = updated[index].copy(nTransg = newVal)
                        viewModel.pkgDetailedProductionRows.value = updated
                    },
                    modifier = Modifier.weight(1.1f)
                )

                // Outros
                MiniInputCell(
                    value = rowItem.outros,
                    onValueChange = { newVal ->
                        val updated = rows.toMutableList()
                        updated[index] = updated[index].copy(outros = newVal)
                        viewModel.pkgDetailedProductionRows.value = updated
                    },
                    modifier = Modifier.weight(1.1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Summary footer
        val totalRealized = rows.sumOf { it.real.toIntOrNull() ?: 0 }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Meta/turno: 900 fds",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(10.dp)
                )
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Realizado: $totalRealized fds",
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }
    }
}

@Composable
private fun MiniInputCell(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "-",
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f),
        border = BorderStroke(
            1.dp,
            if (value.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        ),
        modifier = modifier.height(34.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ----------------------------------------------------
// TAB 3: MÁQUINA (Page 4)
// ----------------------------------------------------
@Composable
private fun MachineTabContent(viewModel: ProductionViewModel) {
    val clean by viewModel.pkgMachineClean.collectAsState()
    val broken by viewModel.pkgMachineBroken.collectAsState()
    val adjusted by viewModel.pkgMachineAdjusted.collectAsState()
    val preventive by viewModel.pkgMachinePreventive.collectAsState()
    val noBobbin by viewModel.pkgMachineNoBobbin.collectAsState()

    val sectorClean by viewModel.pkgSectorClean.collectAsState()
    val trashBoxesClean by viewModel.pkgTrashBoxesClean.collectAsState()

    val abastecido by viewModel.pkgAbastecido.collectAsState()
    val sameProduct by viewModel.pkgPackagingSameProduct.collectAsState()

    // 1. COMO RECEBEU A MÁQUINA?
    PdfCard {
        PdfSectionHeader("COMO RECEBEU A MÁQUINA?")
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PdfSegmentedButton(
                    text = "Limpa",
                    isSelected = clean,
                    onClick = { viewModel.pkgMachineClean.value = true },
                    modifier = Modifier.weight(1f),
                    colorVariant = SegmentedColorVariant.POSITIVE
                )
                PdfSegmentedButton(
                    text = "Suja",
                    isSelected = !clean,
                    onClick = { viewModel.pkgMachineClean.value = false },
                    modifier = Modifier.weight(1f),
                    colorVariant = SegmentedColorVariant.NEGATIVE
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PdfSegmentedButton(
                    text = "Quebrada",
                    isSelected = broken,
                    onClick = { viewModel.pkgMachineBroken.value = true },
                    modifier = Modifier.weight(1f),
                    colorVariant = SegmentedColorVariant.NEGATIVE
                )
                PdfSegmentedButton(
                    text = "Disponível",
                    isSelected = !broken,
                    onClick = { viewModel.pkgMachineBroken.value = false },
                    modifier = Modifier.weight(1f),
                    colorVariant = SegmentedColorVariant.POSITIVE
                )
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                PdfSegmentedButton(
                    text = "Ajustada",
                    isSelected = adjusted,
                    onClick = { viewModel.pkgMachineAdjusted.value = !adjusted },
                    modifier = Modifier.weight(0.5f),
                    colorVariant = SegmentedColorVariant.POSITIVE
                )
                Spacer(modifier = Modifier.weight(0.5f))
            }
            PdfSegmentedButton(
                text = "Máquina na Preventiva",
                isSelected = preventive,
                onClick = { viewModel.pkgMachinePreventive.value = !preventive },
                modifier = Modifier.fillMaxWidth(),
                colorVariant = SegmentedColorVariant.WARNING
            )
            PdfSegmentedButton(
                text = "Máquina sem Bobinas",
                isSelected = noBobbin,
                onClick = { viewModel.pkgMachineNoBobbin.value = !noBobbin },
                modifier = Modifier.fillMaxWidth(),
                colorVariant = SegmentedColorVariant.NEGATIVE
            )
        }
    }

    // 2. COMO RECEBEU O SETOR?
    PdfCard {
        PdfSectionHeader("COMO RECEBEU O SETOR?")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PdfSegmentedButton(
                text = "Limpo",
                isSelected = sectorClean,
                onClick = { viewModel.pkgSectorClean.value = true },
                modifier = Modifier.weight(1f),
                colorVariant = SegmentedColorVariant.POSITIVE
            )
            PdfSegmentedButton(
                text = "Sujo",
                isSelected = !sectorClean,
                onClick = { viewModel.pkgSectorClean.value = false },
                modifier = Modifier.weight(1f),
                colorVariant = SegmentedColorVariant.NEGATIVE
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Lixeiras e Caixas Brancas:",
            color = PdfTextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            PdfSegmentedButton(
                text = "Com produtos, sujas ou sem plástico",
                isSelected = !trashBoxesClean,
                onClick = { viewModel.pkgTrashBoxesClean.value = false },
                modifier = Modifier.fillMaxWidth(),
                colorVariant = SegmentedColorVariant.NEGATIVE
            )
            PdfSegmentedButton(
                text = "Várias, limpas e com plástico",
                isSelected = trashBoxesClean,
                onClick = { viewModel.pkgTrashBoxesClean.value = true },
                modifier = Modifier.fillMaxWidth(),
                colorVariant = SegmentedColorVariant.POSITIVE
            )
        }
    }

    // 3. ABASTECIMENTO
    PdfCard {
        PdfSectionHeader("ABASTECIMENTO")
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            PdfSegmentedButton(
                text = "✓ Abastecido (com bobinas e filmes)",
                isSelected = abastecido,
                onClick = { viewModel.pkgAbastecido.value = true },
                modifier = Modifier.fillMaxWidth(),
                colorVariant = SegmentedColorVariant.POSITIVE
            )
            PdfSegmentedButton(
                text = "✕ Desabastecido (sem bobinas e filmes)",
                isSelected = !abastecido,
                onClick = { viewModel.pkgAbastecido.value = false },
                modifier = Modifier.fillMaxWidth(),
                colorVariant = SegmentedColorVariant.NEGATIVE
            )
        }
    }

    // 4. EMBALAGEM E ENFARDADEIRA SÃO DO MESMO PRODUTO?
    PdfCard {
        PdfSectionHeader("EMBALAGEM E ENFARDADEIRA SÃO DO MESMO PRODUTO?")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PdfSegmentedButton(
                text = "✓ Conforme",
                isSelected = sameProduct,
                onClick = { viewModel.pkgPackagingSameProduct.value = true },
                modifier = Modifier.weight(1f),
                colorVariant = SegmentedColorVariant.POSITIVE
            )
            PdfSegmentedButton(
                text = "✕ Não Conforme",
                isSelected = !sameProduct,
                onClick = { viewModel.pkgPackagingSameProduct.value = false },
                modifier = Modifier.weight(1f),
                colorVariant = SegmentedColorVariant.NEGATIVE
            )
        }
    }
}

// ----------------------------------------------------
// TAB 4: PESOS (Page 5)
// ----------------------------------------------------
@Composable
private fun WeightsTabContent(viewModel: ProductionViewModel) {
    val gramatura by viewModel.pkgSelectedGramatura.collectAsState()
    val grid400 by viewModel.pkgWeightsGrid400.collectAsState()
    val grid500 by viewModel.pkgWeightsGrid500.collectAsState()
    val shiftAvg by viewModel.pkgShiftAverageWeight.collectAsState()
    val tare by viewModel.pkgPackageTareWeight.collectAsState()

    val hourHeaders = listOf(
        "1ª\n06:20",
        "2ª\n07:20",
        "3ª\n08:20",
        "4ª\n09:20",
        "5ª\n10:20",
        "6ª\n11:20",
        "7ª\n12:20",
        "8ª\n13:40"
    )

    PdfCard {
        PdfSectionHeader("PESO LÍQUIDO (G) — VERIFICAR A CADA 1 HORA")
        Text(
            text = "Peso Líquido = Peso do produto - Peso da embalagem",
            color = PdfTextPlaceholder,
            fontSize = 10.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Gramatura Selector Tabs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PdfSegmentedButton(
                text = "400g (Padrão 398 a 402g)",
                isSelected = gramatura == "400g",
                onClick = { viewModel.pkgSelectedGramatura.value = "400g" },
                modifier = Modifier.weight(1f)
            )
            PdfSegmentedButton(
                text = "500g (Padrão 497,5 a 502,5g)",
                isSelected = gramatura == "500g",
                onClick = { viewModel.pkgSelectedGramatura.value = "500g" },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Toque na célula para marcar o peso da Faixa (g):",
            color = PdfTextMuted,
            fontSize = 10.sp,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        val activeGrid = if (gramatura == "400g") grid400 else grid500
        val isTarget400 = gramatura == "400g"

        // Horizontal Scrollable Table like PDF
        val scrollState = rememberScrollState()
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)), RoundedCornerShape(8.dp))
        ) {
            Column(
                modifier = Modifier
                    .horizontalScroll(scrollState)
                    .padding(6.dp)
            ) {
                // Table Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(4.dp))
                        .padding(vertical = 6.dp, horizontal = 4.dp)
                ) {
                    Text(
                        text = "FAIXA (g)",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(62.dp)
                    )

                    hourHeaders.forEach { headerText ->
                        Text(
                            text = headerText,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 8.sp,
                            lineHeight = 10.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.width(42.dp)
                        )
                    }

                    Text(
                        text = "TOTAL",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(44.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Table Rows
                activeGrid.forEach { (faixa, hourlyValues) ->
                    val isNominal = if (isTarget400) faixa == "400" else faixa == "500"
                    val isStandard = if (isTarget400) {
                        faixa in listOf("398", "399", "400", "401", "402")
                    } else {
                        faixa in listOf("498", "499", "500", "501", "502", "503", "504", "505")
                    }

                    val rowTotal = hourlyValues.count { it.isNotBlank() }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(
                                if (isNominal) PdfCyanPrimary.copy(alpha = 0.08f)
                                else if (isStandard) Color.Transparent
                                else PdfAmber.copy(alpha = 0.04f)
                            )
                            .padding(vertical = 2.dp, horizontal = 2.dp)
                    ) {
                        // Faixa Label
                        Box(
                            modifier = Modifier.width(62.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(
                                            if (isNominal) PdfCyanPrimary
                                            else if (isStandard) PdfGreen
                                            else PdfAmber
                                        )
                                )
                                Text(
                                    text = if (faixa.startsWith("<") || faixa.startsWith(">")) faixa else "$faixa g",
                                    color = if (isNominal) MaterialTheme.colorScheme.primary else if (isStandard) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.tertiary,
                                    fontSize = 10.sp,
                                    fontWeight = if (isNominal || isStandard) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }

                        // 8 Hourly Cells
                        hourlyValues.forEachIndexed { colIdx, cellVal ->
                            val isFilled = cellVal.isNotBlank()
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (isFilled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f),
                                border = BorderStroke(
                                    1.dp,
                                    if (isFilled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier
                                    .padding(horizontal = 2.dp)
                                    .size(width = 38.dp, height = 30.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .clickable {
                                        viewModel.toggleWeightGridCell(gramatura, faixa, colIdx)
                                    }
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    if (isFilled) {
                                        Text(
                                            text = cellVal,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }

                        // Total column
                        Box(
                            modifier = Modifier
                                .width(44.dp)
                                .padding(horizontal = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (rowTotal > 0) "$rowTotal" else "-",
                                color = if (rowTotal > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PdfTextField(
                value = shiftAvg,
                onValueChange = { viewModel.pkgShiftAverageWeight.value = it },
                label = "Média do turno (g)",
                placeholder = "Ex: 400.2 g",
                modifier = Modifier.weight(1f)
            )
            PdfTextField(
                value = tare,
                onValueChange = { viewModel.pkgPackageTareWeight.value = it },
                label = "Peso embalagem (g)",
                placeholder = "Ex: 4.5 g",
                modifier = Modifier.weight(1f)
            )
        }
    }

    // Warning Banner Card
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "⚠️ Padrão de Peso Líquido",
                    color = MaterialTheme.colorScheme.tertiary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Pacote 400g: 398 a 402 g\nPacote 500g: 497,5 a 502,5 g\nSe fora do padrão, comunicar a supervisão e proceder os ajustes necessários.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }
    }
}

// ----------------------------------------------------
// TAB 5: EMBALAGENS (Page 6) — EXACTO CONFORME DESIGN
// ----------------------------------------------------
@Composable
private fun TableHeaderCell(
    text: String,
    width: androidx.compose.ui.unit.Dp,
    textAlign: TextAlign = TextAlign.Start
) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        textAlign = textAlign,
        lineHeight = 13.sp,
        modifier = Modifier
            .width(width)
            .padding(horizontal = 4.dp, vertical = 4.dp)
    )
}

@Composable
private fun TableInputCell(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    isNumeric: Boolean = false,
    width: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.45f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        modifier = modifier
            .width(width)
            .height(36.dp)
            .padding(horizontal = 2.dp, vertical = 2.dp)
    ) {
        Box(
            contentAlignment = Alignment.CenterStart,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp)
        ) {
            if (value.isEmpty() && placeholder.isNotEmpty()) {
                Text(
                    text = placeholder,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Start
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Start
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(
                    keyboardType = if (isNumeric) KeyboardType.Number else KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun CNCToggleCell(
    isC: Boolean,
    onToggle: (Boolean) -> Unit,
    width: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .width(width)
            .height(36.dp)
            .padding(horizontal = 2.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // C Button (Verde)
        Surface(
            shape = CircleShape,
            color = if (isC) Color(0xFF1B5E20).copy(alpha = 0.35f) else MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.3f),
            border = BorderStroke(1.dp, if (isC) Color(0xFF4CAF50) else MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .clickable { onToggle(true) }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "C",
                    color = if (isC) Color(0xFF81C784) else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.width(4.dp))
        // NC Button (Vermelho)
        Surface(
            shape = CircleShape,
            color = if (!isC) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.3f),
            border = BorderStroke(1.dp, if (!isC) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .clickable { onToggle(false) }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "NC",
                    color = if (!isC) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun PackagingTabContent(viewModel: ProductionViewModel) {
    val empInitialWeight by viewModel.pkgEmpacotadoraInitialWeight.collectAsState()
    val empRows by viewModel.pkgEmpacotadoraRows.collectAsState()
    val empDevolucaoPeso by viewModel.pkgEmpDevolucaoPeso.collectAsState()
    val empDevolucaoDefeito by viewModel.pkgEmpDevolucaoDefeito.collectAsState()

    val enfInitialWeight by viewModel.pkgEnfardadeiraInitialWeight.collectAsState()
    val enfRows by viewModel.pkgEnfardadeiraRows.collectAsState()
    val enfDevolucaoPeso by viewModel.pkgEnfDevolucaoPeso.collectAsState()
    val enfDevolucaoDefeito by viewModel.pkgEnfDevolucaoDefeito.collectAsState()

    // 1. EMPACOTADEIRA
    PdfCard {
        PdfSectionHeader("CONTROLE DE EMBALAGENS — EMPACOTADEIRA")

        PdfTextField(
            value = empInitialWeight,
            onValueChange = { viewModel.pkgEmpacotadoraInitialWeight.value = it },
            label = "Peso inicial (que estava na máquina)",
            placeholder = "kg"
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Table Empacotadeira with Horizontal Scroll
        val scrollEmp = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollEmp)
        ) {
            // Header Row
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier
                    .padding(bottom = 6.dp)
            ) {
                TableHeaderCell("QTD", 44.dp)
                TableHeaderCell("FORNECEDOR", 130.dp)
                TableHeaderCell("LOTE", 100.dp)
                TableHeaderCell("BIGODE\nESQ.", 66.dp, TextAlign.Center)
                TableHeaderCell("BIGODE\nDIR.", 66.dp, TextAlign.Center)
                TableHeaderCell("DESPERD.\nFILME", 84.dp)
                TableHeaderCell("PESO\nDEF.QUAL.", 84.dp)
                TableHeaderCell("QUAL\nDEFEITO?", 130.dp)
            }

            // 8 Data Rows
            empRows.forEachIndexed { idx, item ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    // QTD
                    TableInputCell(
                        value = item.quantity,
                        onValueChange = { newVal ->
                            val list = empRows.toMutableList()
                            list[idx] = list[idx].copy(quantity = newVal)
                            viewModel.pkgEmpacotadoraRows.value = list
                        },
                        isNumeric = true,
                        width = 44.dp
                    )

                    // FORNECEDOR
                    TableInputCell(
                        value = item.supplier,
                        onValueChange = { newVal ->
                            val list = empRows.toMutableList()
                            list[idx] = list[idx].copy(supplier = newVal)
                            viewModel.pkgEmpacotadoraRows.value = list
                        },
                        isNumeric = false,
                        width = 130.dp
                    )

                    // LOTE
                    TableInputCell(
                        value = item.lot,
                        onValueChange = { newVal ->
                            val list = empRows.toMutableList()
                            list[idx] = list[idx].copy(lot = newVal)
                            viewModel.pkgEmpacotadoraRows.value = list
                        },
                        isNumeric = false,
                        width = 100.dp
                    )

                    // BIGODE ESQ (C / NC)
                    CNCToggleCell(
                        isC = item.stiffnessLeftC,
                        onToggle = { isC ->
                            val list = empRows.toMutableList()
                            list[idx] = list[idx].copy(stiffnessLeftC = isC)
                            viewModel.pkgEmpacotadoraRows.value = list
                        },
                        width = 66.dp
                    )

                    // BIGODE DIR (C / NC)
                    CNCToggleCell(
                        isC = item.stiffnessRightC,
                        onToggle = { isC ->
                            val list = empRows.toMutableList()
                            list[idx] = list[idx].copy(stiffnessRightC = isC)
                            viewModel.pkgEmpacotadoraRows.value = list
                        },
                        width = 66.dp
                    )

                    // DESPERD. FILME
                    TableInputCell(
                        value = item.filmWasteKg,
                        onValueChange = { newVal ->
                            val list = empRows.toMutableList()
                            list[idx] = list[idx].copy(filmWasteKg = newVal)
                            viewModel.pkgEmpacotadoraRows.value = list
                        },
                        isNumeric = true,
                        width = 84.dp
                    )

                    // PESO DEF.QUAL.
                    TableInputCell(
                        value = item.qualityDefectWeight,
                        onValueChange = { newVal ->
                            val list = empRows.toMutableList()
                            list[idx] = list[idx].copy(qualityDefectWeight = newVal)
                            viewModel.pkgEmpacotadoraRows.value = list
                        },
                        isNumeric = true,
                        width = 84.dp
                    )

                    // QUAL DEFEITO?
                    TableInputCell(
                        value = item.defectDescription,
                        onValueChange = { newVal ->
                            val list = empRows.toMutableList()
                            list[idx] = list[idx].copy(defectDescription = newVal)
                            viewModel.pkgEmpacotadoraRows.value = list
                        },
                        isNumeric = false,
                        width = 130.dp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PdfTextField(
                value = empDevolucaoPeso,
                onValueChange = { viewModel.pkgEmpDevolucaoPeso.value = it },
                label = "Devolução ao estoque: Peso bobina bloqueada (kg)",
                placeholder = "0.0",
                modifier = Modifier.weight(1.1f)
            )
            PdfTextField(
                value = empDevolucaoDefeito,
                onValueChange = { viewModel.pkgEmpDevolucaoDefeito.value = it },
                label = "Qual o defeito?",
                placeholder = "Motivo do bloqueio",
                modifier = Modifier.weight(0.9f)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Peso Líquido = Peso Bruto - Peso do Tubete",
            color = PdfTextPlaceholder,
            fontSize = 11.sp
        )
    }

    // 2. ENFARDADEIRA
    PdfCard {
        PdfSectionHeader("CONTROLE DE EMBALAGENS — ENFARDADEIRA")

        PdfTextField(
            value = enfInitialWeight,
            onValueChange = { viewModel.pkgEnfardadeiraInitialWeight.value = it },
            label = "Peso inicial (que estava na máquina)",
            placeholder = "kg"
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Table Enfardadeira with Horizontal Scroll
        val scrollEnf = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollEnf)
        ) {
            // Header Row
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier
                    .padding(bottom = 6.dp)
            ) {
                TableHeaderCell("QTD", 44.dp)
                TableHeaderCell("FORNECEDOR", 140.dp)
                TableHeaderCell("LOTE", 110.dp)
                TableHeaderCell("DESPERD.\nFILME", 90.dp)
                TableHeaderCell("PESO\nDEF.QUAL.", 90.dp)
                TableHeaderCell("QUAL\nDEFEITO?", 140.dp)
            }

            // 8 Data Rows
            enfRows.forEachIndexed { idx, item ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    // QTD
                    TableInputCell(
                        value = item.quantity,
                        onValueChange = { newVal ->
                            val list = enfRows.toMutableList()
                            list[idx] = list[idx].copy(quantity = newVal)
                            viewModel.pkgEnfardadeiraRows.value = list
                        },
                        isNumeric = true,
                        width = 44.dp
                    )

                    // FORNECEDOR
                    TableInputCell(
                        value = item.supplier,
                        onValueChange = { newVal ->
                            val list = enfRows.toMutableList()
                            list[idx] = list[idx].copy(supplier = newVal)
                            viewModel.pkgEnfardadeiraRows.value = list
                        },
                        isNumeric = false,
                        width = 140.dp
                    )

                    // LOTE
                    TableInputCell(
                        value = item.lot,
                        onValueChange = { newVal ->
                            val list = enfRows.toMutableList()
                            list[idx] = list[idx].copy(lot = newVal)
                            viewModel.pkgEnfardadeiraRows.value = list
                        },
                        isNumeric = false,
                        width = 110.dp
                    )

                    // DESPERD. FILME
                    TableInputCell(
                        value = item.filmWasteKg,
                        onValueChange = { newVal ->
                            val list = enfRows.toMutableList()
                            list[idx] = list[idx].copy(filmWasteKg = newVal)
                            viewModel.pkgEnfardadeiraRows.value = list
                        },
                        isNumeric = true,
                        width = 90.dp
                    )

                    // PESO DEF.QUAL.
                    TableInputCell(
                        value = item.qualityDefectWeight,
                        onValueChange = { newVal ->
                            val list = enfRows.toMutableList()
                            list[idx] = list[idx].copy(qualityDefectWeight = newVal)
                            viewModel.pkgEnfardadeiraRows.value = list
                        },
                        isNumeric = true,
                        width = 90.dp
                    )

                    // QUAL DEFEITO?
                    TableInputCell(
                        value = item.defectDescription,
                        onValueChange = { newVal ->
                            val list = enfRows.toMutableList()
                            list[idx] = list[idx].copy(defectDescription = newVal)
                            viewModel.pkgEnfardadeiraRows.value = list
                        },
                        isNumeric = false,
                        width = 140.dp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PdfTextField(
                value = enfDevolucaoPeso,
                onValueChange = { viewModel.pkgEnfDevolucaoPeso.value = it },
                label = "Devolução ao estoque: Peso bobina bloqueada (kg)",
                placeholder = "0.0",
                modifier = Modifier.weight(1.1f)
            )
            PdfTextField(
                value = enfDevolucaoDefeito,
                onValueChange = { viewModel.pkgEnfDevolucaoDefeito.value = it },
                label = "Qual o defeito?",
                placeholder = "Motivo do bloqueio",
                modifier = Modifier.weight(0.9f)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Peso Líquido = Peso Bruto - Peso do Tubete",
            color = PdfTextPlaceholder,
            fontSize = 11.sp
        )
    }
}

// ----------------------------------------------------
// TAB 6: NOTAS (Page 7)
// ----------------------------------------------------
@Composable
private fun NotesTabContent(viewModel: ProductionViewModel) {
    val sobraLaminado by viewModel.pkgSobraLaminado.collectAsState()
    val sobraEnfardadeira by viewModel.pkgSobraEnfardadeira.collectAsState()
    val obs by viewModel.pkgGeneralObservations.collectAsState()

    // 1. ANOTAÇÕES DA SOBRA
    PdfCard {
        PdfSectionHeader("ANOTAÇÕES DA SOBRA")
        PdfTextField(
            value = sobraLaminado,
            onValueChange = { viewModel.pkgSobraLaminado.value = it },
            label = "Laminado",
            placeholder = "Anotações sobre laminado"
        )
        Spacer(modifier = Modifier.height(10.dp))
        PdfTextField(
            value = sobraEnfardadeira,
            onValueChange = { viewModel.pkgSobraEnfardadeira.value = it },
            label = "Enfardadeira",
            placeholder = "Anotações sobre enfardadeira"
        )
    }

    // 2. OBSERVAÇÕES GERAIS
    PdfCard {
        PdfSectionHeader("OBSERVAÇÕES GERAIS")
        PdfTextField(
            value = obs,
            onValueChange = { viewModel.pkgGeneralObservations.value = it },
            placeholder = "Registre aqui qualquer observação relevante do turno...",
            singleLine = false,
            modifier = Modifier.height(120.dp)
        )
    }

    Spacer(modifier = Modifier.height(6.dp))

    // Big Action Button
    PdfSaveButton(
        text = "✓ Salvar Registro do Turno",
        onClick = { viewModel.savePackagingControl() }
    )
}
