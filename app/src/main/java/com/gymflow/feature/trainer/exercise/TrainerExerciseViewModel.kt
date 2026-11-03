package com.gymflow.feature.trainer.exercise

import androidx.lifecycle.ViewModel
import com.gymflow.domain.model.Exercise
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class TrainerExerciseLibraryUiState(
    val searchQuery: String = "",
    val selectedCategory: String = "All",
    val selectedEquipment: String = "All",
    val categories: List<String> = listOf(
        "All", "Chest", "Back", "Legs", "Shoulders", "Biceps", "Triceps", "Abs", "Cardio", "Full Body"
    ),
    val equipmentList: List<String> = listOf(
        "All", "Barbell", "Dumbbell", "Machine", "Cable", "Bodyweight"
    ),
    val exercises: List<Exercise> = listOf(
        Exercise(
            id = "bench-press",
            name = "Bench Press",
            category = "Chest",
            muscleGroup = "Pectoralis Major, Anterior Deltoids, Triceps",
            equipment = "Barbell",
            defaultRestSeconds = 45,
            instructions = listOf(
                "Lie flat on the bench with feet firmly planted on the floor.",
                "Grip the barbell slightly wider than shoulder-width.",
                "Retract your shoulder blades and brace your core.",
                "Lower the bar under control to the mid-chest.",
                "Press upward explosively while keeping shoulder blades pinned."
            )
        ),
        Exercise(
            id = "incline-dumbbell-press",
            name = "Incline Dumbbell Press",
            category = "Chest",
            muscleGroup = "Clavicular Pectoralis (Upper Chest), Triceps",
            equipment = "Dumbbell",
            defaultRestSeconds = 45,
            instructions = listOf(
                "Set bench angle to 30 degrees.",
                "Hold dumbbells at shoulder height with palms facing forward.",
                "Press dumbbells straight up, converging slightly at the top.",
                "Lower with a controlled 3-second negative to feel a deep chest stretch."
            )
        ),
        Exercise(
            id = "cable-fly",
            name = "Cable Fly",
            category = "Chest",
            muscleGroup = "Sternal Pectoralis Major",
            equipment = "Cable",
            defaultRestSeconds = 45,
            instructions = listOf(
                "Set pulleys at chest height.",
                "Step forward into a staggered stance with a slight forward lean.",
                "Bring handles together in a hugging motion.",
                "Squeeze pectorals firmly for 1 second at full contraction."
            )
        ),
        Exercise(
            id = "triceps-pushdown",
            name = "Triceps Pushdown",
            category = "Triceps",
            muscleGroup = "Lateral & Medial Triceps Head",
            equipment = "Cable",
            defaultRestSeconds = 45,
            instructions = listOf(
                "Attach a straight bar or rope to a high pulley.",
                "Pin elbows firmly at your ribcage throughout the movement.",
                "Push downwards until arms are completely locked out.",
                "Allow hands to return to chest height slowly under tension."
            )
        ),
        Exercise(
            id = "barbell-squat",
            name = "Barbell Squat",
            category = "Legs",
            muscleGroup = "Quadriceps, Gluteus Maximus, Hamstrings",
            equipment = "Barbell",
            defaultRestSeconds = 90,
            instructions = listOf(
                "Position barbell across upper traps with a tight upper back.",
                "Stand with feet shoulder-width apart, toes angled out slightly.",
                "Break at hips and knees simultaneously and squat to parallel.",
                "Drive through mid-foot to return to starting position."
            )
        ),
        Exercise(
            id = "leg-press",
            name = "Leg Press",
            category = "Legs",
            muscleGroup = "Quadriceps, Adductors, Glutes",
            equipment = "Machine",
            defaultRestSeconds = 60,
            instructions = listOf(
                "Place feet shoulder-width on the sled platform.",
                "Disengage safety latches while pressing sled up.",
                "Lower sled until knees form a 90-degree angle.",
                "Press up through heels without locking knees aggressively."
            )
        ),
        Exercise(
            id = "deadlift",
            name = "Barbell Deadlift",
            category = "Back",
            muscleGroup = "Erector Spinae, Hamstrings, Glutes, Lats",
            equipment = "Barbell",
            defaultRestSeconds = 90,
            instructions = listOf(
                "Step up to the bar with shins 1 inch away.",
                "Hinge at the hips, grip the bar, and pull chest up tall.",
                "Push the floor away through your heels to stand upright.",
                "Hinge at hips first on the descent before bending knees."
            )
        ),
        Exercise(
            id = "lat-pulldown",
            name = "Lat Pulldown",
            category = "Back",
            muscleGroup = "Latissimus Dorsi, Teres Major, Biceps",
            equipment = "Cable",
            defaultRestSeconds = 45,
            instructions = listOf(
                "Grip bar slightly wider than shoulder-width with overhand grip.",
                "Lean torso back about 10-15 degrees.",
                "Pull bar straight down toward upper collarbone.",
                "Control bar return without shrugging shoulders."
            )
        ),
        Exercise(
            id = "overhead-press",
            name = "Overhead Shoulder Press",
            category = "Shoulders",
            muscleGroup = "Anterior Deltoids, Lateral Deltoids, Triceps",
            equipment = "Barbell",
            defaultRestSeconds = 60,
            instructions = listOf(
                "Rest barbell on clavicles with elbows slightly forward.",
                "Squeeze glutes and brace abdominals firmly.",
                "Press bar straight overhead, tucking chin briefly.",
                "Lock out overhead with bar directly aligned with mid-foot."
            )
        ),
        Exercise(
            id = "barbell-curl",
            name = "Barbell Bicep Curl",
            category = "Biceps",
            muscleGroup = "Biceps Brachii, Brachialis",
            equipment = "Barbell",
            defaultRestSeconds = 45,
            instructions = listOf(
                "Stand upright holding barbell with an underhand grip.",
                "Keep elbows fixed at your sides throughout the curl.",
                "Curl bar up to collarbone height squeezing biceps.",
                "Lower barbell slowly over a 2-second eccentric count."
            )
        ),
        Exercise(
            id = "hanging-leg-raise",
            name = "Hanging Leg Raise",
            category = "Abs",
            muscleGroup = "Rectus Abdominis, Hip Flexors",
            equipment = "Bodyweight",
            defaultRestSeconds = 45,
            instructions = listOf(
                "Hang from a pull-up bar with an overhand grip.",
                "Keep legs straight or slightly bent and posterior pelvic tilt.",
                "Raise feet towards the bar using abdominal contraction.",
                "Lower legs under strict control without swinging."
            )
        )
    ),
    val selectedExerciseForDetails: Exercise? = null
) {
    val filteredExercises: List<Exercise>
        get() = exercises.filter { exercise ->
            val matchesQuery = searchQuery.isBlank() ||
                    exercise.name.contains(searchQuery, ignoreCase = true) ||
                    exercise.category.contains(searchQuery, ignoreCase = true) ||
                    exercise.muscleGroup.contains(searchQuery, ignoreCase = true)

            val matchesCategory = selectedCategory == "All" ||
                    exercise.category.equals(selectedCategory, ignoreCase = true)

            val matchesEquipment = selectedEquipment == "All" ||
                    exercise.equipment.equals(selectedEquipment, ignoreCase = true)

            matchesQuery && matchesCategory && matchesEquipment
        }
}

class TrainerExerciseViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(TrainerExerciseLibraryUiState())
    val uiState: StateFlow<TrainerExerciseLibraryUiState> = _uiState.asStateFlow()

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onCategorySelected(category: String) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun onEquipmentSelected(equipment: String) {
        _uiState.update { it.copy(selectedEquipment = equipment) }
    }

    fun openExerciseDetails(exercise: Exercise) {
        _uiState.update { it.copy(selectedExerciseForDetails = exercise) }
    }

    fun closeExerciseDetails() {
        _uiState.update { it.copy(selectedExerciseForDetails = null) }
    }
}
