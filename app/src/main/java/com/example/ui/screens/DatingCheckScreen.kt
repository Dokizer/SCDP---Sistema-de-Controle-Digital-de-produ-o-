package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import com.example.ui.theme.materialYouTabTransitionSpec
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.compose.foundation.isSystemInDarkTheme
import com.example.ui.theme.ThemeMode
import com.example.ui.AppScreen
import com.example.ui.DetailedDatingCheckRow
import com.example.ui.ProductionViewModel
import com.example.util.ImageStorageUtil
import java.io.File
import com.example.ui.components.PdfCard
import com.example.ui.components.PdfDateField
import com.example.ui.components.PdfTimeField
import com.example.ui.components.PdfSectionHeader
import com.example.ui.components.PdfSegmentedButton
import com.example.ui.components.SegmentedColorVariant
import com.example.ui.components.PdfStepBottomBar
import com.example.ui.components.PdfTabRow
import com.example.ui.components.PdfTextField
import com.example.ui.components.PdfTopBar
import com.example.ui.theme.PdfButtonSelectedBg
import com.example.ui.theme.PdfButtonUnselectedBg
import com.example.ui.theme.PdfButtonUnselectedBorder
import com.example.ui.theme.PdfCardBackground
import com.example.ui.theme.PdfCardBorder
import com.example.ui.theme.PdfCyanPrimary
import com.example.ui.theme.PdfDarkBackground
import com.example.ui.theme.PdfGreen
import com.example.ui.theme.PdfGreenDark
import com.example.ui.theme.PdfInputBackground
import com.example.ui.theme.PdfInputBorder
import com.example.ui.theme.PdfRed
import com.example.ui.theme.PdfTeal
import com.example.ui.theme.PdfTextDark
import com.example.ui.theme.PdfTextMuted
import com.example.ui.theme.PdfTextPlaceholder
import com.example.ui.theme.PdfTextWhite

