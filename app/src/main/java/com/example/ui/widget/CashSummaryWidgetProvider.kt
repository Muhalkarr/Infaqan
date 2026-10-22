package com.example.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.core.database.AmanahDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class CashSummaryWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val pendingIntentAdd = PendingIntent.getActivity(
            context,
            101,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("NAVIGATE_TO", "ADD_TRANSACTION_EXPENSE")
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val pendingIntentOpen = PendingIntent.getActivity(
            context,
            102,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("NAVIGATE_TO", "DASHBOARD")
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_cash_summary).apply {
                setOnClickPendingIntent(R.id.widget_cash_add_btn, pendingIntentAdd)
                setOnClickPendingIntent(R.id.widget_cash_root, pendingIntentOpen)
            }
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        // Asynchronously calculate total balance from local Room database
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AmanahDatabase.getDatabase(context)
                val entries = db.journalDao().getAllEntries()
                var totalBalance = 0.0
                val assetPrefixes = setOf("acc_cash", "acc_bank", "acc_ewallet", "acc_gold", "acc_savings", "acc_other_asset", "acc_bank_konven", "acc_bank_special")
                for (entry in entries) {
                    try {
                        val arr = org.json.JSONArray(entry.linesJson)
                        for (i in 0 until arr.length()) {
                            val obj = arr.getJSONObject(i)
                            val accId = obj.optString("accountId")
                            if (assetPrefixes.contains(accId) || accId.startsWith("acc_wallet_") || accId.startsWith("acc_custom_")) {
                                val debit = obj.optDouble("debit", 0.0)
                                val credit = obj.optDouble("credit", 0.0)
                                totalBalance += (debit - credit)
                            }
                        }
                    } catch (_: Exception) {}
                }
                totalBalance = totalBalance.coerceAtLeast(0.0)
                val nf = NumberFormat.getNumberInstance(Locale("id", "ID"))
                val formattedBalance = "Rp ${nf.format(totalBalance.toLong())}"

                for (appWidgetId in appWidgetIds) {
                    val views = RemoteViews(context.packageName, R.layout.widget_cash_summary).apply {
                        setTextViewText(R.id.widget_cash_balance, formattedBalance)
                        setOnClickPendingIntent(R.id.widget_cash_add_btn, pendingIntentAdd)
                        setOnClickPendingIntent(R.id.widget_cash_root, pendingIntentOpen)
                    }
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            } catch (_: Exception) {
                // Ignore background widget fetch errors
            }
        }
    }
}
