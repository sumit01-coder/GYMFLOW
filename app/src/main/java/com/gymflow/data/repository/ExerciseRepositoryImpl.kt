package com.gymflow.data.repository

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.gymflow.core.database.ExerciseDao
import com.gymflow.core.database.ExerciseEntity
import com.gymflow.domain.model.Exercise
import com.gymflow.domain.repository.ExerciseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExerciseRepositoryImpl @Inject constructor(
    private val exerciseDao: ExerciseDao,
    private val firestore: FirebaseFirestore,
    @ApplicationContext private val context: Context
) : ExerciseRepository {

    companion object {
        const val FIRESTORE_COLLECTION_EXERCISES = "exercises"
        const val BASE_IMAGE_URL = "https://raw.githubusercontent.com/yuhonas/free-exercise-db/main/exercises/"
    }

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    private fun loadCatalogFromAssets(): List<Exercise> {
        return try {
            context.assets.open("exercises_catalog.json").use { stream ->
                val text = stream.bufferedReader().use { it.readText() }
                json.decodeFromString<List<Exercise>>(text)
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    override fun getExercises(): Flow<List<Exercise>> {
        return exerciseDao.getAllExercises().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getExercisesByCategory(category: String): Flow<List<Exercise>> {
        return (if (category.equals("All", ignoreCase = true)) {
            exerciseDao.getAllExercises()
        } else {
            exerciseDao.getExercisesByCategory(category)
        }).map { entities -> entities.map { it.toDomain() } }
    }

    override fun getExercisesByCategoryAndEquipment(category: String, equipment: String): Flow<List<Exercise>> {
        val catAll = category.equals("All", ignoreCase = true)
        val eqAll = equipment.equals("All", ignoreCase = true)

        val flow = when {
            catAll && eqAll -> exerciseDao.getAllExercises()
            catAll -> exerciseDao.getExercisesByEquipment(equipment)
            eqAll -> exerciseDao.getExercisesByCategory(category)
            else -> exerciseDao.getExercisesByCategoryAndEquipment(category, equipment)
        }

        return flow.map { entities -> entities.map { it.toDomain() } }
    }

    override fun searchExercises(query: String): Flow<List<Exercise>> {
        return exerciseDao.searchExercises(query.trim()).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getExerciseById(id: String): Flow<Exercise?> {
        return exerciseDao.observeExerciseById(id).map { it?.toDomain() }
    }

    override suspend fun saveCustomExercise(exercise: Exercise) {
        withContext(Dispatchers.IO) {
            exerciseDao.upsertExercise(ExerciseEntity.fromDomain(exercise))
            try {
                firestore.collection(FIRESTORE_COLLECTION_EXERCISES)
                    .document(exercise.id)
                    .set(exerciseToMap(exercise), SetOptions.merge())
                    .await()
            } catch (_: Exception) {
                // Offline fallback - already cached in Room
            }
        }
    }

    override suspend fun seedDefaultCatalogIfEmpty() {
        withContext(Dispatchers.IO) {
            val count = exerciseDao.getExerciseCount()
            if (count < 1000) {
                val assetExercises = loadCatalogFromAssets()
                val catalog = if (assetExercises.isNotEmpty()) assetExercises else createComprehensiveExerciseCatalog()
                val entities = catalog.map { ExerciseEntity.fromDomain(it) }
                exerciseDao.upsertExercises(entities)
            }
        }
    }

    override suspend fun syncExercisesWithFirestore() {
        withContext(Dispatchers.IO) {
            try {
                // Ensure local catalog is populated first
                seedDefaultCatalogIfEmpty()

                // Try fetching remote exercises from Firestore
                val snapshot = firestore.collection(FIRESTORE_COLLECTION_EXERCISES).limit(50).get().await()
                if (snapshot.isEmpty) {
                    // Seed Firestore from local catalog if cloud collection is empty (first 20 to avoid write spikes)
                    val localExercises = createComprehensiveExerciseCatalog().take(20)
                    for (ex in localExercises) {
                        firestore.collection(FIRESTORE_COLLECTION_EXERCISES)
                            .document(ex.id)
                            .set(exerciseToMap(ex), SetOptions.merge())
                    }
                } else {
                    // Upsert any remote additions into local Room DB
                    val remoteEntities = snapshot.documents.mapNotNull { doc ->
                        val id = doc.id
                        val name = doc.getString("name") ?: return@mapNotNull null
                        val category = doc.getString("category") ?: "Full Body"
                        val muscleGroup = doc.getString("muscleGroup") ?: ""
                        val equipment = doc.getString("equipment") ?: "Machine"
                        val machineName = doc.getString("machineName")
                        val defaultSets = doc.getLong("defaultSets")?.toInt() ?: 3
                        val defaultReps = doc.getLong("defaultReps")?.toInt() ?: 12
                        val defaultWeightKg = doc.getDouble("defaultWeightKg")?.toFloat() ?: 20f
                        val defaultRestSeconds = doc.getLong("defaultRestSeconds")?.toInt() ?: 45
                        val mediaUrl = doc.getString("mediaUrl")
                        val coachTip = doc.getString("coachTip") ?: ""
                        @Suppress("UNCHECKED_CAST")
                        val instructions = (doc.get("instructions") as? List<String>) ?: emptyList()

                        Exercise(
                            id = id,
                            name = name,
                            category = category,
                            muscleGroup = muscleGroup,
                            equipment = equipment,
                            instructions = instructions,
                            defaultRestSeconds = defaultRestSeconds,
                            mediaUrl = mediaUrl,
                            machineName = machineName,
                            defaultSets = defaultSets,
                            defaultReps = defaultReps,
                            defaultWeightKg = defaultWeightKg,
                            coachTip = coachTip
                        )
                    }.map { ExerciseEntity.fromDomain(it) }

                    if (remoteEntities.isNotEmpty()) {
                        exerciseDao.upsertExercises(remoteEntities)
                    }
                }
            } catch (_: Exception) {
                // Offline fallback: rely on local Room cache
                seedDefaultCatalogIfEmpty()
            }
        }
    }

    private fun exerciseToMap(ex: Exercise): Map<String, Any?> {
        return mapOf(
            "id" to ex.id,
            "name" to ex.name,
            "category" to ex.category,
            "muscleGroup" to ex.muscleGroup,
            "equipment" to ex.equipment,
            "machineName" to ex.machineName,
            "defaultSets" to ex.defaultSets,
            "defaultReps" to ex.defaultReps,
            "defaultWeightKg" to ex.defaultWeightKg.toDouble(),
            "defaultRestSeconds" to ex.defaultRestSeconds,
            "mediaUrl" to ex.mediaUrl,
            "instructions" to ex.instructions,
            "coachTip" to ex.coachTip
        )
    }

    private fun createComprehensiveExerciseCatalog(): List<Exercise> {
        return listOf(
            // ==================== CHEST (7 Exercises) ====================
            Exercise(
                id = "ex_chest_barbell_bench_press",
                name = "Flat Barbell Bench Press",
                category = "Chest",
                muscleGroup = "Pectoralis Major (Mid & Lower), Anterior Deltoid, Triceps",
                equipment = "Barbell",
                machineName = "Olympic Flat Bench Press Station",
                defaultSets = 4,
                defaultReps = 8,
                defaultWeightKg = 60f,
                defaultRestSeconds = 90,
                mediaUrl = "${BASE_IMAGE_URL}Barbell_Bench_Press_-_Medium_Grip/0.jpg",
                coachTip = "Retract scapulae and pin them against the bench throughout the lift to protect rotator cuffs.",
                instructions = listOf(
                    "Lie flat on the bench with eyes directly below the barbell.",
                    "Plant feet flat into the floor, grip the bar slightly wider than shoulder-width.",
                    "Unrack the bar and bring it directly over your mid-chest.",
                    "Lower under control until the bar lightly touches your sternum.",
                    "Press up forcefully along a slight diagonal back over your shoulders."
                )
            ),
            Exercise(
                id = "ex_chest_incline_dumbbell_press",
                name = "Incline Dumbbell Press",
                category = "Chest",
                muscleGroup = "Clavicular Pectoralis (Upper Chest), Anterior Deltoid",
                equipment = "Dumbbell",
                machineName = "Adjustable Incline Bench (30° Angle)",
                defaultSets = 3,
                defaultReps = 10,
                defaultWeightKg = 22f,
                defaultRestSeconds = 60,
                mediaUrl = "${BASE_IMAGE_URL}Incline_Dumbbell_Press/0.jpg",
                coachTip = "Set incline to 30 degrees to bias the clavicular head without overloading front delts.",
                instructions = listOf(
                    "Set bench to a 30-degree incline and sit with dumbbells on your knees.",
                    "Kick dumbbells back to shoulder height as you lie down.",
                    "Press both dumbbells upward in a slight converging arc.",
                    "Lower with a controlled 3-second eccentric to feel a deep upper chest stretch.",
                    "Drive back up without clinking dumbbells together at the top."
                )
            ),
            Exercise(
                id = "ex_chest_pec_deck_fly",
                name = "Seated Pec Deck Fly",
                category = "Chest",
                muscleGroup = "Sternal Pectoralis Major (Inner & Mid Chest)",
                equipment = "Machine",
                machineName = "Pin-Loaded Pec Deck Machine",
                defaultSets = 3,
                defaultReps = 12,
                defaultWeightKg = 45f,
                defaultRestSeconds = 45,
                mediaUrl = "${BASE_IMAGE_URL}Butterfly/0.jpg",
                coachTip = "Keep elbows at nipple height and pause for 1 second at full contraction.",
                instructions = listOf(
                    "Adjust seat height so handles align with mid-chest level.",
                    "Place forearms or hands against the pads with elbows softly bent.",
                    "Contract chest to bring handles together across your centerline.",
                    "Squeeze your pectorals intensely for 1 second.",
                    "Slowly return until you feel a comfortable stretch across the chest."
                )
            ),
            Exercise(
                id = "ex_chest_cable_crossover",
                name = "Standing Cable Chest Fly",
                category = "Chest",
                muscleGroup = "Pectoralis Major, Sternal Head",
                equipment = "Cable",
                machineName = "Dual Adjustable Cable Column",
                defaultSets = 3,
                defaultReps = 15,
                defaultWeightKg = 15f,
                defaultRestSeconds = 45,
                mediaUrl = "${BASE_IMAGE_URL}Cable_Crossover/0.jpg",
                coachTip = "Maintain a steady soft bend in your elbows; do not turn the fly into a press.",
                instructions = listOf(
                    "Set dual pulleys at chest or shoulder height with single D-handles.",
                    "Step forward into a staggered stance with slight torso forward lean.",
                    "Bring handles forward in a wide hugging motion.",
                    "Cross your hands slightly at the peak for maximum inner chest recruitment.",
                    "Control the return phase smoothly under constant cable tension."
                )
            ),
            Exercise(
                id = "ex_chest_hammer_strength_press",
                name = "Hammer Strength Incline Press",
                category = "Chest",
                muscleGroup = "Upper Pectoralis, Anterior Deltoids, Triceps",
                equipment = "Machine",
                machineName = "Plate-Loaded Incline Chest Press",
                defaultSets = 3,
                defaultReps = 10,
                defaultWeightKg = 35f,
                defaultRestSeconds = 60,
                mediaUrl = "${BASE_IMAGE_URL}Leverage_Incline_Chest_Press/0.jpg",
                coachTip = "Drive through your palms and keep your lower back pressed firmly into the backrest.",
                instructions = listOf(
                    "Adjust seat so horizontal handles align with upper clavicle.",
                    "Grip handles firmly and brace core into seat pad.",
                    "Drive handles upward along the converging guided path.",
                    "Lock out momentarily without hyperextending elbows.",
                    "Lower under control for 2 seconds before reversing."
                )
            ),
            Exercise(
                id = "ex_chest_dips",
                name = "Chest Dips",
                category = "Chest",
                muscleGroup = "Lower Pectoralis Major, Triceps Brachii",
                equipment = "Bodyweight",
                machineName = "Parallel Dip Bar Station / Assisted Dip Machine",
                defaultSets = 3,
                defaultReps = 10,
                defaultWeightKg = 0f,
                defaultRestSeconds = 60,
                mediaUrl = "${BASE_IMAGE_URL}Dips_-_Chest_Version/0.jpg",
                coachTip = "Lean torso forward 30 degrees and flare elbows slightly to bias the lower chest over triceps.",
                instructions = listOf(
                    "Grip parallel dip bars and suspend body with arms locked out.",
                    "Tilt torso forward approximately 30 degrees.",
                    "Lower body by bending elbows until upper arms are parallel to the floor.",
                    "Drive through palms to return to starting position, flexing chest at top."
                )
            ),
            Exercise(
                id = "ex_chest_decline_cable_press",
                name = "Decline Cable Chest Press",
                category = "Chest",
                muscleGroup = "Lower Pectoralis Major, Serratus Anterior",
                equipment = "Cable",
                machineName = "High Cable Column with Stirrup Handles",
                defaultSets = 3,
                defaultReps = 12,
                defaultWeightKg = 20f,
                defaultRestSeconds = 45,
                mediaUrl = "${BASE_IMAGE_URL}Leverage_Decline_Chest_Press/0.jpg",
                coachTip = "Press downward and forward at a 45-degree angle to isolate lower pecs.",
                instructions = listOf(
                    "Set pulleys at the top position with stirrup handles.",
                    "Step forward and press handles downward toward the hip crease.",
                    "Contract the lower chest firmly at the bottom.",
                    "Return slowly to shoulder height while resisting the cable weight."
                )
            ),

            // ==================== BACK (7 Exercises) ====================
            Exercise(
                id = "ex_back_lat_pulldown",
                name = "Wide-Grip Lat Pulldown",
                category = "Back",
                muscleGroup = "Latissimus Dorsi, Teres Major, Biceps",
                equipment = "Cable",
                machineName = "Cable Lat Pulldown Column",
                defaultSets = 4,
                defaultReps = 10,
                defaultWeightKg = 50f,
                defaultRestSeconds = 60,
                mediaUrl = "${BASE_IMAGE_URL}Wide-Grip_Lat_Pulldown/0.jpg",
                coachTip = "Pull through your elbows rather than gripping too tightly with your hands.",
                instructions = listOf(
                    "Adjust thigh pads snugly to prevent your hips from lifting.",
                    "Take a wide overhand grip on the lat bar.",
                    "Depress shoulders, puff chest up, and pull bar smoothly to collarbone level.",
                    "Squeeze lats hard at bottom for 1 second.",
                    "Slowly let the bar return overhead to a full lat stretch."
                )
            ),
            Exercise(
                id = "ex_back_seated_cable_row",
                name = "Seated Cable Row",
                category = "Back",
                muscleGroup = "Rhomboids, Middle Trapezius, Latissimus Dorsi",
                equipment = "Cable",
                machineName = "Low Cable Row Station (V-Grip Handle)",
                defaultSets = 3,
                defaultReps = 12,
                defaultWeightKg = 45f,
                defaultRestSeconds = 45,
                mediaUrl = "${BASE_IMAGE_URL}Seated_Cable_Rows/0.jpg",
                coachTip = "Keep spine neutral. Avoid rocking backward excessively to gain momentum.",
                instructions = listOf(
                    "Sit with feet firmly braced on footplates with knees slightly bent.",
                    "Grasp V-bar handle with arms fully extended and back tall.",
                    "Pull the handle into your lower abdomen while driving elbows backward.",
                    "Retract and squeeze your shoulder blades together firmly.",
                    "Extend arms back slowly until lats stretch fully."
                )
            ),
            Exercise(
                id = "ex_back_barbell_bent_row",
                name = "Barbell Bent-Over Row",
                category = "Back",
                muscleGroup = "Latissimus Dorsi, Trapezius, Erector Spinae",
                equipment = "Barbell",
                machineName = "Olympic Barbell & Bumper Plates",
                defaultSets = 4,
                defaultReps = 8,
                defaultWeightKg = 50f,
                defaultRestSeconds = 60,
                mediaUrl = "${BASE_IMAGE_URL}Bent_Over_Barbell_Row/0.jpg",
                coachTip = "Maintain a rigid 45-degree hip hinge to keep stress off the lumbar spine.",
                instructions = listOf(
                    "Stand hip-width apart and hinge hips back with a flat back.",
                    "Grip barbell just outside knees with an overhand grip.",
                    "Pull bar smoothly into your navel, driving elbows toward the ceiling.",
                    "Squeeze your mid-back at the peak.",
                    "Lower bar under control to arm's length without rounding shoulders."
                )
            ),
            Exercise(
                id = "ex_back_deadlift",
                name = "Conventional Barbell Deadlift",
                category = "Back",
                muscleGroup = "Erector Spinae, Glutes, Hamstrings, Traps",
                equipment = "Barbell",
                machineName = "Olympic Lifting Platform & Barbell",
                defaultSets = 4,
                defaultReps = 6,
                defaultWeightKg = 90f,
                defaultRestSeconds = 90,
                mediaUrl = "${BASE_IMAGE_URL}Barbell_Deadlift/0.jpg",
                coachTip = "Pull the slack out of the barbell before breaking the floor.",
                instructions = listOf(
                    "Stand with feet hip-width apart, bar touching mid-foot.",
                    "Hinge at hips and grip barbell just outside your shins.",
                    "Wedge hips down, chest up, and pull lats tight.",
                    "Push the floor away through mid-foot until hips and knees lock out.",
                    "Hinge back at hips to lower bar safely to floor."
                )
            ),
            Exercise(
                id = "ex_back_tbar_row",
                name = "Chest-Supported T-Bar Row",
                category = "Back",
                muscleGroup = "Middle Trapezius, Rhomboids, Rear Deltoids",
                equipment = "Machine",
                machineName = "Plate-Loaded T-Bar Row Machine",
                defaultSets = 3,
                defaultReps = 10,
                defaultWeightKg = 35f,
                defaultRestSeconds = 60,
                mediaUrl = "${BASE_IMAGE_URL}T-Bar_Row_with_Handle/0.jpg",
                coachTip = "Chest pad removes lower back strain, allowing strict isolation of mid-back.",
                instructions = listOf(
                    "Lie face down on the padded incline with feet secure on footplates.",
                    "Grip handles with neutral or overhand grip.",
                    "Pull handles toward chest while retracting scapulae.",
                    "Hold peak squeeze for 1 second.",
                    "Lower under control to full arm stretch."
                )
            ),
            Exercise(
                id = "ex_back_assisted_pullup",
                name = "Assisted Pull-Up Machine",
                category = "Back",
                muscleGroup = "Latissimus Dorsi, Biceps Brachii",
                equipment = "Machine",
                machineName = "Weight-Assisted Chin/Dip Station",
                defaultSets = 3,
                defaultReps = 8,
                defaultWeightKg = 25f,
                defaultRestSeconds = 60,
                mediaUrl = "${BASE_IMAGE_URL}Band_Assisted_Pull-Up/0.jpg",
                coachTip = "Counterweight assists your bodyweight. Lower counterweight as you get stronger.",
                instructions = listOf(
                    "Set desired counterweight pin on the machine stack.",
                    "Place knees or feet on the padded support bar.",
                    "Grip wide overhead handles.",
                    "Pull your body upward until your chin clears the bar level.",
                    "Lower smoothly under control until arms are fully extended."
                )
            ),
            Exercise(
                id = "ex_back_single_arm_dumbbell_row",
                name = "Single-Arm Dumbbell Row",
                category = "Back",
                muscleGroup = "Latissimus Dorsi, Rhomboids, Forearms",
                equipment = "Dumbbell",
                machineName = "Flat Bench & Dumbbell Rack",
                defaultSets = 3,
                defaultReps = 10,
                defaultWeightKg = 24f,
                defaultRestSeconds = 45,
                mediaUrl = "${BASE_IMAGE_URL}One-Arm_Dumbbell_Row/0.jpg",
                coachTip = "Pull dumbbell toward your hip pocket, not your armpit, to bias the lower lat.",
                instructions = listOf(
                    "Place one knee and same-side hand firmly on a flat bench.",
                    "Hold dumbbell in the free hand with arm hanging straight down.",
                    "Pull dumbbell upward and back toward your hip.",
                    "Contract lat firmly at top without rotating torso.",
                    "Lower under control until arm stretches completely."
                )
            ),

            // ==================== SHOULDERS (6 Exercises) ====================
            Exercise(
                id = "ex_sh_overhead_press",
                name = "Standing Overhead Barbell Press",
                category = "Shoulders",
                muscleGroup = "Anterior Deltoid, Lateral Deltoid, Triceps",
                equipment = "Barbell",
                machineName = "Squat Stand / Power Rack & Barbell",
                defaultSets = 4,
                defaultReps = 8,
                defaultWeightKg = 40f,
                defaultRestSeconds = 60,
                mediaUrl = "${BASE_IMAGE_URL}Standing_Military_Press/0.jpg",
                coachTip = "Squeeze glutes tight to avoid excessive arching in your lower back.",
                instructions = listOf(
                    "Rest barbell on clavicles with hands just outside shoulders.",
                    "Brace core, glutes, and thighs tightly.",
                    "Press bar vertically, tilting head back slightly to clear the chin.",
                    "Lock out arms overhead with bar directly over ear line.",
                    "Lower smoothly back to resting position on front delts."
                )
            ),
            Exercise(
                id = "ex_sh_seated_dumbbell_press",
                name = "Seated Dumbbell Shoulder Press",
                category = "Shoulders",
                muscleGroup = "Anterior Deltoid, Lateral Deltoid",
                equipment = "Dumbbell",
                machineName = "90° Upright Utility Bench",
                defaultSets = 3,
                defaultReps = 10,
                defaultWeightKg = 18f,
                defaultRestSeconds = 45,
                mediaUrl = "${BASE_IMAGE_URL}Dumbbell_Shoulder_Press/0.jpg",
                coachTip = "Lower dumbbells until upper arms are slightly below parallel to maximize range.",
                instructions = listOf(
                    "Sit upright with back supported firmly against bench pad.",
                    "Raise dumbbells to ear height with palms facing forward or slightly inward.",
                    "Press dumbbells upward until arms are straight overhead.",
                    "Lower under control for 2 seconds back to ear level."
                )
            ),
            Exercise(
                id = "ex_sh_cable_lateral_raise",
                name = "Cable Lateral Raise",
                category = "Shoulders",
                muscleGroup = "Lateral Deltoid (Side Shoulder)",
                equipment = "Cable",
                machineName = "Low Pulley Cable Column (Single Handle)",
                defaultSets = 3,
                defaultReps = 15,
                defaultWeightKg = 8f,
                defaultRestSeconds = 45,
                mediaUrl = "${BASE_IMAGE_URL}Cable_Seated_Lateral_Raise/0.jpg",
                coachTip = "Cable maintains constant resistance throughout the movement, unlike dumbbells.",
                instructions = listOf(
                    "Set cable pulley to lowest position or knee height.",
                    "Stand sideways to machine, holding single handle across body.",
                    "Raise arm out to the side until parallel to floor.",
                    "Pause at top for peak lateral deltoid contraction.",
                    "Lower arm slowly under cable tension."
                )
            ),
            Exercise(
                id = "ex_sh_dumbbell_lateral_raise",
                name = "Dumbbell Lateral Raise",
                category = "Shoulders",
                muscleGroup = "Lateral Deltoid",
                equipment = "Dumbbell",
                machineName = "Free Weight Area",
                defaultSets = 4,
                defaultReps = 15,
                defaultWeightKg = 10f,
                defaultRestSeconds = 45,
                mediaUrl = "${BASE_IMAGE_URL}Side_Lateral_Raise/0.jpg",
                coachTip = "Lead with your elbows and imagine pouring water from a pitcher at top.",
                instructions = listOf(
                    "Stand tall with dumbbells resting at your outer thighs.",
                    "Maintain a slight bend in elbows and lean forward 5 degrees.",
                    "Raise arms out to sides until shoulder height.",
                    "Hold for a split second, then lower with control."
                )
            ),
            Exercise(
                id = "ex_sh_reverse_pec_deck",
                name = "Reverse Pec Deck (Rear Delts)",
                category = "Shoulders",
                muscleGroup = "Posterior Deltoid, Rhomboids",
                equipment = "Machine",
                machineName = "Dual Rear Delt / Pec Deck Machine",
                defaultSets = 3,
                defaultReps = 15,
                defaultWeightKg = 35f,
                defaultRestSeconds = 45,
                mediaUrl = "${BASE_IMAGE_URL}Cable_Rear_Delt_Fly/0.jpg",
                coachTip = "Push through the back of your hands and avoid shrugging your traps up.",
                instructions = listOf(
                    "Sit facing inward toward the machine backrest pad.",
                    "Grip horizontal or vertical handles with arms straight.",
                    "Pull arms back horizontally in a wide reverse-fly arc.",
                    "Squeeze rear deltoids forcefully at full extension.",
                    "Return slowly without allowing the weight stack to touch."
                )
            ),
            Exercise(
                id = "ex_sh_cable_face_pull",
                name = "Cable Face Pull",
                category = "Shoulders",
                muscleGroup = "Posterior Deltoid, Rotator Cuff, Upper Trapezius",
                equipment = "Cable",
                machineName = "High Cable Column with Rope Attachment",
                defaultSets = 3,
                defaultReps = 15,
                defaultWeightKg = 18f,
                defaultRestSeconds = 45,
                mediaUrl = "${BASE_IMAGE_URL}Face_Pull/0.jpg",
                coachTip = "Pull rope apart toward eyes and externally rotate hands back like a double bicep pose.",
                instructions = listOf(
                    "Attach rope to cable pulley set at eye height.",
                    "Grip ends with thumbs facing backward.",
                    "Step back and pull rope directly toward bridge of nose.",
                    "Rotate forearms back into external rotation.",
                    "Hold for 1 second, then return smoothly."
                )
            ),

            // ==================== BICEPS (5 Exercises) ====================
            Exercise(
                id = "ex_bi_barbell_curl",
                name = "Standing Barbell Bicep Curl",
                category = "Biceps",
                muscleGroup = "Biceps Brachii (Short & Long Head), Brachialis",
                equipment = "Barbell",
                machineName = "EZ-Curl Bar or Olympic Barbell",
                defaultSets = 3,
                defaultReps = 10,
                defaultWeightKg = 25f,
                defaultRestSeconds = 45,
                mediaUrl = "${BASE_IMAGE_URL}Barbell_Curl/0.jpg",
                coachTip = "Keep elbows pinned against ribs. Do not swing torso to heave the weight.",
                instructions = listOf(
                    "Stand upright holding bar with shoulder-width underhand grip.",
                    "Pin elbows to your sides.",
                    "Curl the bar upward by flexing biceps until fully contracted.",
                    "Squeeze biceps hard at top.",
                    "Lower under a 2-second eccentric count back to straight arms."
                )
            ),
            Exercise(
                id = "ex_bi_incline_dumbbell_curl",
                name = "Incline Dumbbell Bicep Curl",
                category = "Biceps",
                muscleGroup = "Biceps Brachii (Long Head Bias)",
                equipment = "Dumbbell",
                machineName = "Adjustable Incline Bench (45° Angle)",
                defaultSets = 3,
                defaultReps = 12,
                defaultWeightKg = 12f,
                defaultRestSeconds = 45,
                mediaUrl = "${BASE_IMAGE_URL}Incline_Inner_Biceps_Curl/0.jpg",
                coachTip = "Incline puts the long head of the bicep in a pre-stretched position.",
                instructions = listOf(
                    "Sit back on a 45-degree incline bench with arms hanging straight down.",
                    "Curl dumbbells upward while supinating wrists (palms facing up).",
                    "Squeeze biceps at peak without letting elbows drift forward.",
                    "Lower dumbbells slowly to full extension stretch."
                )
            ),
            Exercise(
                id = "ex_bi_preacher_curl_machine",
                name = "Preacher Curl Machine",
                category = "Biceps",
                muscleGroup = "Biceps Brachii (Short Head Focus), Brachialis",
                equipment = "Machine",
                machineName = "Pin-Loaded Preacher Curl Station",
                defaultSets = 3,
                defaultReps = 12,
                defaultWeightKg = 30f,
                defaultRestSeconds = 45,
                mediaUrl = "${BASE_IMAGE_URL}Machine_Preacher_Curls/0.jpg",
                coachTip = "Chest and armpits must rest firmly against the angled pad to eliminate cheating.",
                instructions = listOf(
                    "Adjust seat so armpits sit comfortably on top of the preacher pad.",
                    "Grip handles with underhand grip.",
                    "Curl handles upward toward chin.",
                    "Contract biceps for 1 second at top.",
                    "Lower slowly until arms are nearly straight before stack touches."
                )
            ),
            Exercise(
                id = "ex_bi_cable_hammer_curl",
                name = "Cable Rope Hammer Curl",
                category = "Biceps",
                muscleGroup = "Brachioradialis, Brachialis, Biceps Long Head",
                equipment = "Cable",
                machineName = "Low Cable Pulley with Rope",
                defaultSets = 3,
                defaultReps = 12,
                defaultWeightKg = 20f,
                defaultRestSeconds = 45,
                mediaUrl = "${BASE_IMAGE_URL}Cable_Hammer_Curls_-_Rope_Attachment/0.jpg",
                coachTip = "Neutral grip targets forearm thickness and the brachialis muscle underneath.",
                instructions = listOf(
                    "Attach rope to lowest cable pulley.",
                    "Stand upright holding rope with neutral grip (palms facing each other).",
                    "Curl rope upward while keeping elbows stationary.",
                    "Squeeze forearms and biceps at the top.",
                    "Lower under control to arm's length."
                )
            ),
            Exercise(
                id = "ex_bi_concentration_curl",
                name = "Seated Concentration Curl",
                category = "Biceps",
                muscleGroup = "Biceps Brachii Peak",
                equipment = "Dumbbell",
                machineName = "Flat Utility Bench",
                defaultSets = 3,
                defaultReps = 12,
                defaultWeightKg = 12f,
                defaultRestSeconds = 45,
                mediaUrl = "${BASE_IMAGE_URL}Concentration_Curls/0.jpg",
                coachTip = "Lock the back of your tricep against inner thigh to strictly isolate the curl.",
                instructions = listOf(
                    "Sit on bench with legs spread wide.",
                    "Brace elbow of working arm against inner thigh.",
                    "Curl dumbbell upward smoothly toward opposite chest.",
                    "Squeeze bicep peak intensely.",
                    "Lower under 2-second negative count."
                )
            ),

            // ==================== TRICEPS (5 Exercises) ====================
            Exercise(
                id = "ex_tri_rope_pushdown",
                name = "Cable Rope Triceps Pushdown",
                category = "Triceps",
                muscleGroup = "Triceps Brachii (Lateral & Medial Heads)",
                equipment = "Cable",
                machineName = "High Cable Pulley with Rope Attachment",
                defaultSets = 4,
                defaultReps = 12,
                defaultWeightKg = 22f,
                defaultRestSeconds = 45,
                mediaUrl = "${BASE_IMAGE_URL}Triceps_Pushdown_-_Rope_Attachment/0.jpg",
                coachTip = "Spread rope handles outward at bottom of pushdown for maximal lateral head squeeze.",
                instructions = listOf(
                    "Attach rope to high pulley and grip ends firmly.",
                    "Pin elbows against ribcage with slight torso forward lean.",
                    "Extend arms downward until elbows fully lock out.",
                    "Spread rope handles apart at bottom for 1 second peak squeeze.",
                    "Return slowly to 90 degrees elbow bend."
                )
            ),
            Exercise(
                id = "ex_tri_skull_crushers",
                name = "EZ-Bar Skull Crushers",
                category = "Triceps",
                muscleGroup = "Triceps Brachii (Long & Medial Heads)",
                equipment = "Barbell",
                machineName = "Flat Bench & EZ-Curl Bar",
                defaultSets = 3,
                defaultReps = 10,
                defaultWeightKg = 25f,
                defaultRestSeconds = 45,
                mediaUrl = "${BASE_IMAGE_URL}EZ-Bar_Skullcrusher/0.jpg",
                coachTip = "Lower bar toward crown of head rather than forehead for greater triceps stretch.",
                instructions = listOf(
                    "Lie back on bench holding EZ bar with narrow overhand grip.",
                    "Position bar over forehead with elbows pointing upward.",
                    "Hinge at elbows to lower bar toward crown of head.",
                    "Extend elbows back up to starting position.",
                    "Keep upper arms stationary throughout."
                )
            ),
            Exercise(
                id = "ex_tri_overhead_cable_extension",
                name = "Overhead Cable Triceps Extension",
                category = "Triceps",
                muscleGroup = "Triceps Brachii (Long Head Deep Stretch)",
                equipment = "Cable",
                machineName = "Cable Column with Rope",
                defaultSets = 3,
                defaultReps = 12,
                defaultWeightKg = 18f,
                defaultRestSeconds = 45,
                mediaUrl = "${BASE_IMAGE_URL}Cable_Rope_Overhead_Triceps_Extension/0.jpg",
                coachTip = "Overhead arm position places triceps long head on maximum stretch for optimal growth.",
                instructions = listOf(
                    "Set pulley at chest height with rope attachment.",
                    "Grip rope overhead, face away from machine, and step into staggered lunge.",
                    "Extend elbows forward and out until arms are straight.",
                    "Pause and squeeze triceps.",
                    "Allow elbows to bend back deeply behind head."
                )
            ),
            Exercise(
                id = "ex_tri_dip_machine",
                name = "Seated Triceps Dip Machine",
                category = "Triceps",
                muscleGroup = "Triceps Brachii All Three Heads",
                equipment = "Machine",
                machineName = "Pin-Loaded Seated Dip Machine",
                defaultSets = 3,
                defaultReps = 12,
                defaultWeightKg = 50f,
                defaultRestSeconds = 45,
                mediaUrl = "${BASE_IMAGE_URL}Dip_Machine/0.jpg",
                coachTip = "Keep back against the pad and lock elbows out completely at the bottom.",
                instructions = listOf(
                    "Sit on machine and adjust lap belt or thigh rollers securely.",
                    "Grip parallel handles at your sides.",
                    "Push handles straight down until arms are locked out.",
                    "Contract triceps firmly at bottom.",
                    "Control the return until elbows reach 90-degree flexion."
                )
            ),
            Exercise(
                id = "ex_tri_close_grip_bench",
                name = "Close-Grip Barbell Bench Press",
                category = "Triceps",
                muscleGroup = "Triceps Brachii, Upper Chest",
                equipment = "Barbell",
                machineName = "Olympic Flat Bench Press",
                defaultSets = 3,
                defaultReps = 8,
                defaultWeightKg = 50f,
                defaultRestSeconds = 60,
                mediaUrl = "${BASE_IMAGE_URL}Close-Grip_Barbell_Bench_Press/0.jpg",
                coachTip = "Grip shoulder-width apart. Going narrower puts unnecessary stress on wrists.",
                instructions = listOf(
                    "Lie on bench and grip bar directly shoulder-width apart.",
                    "Unrack bar and lower under control with elbows tucked close to ribcage.",
                    "Touch lower sternum lightly.",
                    "Press up forcefully focusing on triceps drive."
                )
            ),

            // ==================== LEGS (7 Exercises) ====================
            Exercise(
                id = "ex_leg_barbell_squat",
                name = "Barbell Back Squat",
                category = "Legs",
                muscleGroup = "Quadriceps, Gluteus Maximus, Hamstrings, Adductors",
                equipment = "Barbell",
                machineName = "Power Rack / Squat Cage & Olympic Barbell",
                defaultSets = 4,
                defaultReps = 8,
                defaultWeightKg = 80f,
                defaultRestSeconds = 90,
                mediaUrl = "${BASE_IMAGE_URL}Barbell_Full_Squat/0.jpg",
                coachTip = "Maintain 360-degree abdominal brace and drive through your mid-foot.",
                instructions = listOf(
                    "Rest bar across upper traps and unrack with a solid stance.",
                    "Stand with feet shoulder-width, toes turned slightly out.",
                    "Inhale deeply, brace core, and sit hips back and down.",
                    "Descend until hip crease is parallel with knee joint.",
                    "Drive through floor to return to standing lockout."
                )
            ),
            Exercise(
                id = "ex_leg_incline_leg_press",
                name = "45-Degree Incline Leg Press",
                category = "Legs",
                muscleGroup = "Quadriceps, Glutes",
                equipment = "Machine",
                machineName = "Plate-Loaded 45° Sled Leg Press",
                defaultSets = 4,
                defaultReps = 12,
                defaultWeightKg = 120f,
                defaultRestSeconds = 60,
                mediaUrl = "${BASE_IMAGE_URL}Leg_Press/0.jpg",
                coachTip = "Do not lock out knees violently at top; keep soft bend to protect joints.",
                instructions = listOf(
                    "Sit with lower back and hips firmly pressed against backrest.",
                    "Place feet shoulder-width on center of sled footplate.",
                    "Release safety locks and lower sled slowly until knees reach 90 degrees.",
                    "Press firmly through mid-foot and heels to drive sled back up.",
                    "Stop just short of total knee lockout."
                )
            ),
            Exercise(
                id = "ex_leg_quad_extension",
                name = "Leg Extension Machine",
                category = "Legs",
                muscleGroup = "Quadriceps (Rectus Femoris, Vastus Lateralis)",
                equipment = "Machine",
                machineName = "Pin-Loaded Leg Extension Machine",
                defaultSets = 3,
                defaultReps = 12,
                defaultWeightKg = 45f,
                defaultRestSeconds = 45,
                mediaUrl = "${BASE_IMAGE_URL}Leg_Extensions/0.jpg",
                coachTip = "Pause for a full second at top of extension to maximize quad peak contraction.",
                instructions = listOf(
                    "Adjust backrest so knees align with machine pivot axis.",
                    "Position lower shin pad comfortably just above ankles.",
                    "Grip side handles and extend legs upward until straight.",
                    "Hold peak contraction for 1 second.",
                    "Lower slowly under control without banging weight stack."
                )
            ),
            Exercise(
                id = "ex_leg_lying_hamstring_curl",
                name = "Lying Hamstring Curl",
                category = "Legs",
                muscleGroup = "Hamstrings (Biceps Femoris, Semitendinosus)",
                equipment = "Machine",
                machineName = "Pin-Loaded Prone Leg Curl Machine",
                defaultSets = 3,
                defaultReps = 12,
                defaultWeightKg = 40f,
                defaultRestSeconds = 45,
                mediaUrl = "${BASE_IMAGE_URL}Lying_Leg_Curls/0.jpg",
                coachTip = "Keep hips glued to the bench pad; do not lift lower back to cheat the curl.",
                instructions = listOf(
                    "Lie face down on contoured bench with knee caps just off pad edge.",
                    "Hook heels underneath lever roller pad.",
                    "Curl legs upward toward glutes as far as possible.",
                    "Squeeze hamstrings for 1 second.",
                    "Lower legs under controlled 3-second negative."
                )
            ),
            Exercise(
                id = "ex_leg_romanian_deadlift",
                name = "Romanian Deadlift (RDL)",
                category = "Legs",
                muscleGroup = "Hamstrings, Gluteus Maximus, Erector Spinae",
                equipment = "Barbell",
                machineName = "Olympic Barbell / Dumbbell Set",
                defaultSets = 3,
                defaultReps = 10,
                defaultWeightKg = 60f,
                defaultRestSeconds = 60,
                mediaUrl = "${BASE_IMAGE_URL}Romanian_Deadlift/0.jpg",
                coachTip = "Hinge backward at hips as if trying to touch the wall behind you with your glutes.",
                instructions = listOf(
                    "Stand tall holding bar in front of thighs with shoulder-width grip.",
                    "Softly bend knees and keep them fixed at that angle.",
                    "Push hips backward while sliding bar down along shins.",
                    "Descend until deep hamstring stretch is felt (around mid-shin).",
                    "Drive hips forward through glutes to return upright."
                )
            ),
            Exercise(
                id = "ex_leg_hack_squat",
                name = "Hack Squat Machine",
                category = "Legs",
                muscleGroup = "Quadriceps Deep Sweep, Glutes",
                equipment = "Machine",
                machineName = "Plate-Loaded Hack Squat Sled",
                defaultSets = 3,
                defaultReps = 10,
                defaultWeightKg = 70f,
                defaultRestSeconds = 60,
                mediaUrl = "${BASE_IMAGE_URL}Hack_Squat/0.jpg",
                coachTip = "Guided track supports spine, letting you safely achieve deep knee flexion.",
                instructions = listOf(
                    "Position shoulders snugly under shoulder pads with back flat on pad.",
                    "Set feet shoulder-width on platform.",
                    "Disengage safety levers and descend smoothly until thighs hit parallel.",
                    "Drive through heels to return to standing position."
                )
            ),
            Exercise(
                id = "ex_leg_standing_calf_raise",
                name = "Standing Calf Raise Machine",
                category = "Legs",
                muscleGroup = "Gastrocnemius, Soleus (Calves)",
                equipment = "Machine",
                machineName = "Pin-Loaded Standing Calf Machine",
                defaultSets = 4,
                defaultReps = 15,
                defaultWeightKg = 65f,
                defaultRestSeconds = 45,
                mediaUrl = "${BASE_IMAGE_URL}Smith_Machine_Calf_Raise/0.jpg",
                coachTip = "Descend into a deep bottom stretch for 2 seconds to eliminate Achilles bounce.",
                instructions = listOf(
                    "Place balls of feet on edge of platform with heels hanging off.",
                    "Rest shoulder pads on shoulders with legs straight.",
                    "Drop heels deeply below platform level to full stretch.",
                    "Drive onto balls of feet as high as possible.",
                    "Hold peak contraction for 1 second."
                )
            ),

            // ==================== ABS / CORE (6 Exercises) ====================
            Exercise(
                id = "ex_abs_cable_rope_crunch",
                name = "Kneeling Cable Rope Crunch",
                category = "Abs",
                muscleGroup = "Rectus Abdominis (Six-Pack Core)",
                equipment = "Cable",
                machineName = "High Cable Column with Rope Attachment",
                defaultSets = 3,
                defaultReps = 15,
                defaultWeightKg = 25f,
                defaultRestSeconds = 45,
                mediaUrl = "${BASE_IMAGE_URL}Kneeling_Cable_Crunch_With_Alternating_Oblique_Twists/0.jpg",
                coachTip = "Flex spine and crunch ribs toward hips; do not just sit back on your heels.",
                instructions = listOf(
                    "Kneel a few feet in front of high pulley with rope held at temple height.",
                    "Lock hips in place with a 90-degree thigh angle.",
                    "Crunch ribcage down toward pelvis, flexing abdominals hard.",
                    "Hold contraction for 1 second at bottom.",
                    "Slowly uncurl spine back to starting stretch."
                )
            ),
            Exercise(
                id = "ex_abs_hanging_leg_raise",
                name = "Hanging Leg Raise",
                category = "Abs",
                muscleGroup = "Lower Rectus Abdominis, Hip Flexors",
                equipment = "Bodyweight",
                machineName = "Pull-Up Rig / Captain's Chair Station",
                defaultSets = 3,
                defaultReps = 12,
                defaultWeightKg = 0f,
                defaultRestSeconds = 45,
                mediaUrl = "${BASE_IMAGE_URL}Hanging_Leg_Raise/0.jpg",
                coachTip = "Tilt pelvis upward at top of movement rather than just swinging legs.",
                instructions = listOf(
                    "Hang from pull-up bar with overhand grip and engaged shoulders.",
                    "Tuck pelvis under and raise straight legs (or knees) to hip height.",
                    "Squeeze lower abdomen firmly at the top.",
                    "Lower legs under control to prevent body swinging."
                )
            ),
            Exercise(
                id = "ex_abs_cable_woodchopper",
                name = "Standing Cable Woodchopper",
                category = "Abs",
                muscleGroup = "Internal & External Obliques, Transverse Abdominis",
                equipment = "Cable",
                machineName = "Adjustable Cable Column",
                defaultSets = 3,
                defaultReps = 12,
                defaultWeightKg = 15f,
                defaultRestSeconds = 45,
                mediaUrl = "${BASE_IMAGE_URL}Standing_Cable_Wood_Chop/0.jpg",
                coachTip = "Rotate through your core and pivot back foot rather than just using arm strength.",
                instructions = listOf(
                    "Set pulley at high or shoulder level with single handle.",
                    "Stand sideways with feet wider than shoulder-width, gripping handle with both hands.",
                    "Rotate torso diagonally downward across your body.",
                    "Squeeze obliques at finish position.",
                    "Control the return back to starting position."
                )
            ),
            Exercise(
                id = "ex_abs_machine_crunch",
                name = "Seated Machine Abdominal Crunch",
                category = "Abs",
                muscleGroup = "Rectus Abdominis",
                equipment = "Machine",
                machineName = "Pin-Loaded Ab Crunch Machine",
                defaultSets = 3,
                defaultReps = 15,
                defaultWeightKg = 35f,
                defaultRestSeconds = 45,
                mediaUrl = "${BASE_IMAGE_URL}Ab_Crunch_Machine/0.jpg",
                coachTip = "Exhale completely as you curl forward to achieve maximal abdominal contraction.",
                instructions = listOf(
                    "Sit with chest against upper pad and feet hooked under lower rollers.",
                    "Grip handles overhead or at sides.",
                    "Contract abdominals to curl upper body forward while drawing knees upward.",
                    "Hold contraction for 1 second.",
                    "Slowly return without letting weight stack touch."
                )
            ),
            Exercise(
                id = "ex_abs_decline_bench_crunch",
                name = "Decline Bench Crunch",
                category = "Abs",
                muscleGroup = "Rectus Abdominis, Core Stabilizers",
                equipment = "Bodyweight",
                machineName = "Decline Abdominal Bench",
                defaultSets = 3,
                defaultReps = 15,
                defaultWeightKg = 0f,
                defaultRestSeconds = 45,
                mediaUrl = "${BASE_IMAGE_URL}Decline_Crunch/0.jpg",
                coachTip = "Cross hands over chest or lightly touch temples; do not pull on your neck.",
                instructions = listOf(
                    "Lock legs securely under ankle rollers of decline bench.",
                    "Lie back with hands across chest.",
                    "Curl torso up toward thighs, rounding upper back slightly.",
                    "Squeeze abs at top, then lower under 2-second control."
                )
            ),
            Exercise(
                id = "ex_abs_plank",
                name = "Forearm Core Plank",
                category = "Abs",
                muscleGroup = "Transverse Abdominis, Core Bracing, Glutes",
                equipment = "Bodyweight",
                machineName = "Exercise Mat",
                defaultSets = 3,
                defaultReps = 45, // 45 seconds
                defaultWeightKg = 0f,
                defaultRestSeconds = 45,
                mediaUrl = "${BASE_IMAGE_URL}Plank/0.jpg",
                coachTip = "Pull belly button toward spine and squeeze glutes to keep pelvis neutral.",
                instructions = listOf(
                    "Place forearms on floor with elbows directly under shoulders.",
                    "Extend legs straight back, resting on toes.",
                    "Create a straight line from heels to head.",
                    "Hold rigid posture with continuous abdominal bracing."
                )
            )
        )
    }
}

