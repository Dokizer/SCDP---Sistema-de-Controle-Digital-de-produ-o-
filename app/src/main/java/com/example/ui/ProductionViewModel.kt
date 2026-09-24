package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.DatingCheckEntity
import com.example.data.model.DatingCheckItem
import com.example.data.model.DatingInspectionItem
import com.example.data.model.DowntimeItem
import com.example.data.model.HistoryType
import com.example.data.model.MachineDowntimeEntity
import com.example.data.model.PackagingControlEntity
import com.example.data.model.PackagingControlItem
import com.example.data.model.ProductionHourItem
import com.example.data.model.StopCode
import com.example.data.model.StopCodesData
import com.example.data.model.UnifiedHistoryItem
import com.example.data.repository.ProductionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import android.content.Context
import com.example.ui.theme.ThemeMode
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class DetailedProductionRow(
    val hourLabel: String,
    val meta: String,
    val real: String = "",
    val nTransg: String = "",
    val outros: String = ""
)

data class DetailedDowntimeEntry(
    val id: String = UUID.randomUUID().toString(),
    val startParada: String = "",
    val endParada: String = "",
    val startManutencao: String = "",
    val endManutencao: String = "",
    val stopCode: String = "",
    val stopDescription: String = "",
    val comments: String = ""
)

data class DetailedDatingCheckRow(
    val id: String = UUID.randomUUID().toString(),
    val hourLabel: String,
    val lotNumber: String = "",
    val printedTime: String = "",
    val printedExpiration: String = "",
    val isCompliant: Boolean = true,
    val photoUri: String? = null,
    val isExpanded: Boolean = false
)

class ProductionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ProductionRepository

    val historyItems: StateFlow<List<UnifiedHistoryItem>>

    init {
        val db = AppDatabase.getDatabase(application)
        repository = ProductionRepository(db.productionDao())
        historyItems = repository.unifiedHistory.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
    }

    suspend fun getPackagingControl(id: String): PackagingControlEntity? = repository.getPackagingControl(id)
    suspend fun getMachineDowntime(id: String): MachineDowntimeEntity? = repository.getMachineDowntime(id)
    suspend fun getDatingCheck(id: String): DatingCheckEntity? = repository.getDatingCheck(id)

    fun deleteHistoryItem(id: String, type: HistoryType) {
        viewModelScope.launch {
            repository.deleteHistoryItem(id, type)
            _snackbarMessage.value = "Registro excluído com sucesso."
        }
    }

    private val prefs = application.getSharedPreferences("app_settings_scdp", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(
        run {
            val saved = prefs.getString("theme_mode", ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
            try {
                ThemeMode.valueOf(saved)
            } catch (e: Exception) {
                ThemeMode.SYSTEM
            }
        }
    )
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        prefs.edit().putString("theme_mode", mode.name).apply()
    }

    fun toggleThemeMode() {
        val next = when (_themeMode.value) {
            ThemeMode.SYSTEM -> ThemeMode.DARK
            ThemeMode.DARK -> ThemeMode.LIGHT
            ThemeMode.LIGHT -> ThemeMode.SYSTEM
        }
        setThemeMode(next)
    }

    private val _currentScreen = MutableStateFlow<AppScreen>(AppScreen.Dashboard)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun clearSnackbarMessage() {
        _snackbarMessage.value = null
    }

    fun showSnackbar(message: String) {
        _snackbarMessage.value = message
    }

    private fun getCurrentDateStr(): String {
        return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
    }

    // ==========================================
    // 1. CONTROLE DIÁRIO DE EMPACOTAMENTO
    // ==========================================
    val packagingTab = MutableStateFlow(0)

    // Tab 1: Cabeçalho
    val pkgDate = MutableStateFlow(getCurrentDateStr())
    val pkgProduct = MutableStateFlow("Farinha Tipo 1")
    val pkgProductChangedTo = MutableStateFlow("")
    val pkgProductChangeTime = MutableStateFlow("")
    val pkgResponsible = MutableStateFlow("")
    val pkgShift = MutableStateFlow("1º Turno")
    val pkgLine = MutableStateFlow("M1")

    val pkgEmpacotadoraInternaC = MutableStateFlow(true)
    val pkgEmpacotadoraExternaC = MutableStateFlow(true)
    val pkgEnfardadeiraInternaC = MutableStateFlow(true)
    val pkgEnfardadeiraExternaC = MutableStateFlow(true)

    // Tab 2: Produção
    val pkgInitialSpeed = MutableStateFlow("0")
    val pkgChangeTime = MutableStateFlow("")
    val pkgSpeedChangeReason = MutableStateFlow("")
    val pkgSpeedChanged = MutableStateFlow(false)

    val pkgDetailedProductionRows = MutableStateFlow(
        listOf(
            DetailedProductionRow("06:20 / 14:40 / 22:40", "108"),
            DetailedProductionRow("07:20 / 15:40 / 23:40", "216"),
            DetailedProductionRow("08:20 / 16:40 / 00:40", "324"),
            DetailedProductionRow("09:20 / 17:40 / 01:40", "432"),
            DetailedProductionRow("10:20 / 18:40 / 02:40", "540"),
            DetailedProductionRow("11:20 / 19:40 / 03:40", "648"),
            DetailedProductionRow("12:20 / 20:40 / 04:40", "756"),
            DetailedProductionRow("13:40 / 21:40 / --:--", "900")
        )
    )

    // Tab 3: Máquina
    val pkgMachineClean = MutableStateFlow(true) // Limpa / Suja
    val pkgMachineBroken = MutableStateFlow(false) // Quebrada / Disponível
    val pkgMachineAdjusted = MutableStateFlow(false) // Ajustada
    val pkgMachinePreventive = MutableStateFlow(false) // Na Preventiva
    val pkgMachineNoBobbin = MutableStateFlow(false) // Sem Bobinas

    val pkgSectorClean = MutableStateFlow(true) // Limpo / Sujo
    val pkgTrashBoxesClean = MutableStateFlow(true) // Com produtos vs Várias limpas com plástico

    val pkgAbastecido = MutableStateFlow(true) // Abastecido vs Desabastecido
    val pkgPackagingSameProduct = MutableStateFlow(true) // Conforme vs Não Conforme

    // Tab 4: Pesos
    val pkgSelectedGramatura = MutableStateFlow("400g")
    val pkgShiftAverageWeight = MutableStateFlow("")
    val pkgPackageTareWeight = MutableStateFlow("")

    val pkgWeightsMatrix400 = MutableStateFlow(
        listOf("405", "404", "403", "402", "401", "400", "399", "398").associateWith { "" }
    )
    val pkgWeightsMatrix500 = MutableStateFlow(
        listOf("506", "505", "504", "503", "502", "501", "500", "499").associateWith { "" }
    )

    val pkgWeightsGrid400 = MutableStateFlow(
        listOf("405", "404", "403", "402", "401", "400", "399", "398", "<398").associateWith {
            List(8) { "" }
        }
    )
    val pkgWeightsGrid500 = MutableStateFlow(
        listOf("506", "505", "504", "503", "502", "501", "500", "499", "498", "<498").associateWith {
            List(8) { "" }
        }
    )

    fun updateWeightGridCell(gramatura: String, faixa: String, colIndex: Int, value: String) {
        if (gramatura == "400g") {
            val map = pkgWeightsGrid400.value.toMutableMap()
            val list = (map[faixa] ?: List(8) { "" }).toMutableList()
            if (colIndex in 0..7) {
                list[colIndex] = value
                map[faixa] = list
                pkgWeightsGrid400.value = map
            }
        } else {
            val map = pkgWeightsGrid500.value.toMutableMap()
            val list = (map[faixa] ?: List(8) { "" }).toMutableList()
            if (colIndex in 0..7) {
                list[colIndex] = value
                map[faixa] = list
                pkgWeightsGrid500.value = map
            }
        }
    }

    fun toggleWeightGridCell(gramatura: String, faixa: String, colIndex: Int) {
        val currentVal = if (gramatura == "400g") {
            pkgWeightsGrid400.value[faixa]?.getOrNull(colIndex) ?: ""
        } else {
            pkgWeightsGrid500.value[faixa]?.getOrNull(colIndex) ?: ""
        }
        val newVal = if (currentVal.isBlank()) faixa else ""
        updateWeightGridCell(gramatura, faixa, colIndex, newVal)
        recalculateShiftAverageWeight(gramatura)
    }

    private fun recalculateShiftAverageWeight(gramatura: String) {
        val grid = if (gramatura == "400g") pkgWeightsGrid400.value else pkgWeightsGrid500.value
        val numericList = mutableListOf<Double>()
        grid.forEach { (faixaKey, list) ->
            val cleanFaixa = faixaKey.replace("<", "").replace(">", "").replace(",", ".").trim()
            val num = cleanFaixa.toDoubleOrNull()
            if (num != null) {
                list.forEach { cell ->
                    if (cell.isNotBlank()) {
                        numericList.add(num)
                    }
                }
            }
        }
        if (numericList.isNotEmpty()) {
            val avg = numericList.average()
            pkgShiftAverageWeight.value = String.format(Locale.US, "%.1f", avg)
        }
    }

    // Tab 5: Embalagens
    val pkgEmpacotadoraInitialWeight = MutableStateFlow("")
    val pkgEmpacotadoraRows = MutableStateFlow(
        List(8) { PackagingControlItem(quantity = "", supplier = "", lot = "", stiffnessLeftC = true, stiffnessRightC = true, filmWasteKg = "", qualityDefectWeight = "", defectDescription = "") }
    )

    val pkgEnfardadeiraInitialWeight = MutableStateFlow("")
    val pkgEnfardadeiraRows = MutableStateFlow(
        List(8) { PackagingControlItem(quantity = "", supplier = "", lot = "", filmWasteKg = "", qualityDefectWeight = "", defectDescription = "") }
    )
    val pkgEmpDevolucaoPeso = MutableStateFlow("")
    val pkgEmpDevolucaoDefeito = MutableStateFlow("")
    val pkgEnfDevolucaoPeso = MutableStateFlow("")
    val pkgEnfDevolucaoDefeito = MutableStateFlow("")

    // Tab 6: Notas
    val pkgSobraLaminado = MutableStateFlow("")
    val pkgSobraEnfardadeira = MutableStateFlow("")
    val pkgGeneralObservations = MutableStateFlow("")

    fun savePackagingControl() {
        viewModelScope.launch {
            val totalRealized = pkgDetailedProductionRows.value.sumOf { it.real.toIntOrNull() ?: 0 }

            val hourlyArray = JSONArray().apply {
                pkgDetailedProductionRows.value.forEach { row ->
                    put(JSONObject().apply {
                        put("hourLabel", row.hourLabel)
                        put("meta", row.meta)
                        put("real", row.real)
                        put("nTransg", row.nTransg)
                        put("outros", row.outros)
                    })
                }
            }.toString()

            val weightsJsonObj = JSONObject().apply {
                put("weights400", JSONObject(pkgWeightsMatrix400.value))
                put("weights500", JSONObject(pkgWeightsMatrix500.value))
                put("gramatura", pkgSelectedGramatura.value)
                val gridMap = if (pkgSelectedGramatura.value == "400g") pkgWeightsGrid400.value else pkgWeightsGrid500.value
                val gridObj = JSONObject()
                gridMap.forEach { (k, list) ->
                    gridObj.put(k, JSONArray(list))
                }
                put("grid", gridObj)
            }.toString()

            val empArray = JSONArray().apply {
                pkgEmpacotadoraRows.value.filter { it.supplier.isNotBlank() || it.lot.isNotBlank() }.forEach { item ->
                    put(JSONObject().apply {
                        put("quantity", item.quantity)
                        put("supplier", item.supplier)
                        put("lot", item.lot)
                        put("stiffnessLeftC", item.stiffnessLeftC)
                        put("stiffnessRightC", item.stiffnessRightC)
                        put("filmWasteKg", item.filmWasteKg)
                        put("qualityDefectWeight", item.qualityDefectWeight)
                        put("defectDescription", item.defectDescription)
                    })
                }
            }.toString()

            val enfArray = JSONArray().apply {
                pkgEnfardadeiraRows.value.filter { it.supplier.isNotBlank() || it.lot.isNotBlank() }.forEach { item ->
                    put(JSONObject().apply {
                        put("quantity", item.quantity)
                        put("supplier", item.supplier)
                        put("lot", item.lot)
                        put("filmWasteKg", item.filmWasteKg)
                        put("qualityDefectWeight", item.qualityDefectWeight)
                        put("defectDescription", item.defectDescription)
                    })
                }
            }.toString()

            val entity = PackagingControlEntity(
                id = UUID.randomUUID().toString(),
                date = pkgDate.value,
                product = pkgProduct.value,
                productChangedTo = pkgProductChangedTo.value,
                productChangeTime = pkgProductChangeTime.value,
                responsible = pkgResponsible.value.ifBlank { "Operador Responsável" },
                shift = pkgShift.value,
                line = pkgLine.value,
                empacotadoraInternaC = pkgEmpacotadoraInternaC.value,
                empacotadoraExternaC = pkgEmpacotadoraExternaC.value,
                enfardadeiraInternaC = pkgEnfardadeiraInternaC.value,
                enfardadeiraExternaC = pkgEnfardadeiraExternaC.value,
                initialSpeed = pkgInitialSpeed.value,
                changeTime = pkgChangeTime.value,
                speedChangeReason = pkgSpeedChangeReason.value,
                speedChanged = pkgSpeedChanged.value,
                hourlyProductionJson = hourlyArray,
                totalRealizedFds = totalRealized,
                machineCondition = if (pkgMachineClean.value) "C" else "NC",
                setorCondition = if (pkgSectorClean.value) "C" else "NC",
                trashCondition = if (pkgTrashBoxesClean.value) "C" else "NC",
                supplyCondition = if (pkgAbastecido.value) "C" else "NC",
                packagingSameProduct = pkgPackagingSameProduct.value,
                weightsJson = weightsJsonObj,
                shiftAverageWeight = pkgShiftAverageWeight.value,
                packageTareWeight = pkgPackageTareWeight.value,
                empacotadoraRowsJson = empArray,
                enfardadeiraRowsJson = enfArray,
                empacotadoraInitialWeight = pkgEmpacotadoraInitialWeight.value,
                enfardadeiraInitialWeight = pkgEnfardadeiraInitialWeight.value,
                empDevolucaoPeso = pkgEmpDevolucaoPeso.value,
                empDevolucaoDefeito = pkgEmpDevolucaoDefeito.value,
                enfDevolucaoPeso = pkgEnfDevolucaoPeso.value,
                enfDevolucaoDefeito = pkgEnfDevolucaoDefeito.value,
                leftoverLaminated = pkgSobraLaminado.value,
                leftoverBaler = pkgSobraEnfardadeira.value,
                generalObservations = pkgGeneralObservations.value,
                createdAt = System.currentTimeMillis()
            )

            val synced = repository.savePackagingControl(entity)
            _snackbarMessage.value = if (synced) {
                "✓ Controle de Empacotamento salvo e sincronizado!"
            } else {
                "✓ Registro salvo com sucesso no dispositivo!"
            }
            _currentScreen.value = AppScreen.Dashboard
        }
    }

    // ==========================================
    // 2. APONTAMENTO DE PARADA DE MÁQUINAS
    // ==========================================
    val downtimeTab = MutableStateFlow(0) // 0: Cabeçalho, 1: Registro, 2: Códigos

    // Tab 1: Cabeçalho
    val dtDate = MutableStateFlow(getCurrentDateStr())
    val dtResponsible = MutableStateFlow("")
    val dtShift = MutableStateFlow("1º Turno")
    val dtShiftStart = MutableStateFlow("")
    val dtShiftEnd = MutableStateFlow("")
    val dtLine = MutableStateFlow("M1")
    val dtProduct = MutableStateFlow("São Braz") // São Braz, Nordestino, Novamilho, Gostosin
    val dtIsTransgenic = MutableStateFlow(false)

    // Tab 2: Registro
    val dtDetailedEntries = MutableStateFlow(
        listOf(
            DetailedDowntimeEntry(
                startParada = "",
                endParada = "",
                startManutencao = "",
                endManutencao = "",
                stopCode = "",
                stopDescription = "",
                comments = ""
            )
        )
    )

    fun addDetailedDowntimeEntry() {
        val current = dtDetailedEntries.value.toMutableList()
        current.add(DetailedDowntimeEntry())
        dtDetailedEntries.value = current
    }

    fun removeDetailedDowntimeEntry(index: Int) {
        val current = dtDetailedEntries.value.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            dtDetailedEntries.value = current
        }
    }

    // Tab 3: Códigos de Parada
    val stopCodeSearchQuery = MutableStateFlow("")
    var activeStopEntryIndexForCode: Int = 0

    val allStopCodes = StopCodesData.list

    val filteredStopCodes: StateFlow<List<StopCode>> = stopCodeSearchQuery.combine(MutableStateFlow(allStopCodes)) { query, codes ->
        if (query.isBlank()) codes else {
            codes.filter {
                it.code.contains(query, ignoreCase = true) ||
                        it.category.contains(query, ignoreCase = true) ||
                        it.description.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), allStopCodes)

    fun selectStopCode(code: StopCode) {
        val list = dtDetailedEntries.value.toMutableList()
        if (activeStopEntryIndexForCode in list.indices) {
            list[activeStopEntryIndexForCode] = list[activeStopEntryIndexForCode].copy(
                stopCode = code.code,
                stopDescription = code.description
            )
            dtDetailedEntries.value = list
        }
        downtimeTab.value = 1 // Return to Registro tab
    }

    fun saveMachineDowntime() {
        viewModelScope.launch {
            val totalCount = dtDetailedEntries.value.size

            var totalMinutes = 0
            val entriesArray = JSONArray().apply {
                dtDetailedEntries.value.forEach { item ->
                    try {
                        val pStart = item.startParada.trim()
                        val pEnd = item.endParada.trim()
                        if (pStart.contains(":") && pEnd.contains(":")) {
                            val sParts = pStart.split(":")
                            val eParts = pEnd.split(":")
                            val sMin = sParts[0].toInt() * 60 + sParts[1].toInt()
                            val eMin = eParts[0].toInt() * 60 + eParts[1].toInt()
                            val diff = if (eMin >= sMin) eMin - sMin else (1440 - sMin) + eMin
                            totalMinutes += diff
                        }
                    } catch (_: Exception) {}

                    put(JSONObject().apply {
                        put("id", item.id)
                        put("stopCode", item.stopCode)
                        put("stopDescription", item.stopDescription)
                        put("startParada", item.startParada)
                        put("endParada", item.endParada)
                        put("startManutencao", item.startManutencao)
                        put("endManutencao", item.endManutencao)
                        put("comments", item.comments)
                    })
                }
            }.toString()

            val entity = MachineDowntimeEntity(
                id = UUID.randomUUID().toString(),
                date = dtDate.value,
                responsible = dtResponsible.value.ifBlank { "Operador de Linha" },
                shift = dtShift.value,
                shiftStart = dtShiftStart.value,
                shiftEnd = dtShiftEnd.value,
                line = dtLine.value,
                product = dtProduct.value,
                isTransgenic = dtIsTransgenic.value,
                entriesJson = entriesArray,
                totalStopsCount = totalCount,
                totalDowntimeMinutes = totalMinutes,
                createdAt = System.currentTimeMillis()
            )

            val synced = repository.saveMachineDowntime(entity)
            _snackbarMessage.value = if (synced) {
                "✓ Apontamento de Paradas salvo e sincronizado!"
            } else {
                "✓ Registro salvo com sucesso no dispositivo!"
            }
            _currentScreen.value = AppScreen.Dashboard
        }
    }

    // ==========================================
    // 3. CHECAGEM DE DATAGEM DOS PACOTES
    // ==========================================
    val datingTab = MutableStateFlow(0) // 0: Cabeçalho, 1: Checagem, 2: Inspeção

    // Tab 1: Cabeçalho
    val datingDate = MutableStateFlow(getCurrentDateStr())
    val datingMachine = MutableStateFlow("M1")
    val datingResponsible = MutableStateFlow("")
    val datingShift = MutableStateFlow("1º Turno")

    // Tab 2: Checagem
    val datingCheckRows = MutableStateFlow(
        listOf(
            DetailedDatingCheckRow(hourLabel = "INÍCIO DA PRODUÇÃO 06:30 / 14:40 / 23:00", isExpanded = true),
            DetailedDatingCheckRow(hourLabel = "07:30 / 15:40 / 00:00"),
            DetailedDatingCheckRow(hourLabel = "08:30 / 16:40 / 01:00"),
            DetailedDatingCheckRow(hourLabel = "09:30 / 17:40 / 02:00"),
            DetailedDatingCheckRow(hourLabel = "10:30 / 18:40 / 03:00"),
            DetailedDatingCheckRow(hourLabel = "11:30 / 19:40 / 04:00"),
            DetailedDatingCheckRow(hourLabel = "12:30 / 20:40 / 05:00"),
            DetailedDatingCheckRow(hourLabel = "13:30 / 21:40 / --:--")
        )
    )

    // Tab 3: Inspeção
    val datingAssinatura1 = MutableStateFlow("")
    val datingAssinatura2 = MutableStateFlow("")
    val datingAssinatura3 = MutableStateFlow("")
    val datingCorrectiveActions = MutableStateFlow("")

    fun saveDatingCheck() {
        viewModelScope.launch {
            val compliantCount = datingCheckRows.value.count { it.isCompliant }
            val nonCompliantCount = datingCheckRows.value.count { !it.isCompliant }

            val checksArray = JSONArray().apply {
                datingCheckRows.value.forEach { item ->
                    put(JSONObject().apply {
                        put("hourLabel", item.hourLabel)
                        put("lotNumber", item.lotNumber)
                        put("printedTime", item.printedTime)
                        put("printedExpiration", item.printedExpiration)
                        put("isCompliant", item.isCompliant)
                        put("photoUri", item.photoUri ?: "")
                    })
                }
            }.toString()

            val entity = DatingCheckEntity(
                id = UUID.randomUUID().toString(),
                date = datingDate.value,
                responsible = datingResponsible.value.ifBlank { "Inspetor de Qualidade" },
                shift = datingShift.value,
                machine = datingMachine.value,
                checksJson = checksArray,
                inspectionsJson = JSONObject().apply {
                    put("ass1", datingAssinatura1.value)
                    put("ass2", datingAssinatura2.value)
                    put("ass3", datingAssinatura3.value)
                }.toString(),
                compliantCount = compliantCount,
                nonCompliantCount = nonCompliantCount,
                correctiveActions = datingCorrectiveActions.value,
                createdAt = System.currentTimeMillis()
            )

            val synced = repository.saveDatingCheck(entity)
            _snackbarMessage.value = if (synced) {
                "✓ Checagem de Datagem salva e sincronizada!"
            } else {
                "✓ Registro salvo com sucesso no dispositivo!"
            }
            _currentScreen.value = AppScreen.Dashboard
        }
    }
}
