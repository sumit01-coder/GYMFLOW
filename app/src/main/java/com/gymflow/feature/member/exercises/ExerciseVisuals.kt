package com.gymflow.feature.member.exercises

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.gymflow.core.theme.GymFlowBlack
import com.gymflow.core.theme.GymFlowBorder
import com.gymflow.core.theme.GymFlowTheme
import com.gymflow.core.theme.GymFlowWhite

object ExerciseVisuals {

    const val BASE_IMAGE_URL = "https://cdn.jsdelivr.net/gh/yuhonas/free-exercise-db@main/exercises/"
    const val BASE_GIF_URL = "https://cdn.jsdelivr.net/gh/omercotkd/exercises-gifs@master/assets/"
    const val FALLBACK_GIF_URL = "https://raw.githubusercontent.com/omercotkd/exercises-gifs/master/assets/"

    val EXERCISE_GIF_IDS: Map<String, String> = mapOf(
        // CHEST (7)
        "ex_chest_barbell_bench_press" to "0025",
        "ex_chest_incline_dumbbell_press" to "0314",
        "ex_chest_pec_deck_fly" to "0009",
        "ex_chest_cable_crossover" to "1261",
        "ex_chest_hammer_strength_press" to "1289",
        "ex_chest_dips" to "0009",
        "ex_chest_decline_cable_press" to "1261",

        // BACK (7)
        "ex_back_lat_pulldown" to "2330",
        "ex_back_seated_cable_row" to "0159",
        "ex_back_barbell_bent_row" to "0027",
        "ex_back_deadlift" to "0032",
        "ex_back_tbar_row" to "1349",
        "ex_back_assisted_pullup" to "0017",
        "ex_back_single_arm_dumbbell_row" to "0292",

        // SHOULDERS (6)
        "ex_sh_overhead_press" to "0103",
        "ex_sh_seated_dumbbell_press" to "0290",
        "ex_sh_cable_lateral_raise" to "0178",
        "ex_sh_dumbbell_lateral_raise" to "0334",
        "ex_sh_reverse_pec_deck" to "1022",
        "ex_sh_cable_face_pull" to "0178",

        // BICEPS (5)
        "ex_bi_barbell_curl" to "0031",
        "ex_bi_incline_dumbbell_curl" to "0315",
        "ex_bi_preacher_curl_machine" to "0059",
        "ex_bi_cable_hammer_curl" to "0165",
        "ex_bi_concentration_curl" to "0976",

        // TRICEPS (5)
        "ex_tri_rope_pushdown" to "0241",
        "ex_tri_skull_crushers" to "1721",
        "ex_tri_overhead_cable_extension" to "0092",
        "ex_tri_dip_machine" to "0009",
        "ex_tri_close_grip_bench" to "0030",

        // LEGS (7)
        "ex_leg_barbell_squat" to "0043",
        "ex_leg_incline_leg_press" to "0739",
        "ex_leg_quad_extension" to "0585",
        "ex_leg_lying_hamstring_curl" to "0586",
        "ex_leg_romanian_deadlift" to "0085",
        "ex_leg_hack_squat" to "0046",
        "ex_leg_standing_calf_raise" to "1372",

        // ABS (6)
        "ex_abs_cable_rope_crunch" to "0175",
        "ex_abs_hanging_leg_raise" to "0472",
        "ex_abs_cable_woodchopper" to "0175",
        "ex_abs_machine_crunch" to "0277",
        "ex_abs_decline_bench_crunch" to "0277",
        "ex_abs_plank" to "0464",

        // Aliases
        "lat-pulldown" to "2330",
        "barbell-row" to "0027",
        "bicep-curl" to "0031",
        "squat" to "0043",
        "leg-press" to "0739",
        "calf-raise" to "1372",
        "overhead-press" to "0103",
        "lateral-raise" to "0334",
        "dumbbell-bench" to "0314",
        "cable-crunches" to "0175",
        "romanian-deadlift" to "0085",
        "seated-cable-row" to "0159",
        "ex_chest_press" to "0025",
        "ex_lat_pulldown" to "2330",
        "ex_barbell_squat" to "0043",
        "ex_cable_fly" to "1261",
        "ex_deadlift" to "0032",
        "ex_barbell_curl" to "0031",
        "ex_triceps_pushdown" to "0241",
        "ex_overhead_press" to "0103",
        "ex_sh_overhead_press" to "0103",
        "ex_sh_seated_dumbbell_press" to "0290",
        "ex_sh_cable_lateral_raise" to "0178",
        "ex_sh_reverse_pec_deck" to "1022"
    )

