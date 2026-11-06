package com.gymflow.core.updater.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.gymflow.core.updater.AppUpdateInfo
import com.gymflow.core.updater.AppUpdateState
import java.io.File
import java.util.Locale

@Composable
fun AppUpdateDialog(
    state: AppUpdateState,
    onDownloadClick: (AppUpdateInfo) -> Unit,
    onInstallClick: (File) -> Unit,
    onRetryClick: () -> Unit = {},
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (state is AppUpdateState.Idle) return

    val context = LocalContext.current

    Dialog(
        onDismissRequest = {
            // Allow dismiss unless downloading
            if (state !is AppUpdateState.Downloading) {
                onDismiss()
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = state !is AppUpdateState.Downloading,
            dismissOnClickOutside = state !is AppUpdateState.Downloading,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 24.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF101014))
                .border(1.dp, Color(0xFF272732), RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            when (state) {
                is AppUpdateState.Checking -> {
                    CheckingContent()
                }

                is AppUpdateState.UpdateAvailable -> {
                    UpdateAvailableContent(
                        info = state.info,
                        onDownload = { onDownloadClick(state.info) },
                        onDismiss = onDismiss,
                        onViewOnGitHub = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(state.info.releasePageUrl))
                            context.startActivity(intent)
                        }
                    )
                }

                is AppUpdateState.Downloading -> {
                    DownloadingContent(
                        info = state.info,
                        progress = state.progress,
                        downloadedBytes = state.downloadedBytes,
                        totalBytes = state.totalBytes,
                        isDeltaPatch = state.isDeltaPatch
                    )
                }

                is AppUpdateState.Patching -> {
                    PatchingContent(
                        info = state.info,
                        progress = state.progress
                    )
                }

                is AppUpdateState.ReadyToInstall -> {
                    ReadyToInstallContent(
                        info = state.info,
                        onInstall = { onInstallClick(state.apkFile) }
                    )
                }

                is AppUpdateState.UpToDate -> {
                    UpToDateContent(
                        version = state.currentVersion,
                        onDismiss = onDismiss
                    )
                }

                is AppUpdateState.Error -> {
                    ErrorContent(
                        message = state.message,
                        onRetry = onRetryClick,
                        onDismiss = onDismiss
                    )
                }

                AppUpdateState.Idle -> Unit
            }
        }
    }
}

