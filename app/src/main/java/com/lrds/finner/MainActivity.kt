package com.lrds.finner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lrds.finner.ui.dashboard.MonthlyDashboardScreen
import com.lrds.finner.ui.dashboard.MonthlyDashboardViewModel
import com.lrds.finner.ui.theme.FinnerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val appContainer = (application as FinnerApplication).appContainer

        setContent {
            FinnerTheme {
                val dashboardViewModel: MonthlyDashboardViewModel = viewModel(
                    factory = MonthlyDashboardViewModel.provideFactory(
                        repository = appContainer.financeRepository,
                        observeMonthlyDashboardUseCase = appContainer.observeMonthlyDashboardUseCase,
                        authManager = appContainer.authManager
                    )
                )
                MonthlyDashboardScreen(viewModel = dashboardViewModel)
            }
        }
    }
}
