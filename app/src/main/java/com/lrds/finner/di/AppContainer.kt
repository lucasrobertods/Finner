package com.lrds.finner.di

import android.content.Context
import com.lrds.finner.core.model.HouseholdAuthManager
import com.lrds.finner.data.local.FinnerDatabase
import com.lrds.finner.data.remote.FirestoreSyncDataSource
import com.lrds.finner.data.repository.FinanceRepository
import com.lrds.finner.data.repository.OfflineFirstFinanceRepository
import com.lrds.finner.domain.usecase.ObserveMonthlyDashboardUseCase

class AppContainer(context: Context) {
    val authManager: HouseholdAuthManager by lazy {
        HouseholdAuthManager(context.applicationContext)
    }

    val database: FinnerDatabase by lazy {
        FinnerDatabase.getInstance(context)
    }

    val firestoreSyncDataSource: FirestoreSyncDataSource by lazy {
        FirestoreSyncDataSource(
            context = context.applicationContext,
            authManager = authManager
        )
    }

    val financeRepository: FinanceRepository by lazy {
        OfflineFirstFinanceRepository(
            database = database,
            remoteSync = firestoreSyncDataSource
        )
    }

    val observeMonthlyDashboardUseCase: ObserveMonthlyDashboardUseCase by lazy {
        ObserveMonthlyDashboardUseCase(financeRepository)
    }
}
