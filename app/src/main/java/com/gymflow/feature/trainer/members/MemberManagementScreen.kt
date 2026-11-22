package com.gymflow.feature.trainer.members

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gymflow.core.whatsapp.WhatsAppMessenger
import com.gymflow.domain.model.GymMember
import com.gymflow.domain.model.MembershipDuration
import com.gymflow.domain.model.PaymentMethod
import com.gymflow.domain.model.PaymentStatus
import com.gymflow.feature.trainer.components.TrainerBottomNavBar
import com.gymflow.feature.trainer.components.TrainerBottomNavTab
import com.gymflow.feature.trainer.components.TrainerTextField
import com.gymflow.feature.trainer.components.TrainerTopBar
import com.gymflow.feature.trainer.theme.TrainerDarkBg
import com.gymflow.feature.trainer.theme.TrainerDarkBorder
import com.gymflow.feature.trainer.theme.TrainerDarkCard
import com.gymflow.feature.trainer.theme.TrainerDarkTextPrimary
import com.gymflow.feature.trainer.theme.TrainerDarkTextSecondary
import com.gymflow.feature.trainer.theme.TrainerDarkTextTertiary
import com.gymflow.feature.trainer.theme.TrainerGreenAccent
import com.gymflow.feature.trainer.theme.TrainerTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MemberManagementScreen(
    onBackClick: () -> Unit,
    onMemberClick: (String) -> Unit,
    onViewProgressClick: (String) -> Unit,
    onCreatePlanForMember: (String) -> Unit,
    onNavigateToDashboard: () -> Unit,
    onNavigateToPlans: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MemberManagementViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    TrainerTheme {
        Scaffold(
            topBar = {
                TrainerTopBar(
                    title = "Gym Member Management",
                    subtitle = "${uiState.totalMembersCount} Total Members • ${uiState.expiringCount} Expiring Soon",
                    onBackClick = onBackClick
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { viewModel.openAddMemberDialog() },
                    containerColor = TrainerGreenAccent,
                    contentColor = Color.Black,
                    shape = CircleShape
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Member")
                }
            },
            bottomBar = {
                TrainerBottomNavBar(
                    selectedTab = TrainerBottomNavTab.MEMBERS,
                    onTabSelected = { tab ->
                        when (tab) {
                            TrainerBottomNavTab.DASHBOARD -> onNavigateToDashboard()
                            TrainerBottomNavTab.MEMBERS -> {}
                            TrainerBottomNavTab.PLANS -> onNavigateToPlans()
                            TrainerBottomNavTab.ANALYTICS -> onNavigateToAnalytics()
                            TrainerBottomNavTab.SETTINGS -> onNavigateToSettings()
                        }
                    }
                )
            },
            containerColor = TrainerDarkBg
        ) { innerPadding ->
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                // Search Bar
                TrainerTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    placeholder = "Search member name, phone, coach...",
                    leadingIcon = Icons.Default.Search,
                    trailingIcon = if (uiState.searchQuery.isNotEmpty()) {
                        {
                            IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear search",
                                    tint = TrainerDarkTextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    } else null
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Filter Tabs: [All], [Expiring Soon], [Expired], [Pending Payment]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MemberFilterTab.entries.forEach { tab ->
                        val isSelected = uiState.selectedFilter == tab
                        val tabLabel = when (tab) {
                            MemberFilterTab.ALL -> "All (${uiState.totalMembersCount})"
                            MemberFilterTab.EXPIRING_SOON -> "Expiring (${uiState.expiringCount})"
                            MemberFilterTab.EXPIRED -> "Expired (${uiState.expiredCount})"
                            MemberFilterTab.PENDING_PAYMENT -> "Unpaid (${uiState.pendingPaymentCount})"
                        }

                        val badgeColor = when (tab) {
                            MemberFilterTab.EXPIRING_SOON -> Color(0xFFFBBF24)
                            MemberFilterTab.EXPIRED -> Color(0xFFEF4444)
                            MemberFilterTab.PENDING_PAYMENT -> Color(0xFFF97316)
                            else -> if (isSelected) Color.White else TrainerDarkCard
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(9999.dp))
                                .background(if (isSelected) badgeColor else TrainerDarkCard)
                                .border(
                                    1.dp,
                                    if (isSelected) badgeColor else TrainerDarkBorder,
                                    RoundedCornerShape(9999.dp)
                                )
                                .clickable { viewModel.onFilterSelected(tab) }
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tabLabel,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.Black else TrainerDarkTextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Members List
                if (uiState.filteredMembers.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = TrainerGreenAccent,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (uiState.totalMembersCount == 0) "No Gym Members Yet" else "No matching gym members found",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TrainerDarkTextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (uiState.totalMembersCount == 0)
                                    "Tap '+ Add Member' below to register your first gym member, record payment, and track subscription expiration."
                                else
                                    "Try adjusting your search query or switching the filter tab above.",
                                fontSize = 13.sp,
                                color = TrainerDarkTextSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            if (uiState.totalMembersCount == 0) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { viewModel.openAddMemberDialog() },
                                    colors = ButtonDefaults.buttonColors(containerColor = TrainerGreenAccent, contentColor = Color.Black),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Add First Member", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(uiState.filteredMembers, key = { it.id }) { member ->
                            MemberCardItem(
                                member = member,
                                dateFormatter = dateFormatter,
                                onMemberClick = { onMemberClick(member.id) },
                                onSendWhatsApp = { viewModel.openWhatsAppPreview(member) },
                                onRenewClick = { viewModel.openRenewDialog(member) },
                                onViewProgress = { onViewProgressClick(member.id) }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(60.dp))
                        }
                    }
                }
            }
        }

        // Dialog: Add New Member
        if (uiState.isAddMemberDialogOpen) {
            AddMemberDialog(
                onDismiss = { viewModel.closeAddMemberDialog() },
                onConfirm = { name, phone, email, trainer, duration, fee, paid, method, notes ->
                    viewModel.addNewMember(
                        name = name,
                        phone = phone,
                        email = email,
                        trainerName = trainer,
                        duration = duration,
                        feeAmount = fee,
                        amountPaid = paid,
                        paymentMethod = method,
                        notes = notes
                    )
                    Toast.makeText(context, "Member added successfully!", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // Dialog: Renew Membership
        if (uiState.isRenewDialogOpen && uiState.selectedMemberForRenewal != null) {
            val member = uiState.selectedMemberForRenewal!!
            RenewMembershipDialog(
                member = member,
                onDismiss = { viewModel.closeRenewDialog() },
                onConfirm = { duration, fee, method ->
                    viewModel.renewMember(
                        memberId = member.id,
                        additionalDuration = duration,
                        feeAmount = fee,
                        paymentMethod = method
                    )
                    Toast.makeText(context, "Membership renewed for ${member.name}!", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // Dialog: WhatsApp Renewal Preview & Direct Dispatch
        if (uiState.isWhatsAppPreviewOpen && uiState.selectedMemberForWhatsApp != null) {
            val member = uiState.selectedMemberForWhatsApp!!
            WhatsAppPreviewDialog(
                member = member,
                onDismiss = { viewModel.closeWhatsAppPreview() },
                onSend = { customMessage ->
                    val success = WhatsAppMessenger.openChat(
                        context = context,
                        rawPhone = member.phoneNumber,
                        message = customMessage
                    )
                    if (success) {
                        viewModel.markReminderSent(member.id)
                    }
                    viewModel.closeWhatsAppPreview()
                }
            )
        }
    }
}

@Composable
private fun MemberCardItem(
    member: GymMember,
    dateFormatter: SimpleDateFormat,
    onMemberClick: () -> Unit,
    onSendWhatsApp: () -> Unit,
    onRenewClick: () -> Unit,
    onViewProgress: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(TrainerDarkCard)
            .border(1.dp, TrainerDarkBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onMemberClick)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header: Avatar, Name, Duration Badge, Status Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF27272A)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = member.name.take(2).uppercase(),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TrainerDarkTextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = member.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TrainerDarkTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        val contactText = when {
                            member.phoneNumber.isNotBlank() -> member.phoneNumber
                            member.email.isNotBlank() -> member.email
                            else -> "No phone added"
                        }
                        Text(
                            text = contactText,
                            fontSize = 12.sp,
                            color = if (member.phoneNumber.isNotBlank()) TrainerDarkTextSecondary else TrainerDarkTextTertiary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (!member.assignedTrainerName.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(3.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(TrainerGreenAccent)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "Coach: ${member.assignedTrainerName}",
                                    fontSize = 11.sp,
                                    color = TrainerDarkTextTertiary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Membership Plan Badge (e.g. 3 Months, 12 Months)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1E293B))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = member.duration.displayName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF38BDF8),
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Expiry & Remaining Days Indicator Bar
            val formattedExpiry = dateFormatter.format(Date(member.endDateEpochMs))
            val days = member.remainingDays
            val progressFraction = (days / (member.duration.months * 30f)).coerceIn(0f, 1f)

            val statusColor = when {
                member.isExpired -> Color(0xFFEF4444)
                days <= 3 -> Color(0xFFEF4444)
                days <= 7 -> Color(0xFFFBBF24)
                else -> TrainerGreenAccent
            }

            val statusLabel = when {
                member.isExpired -> "EXPIRED"
                days == 0 -> "EXPIRES TODAY"
                days == 1 -> "1 DAY LEFT"
                else -> "$days DAYS REMAINING"
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (member.isExpired) Icons.Default.Warning else Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Valid till $formattedExpiry",
                        fontSize = 12.sp,
                        color = TrainerDarkTextSecondary
                    )
                }

                Text(
                    text = statusLabel,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = statusColor,
                trackColor = Color(0xFF27272A)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Action Row: WhatsApp Reminder & Quick Renew
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Direct WhatsApp Button
                val hasPhone = member.phoneNumber.isNotBlank()
                Button(
                    onClick = onSendWhatsApp,
                    modifier = Modifier
                        .weight(1.3f)
                        .height(38.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (hasPhone) Color(0xFF25D366) else Color(0xFF27272A),
                        contentColor = if (hasPhone) Color.White else TrainerDarkTextSecondary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "WhatsApp",
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (hasPhone) "WhatsApp Alert" else "No WhatsApp #",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Renew Button
                Button(
                    onClick = onRenewClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF27272A),
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Renew",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Renew",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun AddMemberDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        name: String,
        phone: String,
        email: String,
        trainer: String,
        duration: MembershipDuration,
        fee: Double,
        paid: Double,
        method: PaymentMethod,
        notes: String
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var trainerName by remember { mutableStateOf("") }
    var selectedDuration by remember { mutableStateOf(MembershipDuration.THREE_MONTHS) }
    var feeAmountText by remember { mutableStateOf("4500") }
    var amountPaidText by remember { mutableStateOf("4500") }
    var selectedMethod by remember { mutableStateOf(PaymentMethod.UPI) }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add New Gym Member",
                fontWeight = FontWeight.Bold,
                color = TrainerDarkTextPrimary,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name *") },
                    leadingIcon = { Icon(Icons.Default.Person, null) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = dialogTextFieldColors()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("WhatsApp Phone (+91...) *") },
                    leadingIcon = { Icon(Icons.Default.Phone, null) },
                    placeholder = { Text("Enter 10-digit mobile number") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = dialogTextFieldColors()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    leadingIcon = { Icon(Icons.Default.Email, null) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = dialogTextFieldColors()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = trainerName,
                    onValueChange = { trainerName = it },
                    label = { Text("Assigned Trainer / Manager") },
                    placeholder = { Text("e.g. Coach Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = dialogTextFieldColors()
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Select Membership Duration *",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TrainerDarkTextSecondary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Duration Selector Chips: 1, 2, 3, 6, 9, 12, 18 Months
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MembershipDuration.entries.forEach { duration ->
                        val isSelected = selectedDuration == duration
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) TrainerGreenAccent else Color(0xFF27272A))
                                .clickable { selectedDuration = duration }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = duration.displayName,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.Black else Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = feeAmountText,
                        onValueChange = { feeAmountText = it },
                        label = { Text("Fee (₹)") },
                        leadingIcon = { Icon(Icons.Default.LocalAtm, null) },
                        modifier = Modifier.weight(1f),
                        colors = dialogTextFieldColors()
                    )

                    OutlinedTextField(
                        value = amountPaidText,
                        onValueChange = { amountPaidText = it },
                        label = { Text("Paid (₹)") },
                        modifier = Modifier.weight(1f),
                        colors = dialogTextFieldColors()
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Payment Mode",
                    fontSize = 12.sp,
                    color = TrainerDarkTextSecondary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PaymentMethod.entries.forEach { method ->
                        val isSelected = selectedMethod == method
                        val label = when (method) {
                            PaymentMethod.CARD -> "Card"
                            else -> method.displayName
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(0xFF38BDF8) else Color(0xFF27272A))
                                .clickable { selectedMethod = method }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.Black else Color.White,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && phone.isNotBlank()) {
                        val fee = feeAmountText.toDoubleOrNull() ?: 0.0
                        val paid = amountPaidText.toDoubleOrNull() ?: fee
                        onConfirm(
                            name,
                            phone,
                            email,
                            trainerName,
                            selectedDuration,
                            fee,
                            paid,
                            selectedMethod,
                            notes
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = TrainerGreenAccent, contentColor = Color.Black)
            ) {
                Text("Register Member", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = TrainerDarkTextSecondary)
            ) {
                Text("Cancel")
            }
        },
        containerColor = Color(0xFF18181B)
    )
}

@Composable
private fun RenewMembershipDialog(
    member: GymMember,
    onDismiss: () -> Unit,
    onConfirm: (duration: MembershipDuration, fee: Double, method: PaymentMethod) -> Unit
) {
    var selectedDuration by remember { mutableStateOf(member.duration) }
    var feeAmountText by remember { mutableStateOf(member.feeAmount.toInt().toString()) }
    var selectedMethod by remember { mutableStateOf(PaymentMethod.UPI) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Renew Membership - ${member.name}",
                fontWeight = FontWeight.Bold,
                color = TrainerDarkTextPrimary,
                fontSize = 17.sp
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Extend subscription for additional duration:",
                    fontSize = 13.sp,
                    color = TrainerDarkTextSecondary
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MembershipDuration.entries.forEach { duration ->
                        val isSelected = selectedDuration == duration
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) TrainerGreenAccent else Color(0xFF27272A))
                                .clickable { selectedDuration = duration }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = duration.displayName,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.Black else Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = feeAmountText,
                    onValueChange = { feeAmountText = it },
                    label = { Text("Renewal Amount (₹)") },
                    leadingIcon = { Icon(Icons.Default.LocalAtm, null) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = dialogTextFieldColors()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PaymentMethod.entries.forEach { method ->
                        val isSelected = selectedMethod == method
                        val label = when (method) {
                            PaymentMethod.CARD -> "Card"
                            else -> method.displayName
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(0xFF38BDF8) else Color(0xFF27272A))
                                .clickable { selectedMethod = method }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.Black else Color.White,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val fee = feeAmountText.toDoubleOrNull() ?: 0.0
                    onConfirm(selectedDuration, fee, selectedMethod)
                },
                colors = ButtonDefaults.buttonColors(containerColor = TrainerGreenAccent, contentColor = Color.Black)
            ) {
                Text("Confirm & Renew", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = TrainerDarkTextSecondary)
            ) {
                Text("Cancel")
            }
        },
        containerColor = Color(0xFF18181B)
    )
}

@Composable
private fun WhatsAppPreviewDialog(
    member: GymMember,
    onDismiss: () -> Unit,
    onSend: (message: String) -> Unit
) {
    val defaultMsg = remember(member) {
        WhatsAppMessenger.generateRenewalMessage(member = member)
    }
    var messageText by remember { mutableStateOf(defaultMsg) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = null,
                    tint = Color(0xFF25D366),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Send WhatsApp Reminder",
                    fontWeight = FontWeight.Bold,
                    color = TrainerDarkTextPrimary,
                    fontSize = 17.sp
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                val hasPhone = member.phoneNumber.isNotBlank()
                if (hasPhone) {
                    Text(
                        text = "Sending directly to ${member.name} (${member.phoneNumber}) via WhatsApp:",
                        fontSize = 12.sp,
                        color = TrainerDarkTextSecondary
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF2E1A1A))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "⚠️ ${member.name} has not registered a phone number yet. You can copy the message to clipboard or ask the athlete to add their number in their profile.",
                            fontSize = 12.sp,
                            color = Color(0xFFFF8A80)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    colors = dialogTextFieldColors(),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, lineHeight = 16.sp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (hasPhone) "💡 Will open normal WhatsApp chat with text pre-filled. Tap send inside WhatsApp to dispatch." else "💡 Tap 'Copy Message' to copy this message to clipboard.",
                    fontSize = 11.sp,
                    color = TrainerDarkTextTertiary
                )
            }
        },
        confirmButton = {
            val context = androidx.compose.ui.platform.LocalContext.current
            val hasPhone = member.phoneNumber.isNotBlank()
            Button(
                onClick = {
                    if (hasPhone) {
                        onSend(messageText)
                    } else {
                        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                        val clip = android.content.ClipData.newPlainText("GymFlow Message", messageText)
                        clipboard?.setPrimaryClip(clip)
                        android.widget.Toast.makeText(context, "Message copied to clipboard", android.widget.Toast.LENGTH_SHORT).show()
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (hasPhone) Color(0xFF25D366) else TrainerGreenAccent,
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.Default.Send, null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (hasPhone) "Open WhatsApp" else "Copy Message", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = TrainerDarkTextSecondary)
            ) {
                Text("Cancel")
            }
        },
        containerColor = Color(0xFF18181B)
    )
}

@Composable
private fun dialogTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = TrainerGreenAccent,
    unfocusedBorderColor = Color(0xFF3F3F46),
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedLabelColor = TrainerGreenAccent,
    unfocusedLabelColor = Color(0xFFA1A1AA)
)
