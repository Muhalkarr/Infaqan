package com.example.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AmanahNumpad(
    onNumberClick: (Int) -> Unit,
    onBackspaceClick: () -> Unit,
    onOperatorClick: (String) -> Unit,
    onDoneClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val buttonModifier = Modifier
        .aspectRatio(1f)
        .padding(4.dp)
        .clip(RoundedCornerShape(12.dp))

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val rows = listOf(
            listOf("7", "8", "9", "÷"),
            listOf("4", "5", "6", "×"),
            listOf("1", "2", "3", "-"),
            listOf("C", "0", "000", "+")
        )

        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { key ->
                    Box(
                        modifier = buttonModifier
                            .weight(1f)
                            .background(
                                if (key in listOf("+", "-", "×", "÷")) MaterialTheme.colorScheme.primaryContainer
                                else if (key == "C") MaterialTheme.colorScheme.errorContainer
                                else MaterialTheme.colorScheme.surface
                            )
                            .clickable {
                                when (key) {
                                    in "0".."9" -> onNumberClick(key.toInt())
                                    "000" -> {
                                        onNumberClick(0)
                                        onNumberClick(0)
                                        onNumberClick(0)
                                    }
                                    "C" -> onBackspaceClick() // Clear all or backspace depending on implementation
                                    in listOf("+", "-", "×", "÷") -> onOperatorClick(key)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = key,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (key in listOf("+", "-", "×", "÷")) MaterialTheme.colorScheme.onPrimaryContainer
                            else if (key == "C") MaterialTheme.colorScheme.onErrorContainer
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
        
        // Bottom Row for Backspace and Done
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable { onBackspaceClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = "Hapus",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Button(
                onClick = onDoneClick,
                modifier = Modifier
                    .weight(3f)
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Selesai / Simpan", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
