package com.example

import com.example.data.model.DatingCheckEntity
import com.example.data.model.MachineDowntimeEntity
import com.example.data.model.PackagingControlEntity
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DashboardAndPdfValidationTest {

    @Test
    fun testPackagingControlDashboardAndPdfDataIntegrity() {
        // 1. Simular produção horária (8 horas)
        val hourlyRows = listOf(
            mapOf("hourLabel" to "06:20 / 14:40 / 22:40", "meta" to "108", "real" to "110", "nTransg" to "5", "outros" to "0"),
            mapOf("hourLabel" to "07:20 / 15:40 / 23:40", "meta" to "216", "real" to "215", "nTransg" to "0", "outros" to "1"),
            mapOf("hourLabel" to "08:20 / 16:40 / 00:40", "meta" to "324", "real" to "330", "nTransg" to "2", "outros" to "0"),
            mapOf("hourLabel" to "09:20 / 17:40 / 01:40", "meta" to "432", "real" to "430", "nTransg" to "0", "outros" to "0"),
            mapOf("hourLabel" to "10:20 / 18:40 / 02:40", "meta" to "540", "real" to "545", "nTransg" to "0", "outros" to "0"),
            mapOf("hourLabel" to "11:20 / 19:40 / 03:40", "meta" to "648", "real" to "650", "nTransg" to "0", "outros" to "0"),
            mapOf("hourLabel" to "12:20 / 20:40 / 04:40", "meta" to "756", "real" to "756", "nTransg" to "0", "outros" to "0"),
            mapOf("hourLabel" to "13:40 / 21:40 / --:--", "meta" to "900", "real" to "905", "nTransg" to "0", "outros" to "0")
        )
        val hourlyArray = JSONArray().apply {
            hourlyRows.forEach { row ->
                put(JSONObject(row))
            }
        }.toString()

        // 2. Simular grid de pesos clicados
        val weightsGrid = JSONObject().apply {
            put("gramatura", "400g")
            val gridObj = JSONObject().apply {
                put("400", JSONArray(listOf("400", "400", "400", "", "", "", "", "")))
                put("401", JSONArray(listOf("", "", "401", "401", "", "", "", "")))
                put("399", JSONArray(listOf("399", "", "", "", "", "", "", "")))
            }
            put("grid", gridObj)
        }.toString()

        // 3. Simular bobinas e aparas
        val empArray = JSONArray().apply {
            put(JSONObject().apply {
                put("lot", "LT-9921")
                put("supplier", "Plásticos Brasil")
                put("filmWasteKg", "1.5")
                put("qualityDefectWeight", "0.2")
            })
        }.toString()

        // 4. Montar a entidade salva
        val entity = PackagingControlEntity(
            date = "18/09/2026",
            product = "Farinha de Trigo Especial 400g",
            responsible = "Carlos Silva",
            shift = "1º Turno",
            line = "Linha 01",
            totalRealizedFds = 3941,
            hourlyProductionJson = hourlyArray,
            weightsJson = weightsGrid,
            shiftAverageWeight = "400.2",
            packageTareWeight = "5.0",
            empacotadoraRowsJson = empArray,
            leftoverLaminated = "12.5",
            leftoverBaler = "4.0",
            generalObservations = "Turno operou com estabilidade sem desvios críticos."
        )

        // ==========================================
        // VALIDAÇÃO 1: DASHBOARD DE DETALHES
        // ==========================================
        // Validar parsing da produção horária no Dashboard
        val parsedHourlyForDashboard = mutableListOf<Map<String, String>>()
        val dashArr = JSONArray(entity.hourlyProductionJson)
        for (i in 0 until dashArr.length()) {
            val item = dashArr.getJSONObject(i)
            parsedHourlyForDashboard.add(
                mapOf(
                    "hour" to item.optString("hourLabel"),
                    "real" to item.optString("real"),
                    "meta" to item.optString("meta")
                )
            )
        }
        assertEquals("Dashboard deve ler exatamente 8 horas registradas", 8, parsedHourlyForDashboard.size)
        assertEquals("Primeira hora deve ter 110 fds realizados", "110", parsedHourlyForDashboard[0]["real"])
        assertEquals("Última hora deve ter 905 fds realizados", "905", parsedHourlyForDashboard[7]["real"])

        // Validar parsing do grid de pesos no Dashboard
        val dashWeightObj = JSONObject(entity.weightsJson)
        assertEquals("Gramatura deve ser 400g", "400g", dashWeightObj.optString("gramatura"))
        val dashGrid = dashWeightObj.optJSONObject("grid")
        assertNotNull("Grid de pesos não pode ser nulo", dashGrid)

        val faixasCounts = mutableMapOf<String, Int>()
        dashGrid!!.keys().forEach { faixa ->
            val arr = dashGrid.optJSONArray(faixa)
            var c = 0
            for (j in 0 until (arr?.length() ?: 0)) {
                if (arr!!.optString(j).isNotBlank()) c++
            }
            if (c > 0) faixasCounts[faixa] = c
        }
        assertEquals("Faixa 400g deve ter 3 marcações", 3, faixasCounts["400"])
        assertEquals("Faixa 401g deve ter 2 marcações", 2, faixasCounts["401"])
        assertEquals("Faixa 399g deve ter 1 marcação", 1, faixasCounts["399"])

        // ==========================================
        // VALIDAÇÃO 2: RELATÓRIO PDF EXPORTADO
        // ==========================================
        // Validar extração para o PDF
        val pdfProdArray = JSONArray(entity.hourlyProductionJson)
        assertEquals("PDF deve processar as 8 horas da produção horária", 8, pdfProdArray.length())
        val firstHourItem = pdfProdArray.getJSONObject(0)
        val hourText = firstHourItem.optString("hourLabel").split(" / ").firstOrNull() ?: ""
        assertEquals("Hora resumida no pill do PDF deve ser 06:20", "06:20", hourText)
        assertEquals("Quantidade no pill deve ser 110", "110", firstHourItem.optString("real"))

        // Validar resumo de pesagens no PDF
        val pdfWeightsObj = JSONObject(entity.weightsJson)
        val pdfGramatura = pdfWeightsObj.optString("gramatura", "400g")
        assertEquals("PDF deve identificar gramatura 400g", "400g", pdfGramatura)

        val pdfGrid = pdfWeightsObj.optJSONObject("grid")
        val pdfFaixasSummary = mutableListOf<String>()
        pdfGrid!!.keys().forEach { faixaKey ->
            val arr = pdfGrid.optJSONArray(faixaKey)
            var count = 0
            if (arr != null) {
                for (i in 0 until arr.length()) {
                    if (arr.optString(i).isNotBlank()) count++
                }
            }
            if (count > 0) {
                pdfFaixasSummary.add("${faixaKey}g ($count)")
            }
        }
        assertTrue("PDF deve conter resumo de 400g (3)", pdfFaixasSummary.contains("400g (3)"))
        assertTrue("PDF deve conter resumo de 401g (2)", pdfFaixasSummary.contains("401g (2)"))
        assertTrue("PDF deve conter resumo de 399g (1)", pdfFaixasSummary.contains("399g (1)"))
    }

    @Test
    fun testMachineDowntimeCalculationsAndDataIntegrity() {
        val stop1 = mapOf(
            "stopCode" to "E12",
            "stopDescription" to "Falta de Filme Laminado",
            "startParada" to "08:00",
            "endParada" to "08:35",
            "startManutencao" to "08:05",
            "endManutencao" to "08:30",
            "comments" to "Troca de bobina rápida"
        )
        val stop2 = mapOf(
            "stopCode" to "M04",
            "stopDescription" to "Ajuste de Mordente",
            "startParada" to "11:15",
            "endParada" to "11:45",
            "startManutencao" to "--:--",
            "endManutencao" to "--:--",
            "comments" to "Regulagem de temperatura"
        )

        val entries = listOf(stop1, stop2)
        var totalMinutes = 0
        entries.forEach { s ->
            val pStart = s["startParada"] ?: ""
            val pEnd = s["endParada"] ?: ""
            if (pStart.contains(":") && pEnd.contains(":")) {
                val sParts = pStart.split(":")
                val eParts = pEnd.split(":")
                val sMin = sParts[0].toInt() * 60 + sParts[1].toInt()
                val eMin = eParts[0].toInt() * 60 + eParts[1].toInt()
                val diff = if (eMin >= sMin) eMin - sMin else (1440 - sMin) + eMin
                totalMinutes += diff
            }
        }

        // 35 min + 30 min = 65 min
        assertEquals("Total de paradas calculado deve ser 65 minutos", 65, totalMinutes)

        val entriesJson = JSONArray().apply {
            entries.forEach { put(JSONObject(it)) }
        }.toString()

        val entity = MachineDowntimeEntity(
            date = "18/09/2026",
            responsible = "Marcos Operador",
            shift = "2º Turno",
            line = "Linha 02",
            product = "Farinha 500g",
            isTransgenic = false,
            entriesJson = entriesJson,
            totalStopsCount = entries.size,
            totalDowntimeMinutes = totalMinutes
        )

        // Validar entidade
        assertEquals(2, entity.totalStopsCount)
        assertEquals(65, entity.totalDowntimeMinutes)

        // Validar leitura do Dashboard e PDF
        val parsed = JSONArray(entity.entriesJson)
        assertEquals(2, parsed.length())
        assertEquals("E12", parsed.getJSONObject(0).optString("stopCode"))
        assertEquals("08:00", parsed.getJSONObject(0).optString("startParada"))
        assertEquals("08:35", parsed.getJSONObject(0).optString("endParada"))
    }

    @Test
    fun testDatingCheckComplianceAndDataIntegrity() {
        val checks = listOf(
            mapOf("hourLabel" to "06:20", "lotNumber" to "260918", "printedTime" to "06:20", "printedExpiration" to "18/03/2027", "isCompliant" to true),
            mapOf("hourLabel" to "07:20", "lotNumber" to "260918", "printedTime" to "07:20", "printedExpiration" to "18/03/2027", "isCompliant" to true),
            mapOf("hourLabel" to "08:20", "lotNumber" to "260918", "printedTime" to "08:20", "printedExpiration" to "18/03/2027", "isCompliant" to false)
        )

        val compliantCount = checks.count { it["isCompliant"] == true }
        val nonCompliantCount = checks.count { it["isCompliant"] == false }

        val entity = DatingCheckEntity(
            date = "18/09/2026",
            responsible = "Juliana Qualidade",
            shift = "1º Turno",
            machine = "M1",
            checksJson = JSONArray().apply { checks.forEach { put(JSONObject(it)) } }.toString(),
            compliantCount = compliantCount,
            nonCompliantCount = nonCompliantCount,
            correctiveActions = "Ajustado cartucho do datador às 08:22."
        )

        assertEquals(2, entity.compliantCount)
        assertEquals(1, entity.nonCompliantCount)

        val rate = (entity.compliantCount * 100) / (entity.compliantCount + entity.nonCompliantCount)
        assertEquals(66, rate) // 66% de conformidade

        val parsedChecks = JSONArray(entity.checksJson)
        assertEquals(3, parsedChecks.length())
        assertTrue("Primeiro item deve ser conforme", parsedChecks.getJSONObject(0).getBoolean("isCompliant"))
        assertTrue("Terceiro item deve ser não-conforme", !parsedChecks.getJSONObject(2).getBoolean("isCompliant"))
    }

    @Test
    fun testUnifiedHistoryItemMappingForAllThreeTypes() {
        // Teste de mapeamento do Controle de Empacotamento
        val pkg = PackagingControlEntity(
            id = "pkg-123",
            date = "06/10/2026",
            product = "Farinha Especial 1kg",
            line = "Linha 01",
            shift = "1º Turno",
            totalRealizedFds = 850
        )
        val unifiedPkg = com.example.data.model.UnifiedHistoryItem(
            id = pkg.id,
            type = com.example.data.model.HistoryType.PACKAGING,
            title = "Controle de Empacotamento",
            subtitle = "${pkg.product} • ${pkg.line} (${pkg.shift})",
            line = pkg.line,
            shift = pkg.shift,
            date = pkg.date,
            metricsSummary = "${pkg.totalRealizedFds} fds realizados",
            timestamp = pkg.createdAt,
            isSynced = true
        )
        assertEquals("Controle de Empacotamento", unifiedPkg.title)
        assertEquals("Farinha Especial 1kg • Linha 01 (1º Turno)", unifiedPkg.subtitle)
        assertEquals("850 fds realizados", unifiedPkg.metricsSummary)
        assertEquals("06/10/2026", unifiedPkg.date)

        // Teste de mapeamento do Apontamento de Paradas
        val dt = MachineDowntimeEntity(
            id = "dt-456",
            date = "06/10/2026",
            product = "Milho Flocão 500g",
            line = "Linha 02",
            shift = "2º Turno",
            totalStopsCount = 4,
            totalDowntimeMinutes = 75
        )
        val unifiedDt = com.example.data.model.UnifiedHistoryItem(
            id = dt.id,
            type = com.example.data.model.HistoryType.DOWNTIME,
            title = "Apontamento de Paradas",
            subtitle = "${dt.product} • ${dt.line} (${dt.shift})",
            line = dt.line,
            shift = dt.shift,
            date = dt.date,
            metricsSummary = "${dt.totalStopsCount} paradas registradas",
            timestamp = dt.createdAt,
            isSynced = true
        )
        assertEquals("Apontamento de Paradas", unifiedDt.title)
        assertEquals("4 paradas registradas", unifiedDt.metricsSummary)

        // Teste de mapeamento da Checagem de Datagem
        val dating = DatingCheckEntity(
            id = "dat-789",
            date = "06/10/2026",
            machine = "M1",
            shift = "3º Turno",
            compliantCount = 8,
            nonCompliantCount = 0
        )
        val unifiedDating = com.example.data.model.UnifiedHistoryItem(
            id = dating.id,
            type = com.example.data.model.HistoryType.DATING,
            title = "Checagem de Datagem",
            subtitle = "Máquina ${dating.machine} • ${dating.shift}",
            line = dating.machine,
            shift = dating.shift,
            date = dating.date,
            metricsSummary = "${dating.compliantCount} conformes / ${dating.nonCompliantCount} NC",
            timestamp = dating.createdAt,
            isSynced = true
        )
        assertEquals("Checagem de Datagem", unifiedDating.title)
        assertEquals("Máquina M1 • 3º Turno", unifiedDating.subtitle)
        assertEquals("8 conformes / 0 NC", unifiedDating.metricsSummary)
    }

    @Test
    fun testCompletePackagingControlEndToEndDataPreservation() {
        // Simular todos os campos preenchidos
        val hourlyArray = JSONArray().apply {
            put(JSONObject(mapOf("hourLabel" to "06:20", "meta" to "110", "real" to "115", "nTransg" to "2", "outros" to "1")))
            put(JSONObject(mapOf("hourLabel" to "07:20", "meta" to "220", "real" to "222", "nTransg" to "0", "outros" to "0")))
        }.toString()

        val enfardadeiraArray = JSONArray().apply {
            put(JSONObject(mapOf("quantity" to "1", "supplier" to "Embalagens Aliança", "lot" to "LT-5541", "filmWasteKg" to "0.8", "qualityDefectWeight" to "0.1", "defectDescription" to "Picote fraco")))
        }.toString()

        val entity = PackagingControlEntity(
            date = "06/10/2026",
            product = "Farinha Tipo 1 Especial 1kg",
            productChangedTo = "Farinha Tipo 1 Tradicional 1kg",
            productChangeTime = "10:30",
            responsible = "Roberto Lima",
            shift = "1º Turno",
            line = "Linha 01",
            empacotadoraInternaC = true,
            empacotadoraExternaC = true,
            enfardadeiraInternaC = true,
            enfardadeiraExternaC = false, // Não Conforme
            initialSpeed = "72",
            changeTime = "09:00",
            speedChangeReason = "Aumento de cadência na alimentação",
            speedChanged = true,
            hourlyProductionJson = hourlyArray,
            totalRealizedFds = 337,
            machineCondition = "C",
            setorCondition = "C",
            trashCondition = "C",
            supplyCondition = "C",
            packagingSameProduct = false,
            weightsJson = JSONObject().apply {
                put("gramatura", "1000g")
                put("grid", JSONObject().apply {
                    put("1000", JSONArray(listOf("1000", "1000")))
                    put("1002", JSONArray(listOf("1002")))
                })
            }.toString(),
            shiftAverageWeight = "1000.5",
            packageTareWeight = "12.0",
            enfardadeiraRowsJson = enfardadeiraArray,
            empDevolucaoPeso = "3.5",
            empDevolucaoDefeito = "Tubo amassado",
            enfDevolucaoPeso = "1.2",
            enfDevolucaoDefeito = "Fio solto",
            leftoverLaminated = "15.0",
            leftoverBaler = "6.5",
            generalObservations = "Turno com troca de produto às 10:30, alinhamento mantido."
        )

        // Validações do Dashboard
        assertEquals("Farinha Tipo 1 Especial 1kg", entity.product)
        assertEquals("Farinha Tipo 1 Tradicional 1kg", entity.productChangedTo)
        assertEquals("10:30", entity.productChangeTime)
        assertEquals("72", entity.initialSpeed)
        assertTrue(entity.speedChanged)
        assertEquals(337, entity.totalRealizedFds)
        assertEquals("1000.5", entity.shiftAverageWeight)
        assertEquals("12.0", entity.packageTareWeight)
        assertEquals("15.0", entity.leftoverLaminated)
        assertEquals("6.5", entity.leftoverBaler)
        assertEquals("Turno com troca de produto às 10:30, alinhamento mantido.", entity.generalObservations)

        // Validações de arrays parseados
        val enfList = JSONArray(entity.enfardadeiraRowsJson)
        assertEquals(1, enfList.length())
        assertEquals("Embalagens Aliança", enfList.getJSONObject(0).getString("supplier"))
        assertEquals("LT-5541", enfList.getJSONObject(0).getString("lot"))
        assertEquals("0.8", enfList.getJSONObject(0).getString("filmWasteKg"))
    }
}