    fun getGifUrl(exerciseId: String?): String? {
        if (exerciseId == null) return null

        // 1. Direct match in curated core exercise map (FIRST PRIORITY)
        val directId = EXERCISE_GIF_IDS[exerciseId]
        if (directId != null) return "$BASE_GIF_URL$directId.gif"

        // 2. Normalized match in curated core map
        val norm = exerciseId.lowercase().replace("-", "_").replace(" ", "_")
        for ((key, gid) in EXERCISE_GIF_IDS) {
            val keyNorm = key.lowercase().replace("-", "_").replace(" ", "_")
            if (norm == keyNorm || norm.contains(keyNorm) || keyNorm.contains(norm)) {
                return "$BASE_GIF_URL$gid.gif"
            }
        }

        // 3. Dynamic mapping for catalog exercises: ex_<muscle>_<slug>
        val validMuscles = setOf(
            "abs", "biceps", "calves", "chest", "forearms", "glutes", "hamstrings",
            "lats", "lower-back", "middle-back", "neck", "quadriceps", "shoulders",
            "traps", "triceps", "cardio", "upper-back"
        )
        if (exerciseId.startsWith("ex_")) {
            val parts = exerciseId.removePrefix("ex_").split("_", limit = 2)
            if (parts.size == 2) {
                val muscle = parts[0].replace("_", "-")
                val slug = parts[1].replace("_", "-")
                if (muscle in validMuscles) {
                    return "https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/$muscle/$slug.gif"
                }
            }
        }

        return null
    }

