package com.example.ui

import com.example.data.model.HistoryType

sealed class AppScreen {
    object Dashboard : AppScreen()
    object PackagingControl : AppScreen()
    object MachineDowntime : AppScreen()
    object DatingCheck : AppScreen()
    object History : AppScreen()
    data class RecordDashboard(val id: String, val type: HistoryType) : AppScreen()
}
