package com.gymflow.feature.trainer.joincode

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gymflow.feature.trainer.components.TrainerButton
import com.gymflow.feature.trainer.components.TrainerButtonStyle
import com.gymflow.feature.trainer.components.TrainerCard
import com.gymflow.feature.trainer.components.TrainerTopBar
import com.gymflow.feature.trainer.theme.TrainerDarkBg
import com.gymflow.feature.trainer.theme.TrainerDarkBorder
import com.gymflow.feature.trainer.theme.TrainerDarkCard
import com.gymflow.feature.trainer.theme.TrainerDarkTextPrimary
import com.gymflow.feature.trainer.theme.TrainerDarkTextSecondary
import com.gymflow.feature.trainer.theme.TrainerDarkTextTertiary
import com.gymflow.feature.trainer.theme.TrainerTheme

@Composable
fun TrainerJoinCodeScreen(
    onBackClick: () -> Unit,
    onMemberApproved: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: TrainerJoinCodeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    TrainerTheme {
        Scaffold(
            topBar = {
                TrainerTopBar(
                    title = "Trainer Invitation Code",
                    subtitle = "Athlete Synchronization",
                    onBackClick = onBackClick
                )
            },
            containerColor = TrainerDarkBg
        ) { innerPadding ->
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Hero Code Card
                TrainerCard(
                    backgroundColor = TrainerDarkCard,
                    borderColor = TrainerDarkBorder,
                    contentPadding = 20.dp
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "YOUR TRAINER INVITATION CODE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TrainerDarkTextTertiary,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Large Monospace Code Display
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF09090B))
                                .border(1.dp, Color(0xFF4ADE80).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = uiState.currentCode,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = Color.White,
                                letterSpacing = 4.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Expiry Notice
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(9999.dp))
                                .background(Color(0xFF27272A))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = Color(0xFFFBBF24),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Expires in ${uiState.expiresHoursRemaining} hours",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFFBBF24)
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Actions Row: Copy & Share
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            TrainerButton(
                                text = if (uiState.isCopiedNotification) "Copied!" else "Copy Code",
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("GymFlow Code", uiState.currentCode)
                                    clipboard.setPrimaryClip(clip)
                                    viewModel.onCodeCopied()
                                },
                                style = TrainerButtonStyle.PRIMARY,
                                height = 44.dp,
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (uiState.isCopiedNotification) Icons.Default.Check else Icons.Default.ContentCopy,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            )

                            TrainerButton(
                                text = "Generate New",
                                onClick = { viewModel.generateNewCode() },
                                style = TrainerButtonStyle.SECONDARY,
                                height = 44.dp,
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Autorenew,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Gym QR Code Visual Mockup
                TrainerCard(
                    backgroundColor = TrainerDarkCard,
                    borderColor = TrainerDarkBorder,
                    contentPadding = 16.dp
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Instant Gym Floor Scanning",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TrainerDarkTextPrimary
                        )
                        Text(
                            text = "Athletes can scan from their GYMFLOW mobile app",
                            fontSize = 12.sp,
                            color = TrainerDarkTextSecondary,
                            modifier = Modifier.padding(top = 2.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Stylized QR pattern box
                        Box(
                            modifier = Modifier
                                .size(140.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val s = size.width / 5
                                // Stylized corner finders
                                drawRect(Color.Black, Offset(0f, 0f), Size(s * 1.6f, s * 1.6f))
                                drawRect(Color.White, Offset(s * 0.3f, s * 0.3f), Size(s, s))
                                drawRect(Color.Black, Offset(s * 0.5f, s * 0.5f), Size(s * 0.6f, s * 0.6f))

                                drawRect(Color.Black, Offset(size.width - s * 1.6f, 0f), Size(s * 1.6f, s * 1.6f))
                                drawRect(Color.White, Offset(size.width - s * 1.3f, s * 0.3f), Size(s, s))
                                drawRect(Color.Black, Offset(size.width - s * 1.1f, s * 0.5f), Size(s * 0.6f, s * 0.6f))

                                drawRect(Color.Black, Offset(0f, size.height - s * 1.6f), Size(s * 1.6f, s * 1.6f))
                                drawRect(Color.White, Offset(s * 0.3f, size.height - s * 1.3f), Size(s, s))
                                drawRect(Color.Black, Offset(s * 0.5f, size.height - s * 1.1f), Size(s * 0.6f, s * 0.6f))

                                // Random matrix dots
                                drawRect(Color.Black, Offset(s * 2.2f, s * 1.2f), Size(s * 0.6f, s * 0.6f))
                                drawRect(Color.Black, Offset(s * 3.2f, s * 2.4f), Size(s * 0.6f, s * 0.6f))
                                drawRect(Color.Black, Offset(s * 1.4f, s * 3.2f), Size(s * 0.6f, s * 0.6f))
                            }

                            // Center dumbbell emblem
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FitnessCenter,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Pending Connection Requests Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Connection Requests (${uiState.pendingRequests.size})",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TrainerDarkTextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (uiState.lastApprovedMemberName != null) {
                    TrainerCard(
                        backgroundColor = Color(0x264ADE80),
                        borderColor = Color(0xFF4ADE80),
                        contentPadding = 12.dp
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color(0xFF4ADE80),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Approved ${uiState.lastApprovedMemberName} — Synchronised to your studio roster!",
                                fontSize = 12.sp,
                                color = Color(0xFF4ADE80)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (uiState.pendingRequests.isEmpty()) {
                    TrainerCard(contentPadding = 16.dp) {
                        Text(
                            text = "No pending connection requests. Share your code to connect athletes.",
                            fontSize = 13.sp,
                            color = TrainerDarkTextSecondary
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        uiState.pendingRequests.forEach { req ->
                            TrainerCard(contentPadding = 16.dp) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF27272A)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Person,
                                                    contentDescription = null,
                                                    tint = TrainerDarkTextPrimary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(12.dp))

                                            Column {
                                                Text(
                                                    text = req.memberName,
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = TrainerDarkTextPrimary
                                                )
                                                Text(
                                                    text = "Goal: ${req.fitnessGoal} · ${req.currentWeightKg}kg",
                                                    fontSize = 12.sp,
                                                    color = TrainerDarkTextSecondary
                                                )
                                            }
                                        }

                                        Text(
                                            text = req.requestedAgo,
                                            fontSize = 11.sp,
                                            color = TrainerDarkTextTertiary
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        TrainerButton(
                                            text = "Approve",
                                            onClick = {
                                                viewModel.approveRequest(req.id)
                                                onMemberApproved()
                                            },
                                            style = TrainerButtonStyle.PRIMARY,
                                            height = 38.dp,
                                            modifier = Modifier.weight(1f)
                                        )

                                        TrainerButton(
                                            text = "Reject",
                                            onClick = { viewModel.rejectRequest(req.id) },
                                            style = TrainerButtonStyle.OUTLINED,
                                            height = 38.dp,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
