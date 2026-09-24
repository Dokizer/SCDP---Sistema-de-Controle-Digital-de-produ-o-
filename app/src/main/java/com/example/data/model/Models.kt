package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class HistoryType {
    PACKAGING,
    DOWNTIME,
    DATING
}

data class UnifiedHistoryItem(
    val id: String,
    val type: HistoryType,
    val title: String,
    val subtitle: String,
    val line: String,
    val shift: String,
    val date: String,
    val metricsSummary: String,
    val timestamp: Long,
    val isSynced: Boolean
)

@Entity(tableName = "packaging_controls")
data class PackagingControlEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val firebaseId: String? = null,
    val date: String = "",
    val product: String = "",
    val productChangedTo: String = "",
    val productChangeTime: String = "",
    val responsible: String = "",
    val shift: String = "",
    val line: String = "",
    val empacotadoraInternaC: Boolean = true,
    val empacotadoraExternaC: Boolean = true,
    val enfardadeiraInternaC: Boolean = true,
    val enfardadeiraExternaC: Boolean = true,
    val initialSpeed: String = "",
    val changeTime: String = "",
    val speedChangeReason: String = "",
    val speedChanged: Boolean = false,
    val hourlyProductionJson: String = "",
    val totalRealizedFds: Int = 0,
    val machineCondition: String = "C",
    val setorCondition: String = "C",
    val trashCondition: String = "C",
    val supplyCondition: String = "C",
    val packagingSameProduct: Boolean = true,
    val weightsJson: String = "",
    val shiftAverageWeight: String = "",
    val packageTareWeight: String = "",
    val empacotadoraRowsJson: String = "",
    val enfardadeiraRowsJson: String = "",
    val empacotadoraInitialWeight: String = "",
    val enfardadeiraInitialWeight: String = "",
    val empDevolucaoPeso: String = "",
    val empDevolucaoDefeito: String = "",
    val enfDevolucaoPeso: String = "",
    val enfDevolucaoDefeito: String = "",
    val leftoverLaminated: String = "",
    val leftoverBaler: String = "",
    val generalObservations: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val isSyncedWithFirebase: Boolean = false
)

data class PackagingControlItem(
    val quantity: String = "",
    val supplier: String = "",
    val lot: String = "",
    val stiffnessLeftC: Boolean = true,
    val stiffnessRightC: Boolean = true,
    val filmWasteKg: String = "",
    val qualityDefectWeight: String = "",
    val defectDescription: String = ""
)

data class ProductionHourItem(
    val hour: String = "",
    val fdsQuantity: String = ""
)

@Entity(tableName = "machine_downtimes")
data class MachineDowntimeEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val firebaseId: String? = null,
    val date: String = "",
    val responsible: String = "",
    val shift: String = "",
    val shiftStart: String = "",
    val shiftEnd: String = "",
    val line: String = "",
    val product: String = "",
    val isTransgenic: Boolean = false,
    val entriesJson: String = "",
    val totalStopsCount: Int = 0,
    val totalDowntimeMinutes: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val isSyncedWithFirebase: Boolean = false
)

data class DowntimeItem(
    val id: String = UUID.randomUUID().toString(),
    val stopCode: String = "",
    val stopDescription: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val durationMinutes: Int = 0,
    val comments: String = ""
)

@Entity(tableName = "dating_checks")
data class DatingCheckEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val firebaseId: String? = null,
    val date: String = "",
    val responsible: String = "",
    val shift: String = "",
    val machine: String = "",
    val checksJson: String = "",
    val inspectionsJson: String = "",
    val compliantCount: Int = 0,
    val nonCompliantCount: Int = 0,
    val correctiveActions: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val isSyncedWithFirebase: Boolean = false
)

data class DatingCheckItem(
    val hour: String = "",
    val lotDate: String = "",
    val expirationDate: String = "",
    val isCompliant: Boolean = true,
    val observations: String = ""
)

data class DatingInspectionItem(
    val hour: String = "",
    val inspectionCode: String = "",
    val compliant: Boolean = true,
    val description: String = ""
)

data class StopCode(
    val code: String,
    val category: String,
    val description: String
)
