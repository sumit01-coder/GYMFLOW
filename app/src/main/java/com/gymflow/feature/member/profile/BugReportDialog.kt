package com.gymflow.feature.member.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gymflow.core.components.GymFlowButton
import com.gymflow.core.components.GymFlowSecondaryButton
import com.gymflow.core.telemetry.GymFlowBugReporter
import com.gymflow.core.theme.GymFlowBlack
import com.gymflow.core.theme.GymFlowGreen
import com.gymflow.core.theme.MemberCardBorder
import com.gymflow.core.theme.MemberTextPrimary
import com.gymflow.core.theme.MemberTextSecondary
import com.gymflow.core.theme.MemberTextTertiary

@Composable
fun BugReportDialog(
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    var description by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var isSubmitted by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = {
            if (!isSubmitting) onDismissRequest()
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.BugReport,
                    contentDescription = null,
                    tint = GymFlowBlack,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isSubmitted) "Report Received" else "Report a Bug / Issue",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MemberTextPrimary
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (isSubmitted) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = GymFlowGreen,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Diagnostic telemetry and report have been sent to Firebase. Thank you!",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MemberTextPrimary
                        )
                    }
                } else {
                    Text(
                        text = "Describe what happened (e.g. update issue, display glitch, or crash). System diagnostics, device info, and error logs will be securely attached.",
                        fontSize = 12.sp,
                        color = MemberTextSecondary,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("What went wrong?", fontSize = 12.sp) },
                        placeholder = { Text("Steps to reproduce or what happened...", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth().height(110.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GymFlowBlack,
                            unfocusedBorderColor = MemberCardBorder,
                            cursorColor = GymFlowBlack
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email (Optional for follow-up)", fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GymFlowBlack,
                            unfocusedBorderColor = MemberCardBorder,
                            cursorColor = GymFlowBlack
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Diagnostics included: Device Model, OS Version, Available Storage, Package Signature SHA-256.",
                        fontSize = 10.sp,
                        color = MemberTextTertiary
                    )
                }
            }
        },
        confirmButton = {
            if (isSubmitted) {
                GymFlowButton(
                    text = "Close",
                    onClick = onDismissRequest
                )
            } else {
                GymFlowButton(
                    text = if (isSubmitting) "Sending..." else "Send Report",
                    onClick = {
                        if (description.isBlank()) {
                            Toast.makeText(context, "Please enter a brief description", Toast.LENGTH_SHORT).show()
                            return@GymFlowButton
                        }
                        isSubmitting = true
                        GymFlowBugReporter.reportUserFeedback(
                            userDescription = description,
                            contactEmail = email.ifBlank { null }
                        )
                        isSubmitting = false
                        isSubmitted = true
                    },
                    enabled = !isSubmitting
                )
            }
        },
        dismissButton = {
            if (!isSubmitted) {
                GymFlowSecondaryButton(
                    text = "Cancel",
                    onClick = onDismissRequest,
                    enabled = !isSubmitting
                )
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp)
    )
}

