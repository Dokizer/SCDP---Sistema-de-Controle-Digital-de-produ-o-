package com.example

import com.example.data.model.DatingCheckEntity
import com.example.data.model.HistoryType
import com.example.data.model.MachineDowntimeEntity
import com.example.data.model.PackagingControlEntity
import com.example.data.model.UnifiedHistoryItem
import com.example.util.SpreadsheetDateFilter
import com.example.util.SpreadsheetExportUtil
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class SpreadsheetExportValidationTest {

    private val UTF8_BOM = "\uFEFF"

    @Test
    fun testConsolidatedSpreadsheetContentAndStructure() {
        val todayStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        val timestamp = System.currentTimeMillis()

        val history = listOf(
            UnifiedHistoryItem(
                id = "pkg-1",
                type = HistoryType.PACKAGING,
                title = "Controle de Empacotamento",
                subtitle = "Farinha Especial 1kg • Linha 01 (1º Turno)",
                line = "Linha 01",
                shift = "1º Turno",
                date = todayStr,
                metricsSummary = "1250 fds realizados",
                timestamp = timestamp,
                isSynced = true
            ),
            UnifiedHistoryItem(
                id = "down-1",
                type = HistoryType.DOWNTIME,
                title = "Apontamento de Paradas",
                subtitle = "Farinha Especial 1kg • Linha 01 (1º Turno)",
                line = "Linha 01",
                shift = "1º Turno",
                date = todayStr,
                metricsSummary = "3 paradas registradas",
                timestamp = timestamp,
                isSynced = true
            ),
            UnifiedHistoryItem(
                id = "date-1",
                type = HistoryType.DATING,
                title = "Checagem de Datagem",
                subtitle = "Máquina Empacotadeira 02 • 1º Turno",
                line = "Empacotadeira 02",
                shift = "1º Turno",
                date = todayStr,
                metricsSummary = "8 conformes / 0 NC",
                timestamp = timestamp,
                isSynced = true
            )
        )

        val csv = SpreadsheetExportUtil.generateConsolidatedCsv(history, SpreadsheetDateFilter.ALL)

        // 1. Validar UTF-8 BOM no início
        assertTrue("O CSV deve iniciar com UTF-8 BOM", csv.startsWith(UTF8_BOM))

        // 2. Validar Cabeçalhos da Empresa e Título
        assertTrue("Deve conter identificação da Farinhas CJ", csv.contains("FARINHAS CJ"))
        assertTrue("Deve conter título do relatório consolidado", csv.contains("RELATÓRIO GERAL CONSOLIDADO"))

        // 3. Validar Colunas do Cabeçalho
        val expectedHeaders = listOf(
            "Tipo de Controle",
            "Data",
            "Turno",
            "Linha / Máquina",
            "Produto / Identificação",
            "Resumo de Métricas",
            "Data e Hora do Registro"
        )
        val headerLine = csv.lines().first { it.contains("Tipo de Controle") }
        expectedHeaders.forEach { header ->
            assertTrue("Cabeçalho deve conter coluna $header", headerLine.contains(header))
        }

        // 4. Validar os dados das 3 rotinas
        assertTrue("Deve conter registro de Empacotamento", csv.contains("Controle de Empacotamento"))
        assertTrue("Deve conter resumo de 1250 fds", csv.contains("1250 fds realizados"))
        assertTrue("Deve conter registro de Paradas", csv.contains("Apontamento de Paradas"))
        assertTrue("Deve conter 3 paradas registradas", csv.contains("3 paradas registradas"))
        assertTrue("Deve conter registro de Datagem", csv.contains("Checagem de Datagem"))
        assertTrue("Deve conter métricas de datagem", csv.contains("8 conformes / 0 NC"))
    }

    @Test
    fun testPackagingSpreadsheetDetailedFields() {
        val todayStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        val entity = PackagingControlEntity(
            date = todayStr,
            shift = "2º Turno",
            line = "Linha 02",
            product = "Farinha de Trigo Tradicional 1kg",
            productChangedTo = "Farinha Tipo 1 Especial 1kg",
            productChangeTime = "16:45",
            responsible = "Marcos Souza",
            totalRealizedFds = 2840,
            initialSpeed = "75",
            speedChanged = true,
            speedChangeReason = "Ajuste de dosador",
            empacotadoraInternaC = true,
            empacotadoraExternaC = false, // NC
            enfardadeiraInternaC = true,
            enfardadeiraExternaC = true,
            machineCondition = "C",
            setorCondition = "C",
            trashCondition = "C",
            supplyCondition = "C",
            shiftAverageWeight = "1002.4",
            packageTareWeight = "11.5",
            empacotadoraInitialWeight = "1001.0",
            enfardadeiraInitialWeight = "1003.0",
            empDevolucaoPeso = "4.2",
            empDevolucaoDefeito = "Solda irregular",
            enfDevolucaoPeso = "1.5",
            enfDevolucaoDefeito = "Saco furado",
            leftoverLaminated = "18.0",
            leftoverBaler = "7.5",
            generalObservations = "Produção atingiu a meta do turno sem paradas graves."
        )

        val csv = SpreadsheetExportUtil.generatePackagingCsv(listOf(entity), SpreadsheetDateFilter.ALL)

        // Validar início UTF-8 BOM
        assertTrue(csv.startsWith(UTF8_BOM))
        assertTrue(csv.contains("PLANILHA DE CONTROLE DIÁRIO DE EMPACOTAMENTO"))

        // Validar campos críticos
        assertTrue("Deve conter o produto", csv.contains("Farinha de Trigo Tradicional 1kg"))
        assertTrue("Deve conter a mudança de produto", csv.contains("Farinha Tipo 1 Especial 1kg"))
        assertTrue("Deve conter horário da troca", csv.contains("16:45"))
        assertTrue("Deve conter o responsável", csv.contains("Marcos Souza"))
        assertTrue("Deve conter os fardos produzidos", csv.contains("2840"))
        assertTrue("Deve conter a velocidade inicial 75 bpm", csv.contains("75"))
        assertTrue("Deve indicar que velocidade foi alterada", csv.contains("Sim"))
        assertTrue("Deve conter o motivo do ajuste", csv.contains("Ajuste de dosador"))
        assertTrue("Deve acusar Não Conforme na Empacotadora Externa", csv.contains("Não Conforme"))
        assertTrue("Deve conter a média de peso 1002.4", csv.contains("1002.4"))
        assertTrue("Deve conter a tara 11.5", csv.contains("11.5"))
        assertTrue("Deve conter devolução da empacotadora 4.2", csv.contains("4.2"))
        assertTrue("Deve conter defeito solda irregular", csv.contains("Solda irregular"))
        assertTrue("Deve conter sobra laminado 18.0", csv.contains("18.0"))
        assertTrue("Deve conter as observações gerais", csv.contains("Produção atingiu a meta do turno sem paradas graves."))

        // Testar exportação individual
        val singleCsv = SpreadsheetExportUtil.generateSinglePackagingCsv(entity)
        assertTrue(singleCsv.contains("Marcos Souza"))
        assertTrue(singleCsv.contains("2840"))
    }

    @Test
    fun testMachineDowntimeSpreadsheetRowExpansion() {
        val todayStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

        val downtimeEntries = JSONArray().apply {
            put(JSONObject().apply {
                put("stopCode", "102")
                put("stopDescription", "Falha Mecânica - Correia Alimentadora")
                put("startParada", "08:15")
                put("endParada", "08:45")
                put("durationMinutes", 30)
                put("startManutencao", "08:20")
                put("endManutencao", "08:40")
                put("comments", "Substituição da correia desgastada.")
            })
            put(JSONObject().apply {
                put("stopCode", "204")
                put("stopDescription", "Troca de Bobina de Filme")
                put("startParada", "11:00")
                put("endParada", "11:15")
                put("durationMinutes", 15)
                put("comments", "Bobina do lote LT-402 finalizada.")
            })
        }

        val entity = MachineDowntimeEntity(
            date = todayStr,
            shift = "1º Turno",
            line = "Linha 01",
            product = "Farinha 1kg",
            isTransgenic = false,
            responsible = "Roberto Lima",
            entriesJson = downtimeEntries.toString(),
            totalStopsCount = 2,
            totalDowntimeMinutes = 45
        )

        val csv = SpreadsheetExportUtil.generateDowntimeCsv(listOf(entity), SpreadsheetDateFilter.ALL)

        // Validar início UTF-8 BOM
        assertTrue(csv.startsWith(UTF8_BOM))
        assertTrue(csv.contains("PLANILHA DETALHADA DE APONTAMENTO DE PARADA DE MÁQUINAS"))

        // Validar que cada parada foi expandida como linha independente
        assertTrue("Deve conter parada de código 102", csv.contains("102"))
        assertTrue("Deve conter descrição da parada mecânica", csv.contains("Falha Mecânica - Correia Alimentadora"))
        assertTrue("Deve conter horário inicial 08:15", csv.contains("08:15"))
        assertTrue("Deve conter horário final 08:45", csv.contains("08:45"))
        assertTrue("Deve conter duração de 30 minutos", csv.contains(";30;"))
        assertTrue("Deve conter observação da substituição", csv.contains("Substituição da correia desgastada."))

        assertTrue("Deve conter a segunda parada 204", csv.contains("204"))
        assertTrue("Deve conter descrição Troca de Bobina", csv.contains("Troca de Bobina de Filme"))
        assertTrue("Deve conter duração de 15 minutos", csv.contains(";15;"))

        // Validar totalizadores de parada
        assertTrue("Deve conter total de 2 paradas", csv.contains(";2;"))
        assertTrue("Deve conter total de 45 minutos no turno", csv.contains(";45;"))

        // Testar exportação individual
        val singleCsv = SpreadsheetExportUtil.generateSingleDowntimeCsv(entity)
        assertTrue(singleCsv.contains("Falha Mecânica - Correia Alimentadora"))
    }

    @Test
    fun testDatingCheckSpreadsheetDetailedRows() {
        val todayStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

        val datingChecks = JSONArray().apply {
            put(JSONObject().apply {
                put("hourLabel", "07:00")
                put("lotNumber", "LOTE-2026-A")
                put("printedTime", "06:58")
                put("printedExpiration", "18/09/2027")
                put("isCompliant", true)
                put("photoUri", "content://media/photo_etiqueta_123.jpg")
            })
            put(JSONObject().apply {
                put("hourLabel", "08:00")
                put("lotNumber", "LOTE-2026-A")
                put("printedTime", "07:55")
                put("printedExpiration", "18/09/2027")
                put("isCompliant", false) // Não conforme
            })
        }

        val entity = DatingCheckEntity(
            date = todayStr,
            shift = "1º Turno",
            machine = "Empacotadeira Bosch 01",
            responsible = "Aline Pereira",
            checksJson = datingChecks.toString(),
            compliantCount = 1,
            nonCompliantCount = 1,
            correctiveActions = "Ajustada fita térmica do datador que estava falhando."
        )

        val csv = SpreadsheetExportUtil.generateDatingCsv(listOf(entity), SpreadsheetDateFilter.ALL)

        // Validar início UTF-8 BOM
        assertTrue(csv.startsWith(UTF8_BOM))
        assertTrue(csv.contains("PLANILHA DE CHECAGEM DE DATAGEM DOS PACOTES"))

        // Validar campos do cabeçalho da máquina e responsável
        assertTrue("Deve conter a máquina", csv.contains("Empacotadeira Bosch 01"))
        assertTrue("Deve conter a responsável Aline", csv.contains("Aline Pereira"))

        // Validar as linhas expandidas
        assertTrue("Deve conter horário 07:00", csv.contains("07:00"))
        assertTrue("Deve conter lote LOTE-2026-A", csv.contains("LOTE-2026-A"))
        assertTrue("Deve conter hora impressa 06:58", csv.contains("06:58"))
        assertTrue("Deve conter validade 18/09/2027", csv.contains("18/09/2027"))
        assertTrue("Deve conter status Conforme", csv.contains("Conforme"))
        assertTrue("Deve acusar foto registrada", csv.contains("Sim (Registrada no App/PDF)"))

        assertTrue("Deve conter horário 08:00", csv.contains("08:00"))
        assertTrue("Deve conter status NÃO CONFORME", csv.contains("NÃO CONFORME"))
        assertTrue("Deve conter ação corretiva", csv.contains("Ajustada fita térmica do datador que estava falhando."))

        // Testar exportação individual
        val singleCsv = SpreadsheetExportUtil.generateSingleDatingCsv(entity)
        assertTrue(singleCsv.contains("LOTE-2026-A"))
        assertTrue(singleCsv.contains("Empacotadeira Bosch 01"))
    }

    @Test
    fun testDateFilteringMechanism() {
        val todayStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        val now = System.currentTimeMillis()

        // Registro de Hoje
        val itemToday = UnifiedHistoryItem(
            id = "item-today",
            type = HistoryType.PACKAGING,
            title = "Item Hoje",
            subtitle = "Linha 01",
            line = "Linha 01",
            shift = "1º Turno",
            date = todayStr,
            metricsSummary = "100 fds",
            timestamp = now,
            isSynced = true
        )

        // Registro de 20 dias atrás
        val calOld = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -20) }
        val oldDateStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calOld.time)
        val itemOld = UnifiedHistoryItem(
            id = "item-old",
            type = HistoryType.PACKAGING,
            title = "Item Antigo",
            subtitle = "Linha 01",
            line = "Linha 01",
            shift = "1º Turno",
            date = oldDateStr,
            metricsSummary = "50 fds",
            timestamp = calOld.timeInMillis,
            isSynced = true
        )

        val list = listOf(itemToday, itemOld)

        // 1. Filtro ALL
        val csvAll = SpreadsheetExportUtil.generateConsolidatedCsv(list, SpreadsheetDateFilter.ALL)
        assertTrue("Filtro ALL deve conter item hoje", csvAll.contains("Item Hoje"))
        assertTrue("Filtro ALL deve conter item antigo", csvAll.contains("Item Antigo"))

        // 2. Filtro TODAY
        val csvToday = SpreadsheetExportUtil.generateConsolidatedCsv(list, SpreadsheetDateFilter.TODAY)
        assertTrue("Filtro TODAY deve conter item hoje", csvToday.contains("Item Hoje"))
        assertFalse("Filtro TODAY NÃO deve conter item antigo", csvToday.contains("Item Antigo"))

        // 3. Filtro LAST_7_DAYS
        val csv7Days = SpreadsheetExportUtil.generateConsolidatedCsv(list, SpreadsheetDateFilter.LAST_7_DAYS)
        assertTrue("Filtro 7 Dias deve conter item hoje", csv7Days.contains("Item Hoje"))
        assertFalse("Filtro 7 Dias NÃO deve conter item de 20 dias atrás", csv7Days.contains("Item Antigo"))
    }

    @Test
    fun testSpecialCharactersEscapingInCsv() {
        val entity = PackagingControlEntity(
            date = "15/10/2026",
            shift = "1º Turno",
            line = "Linha Especial",
            product = "Farinha \"Super\" CJ; Especial", // Aspas e ponto-e-vírgula no nome!
            responsible = "João, O Operador",
            generalObservations = "Observação com\nquebra de linha e \"aspas duplas\" e ;ponto e vírgula."
        )

        val csv = SpreadsheetExportUtil.generatePackagingCsv(listOf(entity), SpreadsheetDateFilter.ALL)

        // Aspas duplas no produto devem ter virado "" e o campo inteiro envolvido em aspas
        assertTrue("Campo com aspas e ponto-e-vírgula deve estar protegido com aspas", csv.contains("\"Farinha \"\"Super\"\" CJ; Especial\""))
        assertTrue("Texto com quebra de linha e ponto-e-vírgula deve estar protegido", csv.contains("\"\"aspas duplas\"\""))
    }

    @Test
    fun printAllGeneratedSpreadsheetsForInspection() {
        val todayStr = "15/10/2026"
        val packaging = PackagingControlEntity(
            date = todayStr,
            shift = "1º Turno",
            line = "Linha 01",
            product = "Farinha Especial 1kg",
            responsible = "Gabriel Costa",
            totalRealizedFds = 3500,
            initialSpeed = "75",
            shiftAverageWeight = "1002.5",
            packageTareWeight = "11.0",
            generalObservations = "Turno operou normalmente."
        )

        val downtimeEntries = JSONArray().apply {
            put(JSONObject().apply {
                put("stopCode", "101")
                put("stopDescription", "Falta de matéria-prima")
                put("startParada", "10:00")
                put("endParada", "10:25")
                put("durationMinutes", 25)
                put("comments", "Aguardando reposição de moagem.")
            })
        }
        val downtime = MachineDowntimeEntity(
            date = todayStr,
            shift = "1º Turno",
            line = "Linha 01",
            product = "Farinha Especial 1kg",
            responsible = "Gabriel Costa",
            entriesJson = downtimeEntries.toString(),
            totalStopsCount = 1,
            totalDowntimeMinutes = 25
        )

        val datingChecks = JSONArray().apply {
            put(JSONObject().apply {
                put("hourLabel", "08:00")
                put("lotNumber", "LOTE-2026-CJ")
                put("printedTime", "07:58")
                put("printedExpiration", "15/10/2027")
                put("isCompliant", true)
            })
        }
        val dating = DatingCheckEntity(
            date = todayStr,
            shift = "1º Turno",
            machine = "Empacotadeira 01",
            responsible = "Gabriel Costa",
            checksJson = datingChecks.toString(),
            compliantCount = 1,
            nonCompliantCount = 0
        )

        val history = listOf(
            UnifiedHistoryItem("1", HistoryType.PACKAGING, "Controle de Empacotamento", "Farinha Especial 1kg", "Linha 01", "1º Turno", todayStr, "3500 fds", System.currentTimeMillis(), true),
            UnifiedHistoryItem("2", HistoryType.DOWNTIME, "Apontamento de Paradas", "Linha 01", "Linha 01", "1º Turno", todayStr, "1 parada (25m)", System.currentTimeMillis(), true),
            UnifiedHistoryItem("3", HistoryType.DATING, "Checagem de Datagem", "Empacotadeira 01", "Empacotadeira 01", "1º Turno", todayStr, "1 C / 0 NC", System.currentTimeMillis(), true)
        )

        println("\n================ [PLANILHA CONSOLIDADA GERAL] ================\n" + SpreadsheetExportUtil.generateConsolidatedCsv(history, SpreadsheetDateFilter.ALL))
        println("\n================ [PLANILHA EMPACOTAMENTO] ================\n" + SpreadsheetExportUtil.generatePackagingCsv(listOf(packaging), SpreadsheetDateFilter.ALL))
        println("\n================ [PLANILHA PARADAS] ================\n" + SpreadsheetExportUtil.generateDowntimeCsv(listOf(downtime), SpreadsheetDateFilter.ALL))
        println("\n================ [PLANILHA DATAGEM] ================\n" + SpreadsheetExportUtil.generateDatingCsv(listOf(dating), SpreadsheetDateFilter.ALL))
    }
}

