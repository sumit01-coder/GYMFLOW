package com.gymflow.feature.member.exercises

enum class StretchTiming(val label: String) {
    PRE_WORKOUT_DYNAMIC("Pre-Workout Warmup"),
    POST_WORKOUT_STATIC("Post-Workout Cooldown"),
    DAILY_MOBILITY("Daily Mobility & Recovery")
}

data class StretchingExercise(
    val id: String,
    val name: String,
    val targetMuscle: String,
    val muscleGroup: String, // "Chest", "Back", "Biceps", "Triceps", "Shoulders", "Legs", "Abs"
    val recommendedDurationSeconds: Int = 30,
    val timing: StretchTiming = StretchTiming.POST_WORKOUT_STATIC,
    val breathingCue: String,
    val instructions: List<String>,
    val coachTip: String,
    val imageUrl: String? = null,
    val gifUrl: String? = null
)

object StretchingCatalog {

    val stretches: List<StretchingExercise> = listOf(
        // CHEST
        StretchingExercise(
            id = "stretch_pec_doorway",
            name = "Doorway Pectoral Stretch",
            targetMuscle = "Pectoralis Major & Minor",
            muscleGroup = "Chest",
            recommendedDurationSeconds = 30,
            timing = StretchTiming.POST_WORKOUT_STATIC,
            breathingCue = "Slow 4-second inhalation through nose; exhale and gently lean forward deeper into the stretch.",
            instructions = listOf(
                "Stand in an open doorway with forearms flat against the doorframe at a 90-degree angle.",
                "Step forward with one foot until you feel a comfortable opening across your chest and shoulders.",
                "Keep your core braced and shoulders down; avoid over-arching your lower back.",
                "Hold for 30 seconds, breathe deeply, then switch leading legs."
            ),
            coachTip = "Adjust your elbow height: higher targets the lower pec; lower targets the upper pec fibers."
        ),
        StretchingExercise(
            id = "stretch_chest_standing_opener",
            name = "Standing Clasp-Behind Chest Opener",
            targetMuscle = "Pectoralis Major & Anterior Deltoids",
            muscleGroup = "Chest",
            recommendedDurationSeconds = 25,
            timing = StretchTiming.PRE_WORKOUT_DYNAMIC,
            breathingCue = "Inhale to expand the ribcage; exhale while pulling shoulders down and back.",
            instructions = listOf(
                "Stand tall with feet hip-width apart and roll your shoulders back.",
                "Clasp your hands behind your lower back and interlace your fingers.",
                "Gently straighten your arms and lift your hands away from your hips.",
                "Open your collarbones toward the ceiling without straining your neck."
            ),
            coachTip = "If your hands cannot interlace comfortably, hold a small towel or resistance band between hands."
        ),

        // BACK
        StretchingExercise(
            id = "stretch_lat_hang",
            name = "Dead Hang Lat Decompression",
            targetMuscle = "Latissimus Dorsi, Teres Major & Spine",
            muscleGroup = "Back",
            recommendedDurationSeconds = 30,
            timing = StretchTiming.POST_WORKOUT_STATIC,
            breathingCue = "Deep diaphragmatic breathing; allow your body weight to decompress the spine on every exhale.",
            instructions = listOf(
                "Grip a sturdy pull-up bar with an overhand grip slightly wider than shoulder width.",
                "Step off the bench or platform and hang freely with relaxed arms.",
                "Keep your core lightly engaged and let your shoulder blades elevate naturally.",
                "Breathe calmly for 20 to 30 seconds before softly returning feet to the floor."
            ),
            coachTip = "Outstanding for spinal decompression and relieving lower back tightness after heavy squats or deadlifts."
        ),
        StretchingExercise(
            id = "stretch_childs_pose_lats",
            name = "Kneeling Child's Pose with Lat Reach",
            targetMuscle = "Latissimus Dorsi, Thoracic Spine & Glutes",
            muscleGroup = "Back",
            recommendedDurationSeconds = 35,
            timing = StretchTiming.POST_WORKOUT_STATIC,
            breathingCue = "Inhale into your side ribs; exhale and sink your hips down toward your heels.",
            instructions = listOf(
                "Kneel on an exercise mat with big toes touching and knees flared wide apart.",
                "Walk your hands straight out in front and sink your hips back onto your heels.",
                "Walk both hands 45 degrees to the right to intensify the stretch along your left lat.",
                "Hold for 20 seconds, return to center, then walk hands 45 degrees to the left."
            ),
            coachTip = "Keep the opposite hip pinned firmly to your heel to maximize the lateral lat stretch."
        ),
        StretchingExercise(
            id = "stretch_cat_cow",
            name = "Cat-Cow Thoracic Mobility Flow",
            targetMuscle = "Erector Spinae & Thoracic Mobility",
            muscleGroup = "Back",
            recommendedDurationSeconds = 40,
            timing = StretchTiming.PRE_WORKOUT_DYNAMIC,
            breathingCue = "Inhale on Cow as belly drops; exhale forcefully on Cat as spine arches toward the ceiling.",
            instructions = listOf(
                "Start on all fours with wrists directly under shoulders and knees under hips.",
                "Cow Pose: Inhale, drop your belly toward the floor, and gently gaze upward.",
                "Cat Pose: Exhale, tuck your tailbone, round your spine, and draw your navel toward your spine.",
                "Flow smoothly between Cat and Cow for 8 to 10 rhythmic breath cycles."
            ),
            coachTip = "Move vertebra by vertebra rather than hinging only at the lower back."
        ),

        // BICEPS
        StretchingExercise(
            id = "stretch_biceps_wall",
            name = "Wall-Assisted Biceps & Forearm Extension",
            targetMuscle = "Biceps Brachii (Short & Long Heads) & Forearms",
            muscleGroup = "Biceps",
            recommendedDurationSeconds = 30,
            timing = StretchTiming.POST_WORKOUT_STATIC,
            breathingCue = "Breathe steadily; relax your facial muscles and shoulder.",
            instructions = listOf(
                "Stand perpendicular to a wall and place your palm flat against it at shoulder height.",
                "Point your fingers backward behind you with arm fully extended.",
                "Slowly rotate your chest and torso away from the wall until a stretch runs through the bicep.",
                "Hold for 30 seconds, then switch to the other arm."
            ),
            coachTip = "Do not allow the shoulder of the stretched arm to hike upward toward your ear."
        ),
        StretchingExercise(
            id = "stretch_biceps_reverse_wrist",
            name = "Kneeling Reverse Wrist & Bicep Stretch",
            targetMuscle = "Biceps Tendons, Brachioradialis & Wrist Flexors",
            muscleGroup = "Biceps",
            recommendedDurationSeconds = 25,
            timing = StretchTiming.POST_WORKOUT_STATIC,
            breathingCue = "Gentle breathing; do not force or bounce the wrists.",
            instructions = listOf(
                "Kneel on a soft mat and place palms on the floor with fingers pointing back toward your knees.",
                "Keep palms flat and arms as straight as comfortable.",
                "Gently shift your body weight slightly backward to increase tension on the inner forearm and bicep.",
                "Hold for 20 to 25 seconds."
            ),
            coachTip = "Essential for preventing golfer's elbow and tendon strain from heavy curls and pull-ups."
        ),

        // TRICEPS
        StretchingExercise(
            id = "stretch_triceps_overhead",
            name = "Overhead Single-Arm Triceps Stretch",
            targetMuscle = "Triceps Brachii (Long Head)",
            muscleGroup = "Triceps",
            recommendedDurationSeconds = 30,
            timing = StretchTiming.POST_WORKOUT_STATIC,
            breathingCue = "Inhale tall through the crown of your head; exhale and gently nudge the elbow down.",
            instructions = listOf(
                "Raise your right arm overhead and bend the elbow so your right hand reaches down your upper back.",
                "Use your left hand to gently grasp your right elbow.",
                "Gently press the elbow downward and back until you feel a deep stretch along the back of the arm.",
                "Maintain upright posture without flaring your ribs or arching your lower back."
            ),
            coachTip = "Because the long head of the triceps crosses the shoulder joint, this overhead position is the only way to stretch it completely."
        ),
        StretchingExercise(
            id = "stretch_cross_body_triceps",
            name = "Cross-Chest Triceps & Posterior Delt Stretch",
            targetMuscle = "Triceps Brachii & Posterior Deltoid",
            muscleGroup = "Triceps",
            recommendedDurationSeconds = 25,
            timing = StretchTiming.POST_WORKOUT_STATIC,
            breathingCue = "Steady relaxed breaths; avoid twisting your torso.",
            instructions = listOf(
                "Bring one arm horizontally across your chest at shoulder height.",
                "Use your opposite forearm or hand to hook above the elbow and hug it closer to your torso.",
                "Keep your shoulders pressed down away from your ears.",
                "Hold for 25 seconds per side."
            ),
            coachTip = "Keep hips and chest squared forward to keep the stretch isolated on the arm and rear shoulder."
        ),

        // SHOULDERS
        StretchingExercise(
            id = "stretch_shoulder_cross_arm",
            name = "Cross-Body Deltoid & Rotator Cuff Stretch",
            targetMuscle = "Posterior Deltoid & Infraspinatus",
            muscleGroup = "Shoulders",
            recommendedDurationSeconds = 30,
            timing = StretchTiming.POST_WORKOUT_STATIC,
            breathingCue = "Exhale slowly as you draw the arm across; release tension in the neck.",
            instructions = listOf(
                "Stand tall with shoulders relaxed.",
                "Extend your right arm straight across your chest.",
                "Hook your left arm around your right forearm and gently pull it closer to your body.",
                "Turn your head gently toward the right shoulder to deepen the release."
            ),
            coachTip = "Pull from above the elbow joint to avoid unnecessary stress on the elbow."
        ),
        StretchingExercise(
            id = "stretch_shoulder_eagle_arms",
            name = "Eagle Arms Shoulder Blade Opener",
            targetMuscle = "Rhomboids, Middle Trapezius & Rear Deltoids",
            muscleGroup = "Shoulders",
            recommendedDurationSeconds = 25,
            timing = StretchTiming.PRE_WORKOUT_DYNAMIC,
            breathingCue = "Inhale between your shoulder blades; exhale and gently lift the elbows.",
            instructions = listOf(
                "Extend both arms forward parallel to the floor.",
                "Cross your left arm under your right arm and bend elbows 90 degrees.",
                "Wrap forearms around each other and press palms or backs of hands together.",
                "Gently elevate elbows to shoulder height and draw hands away from your face."
            ),
            coachTip = "Excellent for releasing upper back tightness and improving thoracic mobility before pressing movements."
        ),

        // LEGS
        StretchingExercise(
            id = "stretch_quad_standing",
            name = "Standing Quadriceps & Hip Flexor Stretch",
            targetMuscle = "Quadriceps (Rectus Femoris, Vastus Lateralis)",
            muscleGroup = "Legs",
            recommendedDurationSeconds = 30,
            timing = StretchTiming.POST_WORKOUT_STATIC,
            breathingCue = "Deep calm breaths; squeeze glute of standing side for stability.",
            instructions = listOf(
                "Stand upright holding a wall or rack with one hand for balance.",
                "Bend your right knee and grasp your right ankle with your right hand behind you.",
                "Gently pull your heel toward your glute while keeping both knees close together.",
                "Squeeze your right glute slightly to push the hip forward and deepen the quad stretch."
            ),
            coachTip = "Keep your standing knee slightly soft and avoid letting your bent knee flare out to the side."
        ),
        StretchingExercise(
            id = "stretch_hamstring_seated",
            name = "Single-Leg Seated Hamstring Reach",
            targetMuscle = "Biceps Femoris, Semitendinosus & Calves",
            muscleGroup = "Legs",
            recommendedDurationSeconds = 35,
            timing = StretchTiming.POST_WORKOUT_STATIC,
            breathingCue = "Inhale lengthen spine; exhale hinge forward from hips over the extended leg.",
            instructions = listOf(
                "Sit on the floor with your right leg straight and left leg bent with foot against right inner thigh.",
                "Hinge forward from your hips with a flat back, reaching your hands toward your right shin or foot.",
                "Keep your right toes flexed back toward your shin.",
                "Hold for 30 to 35 seconds, then switch legs."
            ),
            coachTip = "Hinge from the hips rather than rounding your upper back. Leading with your chest ensures the stretch hits the hamstring belly."
        ),
        StretchingExercise(
            id = "stretch_hip_flexor_kneeling",
            name = "Kneeling Lunge Hip Flexor Stretch",
            targetMuscle = "Psoas Major, Iliacus & Tensor Fasciae Latae",
            muscleGroup = "Legs",
            recommendedDurationSeconds = 30,
            timing = StretchTiming.POST_WORKOUT_STATIC,
            breathingCue = "Exhale and shift hips slightly forward; maintain upright posture.",
            instructions = listOf(
                "Step your left foot forward into a lunge with your right knee resting on a padded mat.",
                "Tuck your pelvis under (posterior pelvic tilt) and squeeze your right glute.",
                "Shift your weight slightly forward until you feel a deep stretch in the front of your right hip.",
                "Optionally raise your right arm straight overhead to increase the stretch."
            ),
            coachTip = "Tucking the tailbone is the secret to isolating the psoas rather than hyperextending the lumbar spine."
        ),
        StretchingExercise(
            id = "stretch_calf_wall",
            name = "Standing Wall Calf & Achilles Stretch",
            targetMuscle = "Gastrocnemius & Soleus",
            muscleGroup = "Legs",
            recommendedDurationSeconds = 30,
            timing = StretchTiming.POST_WORKOUT_STATIC,
            breathingCue = "Breathe smoothly; sink heel firmly into floor on every exhalation.",
            instructions = listOf(
                "Stand facing a wall with hands placed flat against it at chest height.",
                "Step your right leg back about two feet, keeping the back heel firmly pressed on the floor.",
                "Bend your front knee and lean toward the wall with a straight back leg to stretch the gastrocnemius.",
                "Slightly bend the back knee to shift the stretch deeper into the lower calf (soleus)."
            ),
            coachTip = "Crucial for ankle dorsiflexion, which directly improves squat depth and prevents knee valgus."
        ),

        // ABS & CORE
        StretchingExercise(
            id = "stretch_abs_cobra",
            name = "Cobra Abdominal & Hip Stretch",
            targetMuscle = "Rectus Abdominis & Psoas",
            muscleGroup = "Abs",
            recommendedDurationSeconds = 30,
            timing = StretchTiming.POST_WORKOUT_STATIC,
            breathingCue = "Inhale as you press up; exhale and relax your abdominal wall into the curve.",
            instructions = listOf(
                "Lie face down on an exercise mat with hands planted under your shoulders.",
                "Press into your palms to lift your chest off the floor while keeping hips on the ground.",
                "Roll your shoulders back and down away from your ears.",
                "Lengthen through the neck and hold for 25 to 30 seconds."
            ),
            coachTip = "Only lift as high as comfortable for your lower back. Squeeze glutes lightly to protect the lumbar spine."
        ),
        StretchingExercise(
            id = "stretch_lying_spinal_twist",
            name = "Lying Supine Torso Twist",
            targetMuscle = "Internal & External Obliques & Lower Back",
            muscleGroup = "Abs",
            recommendedDurationSeconds = 30,
            timing = StretchTiming.DAILY_MOBILITY,
            breathingCue = "Exhale as the knees drop to the side; allow gravity to do the work.",
            instructions = listOf(
                "Lie flat on your back with arms extended out in a 'T' position at shoulder height.",
                "Bend your knees and draw them toward your chest.",
                "Slowly let both knees fall to the right side while keeping both shoulders pinned to the mat.",
                "Turn your head to look toward your left hand, hold for 30 seconds, then repeat on opposite side."
            ),
            coachTip = "Keep the opposite shoulder anchored to the floor to ensure rotational stretch through the thoracic spine."
        )
    )

