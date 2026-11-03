package com.gymflow.feature.member.measurements

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gymflow.core.components.GymFlowCard
import com.gymflow.core.components.GymFlowPrimaryButton
import com.gymflow.core.components.GymFlowSecondaryButton
import com.gymflow.core.components.GymFlowTextField
import com.gymflow.core.components.GymFlowTopBar
import com.gymflow.core.theme.MemberBg
import com.gymflow.core.theme.MemberCardBorder
import com.gymflow.core.theme.MemberSurfaceSubtle
import com.gymflow.core.theme.MemberTextPrimary
import com.gymflow.core.theme.MemberTextSecondary
import com.gymflow.core.theme.MemberTextTertiary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BodyMeasurementsScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: MeasurementsViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MemberBg)
    ) {
        GymFlowTopBar(
            title = "Body Measurements",
            onBackClick = onNavigateBack,
            actions = {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MemberSurfaceSubtle)
                        .clickable { viewModel.openLogDialog() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Log Measurements",
                        tint = MemberTextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Latest Measurements Headline
            Text(
                text = "Current Measurements",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = MemberTextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            val dateLabel = state.latestMeasurement?.let {
                SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date(it.recordedAtEpochMs))
            } ?: "No records yet"
            Text(
                text = "Last recorded: $dateLabel",
                fontSize = 13.sp,
                color = MemberTextSecondary
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Measurement cards grid
            state.deltas.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowItems.forEach { delta ->
                        MeasurementCard(
                            delta = delta,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (rowItems.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))

            // History Log Section
            Text(
                text = "Measurement History",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = MemberTextPrimary
            )
            Spacer(modifier = Modifier.height(10.dp))

            state.history.forEach { entry ->
                val dateStr = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(entry.recordedAtEpochMs))
                GymFlowCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    contentPadding = 14.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Straighten,
                                contentDescription = null,
                                tint = MemberTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = dateStr,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MemberTextPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = MemberCardBorder, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        HistoryItem(label = "Chest", value = "${entry.chestCm} cm")
                        HistoryItem(label = "Waist", value = "${entry.waistCm} cm")
                        HistoryItem(label = "Arms", value = "${entry.armsCm} cm")
                        HistoryItem(label = "Thighs", value = "${entry.thighsCm} cm")
                        HistoryItem(label = "Hips", value = "${entry.hipsCm} cm")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (state.isLogDialogOpen) {
        val latest = state.latestMeasurement
        LogMeasurementsDialog(
            initialChest = latest?.chestCm ?: 100f,
            initialWaist = latest?.waistCm ?: 80f,
            initialArms = latest?.armsCm ?: 38f,
            initialThighs = latest?.thighsCm ?: 58f,
            initialHips = latest?.hipsCm ?: 96f,
            onDismiss = { viewModel.closeLogDialog() },
            onConfirm = { chest, waist, arms, thighs, hips ->
                viewModel.logNewMeasurements(chest, waist, arms, thighs, hips)
            }
        )
    }
}

@Composable
private fun MeasurementCard(
    delta: MeasurementDelta,
    modifier: Modifier = Modifier
) {
    GymFlowCard(
        modifier = modifier,
        contentPadding = 14.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = delta.partName,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MemberTextSecondary
            )

            if (delta.deltaCm != 0f) {
                val isPositive = delta.deltaCm > 0
                val sign = if (isPositive) "+" else ""
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isPositive) Color(0xFFEFF6FF) else Color(0xFFF3F4F6))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "$sign${delta.deltaCm} cm",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isPositive) Color(0xFF1D4ED8) else Color(0xFF4B5563)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "${delta.currentValueCm} cm",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MemberTextPrimary
        )
    }
}

@Composable
private fun HistoryItem(
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = MemberTextTertiary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MemberTextPrimary
        )
    }
}

@Composable
private fun LogMeasurementsDialog(
    initialChest: Float,
    initialWaist: Float,
    initialArms: Float,
    initialThighs: Float,
    initialHips: Float,
    onDismiss: () -> Unit,
    onConfirm: (Float, Float, Float, Float, Float) -> Unit
) {
    var chestText by remember { mutableStateOf(initialChest.toString()) }
    var waistText by remember { mutableStateOf(initialWaist.toString()) }
    var armsText by remember { mutableStateOf(initialArms.toString()) }
    var thighsText by remember { mutableStateOf(initialThighs.toString()) }
    var hipsText by remember { mutableStateOf(initialHips.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Log Body Measurements",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MemberTextPrimary
            )
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = "Record tape measurements in centimeters (cm).",
                    fontSize = 13.sp,
                    color = MemberTextSecondary
                )
                Spacer(modifier = Modifier.height(14.dp))

                GymFlowTextField(
                    value = chestText,
                    onValueChange = { chestText = it },
                    label = "Chest (cm)",
                    suffixText = "cm",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                Spacer(modifier = Modifier.height(8.dp))

                GymFlowTextField(
                    value = waistText,
                    onValueChange = { waistText = it },
                    label = "Waist (cm)",
                    suffixText = "cm",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                Spacer(modifier = Modifier.height(8.dp))

                GymFlowTextField(
                    value = armsText,
                    onValueChange = { armsText = it },
                    label = "Arms / Biceps (cm)",
                    suffixText = "cm",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                Spacer(modifier = Modifier.height(8.dp))

                GymFlowTextField(
                    value = thighsText,
                    onValueChange = { thighsText = it },
                    label = "Thighs (cm)",
                    suffixText = "cm",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                Spacer(modifier = Modifier.height(8.dp))

                GymFlowTextField(
                    value = hipsText,
                    onValueChange = { hipsText = it },
                    label = "Hips (cm)",
                    suffixText = "cm",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
        },
        confirmButton = {
            GymFlowPrimaryButton(
                text = "Save",
                onClick = {
                    val chest = chestText.toFloatOrNull() ?: initialChest
                    val waist = waistText.toFloatOrNull() ?: initialWaist
                    val arms = armsText.toFloatOrNull() ?: initialArms
                    val thighs = thighsText.toFloatOrNull() ?: initialThighs
                    val hips = hipsText.toFloatOrNull() ?: initialHips
                    onConfirm(chest, waist, arms, thighs, hips)
                }
            )
        },
        dismissButton = {
            GymFlowSecondaryButton(
                text = "Cancel",
                onClick = onDismiss
            )
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp)
    )
}
