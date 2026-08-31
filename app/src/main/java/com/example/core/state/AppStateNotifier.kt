package com.example.core.state

import com.example.core.scheduler.RecurringTransaction
import com.example.core.scheduler.RecurringType
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Date
import java.util.UUID

enum class NotificationSeverity {
    INFO,
    SUCCESS,
    WARNING,
    ALERT
}

data class AppNotification(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val message: String,
    val severity: NotificationSeverity = NotificationSeverity.INFO,
    val timestamp: Date = Date(),
    val isRead: Boolean = false,
    val actionLabel: String? = null
)

sealed class AppEvent {
    data class ScheduledTaskExecuted(
        val recurringId: String,
        val title: String,
        val amount: Double,
        val type: RecurringType,
        val infaqAllocated: Double,
        val timestamp: Date = Date()
    ) : AppEvent()

    data class ScheduledTaskFailed(
        val recurringId: String,
        val title: String,
        val reason: String
    ) : AppEvent()

    data class BudgetCapExceeded(
        val categoryName: String,
        val currentSpent: Double,
        val budgetLimit: Double
    ) : AppEvent()

    data class SedekahSubuhStreakUpdated(
        val currentStreak: Int,
        val amount: Double
    ) : AppEvent()

    data class VaultDisbursementLogged(
        val recipient: String,
        val amount: Double,
        val asnaf: String
    ) : AppEvent()
}

/**
 * AppStateNotifier handles app-level notifications and coordinates scheduled recurring tasks,
 * broadcasting events to subscribers and keeping a persistent log of automated ledger events.
 */
class AppStateNotifier {

    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    private val _eventFlow = MutableSharedFlow<AppEvent>(extraBufferCapacity = 64)
    val eventFlow: SharedFlow<AppEvent> = _eventFlow.asSharedFlow()

    private val _latestAlert = MutableStateFlow<AppNotification?>(null)
    val latestAlert: StateFlow<AppNotification?> = _latestAlert.asStateFlow()

    /**
     * Post a new notification to the app state
     */
    fun notify(
        title: String,
        message: String,
        severity: NotificationSeverity = NotificationSeverity.INFO,
        actionLabel: String? = null
    ): AppNotification {
        val notification = AppNotification(
            title = title,
            message = message,
            severity = severity,
            timestamp = Date(),
            actionLabel = actionLabel
        )
        _notifications.update { listOf(notification) + it.take(49) }
        _latestAlert.value = notification
        return notification
    }

    /**
     * Clear the latest active alert popup banner
     */
    fun dismissLatestAlert() {
        _latestAlert.value = null
    }

    /**
     * Mark a specific notification as read
     */
    fun markAsRead(notificationId: String) {
        _notifications.update { list ->
            list.map { if (it.id == notificationId) it.copy(isRead = true) else it }
        }
    }

    /**
     * Clear all notifications
     */
    fun clearAll() {
        _notifications.value = emptyList()
        _latestAlert.value = null
    }

    /**
     * Handle execution and dispatching of scheduled recurring tasks
     */
    fun handleScheduledTasks(
        dueTasks: List<RecurringTransaction>,
        executeCallback: (RecurringTransaction) -> Pair<Boolean, Double> // returns Pair(success, infaqPurified)
    ): Int {
        var processedCount = 0

        for (task in dueTasks) {
            try {
                val (success, infaqPurified) = executeCallback(task)
                if (success) {
                    processedCount++
                    val event = AppEvent.ScheduledTaskExecuted(
                        recurringId = task.id,
                        title = task.title,
                        amount = task.amount,
                        type = task.type,
                        infaqAllocated = infaqPurified
                    )
                    _eventFlow.tryEmit(event)

                    val typeLabel = if (task.type == RecurringType.INCOME) "Pemasukan Ujrah/Gaji" else "Pembayaran Tagihan"
                    notify(
                        title = "Otomasi Terjadwal Berhasil",
                        message = "$typeLabel '${task.title}' sebesar Rp ${formatAmount(task.amount)} berhasil diproses." +
                                if (infaqPurified > 0) " Infaq disucikan: Rp ${formatAmount(infaqPurified)}." else "",
                        severity = NotificationSeverity.SUCCESS
                    )
                } else {
                    val event = AppEvent.ScheduledTaskFailed(
                        recurringId = task.id,
                        title = task.title,
                        reason = "Gagal memproses transaksi otomatis."
                    )
                    _eventFlow.tryEmit(event)
                    notify(
                        title = "Otomasi Gagal",
                        message = "Jadwal '${task.title}' gagal dieksekusi.",
                        severity = NotificationSeverity.ALERT
                    )
                }
            } catch (e: Exception) {
                notify(
                    title = "Kesalahan Eksekusi Jadwal",
                    message = "Error pada '${task.title}': ${e.message}",
                    severity = NotificationSeverity.ALERT
                )
            }
        }

        return processedCount
    }

    private fun formatAmount(amount: Double): String {
        return java.text.NumberFormat.getNumberInstance(java.util.Locale("id", "ID")).format(amount)
    }
}
