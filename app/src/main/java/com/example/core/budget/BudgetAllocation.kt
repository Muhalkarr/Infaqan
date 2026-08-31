package com.example.core.budget

data class BudgetAllocation(
    val id: String,
    val accountId: String,
    val categoryName: String,
    val monthlyLimit: Double,
    val iconKey: String = "shopping",
    val alertThresholdPercent: Double = 0.8
) {
    fun getUsagePercentage(spentAmount: Double): Double {
        if (monthlyLimit <= 0.0) return 0.0
        return spentAmount / monthlyLimit
    }

    fun getRemainingAmount(spentAmount: Double): Double {
        return (monthlyLimit - spentAmount).coerceAtLeast(0.0)
    }

    fun isOverBudget(spentAmount: Double): Boolean {
        return spentAmount > monthlyLimit
    }

    fun isNearLimit(spentAmount: Double): Boolean {
        return spentAmount >= (monthlyLimit * alertThresholdPercent) && !isOverBudget(spentAmount)
    }
}
