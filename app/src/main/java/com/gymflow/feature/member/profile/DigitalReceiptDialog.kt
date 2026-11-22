package com.gymflow.feature.member.profile

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gymflow.core.theme.GymFlowTheme
import com.gymflow.core.util.QrCodeGenerator
import com.gymflow.domain.model.MembershipOrder
import com.gymflow.domain.model.OrderStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DigitalReceiptDialog(
    order: MembershipOrder,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }
    val formattedDate = remember(order.createdAtEpochMs) {
        dateFormatter.format(Date(order.createdAtEpochMs))
    }

    var animateStamp by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        animateStamp = true
    }

    val verificationPayload = "GYMFLOW:${order.orderId}:${order.memberId}:${order.amountInr}:${order.orderStatus.name}"
    val qrBitmap = remember(verificationPayload) {
        QrCodeGenerator.generateQrCodeBitmap(verificationPayload, 200)
    }

    val statusColor = when (order.orderStatus) {
        OrderStatus.APPROVED -> Color(0xFF22C55E)
        OrderStatus.PENDING_VERIFICATION, OrderStatus.PENDING -> Color(0xFFF59E0B)
        OrderStatus.REJECTED -> Color(0xFFEF4444)
    }

    val statusLabel = when (order.orderStatus) {
        OrderStatus.APPROVED -> "PAID & VERIFIED"
        OrderStatus.PENDING_VERIFICATION, OrderStatus.PENDING -> "PENDING VERIFICATION"
        OrderStatus.REJECTED -> "REJECTED / UNPAID"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = { shareReceipt(context, order, formattedDate) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = GymFlowTheme.colors.accentActive,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Share Receipt", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = GymFlowTheme.colors.textSecondary, fontWeight = FontWeight.SemiBold)
            }
        },
        containerColor = Color(0xFF18181B),
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header: Gym Branding
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF27272A)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("GF", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color(0xFF22C55E))
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = order.gymName.ifBlank { "GymFlow Fitness Center" },
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "OFFICIAL DIGITAL MEMBERSHIP RECEIPT",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp,
                    color = GymFlowTheme.colors.textSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Animated Status Stamp
                AnimatedVisibility(
                    visible = animateStamp,
                    enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(statusColor.copy(alpha = 0.15f))
                            .border(1.dp, statusColor, RoundedCornerShape(20.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = when (order.orderStatus) {
                                    OrderStatus.APPROVED -> Icons.Default.CheckCircle
                                    OrderStatus.REJECTED -> Icons.Default.Close
                                    else -> Icons.Default.HourglassTop
                                },
                                contentDescription = null,
                                tint = statusColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = statusLabel,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Perforated Separator Line
                DashedDivider()

                Spacer(modifier = Modifier.height(14.dp))

                // Key Receipt Information Rows
                ReceiptRow(label = "Order ID", value = order.orderId, isMonospace = true)
                ReceiptRow(label = "Date & Time", value = formattedDate)
                ReceiptRow(label = "Member Name", value = order.memberName)
                ReceiptRow(label = "Membership Plan", value = order.planDurationDisplayName)
                ReceiptRow(label = "Payment Method", value = order.paymentMethod)
                if (order.utrNumber?.isNotBlank() == true) {
                    ReceiptRow(label = "UTR / Reference", value = order.utrNumber, isMonospace = true)
                }
                if (order.targetUpiId.isNotBlank()) {
                    ReceiptRow(label = "Payee UPI", value = order.targetUpiId)
                }

                Spacer(modifier = Modifier.height(14.dp))
                DashedDivider()
                Spacer(modifier = Modifier.height(14.dp))

                // Total Price Breakdown
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Total Paid", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    Text(
                        text = "₹${order.amountInr.toInt()}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF22C55E)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // QR Verification Code
                if (qrBitmap != null) {
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White)
                            .padding(6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (qrBitmap != null) {
                            Image(
                                bitmap = qrBitmap.asImageBitmap(),
                                contentDescription = "Receipt Verification QR",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Scan to verify authentic receipt",
                        fontSize = 10.sp,
                        color = Color(0xFFA1A1AA)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Thank you for training with GymFlow.\nPresent this receipt at the front desk for immediate check-in verification.",
                    fontSize = 10.sp,
                    color = Color(0xFF71717A),
                    textAlign = TextAlign.Center,
                    lineHeight = 14.sp
                )
            }
        }
    )
}

@Composable
private fun ReceiptRow(label: String, value: String, isMonospace: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFFA1A1AA)
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            fontFamily = if (isMonospace) FontFamily.Monospace else FontFamily.Default
        )
    }
}

@Composable
private fun DashedDivider() {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
    ) {
        drawLine(
            color = Color(0xFF3F3F46),
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
        )
    }
}

private fun shareReceipt(context: Context, order: MembershipOrder, formattedDate: String) {
    val text = """
        🧾 GYMFLOW OFFICIAL MEMBERSHIP RECEIPT
        -----------------------------------------
        Gym: ${order.gymName.ifBlank { "GymFlow Fitness Center" }}
        Order ID: ${order.orderId}
        Date: $formattedDate
        Member: ${order.memberName}
        Plan: ${order.planDurationDisplayName}
        Amount Paid: ₹${order.amountInr.toInt()}
        Payment Mode: ${order.paymentMethod}${if (!order.utrNumber.isNullOrBlank()) " (UTR: ${order.utrNumber})" else ""}
        Status: ${order.orderStatus.name}
        -----------------------------------------
        Verified by GymFlow Fitness Ecosystem
    """.trimIndent()

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "GymFlow Receipt - ${order.orderId}")
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "Share GymFlow Receipt"))
}