@Composable
private fun CheckingContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CircularProgressIndicator(
            color = Color(0xFF22C55E),
            strokeWidth = 3.dp,
            modifier = Modifier.size(44.dp)
        )
        Text(
            text = "Checking for Updates...",
            color = Color.White,
            fontSize = 18.dp.value.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Querying GitHub Releases for the latest GymFlow build.",
            color = Color(0xFF94A3B8),
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun UpdateAvailableContent(
    info: AppUpdateInfo,
    onDownload: () -> Unit,
    onDismiss: () -> Unit,
    onViewOnGitHub: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Brand Header & Badge
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color(0xFF181822))
                .border(1.dp, Color(0xFF272735), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.SystemUpdate,
                contentDescription = null,
                tint = Color(0xFF22C55E),
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Badge
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(Color(0x2222C55E))
                .border(1.dp, Color(0x5522C55E), CircleShape)
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text(
                text = "● UPDATE AVAILABLE",
                color = Color(0xFF22C55E),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = info.releaseTitle,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Version Comparison Pill
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF1E1E28))
                .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Text(
                text = "v${info.currentVersion}",
                color = Color(0xFF94A3B8),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Text(text = "→", color = Color(0xFF64748B), fontSize = 12.sp)
            Text(
                text = "v${info.latestVersion}",
                color = Color(0xFF22C55E),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            if (info.fileSizeBytes > 0L) {
                Text(
                    text = "· ${formatBytes(info.fileSizeBytes)}",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Changelog Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 160.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF0C0C10))
                .border(1.dp, Color(0xFF1E1E28), RoundedCornerShape(14.dp))
                .padding(14.dp)
        ) {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = "WHAT'S NEW",
                    color = Color(0xFF64748B),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = info.changelog,
                    color = Color(0xFFCBD5E1),
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Actions
        Button(
            onClick = onDownload,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF22C55E),
                contentColor = Color(0xFF052E16)
            ),
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Download & Install", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onViewOnGitHub) {
                Icon(Icons.Default.OpenInBrowser, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("View on GitHub", color = Color(0xFF94A3B8), fontSize = 12.sp)
            }

            TextButton(onClick = onDismiss) {
                Text("Later", color = Color(0xFF94A3B8), fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun DownloadingContent(
    info: AppUpdateInfo,
    progress: Float,
    downloadedBytes: Long,
    totalBytes: Long,
    isDeltaPatch: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color(0xFF181822))
                .border(1.dp, Color(0xFF272735), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                progress = { progress },
                color = if (isDeltaPatch) Color(0xFF38BDF8) else Color(0xFF22C55E),
                trackColor = Color(0xFF272735),
                strokeWidth = 3.dp,
                modifier = Modifier.size(44.dp)
            )
        }

        if (isDeltaPatch) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFF0369A1).copy(alpha = 0.2f))
                    .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f), RoundedCornerShape(50))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "⚡ FAST DELTA UPDATE",
                    color = Color(0xFF38BDF8),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
            }
        }

        Text(
            text = if (isDeltaPatch) "Downloading Update Patch..." else "Downloading v${info.latestVersion}...",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        LinearProgressIndicator(
            progress = { progress },
            color = if (isDeltaPatch) Color(0xFF38BDF8) else Color(0xFF22C55E),
            trackColor = Color(0xFF1E1E28),
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
        )

        val progressText = if (totalBytes > 0L) {
            val percent = (progress * 100).toInt()
            "$percent% · ${formatBytes(downloadedBytes)} / ${formatBytes(totalBytes)}"
        } else {
            "${formatBytes(downloadedBytes)} downloaded"
        }

        Text(
            text = progressText,
            color = Color(0xFF94A3B8),
            fontSize = 12.sp
        )

        Text(
            text = if (isDeltaPatch) {
                "Only downloading changes. Binary delta patching reduces download size by ~85%."
            } else {
                "The system installer will launch automatically once the download completes."
            },
            color = Color(0xFF64748B),
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PatchingContent(
    info: AppUpdateInfo,
    progress: Float
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color(0xFF181822))
                .border(1.dp, Color(0xFF272735), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                progress = { progress },
                color = Color(0xFF38BDF8),
                trackColor = Color(0xFF272735),
                strokeWidth = 3.dp,
                modifier = Modifier.size(44.dp)
            )
        }

        Text(
            text = "Applying Delta Patch...",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        LinearProgressIndicator(
            progress = { progress },
            color = Color(0xFF38BDF8),
            trackColor = Color(0xFF1E1E28),
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
        )

        Text(
            text = "Reconstructing v${info.latestVersion} APK from local base app...",
            color = Color(0xFF94A3B8),
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ReadyToInstallContent(
    info: AppUpdateInfo,
    onInstall: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color(0x2222C55E))
                .border(2.dp, Color(0xFF22C55E), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF22C55E),
                modifier = Modifier.size(36.dp)
            )
        }

        Text(
            text = "Download Complete!",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "GymFlow v${info.latestVersion} is ready to be installed.",
            color = Color(0xFF94A3B8),
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )

        Button(
            onClick = onInstall,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF22C55E),
                contentColor = Color(0xFF052E16)
            ),
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Install Now ▶", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}

@Composable
private fun UpToDateContent(
    version: String,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color(0xFF181822))
                .border(1.dp, Color(0xFF272735), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.FitnessCenter,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }

        Text(
            text = "You're on the Latest Version",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "GymFlow v$version is up to date with the latest GitHub release.",
            color = Color(0xFF94A3B8),
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )

        Button(
            onClick = onDismiss,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
        ) {
            Text("Got It", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color(0x22EF4444))
                .border(1.dp, Color(0xFFEF4444), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = Color(0xFFEF4444),
                modifier = Modifier.size(36.dp)
            )
        }

        Text(
            text = "Update Notice",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = message,
            color = Color(0xFFEF4444),
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1E1E28),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
            ) {
                Text("Dismiss", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            }

            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF22C55E),
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
            ) {
                Text("Retry", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

private fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 MB"
    val mb = bytes.toDouble() / (1024 * 1024)
    return String.format(Locale.US, "%.1f MB", mb)
}