@Composable
fun DatingCheckScreen(viewModel: ProductionViewModel) {
    val selectedTab by viewModel.datingTab.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val systemInDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> systemInDark
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    val tabs = listOf(
        "Cabeçalho",
        "Checagem",
        "Inspeção"
    )

    Scaffold(
        topBar = {
            Column {
                PdfTopBar(
                    formCode = "FORM.017.FAR.CJ | REV. 00",
                    title = "Checagem de Datagem dos Pacotes",
                    onBackClick = { viewModel.navigateTo(AppScreen.Dashboard) },
                    onThemeToggle = { viewModel.toggleThemeMode() },
                    isDarkTheme = isDark
                )
                PdfTabRow(
                    tabs = tabs,
                    selectedTab = selectedTab,
                    onTabSelected = { viewModel.datingTab.value = it }
                )
            }
        },
        bottomBar = {
            PdfStepBottomBar(
                currentStep = selectedTab,
                totalSteps = tabs.size,
                onPrevious = {
                    if (selectedTab > 0) viewModel.datingTab.value = selectedTab - 1
                },
                onNext = {
                    if (selectedTab < tabs.size - 1) viewModel.datingTab.value = selectedTab + 1
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
                label = "DatingTabMotion"
            ) { currentTab ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    when (currentTab) {
                        0 -> HeaderTabContentDating(viewModel)
                        1 -> CheckTabContentDating(viewModel)
                        2 -> InspectionTabContentDating(viewModel)
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// TAB 1: CABEÇALHO (Page 14)
// ----------------------------------------------------
@Composable
private fun HeaderTabContentDating(viewModel: ProductionViewModel) {
    val date by viewModel.datingDate.collectAsState()
    val machine by viewModel.datingMachine.collectAsState()
    val responsible by viewModel.datingResponsible.collectAsState()
    val shift by viewModel.datingShift.collectAsState()

    // 1. INSTRUÇÃO
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = PdfButtonUnselectedBg,
        border = BorderStroke(1.dp, PdfTeal.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "INSTRUÇÃO",
                color = PdfTeal,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Fotografe a área da embalagem onde os dados do datador foram impressos. Preencha Lote, Horário e Validade. Verifique a cada 1 hora de produção.",
                color = PdfTextMuted,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "C — Conforme   NC — Não Conforme",
                color = PdfTextPlaceholder,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }

    // 2. IDENTIFICAÇÃO
    PdfCard {
        PdfSectionHeader("IDENTIFICAÇÃO")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PdfDateField(
                value = date,
                onValueChange = { viewModel.datingDate.value = it },
                label = "Data",
                placeholder = "DD/MM/AAAA",
                modifier = Modifier.weight(1f)
            )
            PdfTextField(
                value = machine,
                onValueChange = { viewModel.datingMachine.value = it },
                label = "Máquina",
                placeholder = "Ex: M1",
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        PdfTextField(
            value = responsible,
            onValueChange = { viewModel.datingResponsible.value = it },
            label = "Responsável",
            placeholder = "Nome do responsável"
        )
    }

    // 3. TURNO
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
                    onClick = { viewModel.datingShift.value = item },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// ----------------------------------------------------
// TAB 2: CHECAGEM (Page 15)
// ----------------------------------------------------
@Composable
private fun CheckTabContentDating(viewModel: ProductionViewModel) {
    val date by viewModel.datingDate.collectAsState()
    val shift by viewModel.datingShift.collectAsState()
    val checkRows by viewModel.datingCheckRows.collectAsState()

    // Subheader
    Text(
        text = "— $shift — • $date",
        color = PdfTextMuted,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(bottom = 2.dp)
    )

    // Expandable Hour Checks
    checkRows.forEachIndexed { index, rowItem ->
        HourlyCheckCard(
            row = rowItem,
            onToggleExpand = {
                val list = checkRows.toMutableList()
                list[index] = list[index].copy(isExpanded = !list[index].isExpanded)
                viewModel.datingCheckRows.value = list
            },
            onUpdate = { updated ->
                val list = checkRows.toMutableList()
                list[index] = updated
                viewModel.datingCheckRows.value = list
            }
        )
    }
}

@Composable
private fun HourlyCheckCard(
    row: DetailedDatingCheckRow,
    onToggleExpand: () -> Unit,
    onUpdate: (DetailedDatingCheckRow) -> Unit
) {
    val context = LocalContext.current
    var currentCameraFile by remember { mutableStateOf<File?>(null) }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && currentCameraFile != null) {
            val file = currentCameraFile!!
            if (file.exists() && file.length() > 0) {
                ImageStorageUtil.optimizeSavedImage(file)
                onUpdate(row.copy(photoUri = file.absolutePath))
                Toast.makeText(context, "Foto capturada e salva!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Photo Picker launcher (Galeria)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { selectedUri ->
        if (selectedUri != null) {
            val savedPath = ImageStorageUtil.saveUriToInternalStorage(context, selectedUri)
            if (savedPath != null) {
                onUpdate(row.copy(photoUri = savedPath))
                Toast.makeText(context, "Foto anexada com sucesso!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Permission launcher for Camera
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val (file, uri) = ImageStorageUtil.createPermanentCameraOutput(context)
            currentCameraFile = file
            cameraLauncher.launch(uri)
        } else {
            Toast.makeText(context, "Permissão da câmera é necessária para fotografar a etiqueta.", Toast.LENGTH_LONG).show()
        }
    }

    val openCamera = {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            val (file, uri) = ImageStorageUtil.createPermanentCameraOutput(context)
            currentCameraFile = file
            cameraLauncher.launch(uri)
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val openGallery = {
        photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp)),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = PdfCardBackground),
        border = BorderStroke(1.dp, PdfCardBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header row of the expandable card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    if (row.hourLabel.contains("INÍCIO")) {
                        Text(
                            text = "INÍCIO DA PRODUÇÃO",
                            color = PdfCyanPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "06:30 / 14:40 / 23:00",
                            color = PdfTextWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(
                            text = row.hourLabel,
                            color = PdfTextWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (row.lotNumber.isNotBlank() || row.photoUri != null) "Preenchido" else "Pendente",
                        color = if (row.lotNumber.isNotBlank() || row.photoUri != null) PdfGreen else PdfTextPlaceholder,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (row.isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = PdfTextPlaceholder,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            AnimatedVisibility(visible = row.isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Header de foto
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Foto da etiqueta datada:",
                            color = PdfTextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        if (row.photoUri != null) {
                            Text(
                                text = "Remover foto",
                                color = PdfRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable { onUpdate(row.copy(photoUri = null)) }
                                    .padding(4.dp)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = PdfInputBackground,
                        border = BorderStroke(
                            1.dp,
                            if (row.photoUri != null) PdfGreen else PdfInputBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            if (row.photoUri != null) {
                                val imageBitmap = remember(row.photoUri) {
                                    val bmp = ImageStorageUtil.loadBitmap(context, row.photoUri)
                                    bmp?.asImageBitmap()
                                }

                                if (imageBitmap != null) {
                                    Image(
                                        bitmap = imageBitmap,
                                        contentDescription = "Foto capturada da etiqueta",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(160.dp)
                                            .clip(RoundedCornerShape(6.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = PdfGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Foto da etiqueta salva ✓",
                                        color = PdfGreen,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = PdfDarkBackground,
                                        border = BorderStroke(1.dp, PdfCyanPrimary.copy(alpha = 0.5f)),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .clickable { openCamera() }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CameraAlt,
                                                contentDescription = "Câmera",
                                                tint = PdfCyanPrimary,
                                                modifier = Modifier.size(15.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Nova Foto (Câmera)",
                                                color = PdfCyanPrimary,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = PdfDarkBackground,
                                        border = BorderStroke(1.dp, PdfInputBorder),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .clickable { openGallery() }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PhotoLibrary,
                                                contentDescription = "Galeria",
                                                tint = PdfTextMuted,
                                                modifier = Modifier.size(15.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Galeria",
                                                color = PdfTextMuted,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = PdfDarkBackground,
                                        border = BorderStroke(1.dp, PdfGreen.copy(alpha = 0.5f)),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { openCamera() }
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(PdfGreenDark),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.CameraAlt,
                                                    contentDescription = "Câmera",
                                                    tint = PdfGreen,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "Abrir Câmera",
                                                color = PdfTextWhite,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "Fotografar etiqueta",
                                                color = PdfTextPlaceholder,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = PdfDarkBackground,
                                        border = BorderStroke(1.dp, PdfInputBorder),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { openGallery() }
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(PdfDarkBackground),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.PhotoLibrary,
                                                    contentDescription = "Galeria",
                                                    tint = PdfCyanPrimary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "Galeria",
                                                color = PdfTextWhite,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "Escolher arquivo",
                                                color = PdfTextPlaceholder,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Lote
                    PdfTextField(
                        value = row.lotNumber,
                        onValueChange = { onUpdate(row.copy(lotNumber = it)) },
                        label = "Lote",
                        placeholder = "Nº do lote"
                    )

                    // Horário impresso
                    PdfTimeField(
                        value = row.printedTime,
                        onValueChange = { onUpdate(row.copy(printedTime = it)) },
                        label = "Horário impresso",
                        placeholder = "Ex: 14:30"
                    )

                    // Validade impressa
                    PdfDateField(
                        value = row.printedExpiration,
                        onValueChange = { onUpdate(row.copy(printedExpiration = it)) },
                        label = "Validade impressa",
                        placeholder = "Ex: 03/2025"
                    )

                    // Checagem das soldas
                    Text(
                        text = "Checagem das soldas vertical e horizontal",
                        color = PdfTextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PdfSegmentedButton(
                            text = "✓ Conforme",
                            isSelected = row.isCompliant,
                            onClick = { onUpdate(row.copy(isCompliant = true)) },
                            modifier = Modifier.weight(1f),
                            colorVariant = SegmentedColorVariant.POSITIVE
                        )
                        PdfSegmentedButton(
                            text = "✕ Não Conforme",
                            isSelected = !row.isCompliant,
                            onClick = { onUpdate(row.copy(isCompliant = false)) },
                            modifier = Modifier.weight(1f),
                            colorVariant = SegmentedColorVariant.NEGATIVE
                        )
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// TAB 3: INSPEÇÃO (Page 16)
// ----------------------------------------------------
@Composable
private fun InspectionTabContentDating(viewModel: ProductionViewModel) {
    val ass1 by viewModel.datingAssinatura1.collectAsState()
    val ass2 by viewModel.datingAssinatura2.collectAsState()
    val ass3 by viewModel.datingAssinatura3.collectAsState()
    val actions by viewModel.datingCorrectiveActions.collectAsState()
    val checkRows by viewModel.datingCheckRows.collectAsState()

    val total = checkRows.size
    val conformes = checkRows.count { it.isCompliant && it.lotNumber.isNotBlank() }
    val naoConformes = checkRows.count { !it.isCompliant && it.lotNumber.isNotBlank() }

    // 1. INSPEÇÃO E LIMPEZA — CABEÇA E BASE DO DATADOR
    PdfCard {
        PdfSectionHeader("INSPEÇÃO E LIMPEZA — CABEÇA E BASE DO DATADOR")

        // Slot 1
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "HORÁRIO", color = MaterialTheme.colorScheme.primary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(text = "05:20 / 13:40 / 22:00", color = MaterialTheme.colorScheme.onSurface, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
                PdfTextField(
                    value = ass1,
                    onValueChange = { viewModel.datingAssinatura1.value = it },
                    label = "ASSINATURA OPERADOR",
                    placeholder = "Nome / rubrica",
                    modifier = Modifier.weight(1.3f)
                )
            }
        }

        // Slot 2
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "HORÁRIO", color = MaterialTheme.colorScheme.primary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(text = "08:20 / 16:40 / 01:00", color = MaterialTheme.colorScheme.onSurface, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
                PdfTextField(
                    value = ass2,
                    onValueChange = { viewModel.datingAssinatura2.value = it },
                    label = "ASSINATURA OPERADOR",
                    placeholder = "Nome / rubrica",
                    modifier = Modifier.weight(1.3f)
                )
            }
        }

        // Slot 3
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "HORÁRIO", color = MaterialTheme.colorScheme.primary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(text = "11:20 / 19:40 / 04:00", color = MaterialTheme.colorScheme.onSurface, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
                PdfTextField(
                    value = ass3,
                    onValueChange = { viewModel.datingAssinatura3.value = it },
                    label = "ASSINATURA OPERADOR",
                    placeholder = "Nome / rubrica",
                    modifier = Modifier.weight(1.3f)
                )
            }
        }
    }

    // 2. AÇÕES TOMADAS EM CASO DE NÃO CONFORMIDADE
    PdfCard {
        PdfSectionHeader("AÇÕES TOMADAS EM CASO DE NÃO CONFORMIDADE")
        PdfTextField(
            value = actions,
            onValueChange = { viewModel.datingCorrectiveActions.value = it },
            placeholder = "Descreva as ações corretivas tomadas...",
            singleLine = false,
            modifier = Modifier.height(110.dp)
        )
    }

    // 3. RESUMO DO TURNO
    PdfCard {
        PdfSectionHeader("RESUMO DO TURNO")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Total
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "$total", color = MaterialTheme.colorScheme.onSurface, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Total", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
            }

            // Conformes
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f),
                border = BorderStroke(1.dp, PdfGreen.copy(alpha = 0.4f)),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "$conformes", color = PdfGreen, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Conformes", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
            }

            // Não Conformes
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f),
                border = BorderStroke(1.dp, PdfRed.copy(alpha = 0.4f)),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "$naoConformes", color = PdfRed, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Não Conf.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(4.dp))

    // Green Save Button
    Button(
        onClick = { viewModel.saveDatingCheck() },
        colors = ButtonDefaults.buttonColors(containerColor = PdfGreen),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        Text(
            text = "✓ Salvar Checagem",
            color = PdfTextDark,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

