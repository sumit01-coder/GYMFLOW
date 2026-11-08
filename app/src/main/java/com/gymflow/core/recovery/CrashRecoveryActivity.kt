package com.gymflow.core.recovery

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gymflow.BuildConfig
import com.gymflow.app.MainActivity
import com.gymflow.core.theme.GymFlowBlack
import com.gymflow.core.theme.GymFlowBorder
import com.gymflow.core.theme.GymFlowGreen
import com.gymflow.core.theme.GymFlowWhite
import kotlinx.coroutines.launch

class CrashRecoveryActivity : ComponentActivity() {

    companion object {
        const val EXTRA_CRASH_TITLE = "extra_crash_title"
        const val EXTRA_CRASH_MESSAGE = "extra_crash_message"
        const val EXTRA_CRASH_STACKTRACE = "extra_crash_stacktrace"
        const val EXTRA_TIMESTAMP = "extra_timestamp"

        fun createIntent(
            context: Context,
            title: String,
            message: String?,
            stackTrace: String
        ): Intent {
            return Intent(context, CrashRecoveryActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                putExtra(EXTRA_CRASH_TITLE, title)
                putExtra(EXTRA_CRASH_MESSAGE, message ?: "Unexpected runtime termination")
                putExtra(EXTRA_CRASH_STACKTRACE, stackTrace)
                putExtra(EXTRA_TIMESTAMP, System.currentTimeMillis())
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val crashTitle = intent.getStringExtra(EXTRA_CRASH_TITLE) ?: "Unexpected App Interruption"
        val crashMessage = intent.getStringExtra(EXTRA_CRASH_MESSAGE) ?: "An error occurred while launching GymFlow."
        val crashStackTrace = intent.getStringExtra(EXTRA_CRASH_STACKTRACE) ?: "No stack trace available."

        setContent {
            RecoveryScreen(
                crashTitle = crashTitle,
                crashMessage = crashMessage,
                crashStackTrace = crashStackTrace,
                onRestartApp = { restartApplication() },
                onSafeModeReset = { performResetAndRestart() },
                onCopyDiagnostics = { copyToClipboard(crashTitle, crashMessage, crashStackTrace) }
            )
        }
    }

    private fun restartApplication() {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        startActivity(intent)
        finishAffinity()
        android.os.Process.killProcess(android.os.Process.myPid())
    }

    private fun performResetAndRestart() {
        SafeModeResetManager.performSafeModeReset(this)
        Toast.makeText(this, "Local cache reset. Restarting GymFlow...", Toast.LENGTH_SHORT).show()
        restartApplication()
    }

    private fun copyToClipboard(title: String, message: String, stackTrace: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val text = buildString {
            appendLine("--- GYMFLOW CRASH DIAGNOSTICS ---")
            appendLine("App Version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
            appendLine("Android OS: ${android.os.Build.VERSION.RELEASE} (SDK ${android.os.Build.VERSION.SDK_INT})")
            appendLine("Device: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}")
            appendLine("Timestamp: ${System.currentTimeMillis()}")
            appendLine("Title: $title")
            appendLine("Message: $message")
            appendLine("\n--- STACK TRACE ---")
            appendLine(stackTrace)
        }
        val clip = ClipData.newPlainText("GymFlow Diagnostics", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "Crash diagnostics copied to clipboard", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun RecoveryScreen(
    crashTitle: String,
    crashMessage: String,
    crashStackTrace: String,
    onRestartApp: () -> Unit,
    onSafeModeReset: () -> Unit,
    onCopyDiagnostics: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isCheckingUpdate by remember { mutableStateOf(true) }
    var updateInfo by remember { mutableStateOf<EmergencyUpdateInfo?>(null) }
    var updateCheckError by remember { mutableStateOf<String?>(null) }

    var downloadProgress by remember { mutableStateOf<Int?>(null) }
    var downloadError by remember { mutableStateOf<String?>(null) }
    var isDownloaded by remember { mutableStateOf(false) }

    var showDiagnostics by remember { mutableStateOf(false) }

    val context = androidx.compose.ui.platform.LocalContext.current

    // Automatically check for emergency updates on launch
    LaunchedEffect(Unit) {
        val result = EmergencyUpdateEngine.checkLatestRelease()
        isCheckingUpdate = false
        result.onSuccess { info ->
            updateInfo = info
        }.onFailure { error ->
            updateCheckError = error.message ?: "Could not connect to update service"
        }
    }

    fun startDownloadAndInstall(info: EmergencyUpdateInfo) {
        downloadProgress = 0
        downloadError = null
        coroutineScope.launch {
            val result = EmergencyUpdateEngine.downloadAndInstallApk(
                context = context,
                downloadUrl = info.downloadUrl,
                targetVersion = info.versionName
            ) { percent ->
                downloadProgress = percent
            }

            result.onSuccess {
                isDownloaded = true
                downloadProgress = 100
            }.onFailure { error ->
                downloadError = error.message ?: "Failed downloading update"
                downloadProgress = null
            }
        }
    }

    val cardShape = RoundedCornerShape(16.dp)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF0F1115) // Deep Slate Black
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            // Header: GymFlow Branding + Recovery Tag
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(GymFlowBlack)
                            .border(1.dp, GymFlowBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "GYMFLOW",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = GymFlowWhite
                        )
                        Text(
                            text = "CRASH RECOVERY & REPAIR",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            color = Color(0xFFF59E0B)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF1E2128))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "v${BuildConfig.VERSION_NAME}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GymFlowWhite.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Main Alert Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(cardShape)
                    .background(Color(0xFF161920))
                    .border(1.dp, Color(0xFF262A34), cardShape)
                    .padding(20.dp)
            ) {
                Column {
                    Text(
                        text = "App Interrupted",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = GymFlowWhite
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "GymFlow encountered an unexpected error while launching. Our self-healing engine has preserved your account and cloud workout data.",
                        fontSize = 13.sp,
                        color = GymFlowWhite.copy(alpha = 0.75f),
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Brief Error Tag
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1F1212))
                            .border(1.dp, Color(0xFF451A1A), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = crashMessage,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFF87171),
                            maxLines = 3
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // EMERGENCY IN-APP UPDATER SECTION
            Text(
                text = "ONLINE REPAIR & UPDATES",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                color = GymFlowWhite.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(cardShape)
                    .background(Color(0xFF161920))
                    .border(1.dp, Color(0xFF262A34), cardShape)
                    .padding(20.dp)
            ) {
                Column {
                    if (isCheckingUpdate) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = GymFlowGreen
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Checking for emergency bug fix release...",
                                fontSize = 13.sp,
                                color = GymFlowWhite.copy(alpha = 0.8f)
                            )
                        }
                    } else if (updateInfo != null && updateInfo!!.isNewer) {
                        val info = updateInfo!!
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(GymFlowGreen.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = null,
                                    tint = GymFlowGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Update Available: ${info.versionName}",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GymFlowGreen
                                )
                                Text(
                                    text = "Contains fixes for recent crashes (%.1f MB)".format(info.apkSizeMb),
                                    fontSize = 12.sp,
                                    color = GymFlowWhite.copy(alpha = 0.65f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        if (downloadProgress != null) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = if (downloadProgress!! < 100) "Downloading update..." else "Launching installer...",
                                        fontSize = 12.sp,
                                        color = GymFlowWhite.copy(alpha = 0.8f)
                                    )
                                    Text(
                                        text = "${downloadProgress}%",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GymFlowGreen
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { downloadProgress!! / 100f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = GymFlowGreen,
                                    trackColor = Color(0xFF262A34)
                                )
                            }
                        } else {
                            // Primary Download & Fix Button
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(GymFlowGreen)
                                    .clickable { startDownloadAndInstall(info) }
                                    .padding(vertical = 14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Download,
                                        contentDescription = null,
                                        tint = GymFlowBlack,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Download & Repair App",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black,
                                        color = GymFlowBlack
                                    )
                                }
                            }
                        }

                        if (downloadError != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Error: $downloadError",
                                fontSize = 11.sp,
                                color = Color(0xFFF87171)
                            )
                        }
                    } else {
                        // Already latest or offline
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = GymFlowGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (updateCheckError != null) "Update check offline. You can still reset local cache." else "You are running the latest version available.",
                                fontSize = 13.sp,
                                color = GymFlowWhite.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // RECOVERY ACTIONS SECTION
            Text(
                text = "SAFE MODE & REPAIR OPTIONS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                color = GymFlowWhite.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Reset Local Cache Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1E2128))
                    .border(1.dp, Color(0xFF333844), RoundedCornerShape(12.dp))
                    .clickable { onSafeModeReset() }
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2A2E38)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = GymFlowWhite,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Reset Corrupted Local Cache",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = GymFlowWhite
                        )
                        Text(
                            text = "Purges local SQLite schema. Cloud account & history remain safe.",
                            fontSize = 11.sp,
                            color = GymFlowWhite.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Normal Restart Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1E2128))
                    .border(1.dp, Color(0xFF333844), RoundedCornerShape(12.dp))
                    .clickable { onRestartApp() }
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2A2E38)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = null,
                            tint = GymFlowWhite,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Relaunch Application",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = GymFlowWhite
                        )
                        Text(
                            text = "Try launching GymFlow normally again.",
                            fontSize = 11.sp,
                            color = GymFlowWhite.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // TECHNICAL DIAGNOSTICS ACCORDION
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(cardShape)
                    .background(Color(0xFF161920))
                    .border(1.dp, Color(0xFF262A34), cardShape)
                    .clickable { showDiagnostics = !showDiagnostics }
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.BugReport,
                                contentDescription = null,
                                tint = GymFlowWhite.copy(alpha = 0.6f),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Technical Diagnostics Log",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = GymFlowWhite.copy(alpha = 0.8f)
                            )
                        }

                        Icon(
                            imageVector = if (showDiagnostics) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = GymFlowWhite.copy(alpha = 0.6f)
                        )
                    }

                    if (showDiagnostics) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0C0E12))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = crashStackTrace,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFFA1A1AA),
                                lineHeight = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF262A34))
                                .clickable { onCopyDiagnostics() }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = null,
                                    tint = GymFlowWhite,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Copy Error Details",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GymFlowWhite
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