    val EXERCISE_IMAGE_PATHS: Map<String, String> = mapOf(
        // CHEST (7)
        "ex_chest_barbell_bench_press" to "Barbell_Bench_Press_-_Medium_Grip/0.jpg",
        "ex_chest_incline_dumbbell_press" to "Incline_Dumbbell_Press/0.jpg",
        "ex_chest_pec_deck_fly" to "Butterfly/0.jpg",
        "ex_chest_cable_crossover" to "Cable_Crossover/0.jpg",
        "ex_chest_hammer_strength_press" to "Leverage_Incline_Chest_Press/0.jpg",
        "ex_chest_dips" to "Dips_-_Chest_Version/0.jpg",
        "ex_chest_decline_cable_press" to "Leverage_Decline_Chest_Press/0.jpg",

        // BACK (7)
        "ex_back_lat_pulldown" to "Wide-Grip_Lat_Pulldown/0.jpg",
        "ex_back_seated_cable_row" to "Seated_Cable_Rows/0.jpg",
        "ex_back_barbell_bent_row" to "Bent_Over_Barbell_Row/0.jpg",
        "ex_back_deadlift" to "Barbell_Deadlift/0.jpg",
        "ex_back_tbar_row" to "T-Bar_Row_with_Handle/0.jpg",
        "ex_back_assisted_pullup" to "Band_Assisted_Pull-Up/0.jpg",
        "ex_back_single_arm_dumbbell_row" to "One-Arm_Dumbbell_Row/0.jpg",

        // SHOULDERS (6)
        "ex_sh_overhead_press" to "Standing_Military_Press/0.jpg",
        "ex_sh_seated_dumbbell_press" to "Dumbbell_Shoulder_Press/0.jpg",
        "ex_sh_cable_lateral_raise" to "Cable_Seated_Lateral_Raise/0.jpg",
        "ex_sh_dumbbell_lateral_raise" to "Side_Lateral_Raise/0.jpg",
        "ex_sh_reverse_pec_deck" to "Cable_Rear_Delt_Fly/0.jpg",
        "ex_sh_cable_face_pull" to "Face_Pull/0.jpg",

        // BICEPS (5)
        "ex_bi_barbell_curl" to "Barbell_Curl/0.jpg",
        "ex_bi_incline_dumbbell_curl" to "Incline_Inner_Biceps_Curl/0.jpg",
        "ex_bi_preacher_curl_machine" to "Machine_Preacher_Curls/0.jpg",
        "ex_bi_cable_hammer_curl" to "Cable_Hammer_Curls_-_Rope_Attachment/0.jpg",
        "ex_bi_concentration_curl" to "Concentration_Curls/0.jpg",

        // TRICEPS (5)
        "ex_tri_rope_pushdown" to "Triceps_Pushdown_-_Rope_Attachment/0.jpg",
        "ex_tri_skull_crushers" to "EZ-Bar_Skullcrusher/0.jpg",
        "ex_tri_overhead_cable_extension" to "Cable_Rope_Overhead_Triceps_Extension/0.jpg",
        "ex_tri_dip_machine" to "Dip_Machine/0.jpg",
        "ex_tri_close_grip_bench" to "Close-Grip_Barbell_Bench_Press/0.jpg",

        // LEGS (7)
        "ex_leg_barbell_squat" to "Barbell_Full_Squat/0.jpg",
        "ex_leg_incline_leg_press" to "Leg_Press/0.jpg",
        "ex_leg_quad_extension" to "Leg_Extensions/0.jpg",
        "ex_leg_lying_hamstring_curl" to "Lying_Leg_Curls/0.jpg",
        "ex_leg_romanian_deadlift" to "Romanian_Deadlift/0.jpg",
        "ex_leg_hack_squat" to "Hack_Squat/0.jpg",
        "ex_leg_standing_calf_raise" to "Smith_Machine_Calf_Raise/0.jpg",

        // ABS (6)
        "ex_abs_cable_rope_crunch" to "Kneeling_Cable_Crunch_With_Alternating_Oblique_Twists/0.jpg",
        "ex_abs_hanging_leg_raise" to "Hanging_Leg_Raise/0.jpg",
        "ex_abs_cable_woodchopper" to "Standing_Cable_Wood_Chop/0.jpg",
        "ex_abs_machine_crunch" to "Ab_Crunch_Machine/0.jpg",
        "ex_abs_decline_bench_crunch" to "Decline_Crunch/0.jpg",
        "ex_abs_plank" to "Plank/0.jpg",

        // Trainer aliases & canonical identifiers
        "lat-pulldown" to "Wide-Grip_Lat_Pulldown/0.jpg",
        "barbell-row" to "Bent_Over_Barbell_Row/0.jpg",
        "bicep-curl" to "Barbell_Curl/0.jpg",
        "squat" to "Barbell_Full_Squat/0.jpg",
        "leg-press" to "Leg_Press/0.jpg",
        "calf-raise" to "Smith_Machine_Calf_Raise/0.jpg",
        "overhead-press" to "Standing_Military_Press/0.jpg",
        "lateral-raise" to "Side_Lateral_Raise/0.jpg",
        "dumbbell-bench" to "Incline_Dumbbell_Press/0.jpg",
        "cable-crunches" to "Kneeling_Cable_Crunch_With_Alternating_Oblique_Twists/0.jpg",
        "romanian-deadlift" to "Romanian_Deadlift/0.jpg",
        "seated-cable-row" to "Seated_Cable_Rows/0.jpg",
        "ex_chest_press" to "Barbell_Bench_Press_-_Medium_Grip/0.jpg",
        "ex_lat_pulldown" to "Wide-Grip_Lat_Pulldown/0.jpg",
        "ex_barbell_squat" to "Barbell_Full_Squat/0.jpg",
        "ex_cable_fly" to "Cable_Crossover/0.jpg",
        "ex_deadlift" to "Barbell_Deadlift/0.jpg",
        "ex_barbell_curl" to "Barbell_Curl/0.jpg",
        "ex_triceps_pushdown" to "Triceps_Pushdown_-_Rope_Attachment/0.jpg",
        "ex_overhead_press" to "Standing_Military_Press/0.jpg"
    )

