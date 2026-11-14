package com.gymflow.feature.member.fatloss

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.gymflow.core.theme.GymFlowBlack
import com.gymflow.core.theme.GymFlowGreen
import com.gymflow.core.theme.GymFlowGreenLight
import com.gymflow.core.theme.GymFlowTheme
import com.gymflow.core.theme.GymFlowWhite

@Composable
fun TargetFatLossScreen(
    onNavigateBack: () -> Unit,
    onNavigateToZoneDetail: (String) -> Unit
) {
    var selectedView by remember { mutableStateOf(BodyView.FRONT) }
    var selectedZoneId by remember { mutableStateOf<String?>("belly_core") }

    Scaffold(
        containerColor = GymFlowTheme.colors.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(GymFlowTheme.colors.surfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = GymFlowTheme.colors.textPrimary
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "BODY COMPOSITION",
                        style = GymFlowTheme.typography.Caption.copy(
                            color = GymFlowGreen,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                    Text(
                        text = "Target Area Fat Loss",
                        style = GymFlowTheme.typography.Heading1.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Physiology Fact Banner: Spot Reduction Reality
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(GymFlowGreen.copy(alpha = 0.08f))
                    .border(1.dp, GymFlowGreen.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(GymFlowTheme.colors.surfaceVariant)
                            .border(1.dp, GymFlowGreen.copy(alpha = 0.3f), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        SubcomposeAsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data("https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/abs/crunch-floor.gif")
                                .crossfade(true)
                                .build(),
                            contentDescription = "Core Kinetic Fat Loss Motion",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                            loading = {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Science,
                                        contentDescription = null,
                                        tint = GymFlowGreen,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "EVIDENCE-BASED PHYSIOLOGY",
                            style = GymFlowTheme.typography.Caption.copy(
                                color = GymFlowGreen,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Spot reduction of fat is a myth, but targeted muscle firming + systemic caloric deficit is real. Our protocols combine high-intensity fat oxidation with localized hypertrophy to sculpt and tighten each zone.",
                            style = GymFlowTheme.typography.Caption.copy(
                                color = GymFlowTheme.colors.textSecondary,
                                lineHeight = 17.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Interactive Body Anatomy Map
            InteractiveBodyMap(
                selectedView = selectedView,
                selectedZoneId = selectedZoneId,
                onViewChange = { selectedView = it },
                onZoneSelect = { zone ->
                    selectedZoneId = zone.id
                    onNavigateToZoneDetail(zone.id)
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 4-Pillar Overview Cards
            Text(
                text = "The 4-Pillar Target Blueprint",
                style = GymFlowTheme.typography.Heading2.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PillarCard(
                    title = "1. HIIT Circuits",
                    desc = "Elevate EPOC and mobilize fatty acids systemically.",
                    modifier = Modifier.weight(1f)
                )
                PillarCard(
                    title = "2. Local Firming",
                    desc = "Build dense muscle beneath loose skin for tight definition.",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PillarCard(
                    title = "3. Caloric Deficit",
                    desc = "Controlled -450 to -500 kcal deficit to preserve lean mass.",
                    modifier = Modifier.weight(1f)
                )
                PillarCard(
                    title = "4. Nutrient Timing",
                    desc = "High protein (1.8-2.2g/kg) with low glycemic carbohydrates.",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PillarCard(
    title: String,
    desc: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(GymFlowTheme.colors.surface)
            .border(1.dp, GymFlowTheme.colors.border, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column {
            Text(
                text = title,
                style = GymFlowTheme.typography.BodyMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = desc,
                style = GymFlowTheme.typography.Caption.copy(
                    color = GymFlowTheme.colors.textSecondary,
                    lineHeight = 16.sp
                )
            )
        }
    }
}
