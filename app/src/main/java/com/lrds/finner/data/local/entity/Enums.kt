package com.lrds.finner.data.local.entity

enum class CurrencyCode(val symbol: String, val code: String) {
    EUR("€", "EUR"),
    BRL("R$", "BRL")
}

enum class TransactionType(val label: String) {
    INCOME("Inflows (€)"),
    EXPENSE_LOCAL("Outflows (€)"),
    EXPENSE_BRAZIL("Brazil (R$)")
}

enum class EnvelopeType(val displayName: String, val defaultPercentage: Int) {
    BILLS("Bills", 50),
    RESERVE("Reserve", 20),
    JOINT("Joint", 15),
    PERSONAL("Personal", 15);

    companion object {
        val DEFAULT_BUCKET_NAMES: List<String> = entries.map { it.displayName }
    }
}

enum class GoalPriority(val label: String) {
    LONG_TERM("Long-Term Milestone"),
    MEDIUM_TERM("Medium-Term Target"),
    SHORT_TERM("Wishlist / Short-Term")
}

enum class SyncStatus {
    SYNCED,
    PENDING_UPLOAD,
    PENDING_DELETE
}
