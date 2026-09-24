package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import com.example.ui.theme.materialYouScreenTransitionSpec
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.ui.AppScreen
import com.example.ui.ProductionViewModel
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DatingCheckScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.MachineDowntimeScreen
import com.example.ui.screens.PackagingControlScreen
import com.example.ui.screens.RecordDetailDashboardScreen
import com.example.ui.theme.FarinhasCJTheme

class MainActivity : ComponentActivity() {
    private val viewModel: ProductionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            FarinhasCJTheme(themeMode = themeMode) {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: ProductionViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearSnackbarMessage()
        }
    }

    BackHandler(enabled = currentScreen != AppScreen.Dashboard) {
        when (currentScreen) {
            is AppScreen.RecordDashboard -> viewModel.navigateTo(AppScreen.History)
            else -> viewModel.navigateTo(AppScreen.Dashboard)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    materialYouScreenTransitionSpec(targetState, initialState)
                },
                label = "M3ScreenTransition"
            ) { screen ->
                when (screen) {
                    AppScreen.Dashboard -> DashboardScreen(viewModel = viewModel)
                    AppScreen.PackagingControl -> PackagingControlScreen(viewModel = viewModel)
                    AppScreen.MachineDowntime -> MachineDowntimeScreen(viewModel = viewModel)
                    AppScreen.DatingCheck -> DatingCheckScreen(viewModel = viewModel)
                    AppScreen.History -> HistoryScreen(viewModel = viewModel)
                    is AppScreen.RecordDashboard -> RecordDetailDashboardScreen(
                        id = screen.id,
                        type = screen.type,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}