    private val stretchesById: Map<String, StretchingExercise> = stretches.associateBy { it.id }

    fun getStretchById(id: String): StretchingExercise? = stretchesById[id]

    fun getStretchesByMuscleGroup(muscleGroup: String): List<StretchingExercise> {
        val norm = muscleGroup.trim().lowercase()
        return stretches.filter {
            it.muscleGroup.lowercase().contains(norm) ||
                    it.targetMuscle.lowercase().contains(norm) ||
                    norm.contains(it.muscleGroup.lowercase())
        }
    }

    fun getStretchesForExercise(exerciseNameOrCategory: String): List<StretchingExercise> {
        val norm = exerciseNameOrCategory.trim().lowercase()
        return when {
            norm.contains("chest") || norm.contains("press") || norm.contains("fly") || norm.contains("pec") ->
                getStretchesByMuscleGroup("Chest")

            norm.contains("lat") || norm.contains("row") || norm.contains("back") || norm.contains("pull") || norm.contains("deadlift") ->
                getStretchesByMuscleGroup("Back")

            norm.contains("bicep") || norm.contains("curl") || norm.contains("arm") ->
                getStretchesByMuscleGroup("Biceps")

            norm.contains("tricep") || norm.contains("pushdown") || norm.contains("dip") || norm.contains("skull") ->
                getStretchesByMuscleGroup("Triceps")

            norm.contains("shoulder") || norm.contains("overhead") || norm.contains("delt") || norm.contains("lateral") ->
                getStretchesByMuscleGroup("Shoulders")

            norm.contains("squat") || norm.contains("leg") || norm.contains("quad") || norm.contains("hamstring") || norm.contains("calf") ->
                getStretchesByMuscleGroup("Legs")

            norm.contains("ab") || norm.contains("core") || norm.contains("plank") || norm.contains("crunch") ->
                getStretchesByMuscleGroup("Abs")

            else -> stretches.take(4)
        }
    }
}