    fun getImageUrl(exerciseId: String?): String? {
        if (exerciseId == null) return null
        val directPath = EXERCISE_IMAGE_PATHS[exerciseId]
        if (directPath != null) return "$BASE_IMAGE_URL$directPath"

        val norm = exerciseId.lowercase().replace("-", "_").replace(" ", "_")
        for ((key, path) in EXERCISE_IMAGE_PATHS) {
            val keyNorm = key.lowercase().replace("-", "_").replace(" ", "_")
            if (norm.contains(keyNorm) || keyNorm.contains(norm)) {
                return "$BASE_IMAGE_URL$path"
            }
        }
        return getGifUrl(exerciseId)
    }

    fun getEquipmentIcon(equipment: String): ImageVector {
        return when (equipment.lowercase()) {
            "machine", "smith machine" -> Icons.Default.Tune
            "cable" -> Icons.Default.Build
            "barbell", "dumbbell", "kettlebell" -> Icons.Default.FitnessCenter
            "bodyweight", "resistance band" -> Icons.Default.AccessibilityNew
            else -> Icons.Default.Speed
        }
    }

    fun getCategoryColor(category: String): Color {
        return when (category.lowercase()) {
            "chest" -> Color(0xFF1E293B)
            "back" -> Color(0xFF0F172A)
            "shoulders" -> Color(0xFF334155)
            "biceps" -> Color(0xFF1E1E24)
            "triceps" -> Color(0xFF27272A)
            "legs" -> Color(0xFF18181B)
            "abs" -> Color(0xFF09090B)
            "cardio" -> Color(0xFF0B2014)
            else -> GymFlowBlack
        }
    }
}

@Composable
fun ExerciseEquipmentBadge(
    equipment: String,
    machineName: String? = null,
    modifier: Modifier = Modifier
) {
    val displayLabel = machineName ?: equipment
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(GymFlowTheme.colors.surfaceVariant)
            .border(0.5.dp, GymFlowBorder, RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = ExerciseVisuals.getEquipmentIcon(equipment),
            contentDescription = null,
            modifier = Modifier.size(12.dp),
            tint = GymFlowTheme.colors.textSecondary
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = displayLabel,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = GymFlowTheme.colors.textSecondary
        )
    }
}

@Composable
fun MuscleCategoryChip(
    category: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(GymFlowBlack)
            .padding(horizontal = 6.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = category.uppercase(),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = GymFlowWhite,
            letterSpacing = 0.4.sp,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
fun ExerciseThumbnail(
    imageUrl: String? = null,
    exerciseId: String? = null,
    category: String = "Chest",
    equipment: String = "Machine",
    preferGif: Boolean = false,
    modifier: Modifier = Modifier
) {
    val cornerRadius = 10.dp
    val shape = RoundedCornerShape(cornerRadius)
    val resolvedUrl = (if (preferGif) ExerciseVisuals.getGifUrl(exerciseId) else null)
        ?: imageUrl
        ?: ExerciseVisuals.getImageUrl(exerciseId)

    Box(
        modifier = modifier
            .clip(shape)
            .background(GymFlowTheme.colors.surfaceVariant)
            .border(1.dp, GymFlowBorder, shape),
        contentAlignment = Alignment.Center
    ) {
        if (!resolvedUrl.isNullOrBlank()) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(resolvedUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .matchParentSize()
                    .clip(shape),
                loading = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = ExerciseVisuals.getEquipmentIcon(equipment),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = GymFlowTheme.colors.textSecondary.copy(alpha = 0.5f)
                        )
                    }
                },
                error = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = ExerciseVisuals.getEquipmentIcon(equipment),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = GymFlowTheme.colors.textSecondary
                        )
                    }
                }
            )
        } else {
            Icon(
                imageVector = ExerciseVisuals.getEquipmentIcon(equipment),
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = GymFlowTheme.colors.textPrimary
            )
        }
    }
}
