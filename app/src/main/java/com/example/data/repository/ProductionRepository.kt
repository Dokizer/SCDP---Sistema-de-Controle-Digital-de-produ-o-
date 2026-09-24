package com.example.data.repository

import com.example.data.local.ProductionDao
import com.example.data.model.DatingCheckEntity
import com.example.data.model.HistoryType
import com.example.data.model.MachineDowntimeEntity
import com.example.data.model.PackagingControlEntity
import com.example.data.model.UnifiedHistoryItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ProductionRepository(
    private val dao: ProductionDao
) {
    val unifiedHistory: Flow<List<UnifiedHistoryItem>> = combine(
        dao.getAllPackagingControls(),
        dao.getAllMachineDowntimes(),
        dao.getAllDatingChecks()
    ) { packaging, downtime, dating ->
        val list = mutableListOf<UnifiedHistoryItem>()

        packaging.forEach {
            list.add(
                UnifiedHistoryItem(
                    id = it.id,
                    type = HistoryType.PACKAGING,
                    title = "Controle de Empacotamento",
                    subtitle = "${it.product} • ${it.line} (${it.shift})",
                    line = it.line,
                    shift = it.shift,
                    date = it.date,
                    metricsSummary = "${it.totalRealizedFds} fds realizados",
                    timestamp = it.createdAt,
                    isSynced = true
                )
            )
        }

        downtime.forEach {
            list.add(
                UnifiedHistoryItem(
                    id = it.id,
                    type = HistoryType.DOWNTIME,
                    title = "Apontamento de Paradas",
                    subtitle = "${it.product} • ${it.line} (${it.shift})",
                    line = it.line,
                    shift = it.shift,
                    date = it.date,
                    metricsSummary = "${it.totalStopsCount} paradas registradas",
                    timestamp = it.createdAt,
                    isSynced = true
                )
            )
        }

        dating.forEach {
            list.add(
                UnifiedHistoryItem(
                    id = it.id,
                    type = HistoryType.DATING,
                    title = "Checagem de Datagem",
                    subtitle = "Máquina ${it.machine} • ${it.shift}",
                    line = it.machine,
                    shift = it.shift,
                    date = it.date,
                    metricsSummary = "${it.compliantCount} conformes / ${it.nonCompliantCount} NC",
                    timestamp = it.createdAt,
                    isSynced = true
                )
            )
        }

        list.sortedByDescending { it.timestamp }
    }

    suspend fun getPackagingControl(id: String): PackagingControlEntity? = dao.getPackagingControlById(id)
    suspend fun getMachineDowntime(id: String): MachineDowntimeEntity? = dao.getMachineDowntimeById(id)
    suspend fun getDatingCheck(id: String): DatingCheckEntity? = dao.getDatingCheckById(id)

    suspend fun savePackagingControl(entity: PackagingControlEntity): Boolean {
        dao.insertPackagingControl(entity)
        return true
    }

    suspend fun saveMachineDowntime(entity: MachineDowntimeEntity): Boolean {
        dao.insertMachineDowntime(entity)
        return true
    }

    suspend fun saveDatingCheck(entity: DatingCheckEntity): Boolean {
        dao.insertDatingCheck(entity)
        return true
    }

    suspend fun deleteHistoryItem(id: String, type: HistoryType) {
        when (type) {
            HistoryType.PACKAGING -> dao.deletePackagingControlById(id)
            HistoryType.DOWNTIME -> dao.deleteMachineDowntimeById(id)
            HistoryType.DATING -> dao.deleteDatingCheckById(id)
        }
    }
}
