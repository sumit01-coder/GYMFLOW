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
            coachTip = "Adjust your elbow height: higher targets the lower pec; lower targets the upper pec fibers.",
            gifUrl = "https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/pectorals/behind-head-chest-stretch.gif"
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
            coachTip = "If your hands cannot interlace comfortably, hold a small towel or resistance band between hands.",
            gifUrl = "https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/pectorals/dynamic-chest-stretch-male.gif"
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
            coachTip = "Outstanding for spinal decompression and relieving lower back tightness after heavy squats or deadlifts.",
            gifUrl = "https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/lats/exercise-ball-lat-stretch.gif"
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
            coachTip = "Keep the opposite hip pinned firmly to your heel to maximize the lateral lat stretch.",
            gifUrl = "https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/lats/kneeling-lat-stretch.gif"
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
            coachTip = "Move vertebra by vertebra rather than hinging only at the lower back.",
            gifUrl = "https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/lats/side-lying-floor-stretch.gif"
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
            coachTip = "Do not allow the shoulder of the stretched arm to hike upward toward your ear.",
            gifUrl = "https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/forearms/side-wrist-pull-stretch.gif"
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
            coachTip = "Essential for preventing golfer's elbow and tendon strain from heavy curls and pull-ups.",
            gifUrl = "https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/forearms/side-wrist-pull-stretch.gif"
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
            coachTip = "Because the long head of the triceps crosses the shoulder joint, this overhead position is the only way to stretch it completely.",
            gifUrl = "https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/triceps/overhead-triceps-stretch.gif"
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
            coachTip = "Keep hips and chest squared forward to keep the stretch isolated on the arm and rear shoulder.",
            gifUrl = "https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/delts/rear-deltoid-stretch.gif"
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
            coachTip = "Pull from above the elbow joint to avoid unnecessary stress on the elbow.",
            gifUrl = "https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/delts/rear-deltoid-stretch.gif"
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
            coachTip = "Excellent for releasing upper back tightness and improving thoracic mobility before pressing movements.",
            gifUrl = "https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/upper-back/upper-back-stretch.gif"
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
            coachTip = "Keep your standing knee slightly soft and avoid letting your bent knee flare out to the side.",
            gifUrl = "https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/quads/intermediate-hip-flexor-and-quad-stretch.gif"
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
            coachTip = "Hinge from the hips rather than rounding your upper back. Leading with your chest ensures the stretch hits the hamstring belly.",
            gifUrl = "https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/hamstrings/exercise-ball-seated-hamstring-stretch.gif"
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
            coachTip = "Tucking the tailbone is the secret to isolating the psoas rather than hyperextending the lumbar spine.",
            gifUrl = "https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/glutes/exercise-ball-hip-flexor-stretch.gif"
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
            coachTip = "Crucial for ankle dorsiflexion, which directly improves squat depth and prevents knee valgus.",
            gifUrl = "https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/calves/calf-stretch-with-hands-against-wall.gif"
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
            coachTip = "Only lift as high as comfortable for your lower back. Squeeze glutes lightly to protect the lumbar spine.",
            gifUrl = "https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/spine/spine-stretch.gif"
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
            coachTip = "Keep the opposite shoulder anchored to the floor to ensure rotational stretch through the thoracic spine.",
            gifUrl = "https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/glutes/iron-cross-stretch.gif"
        ),

        // OPEN-SOURCE STRETCH (ABS)
        StretchingExercise(
            id = "stretch_os_assisted_prone_rectus_femoris_stretch",
            name = "Assisted Prone Rectus Femoris Stretch",
            targetMuscle = "Rectus Abdominis & Obliques",
            muscleGroup = "Abs",
            recommendedDurationSeconds = 30,
            timing = StretchTiming.POST_WORKOUT_STATIC,
            breathingCue = "Exhale deeply and lengthen through your abdominal wall without over-arching your lumbar spine.",
            instructions = listOf(
                "Adopt the starting position with proper body alignment.",
                "Move into the the core stretch position.",
                "Hold for 20 to 40 seconds breathing deeply.",
                "Return slowly to the starting position and repeat if desired."
            ),
            coachTip = "Focus on relaxing the anterior abdominal wall; do not hyperextend your lower back.",
            gifUrl = "https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/abs/assisted-prone-rectus-femoris-stretch.gif"
        ),

        // OPEN-SOURCE STRETCH (LEGS)
        StretchingExercise(
            id = "stretch_os_assisted_side_lying_adductor_stretch",
            name = "Assisted Side Lying Adductor Stretch",
            targetMuscle = "Adductor Longus, Brevis & Gracilis",
            muscleGroup = "Legs",
            recommendedDurationSeconds = 30,
            timing = StretchTiming.POST_WORKOUT_STATIC,
            breathingCue = "Inhale through your nose; exhale slowly as gravity gently draws your knees wider apart.",
            instructions = listOf(
                "Adopt the starting position with proper body alignment.",
                "Move into the the adductors stretch position.",
                "Hold for 20 to 40 seconds breathing deeply.",
                "Return slowly to the starting position and repeat if desired."
            ),
            coachTip = "Never bounce in an adductor stretch; maintain a gentle steady stretch along the inner thigh.",
            gifUrl = "https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/adductors/assisted-side-lying-adductor-stretch.gif"
        ),

        // OPEN-SOURCE STRETCH (LEGS)
        StretchingExercise(
            id = "stretch_os_assisted_lying_calves_stretch",
            name = "Assisted Lying Calves Stretch",
            targetMuscle = "Gastrocnemius & Soleus",
            muscleGroup = "Legs",
            recommendedDurationSeconds = 30,
            timing = StretchTiming.POST_WORKOUT_STATIC,
            breathingCue = "Exhale as you press your heel firmly toward the floor, keeping your back leg straight.",
            instructions = listOf(
                "Adopt the starting position with proper body alignment.",
                "Move into the the calves stretch position.",
                "Hold for 20 to 40 seconds breathing deeply.",
                "Return slowly to the starting position and repeat if desired."
            ),
            coachTip = "Keep your back knee fully locked to emphasize the gastrocnemius, or slightly bent to isolate the soleus.",
            gifUrl = "https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/calves/assisted-lying-calves-stretch.gif"
        ),

        // OPEN-SOURCE STRETCH (LEGS)
        StretchingExercise(
            id = "stretch_os_calf_push_stretch_with_hands_against_wall",
            name = "Calf Push Stretch With Hands Against Wall",
            targetMuscle = "Gastrocnemius & Soleus",
            muscleGroup = "Legs",
            recommendedDurationSeconds = 30,
            timing = StretchTiming.POST_WORKOUT_STATIC,
            breathingCue = "Exhale as you press your heel firmly toward the floor, keeping your back leg straight.",
            instructions = listOf(
                "Adopt the starting position with proper body alignment.",
                "Move into the the calves stretch position.",
                "Hold for 20 to 40 seconds breathing deeply.",
                "Return slowly to the starting position and repeat if desired."
            ),
            coachTip = "Keep your back knee fully locked to emphasize the gastrocnemius, or slightly bent to isolate the soleus.",
            gifUrl = "https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/calves/calf-push-stretch-with-hands-against-wall.gif"
        ),

        // OPEN-SOURCE STRETCH (LEGS)
        StretchingExercise(
            id = "stretch_os_calf_stretch_with_hands_against_wall",
            name = "Calf Stretch With Hands Against Wall",
            targetMuscle = "Gastrocnemius & Soleus",
            muscleGroup = "Legs",
            recommendedDurationSeconds = 30,
            timing = StretchTiming.POST_WORKOUT_STATIC,
            breathingCue = "Exhale as you press your heel firmly toward the floor, keeping your back leg straight.",
            instructions = listOf(
                "Adopt the starting position with proper body alignment.",
                "Move into the the calves stretch position.",
                "Hold for 20 to 40 seconds breathing deeply.",
                "Return slowly to the starting position and repeat if desired."
            ),
            coachTip = "Keep your back knee fully locked to emphasize the gastrocnemius, or slightly bent to isolate the soleus.",
            gifUrl = "https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/calves/calf-stretch-with-hands-against-wall.gif"
        ),

        // OPEN-SOURCE STRETCH (LEGS)
        StretchingExercise(
            id = "stretch_os_calf_stretch_with_rope",
            name = "Calf Stretch With Rope",
            targetMuscle = "Gastrocnemius & Soleus",
            muscleGroup = "Legs",
            recommendedDurationSeconds = 30,
            timing = StretchTiming.POST_WORKOUT_STATIC,
            breathingCue = "Exhale as you press your heel firmly toward the floor, keeping your back leg straight.",
            instructions = listOf(
                "Adopt the starting position with proper body alignment.",
                "Move into the the calves stretch position.",
                "Hold for 20 to 40 seconds breathing deeply.",
                "Return slowly to the starting position and repeat if desired."
            ),
            coachTip = "Keep your back knee fully locked to emphasize the gastrocnemius, or slightly bent to isolate the soleus.",
            gifUrl = "https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/calves/calf-stretch-with-rope.gif"
        ),

        // OPEN-SOURCE STRETCH (LEGS)
        StretchingExercise(
            id = "stretch_os_circles_knee_stretch",
            name = "Circles Knee Stretch",
            targetMuscle = "Gastrocnemius & Soleus",
            muscleGroup = "Legs",
            recommendedDurationSeconds = 25,
            timing = StretchTiming.PRE_WORKOUT_DYNAMIC,
            breathingCue = "Exhale as you press your heel firmly toward the floor, keeping your back leg straight.",
            instructions = listOf(
                "Adopt the starting position with proper body alignment.",
                "Move into the the calves stretch position.",
                "Hold for 20 to 40 seconds breathing deeply.",
                "Return slowly to the starting position and repeat if desired."
            ),
            coachTip = "Keep your back knee fully locked to emphasize the gastrocnemius, or slightly bent to isolate the soleus.",
            gifUrl = "https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/calves/circles-knee-stretch.gif"
        ),

        // OPEN-SOURCE STRETCH (LEGS)
        StretchingExercise(
            id = "stretch_os_peroneals_stretch",
            name = "Peroneals Stretch",
            targetMuscle = "Gastrocnemius & Soleus",
            muscleGroup = "Legs",
            recommendedDurationSeconds = 30,
            timing = StretchTiming.POST_WORKOUT_STATIC,
            breathingCue = "Exhale as you press your heel firmly toward the floor, keeping your back leg straight.",
            instructions = listOf(
                "Adopt the starting position with proper body alignment.",
                "Move into the the calves stretch position.",
                "Hold for 20 to 40 seconds breathing deeply.",
                "Return slowly to the starting position and repeat if desired."
            ),
            coachTip = "Keep your back knee fully locked to emphasize the gastrocnemius, or slightly bent to isolate the soleus.",
            gifUrl = "https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/calves/peroneals-stretch.gif"
        ),

        // OPEN-SOURCE STRETCH (LEGS)
        StretchingExercise(
            id = "stretch_os_posterior_tibialis_stretch",
            name = "Posterior Tibialis Stretch",
            targetMuscle = "Gastrocnemius & Soleus",
            muscleGroup = "Legs",
            recommendedDurationSeconds = 30,
            timing = StretchTiming.POST_WORKOUT_STATIC,
            breathingCue = "Exhale as you press your heel firmly toward the floor, keeping your back leg straight.",
            instructions = listOf(
                "Adopt the starting position with proper body alignment.",
                "Move into the the calves stretch position.",
                "Hold for 20 to 40 seconds breathing deeply.",
                "Return slowly to the starting position and repeat if desired."
            ),
            coachTip = "Keep your back knee fully locked to emphasize the gastrocnemius, or slightly bent to isolate the soleus.",
            gifUrl = "https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/calves/posterior-tibialis-stretch.gif"
        ),

        // OPEN-SOURCE STRETCH (LEGS)
        StretchingExercise(
            id = "stretch_os_seated_calf_stretch_male",
            name = "Seated Calf Stretch Male",
            targetMuscle = "Gastrocnemius & Soleus",
            muscleGroup = "Legs",
            recommendedDurationSeconds = 30,
            timing = StretchTiming.POST_WORKOUT_STATIC,
            breathingCue = "Exhale as you press your heel firmly toward the floor, keeping your back leg straight.",
            instructions = listOf(
                "Adopt the starting position with proper body alignment.",
                "Move into the the calves stretch position.",
                "Hold for 20 to 40 seconds breathing deeply.",
                "Return slowly to the starting position and repeat if desired."
            ),
            coachTip = "Keep your back knee fully locked to emphasize the gastrocnemius, or slightly bent to isolate the soleus.",
            gifUrl = "https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/calves/seated-calf-stretch-male.gif"
        ),

        // OPEN-SOURCE STRETCH (LEGS)
        StretchingExercise(
            id = "stretch_os_standing_calves_calf_stretch",
            name = "Standing Calves Calf Stretch",
            targetMuscle = "Gastrocnemius & Soleus",
            muscleGroup = "Legs",
            recommendedDurationSeconds = 30,
            timing = StretchTiming.POST_WORKOUT_STATIC,
            breathingCue = "Exhale as you press your heel firmly toward the floor, keeping your back leg straight.",
            instructions = listOf(
                "Adopt the starting position with proper body alignment.",
                "Move into the the calves stretch position.",
                "Hold for 20 to 40 seconds breathing deeply.",
                "Return slowly to the starting position and repeat if desired."
            ),
            coachTip = "Keep your back knee fully locked to emphasize the gastrocnemius, or slightly bent to isolate the soleus.",
            gifUrl = "https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/calves/standing-calves-calf-stretch.gif"
        ),

        // OPEN-SOURCE STRETCH (SHOULDERS)
        StretchingExercise(
            id = "stretch_os_rear_deltoid_stretch",
            name = "Rear Deltoid Stretch",
            targetMuscle = "Posterior Deltoids & Rotator Cuff",
            muscleGroup = "Shoulders",
            recommendedDurationSeconds = 30,
            timing = StretchTiming.POST_WORKOUT_STATIC,
            breathingCue = "Inhale to lengthen your spine; exhale as you gently draw your arm across your chest.",
            instructions = listOf(
                "Adopt the starting position with proper body alignment.",
                "Move into the the shoulders stretch position.",
                "Hold for 20 to 40 seconds breathing deeply.",
                "Return slowly to the starting position and repeat if desired."
            ),
            coachTip = "Keep your shoulders pressed down away from your ears to avoid impingement.",
            gifUrl = "https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/delts/rear-deltoid-stretch.gif"
        ),

        // OPEN-SOURCE STRETCH (BICEPS)
        StretchingExercise(
            id = "stretch_os_side_wrist_pull_stretch",
            name = "Side Wrist Pull Stretch",
            targetMuscle = "Forearm Flexors, Extensors & Carpal Tunnel",
            muscleGroup = "Biceps",
            recommendedDurationSeconds = 30,
            timing = StretchTiming.POST_WORKOUT_STATIC,
            breathingCue = "Breathe smoothly; gently extend through your fingertips and wrist.",
            instructions = listOf(
                "Adopt the starting position with proper body alignment.",
                "Move into the the forearms stretch position.",
                "Hold for 20 to 40 seconds breathing deeply.",
                "Return slowly to the starting position and repeat if desired."
            ),
            coachTip = "Keep your elbow fully straight to feel the stretch through the full length of the forearm.",
            gifUrl = "https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/forearms/side-wrist-pull-stretch.gif"
        ),

        // OPEN-SOURCE STRETCH (LEGS)
        StretchingExercise(
            id = "stretch_os_assisted_lying_glutes_stretch",
            name = "Assisted Lying Glutes Stretch",
            targetMuscle = "Gluteus Maximus, Medius & Piriformis",
            muscleGroup = "Legs",
            recommendedDurationSeconds = 30,
            timing = StretchTiming.POST_WORKOUT_STATIC,
            breathingCue = "Exhale as you draw your thigh closer to your chest, relaxing tension in the hip.",
            instructions = listOf(
                "Adopt the starting position with proper body alignment.",
                "Move into the the glutes stretch position.",
                "Hold for 20 to 40 seconds breathing deeply.",
                "Return slowly to the starting position and repeat if desired."
            ),
            coachTip = "Maintain a neutral spine and avoid rounding your lower back off the floor.",
            gifUrl = "https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/glutes/assisted-lying-glutes-stretch.gif"
        ),

        // OPEN-SOURCE STRETCH (LEGS)
        StretchingExercise(
            id = "stretch_os_assisted_lying_gluteus_and_piriformis_stretch",
            name = "Assisted Lying Gluteus And Piriformis Stretch",
            targetMuscle = "Gluteus Maximus, Medius & Piriformis",
            muscleGroup = "Legs",
            recommendedDurationSeconds = 30,
            timing = StretchTiming.POST_WORKOUT_STATIC,
            breathingCue = "Exhale as you draw your thigh closer to your chest, relaxing tension in the hip.",
            instructions = listOf(
                "Adopt the starting position with proper body alignment.",
                "Move into the the glutes stretch position.",
                "Hold for 20 to 40 seconds breathing deeply.",
                "Return slowly to the starting position and repeat if desired."
            ),
            coachTip = "Maintain a neutral spine and avoid rounding your lower back off the floor.",
            gifUrl = "https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/glutes/assisted-lying-gluteus-and-piriformis-stretch.gif"
        ),

        // OPEN-SOURCE STRETCH (LEGS)
        StretchingExercise(
            id = "stretch_os_exercise_ball_hip_flexor_stretch",
            name = "Exercise Ball Hip Flexor Stretch",
            targetMuscle = "Gluteus Maximus, Medius & Piriformis",
            muscleGroup = "Legs",
            recommendedDurationSeconds = 30,
            timing = StretchTiming.POST_WORKOUT_STATIC,
            breathingCue = "Exhale as you draw your thigh closer to your chest, relaxing tension in the hip.",
            instructions = listOf(
                "Adopt the starting position with proper body alignment.",
                "Move into the the glutes stretch position.",
                "Hold for 20 to 40 seconds breathing deeply.",
                "Return slowly to the starting position and repeat if desired."
            ),
            coachTip = "Maintain a neutral spine and avoid rounding your lower back off the floor.",
            gifUrl = "https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/glutes/exercise-ball-hip-flexor-stretch.gif"
        ),

        // OPEN-SOURCE STRETCH (LEGS)
        StretchingExercise(
            id = "stretch_os_iron_cross_stretch",
            name = "Iron Cross Stretch",
            targetMuscle = "Gluteus Maximus, Medius & Piriformis",
            muscleGroup = "Legs",
            recommendedDurationSeconds = 30,
            timing = StretchTiming.POST_WORKOUT_STATIC,
            breathingCue = "Exhale as you draw your thigh closer to your chest, relaxing tension in the hip.",
            instructions = listOf(
                "Adopt the starting position with proper body alignment.",
                "Move into the the glutes stretch position.",
                "Hold for 20 to 40 seconds breathing deeply.",
                "Return slowly to the starting position and repeat if desired."
            ),
            coachTip = "Maintain a neutral spine and avoid rounding your lower back off the floor.",
            gifUrl = "https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/glutes/iron-cross-stretch.gif"
        ),

        // OPEN-SOURCE STRETCH (LEGS)
        StretchingExercise(
            id = "stretch_os_rocking_frog_stretch",
            name = "Rocking Frog Stretch",
            targetMuscle = "Gluteus Maximus, Medius & Piriformis",
            muscleGroup = "Legs",
            recommendedDurationSeconds = 25,
            timing = StretchTiming.PRE_WORKOUT_DYNAMIC,
            breathingCue = "Exhale as you draw your thigh closer to your chest, relaxing tension in the hip.",
            instructions = listOf(
                "Adopt the starting position with proper body alignment.",
                "Move into the the glutes stretch position.",
                "Hold for 20 to 40 seconds breathing deeply.",
                "Return slowly to the starting position and repeat if desired."
            ),
            coachTip = "Maintain a neutral spine and avoid rounding your lower back off the floor.",
            gifUrl = "https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/glutes/rocking-frog-stretch.gif"
        ),

        // OPEN-SOURCE STRETCH (LEGS)
        StretchingExercise(
            id = "stretch_os_roller_hip_lat_stretch",
            name = "Roller Hip Lat Stretch",
            targetMuscle = "Gluteus Maximus, Medius & Piriformis",
            muscleGroup = "Legs",
            recommendedDurationSeconds = 30,
            timing = StretchTiming.POST_WORKOUT_STATIC,
            breathingCue = "Exhale as you draw your thigh closer to your chest, relaxing tension in the hip.",
            instructions = listOf(
                "Adopt the starting position with proper body alignment.",
                "Move into the the glutes stretch position.",
                "Hold for 20 to 40 seconds breathing deeply.",
                "Return slowly to the starting position and repeat if desired."
            ),
            coachTip = "Maintain a neutral spine and avoid rounding your lower back off the floor.",
            gifUrl = "https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/glutes/roller-hip-lat-stretch.gif"
        ),

        // OPEN-SOURCE STRETCH (LEGS)
        StretchingExercise(
            id = "stretch_os_roller_hip_stretch",
            name = "Roller Hip Stretch",
            targetMuscle = "Gluteus Maximus, Medius & Piriformis",
            muscleGroup = "Legs",
            recommendedDurationSeconds = 30,
            timing = StretchTiming.POST_WORKOUT_STATIC,
            breathingCue = "Exhale as you draw your thigh closer to your chest, relaxing tension in the hip.",
            instructions = listOf(
                "Adopt the starting position with proper body alignment.",
                "Move into the the glutes stretch position.",
                "Hold for 20 to 40 seconds breathing deeply.",
                "Return slowly to the starting position and repeat if desired."
            ),
            coachTip = "Maintain a neutral spine and avoid rounding your lower back off the floor.",
            gifUrl = "https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/glutes/roller-hip-stretch.gif"
        ),

        // OPEN-SOURCE STRETCH (LEGS)
        StretchingExercise(
            id = "stretch_os_seated_glute_stretch",
            name = "Seated Glute Stretch",
            targetMuscle = "Gluteus Maximus, Medius & Piriformis",
            muscleGroup = "Legs",
            recommendedDurationSeconds = 30,
            timing = StretchTiming.POST_WORKOUT_STATIC,
            breathingCue = "Exhale as you draw your thigh closer to your chest, relaxing tension in the hip.",
            instructions = listOf(
                "Adopt the starting position with proper body alignment.",
                "Move into the the glutes stretch position.",
                "Hold for 20 to 40 seconds breathing deeply.",
                "Return slowly to the starting position and repeat if desired."
            ),
            coachTip = "Maintain a neutral spine and avoid rounding your lower back off the floor.",
            gifUrl = "https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/glutes/seated-glute-stretch.gif"
        ),

        // OPEN-SOURCE STRETCH (LEGS)
        StretchingExercise(
            id = "stretch_os_seated_piriformis_stretch",
            name = "Seated Piriformis Stretch",
            targetMuscle = "Gluteus Maximus, Medius & Piriformis",
            muscleGroup = "Legs",
            recommendedDurationSeconds = 30,
            timing = StretchTiming.POST_WORKOUT_STATIC,
            breathingCue = "Exhale as you draw your thigh closer to your chest, relaxing tension in the hip.",
            instructions = listOf(
                "Adopt the starting position with proper body alignment.",
                "Move into the the glutes stretch position.",
                "Hold for 20 to 40 seconds breathing deeply.",
                "Return slowly to the starting position and repeat if desired."
            ),
            coachTip = "Maintain a neutral spine and avoid rounding your lower back off the floor.",
            gifUrl = "https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/glutes/seated-piriformis-stretch.gif"
        ),

        // OPEN-SOURCE STRETCH (LEGS)
        StretchingExercise(
            id = "stretch_os_weighted_stretch_lunge",
            name = "Weighted Stretch Lunge",
            targetMuscle = "Gluteus Maximus, Medius & Piriformis",
            muscleGroup = "Legs",
            recommendedDurationSeconds = 30,
            timing = StretchTiming.POST_WORKOUT_STATIC,
            breathingCue = "Exhale as you draw your thigh closer to your chest, relaxing tension in the hip.",
            instructions = listOf(
                "Adopt the starting position with proper body alignment.",
                "Move into the the glutes stretch position.",
                "Hold for 20 to 40 seconds breathing deeply.",
                "Return slowly to the starting position and repeat if desired."
            ),
            coachTip = "Maintain a neutral spine and avoid rounding your lower back off the floor.",
            gifUrl = "https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/glutes/weighted-stretch-lunge.gif"
        ),

        // OPEN-SOURCE STRETCH (LEGS)
        StretchingExercise(
            id = "stretch_os_exercise_ball_seated_hamstring_stretch",
            name = "Exercise Ball Seated Hamstring Stretch",
            targetMuscle = "Biceps Femoris, Semitendinosus & Semimembranosus",
            muscleGroup = "Legs",
            recommendedDurationSeconds = 30,
            timing = StretchTiming.POST_WORKOUT_STATIC,
            breathingCue = "Inhale deep into your belly; exhale as you hinge forward from the hips with a flat back.",
            instructions = listOf(
                "Adopt the starting position with proper body alignment.",
                "Move into the the hamstrings stretch position.",
                "Hold for 20 to 40 seconds breathing deeply.",
                "Return slowly to the starting position and repeat if desired."
            ),
            coachTip = "Hinge from the hips rather than rounding your thoracic spine to ensure the hamstrings are stretched.",
            gifUrl = "https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/hamstrings/exercise-ball-seated-hamstring-stretch.gif"
        ),

        // OPEN-SOURCE STRETCH (LEGS)
        StretchingExercise(
            id = "stretch_os_hamstring_stretch",
            name = "Hamstring Stretch",
            targetMuscle = "Biceps Femoris, Semitendinosus & Semimembranosus",
            muscleGroup = "Legs",
            recommendedDurationSeconds = 30,
            timing = StretchTiming.POST_WORKOUT_STATIC,
            breathingCue = "Inhale deep into your belly; exhale as you hinge forward from the hips with a flat back.",
            instructions = listOf(
                "Adopt the starting position with proper body alignment.",
                "Move into the the hamstrings stretch position.",
                "Hold for 20 to 40 seconds breathing deeply.",
                "Return slowly to the starting position and repeat if desired."
            ),
            coachTip = "Hinge from the hips rather than rounding your thoracic spine to ensure the hamstrings are stretched.",
            gifUrl = "https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/hamstrings/hamstring-stretch.gif"
        ),

        // OPEN-SOURCE STRETCH (LEGS)
        StretchingExercise(
            id = "stretch_os_leg_up_hamstring_stretch",
            name = "Leg Up Hamstring Stretch",
            targetMuscle = "Biceps Femoris, Semitendinosus & Semimembranosus",
            muscleGroup = "Legs",
            recommendedDurationSeconds = 30,
            timing = StretchTiming.POST_WORKOUT_STATIC,
            breathingCue = "Inhale deep into your belly; exhale as you hinge forward from the hips with a flat back.",
            instructions = listOf(
                "Adopt the starting position with proper body alignment.",
                "Move into the the hamstrings stretch position.",
                "Hold for 20 to 40 seconds breathing deeply.",
                "Return slowly to the starting position and repeat if desired."
            ),
            coachTip = "Hinge from the hips rather than rounding your thoracic spine to ensure the hamstrings are stretched.",
            gifUrl = "https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/hamstrings/leg-up-hamstring-stretch.gif"
        ),

        // OPEN-SOURCE STRETCH (LEGS)
        StretchingExercise(
            id = "stretch_os_runners_stretch",
            name = "Runners Stretch",
            targetMuscle = "Biceps Femoris, Semitendinosus & Semimembranosus",
            muscleGroup = "Legs",
            recommendedDurationSeconds = 30,
            timing = StretchTiming.POST_WORKOUT_STATIC,
            breathingCue = "Inhale deep into your belly; exhale as you hinge forward from the hips with a flat back.",
            instructions = listOf(
                "Adopt the starting position with proper body alignment.",
                "Move into the the hamstrings stretch position.",
                "Hold for 20 to 40 seconds breathing deeply.",
                "Return slowly to the starting position and repeat if desired."
            ),
            coachTip = "Hinge from the hips rather than rounding your thoracic spine to ensure the hamstrings are stretched.",
            gifUrl = "https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/hamstrings/runners-stretch.gif"
        ),

        // OPEN-SOURCE STRETCH (LEGS)
        StretchingExercise(
            id = "stretch_os_standing_hamstring_and_calf_stretch_with_strap",
            name = "Standing Hamstring And Calf Stretch With Strap",
            targetMuscle = "Biceps Femoris, Semitendinosus & Semimembranosus",
            muscleGroup = "Legs",
            recommendedDurationSeconds = 30,
            timing = StretchTiming.POST_WORKOUT_STATIC,
            breathingCue = "Inhale deep into your belly; exhale as you hinge forward from the hips with a flat back.",
            instructions = listOf(
                "Adopt the starting position with proper body alignment.",
                "Move into the the hamstrings stretch position.",
                "Hold for 20 to 40 seconds breathing deeply.",
                "Return slowly to the starting position and repeat if desired."
            ),
            coachTip = "Hinge from the hips rather than rounding your thoracic spine to ensure the hamstrings are stretched.",
            gifUrl = "https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/hamstrings/standing-hamstring-and-calf-stretch-with-strap.gif"
        ),

        // OPEN-SOURCE STRETCH (LEGS)
        StretchingExercise(
            id = "stretch_os_world_greatest_stretch",
            name = "World Greatest Stretch",
            targetMuscle = "Biceps Femoris, Semitendinosus & Semimembranosus",
            muscleGroup = "Legs",
            recommendedDurationSeconds = 25,
            timing = StretchTiming.PRE_WORKOUT_DYNAMIC,
            breathingCue = "Inhale deep into your belly; exhale as you hinge forward from the hips with a flat back.",
            instructions = listOf(
                "Adopt the starting position with proper body alignment.",
                "Move into the the hamstrings stretch position.",
                "Hold for 20 to 40 seconds breathing deeply.",
                "Return slowly to the starting position and repeat if desired."
            ),
            coachTip = "Hinge from the hips rather than rounding your thoracic spine to ensure the hamstrings are stretched.",
            gifUrl = "https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/hamstrings/world-greatest-stretch.gif"
        ),

        // OPEN-SOURCE STRETCH (BACK)
        StretchingExercise(
            id = "stretch_os_back_pec_stretch",
            name = "Back Pec Stretch",
            targetMuscle = "Latissimus Dorsi & Teres Major",
            muscleGroup = "Back",
            recommendedDurationSeconds = 30,
            timing = StretchTiming.POST_WORKOUT_STATIC,
            breathingCue = "Inhale into the side of your ribcage; exhale as you reach further into the lateral line.",
            instructions = listOf(
                "Adopt the starting position with proper body alignment.",
                "Move into the the lats stretch position.",
                "Hold for 20 to 40 seconds breathing deeply.",
                "Return slowly to the starting position and repeat if desired."
            ),
            coachTip = "Keep your core engaged to direct the stretch into the lateral lats rather than the lumbar spine.",
            gifUrl = "https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/lats/back-pec-stretch.gif"
        ),

        // OPEN-SOURCE STRETCH (BACK)
        StretchingExercise(
            id = "stretch_os_exercise_ball_lat_stretch",
            name = "Exercise Ball Lat Stretch",
            targetMuscle = "Latissimus Dorsi & Teres Major",
            muscleGroup = "Back",
            recommendedDurationSeconds = 30,
            timing = StretchTiming.POST_WORKOUT_STATIC,
            breathingCue = "Inhale into the side of your ribcage; exhale as you reach further into the lateral line.",
            instructions = listOf(
                "Adopt the starting position with proper body alignment.",
                "Move into the the lats stretch position.",
                "Hold for 20 to 40 seconds breathing deeply.",
                "Return slowly to the starting position and repeat if desired."
            ),
            coachTip = "Keep your core engaged to direct the stretch into the lateral lats rather than the lumbar spine.",
            gifUrl = "https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/lats/exercise-ball-lat-stretch.gif"
        ),

        // OPEN-SOURCE STRETCH (BACK)
        StretchingExercise(
            id = "stretch_os_exercise_ball_lower_back_stretch_pyramid",
            name = "Exercise Ball Lower Back Stretch Pyramid",
            targetMuscle = "Latissimus Dorsi & Teres Major",
            muscleGroup = "Back",
            recommendedDurationSeconds = 30,
            timing = StretchTiming.POST_WORKOUT_STATIC,
            breathingCue = "Inhale into the side of your ribcage; exhale as you reach further into the lateral line.",
            instructions = listOf(
                "Adopt the starting position with proper body alignment.",
                "Move into the the lats stretch position.",
                "Hold for 20 to 40 seconds breathing deeply.",
                "Return slowly to the starting position and repeat if desired."
            ),
            coachTip = "Keep your core engaged to direct the stretch into the lateral lats rather than the lumbar spine.",
            gifUrl = "https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/lats/exercise-ball-lower-back-stretch-pyramid.gif"
        ),

        // OPEN-SOURCE STRETCH (BACK)
        StretchingExercise(
            id = "stretch_os_exercise_ball_lying_side_lat_stretch",
            name = "Exercise Ball Lying Side Lat Stretch",
            targetMuscle = "Latissimus Dorsi & Teres Major",
            muscleGroup = "Back",
            recommendedDurationSeconds = 30,
            timing = StretchTiming.POST_WORKOUT_STATIC,
            breathingCue = "Inhale into the side of your ribcage; exhale as you reach further into the lateral line.",
            instructions = listOf(
                "Adopt the starting position with proper body alignment.",
                "Move into the the lats stretch position.",
                "Hold for 20 to 40 seconds breathing deeply.",
                "Return slowly to the starting position and repeat if desired."
            ),
            coachTip = "Keep your core engaged to direct the stretch into the lateral lats rather than the lumbar spine.",
            gifUrl = "https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/lats/exercise-ball-lying-side-lat-stretch.gif"
        ),

        // OPEN-SOURCE STRETCH (BACK)
        StretchingExercise(
            id = "stretch_os_kneeling_lat_stretch",
            name = "Kneeling Lat Stretch",
            targetMuscle = "Latissimus Dorsi & Teres Major",
            muscleGroup = "Back",
            recommendedDurationSeconds = 30,
            timing = StretchTiming.POST_WORKOUT_STATIC,
            breathingCue = "Inhale into the side of your ribcage; exhale as you reach further into the lateral line.",
            instructions = listOf(
                "Adopt the starting position with proper body alignment.",
                "Move into the the lats stretch position.",
                "Hold for 20 to 40 seconds breathing deeply.",
                "Return slowly to the starting position and repeat if desired."
            ),
            coachTip = "Keep your core engaged to direct the stretch into the lateral lats rather than the lumbar spine.",
            gifUrl = "https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/lats/kneeling-lat-stretch.gif"
        ),

        // OPEN-SOURCE STRETCH (BACK)
        StretchingExercise(
            id = "stretch_os_roller_side_lat_stretch",
            name = "Roller Side Lat Stretch",
            targetMuscle = "Latissimus Dorsi & Teres Major",
            muscleGroup = "Back",
            recommendedDurationSeconds = 30,
            timing = StretchTiming.POST_WORKOUT_STATIC,
            breathingCue = "Inhale into the side of your ribcage; exhale as you reach further into the lateral line.",
            instructions = listOf(
                "Adopt the starting position with proper body alignment.",
                "Move into the the lats stretch position.",
                "Hold for 20 to 40 seconds breathing deeply.",
                "Return slowly to the starting position and repeat if desired."
            ),
            coachTip = "Keep your core engaged to direct the stretch into the lateral lats rather than the lumbar spine.",
            gifUrl = "https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/lats/roller-side-lat-stretch.gif"
        ),

        // OPEN-SOURCE STRETCH (BACK)
        StretchingExercise(
            id = "stretch_os_seated_lower_back_stretch",
            name = "Seated Lower Back Stretch",
            targetMuscle = "Latissimus Dorsi & Teres Major",
            muscleGroup = "Back",
            recommendedDurationSeconds = 30,
            timing = StretchTiming.POST_WORKOUT_STATIC,
            breathingCue = "Inhale into the side of your ribcage; exhale as you reach further into the lateral line.",
            instructions = listOf(
                "Adopt the starting position with proper body alignment.",
                "Move into the the lats stretch position.",
                "Hold for 20 to 40 seconds breathing deeply.",
                "Return slowly to the starting position and repeat if desired."
            ),
            coachTip = "Keep your core engaged to direct the stretch into the lateral lats rather than the lumbar spine.",
            gifUrl = "https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/lats/seated-lower-back-stretch.gif"
        ),

        // OPEN-SOURCE STRETCH (BACK)
        StretchingExercise(
            id = "stretch_os_side_lying_floor_stretch",
            name = "Side Lying Floor Stretch",
            targetMuscle = "Latissimus Dorsi & Teres Major",
            muscleGroup = "Back",
            recommendedDurationSeconds = 30,
            timing = StretchTiming.POST_WORKOUT_STATIC,
            breathingCue = "Inhale into the side of your ribcage; exhale as you reach further into the lateral line.",
            instructions = listOf(
                "Adopt the starting position with proper body alignment.",
                "Move into the the lats stretch position.",
                "Hold for 20 to 40 seconds breathing deeply.",
                "Return slowly to the starting position and repeat if desired."
            ),
            coachTip = "Keep your core engaged to direct the stretch into the lateral lats rather than the lumbar spine.",
            gifUrl = "https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/lats/side-lying-floor-stretch.gif"
        ),

        // OPEN-SOURCE STRETCH (BACK)
        StretchingExercise(
            id = "stretch_os_standing_lateral_stretch",
            name = "Standing Lateral Stretch",
            targetMuscle = "Latissimus Dorsi & Teres Major",
            muscleGroup = "Back",
            recommendedDurationSeconds = 30,
            timing = StretchTiming.POST_WORKOUT_STATIC,
            breathingCue = "Inhale into the side of your ribcage; exhale as you reach further into the lateral line.",
            instructions = listOf(
                "Adopt the starting position with proper body alignment.",
                "Move into the the lats stretch position.",
                "Hold for 20 to 40 seconds breathing deeply.",
                "Return slowly to the starting position and repeat if desired."
            ),
            coachTip = "Keep your core engaged to direct the stretch into the lateral lats rather than the lumbar spine.",
            gifUrl = "https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/lats/standing-lateral-stretch.gif"
        ),

        // OPEN-SOURCE STRETCH (SHOULDERS)
        StretchingExercise(
            id = "stretch_os_neck_side_stretch",
            name = "Neck Side Stretch",
            targetMuscle = "Levator Scapulae & Upper Trapezius",
            muscleGroup = "Shoulders",
            recommendedDurationSeconds = 30,
            timing = StretchTiming.POST_WORKOUT_STATIC,
            breathingCue = "Exhale slowly while gently letting the weight of your hand guide your ear toward your shoulder.",
            instructions = listOf(
                "Adopt the starting position with proper body alignment.",
                "Move into the the levator scapulae stretch position.",
                "Hold for 20 to 40 seconds breathing deeply.",
                "Return slowly to the starting position and repeat if desired."
            ),
            coachTip = "Apply very gentle fingertip guidance; never forcefully yank on the neck or cervical spine.",
            gifUrl = "https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/levator-scapulae/neck-side-stretch.gif"
        ),

        // OPEN-SOURCE STRETCH (SHOULDERS)
        StretchingExercise(
            id = "stretch_os_side_push_neck_stretch",
            name = "Side Push Neck Stretch",
            targetMuscle = "Levator Scapulae & Upper Trapezius",
            muscleGroup = "Shoulders",
            recommendedDurationSeconds = 30,
            timing = StretchTiming.POST_WORKOUT_STATIC,
            breathingCue = "Exhale slowly while gently letting the weight of your hand guide your ear toward your shoulder.",
            instructions = listOf(
                "Adopt the starting position with proper body alignment.",
                "Move into the the levator scapulae stretch position.",
                "Hold for 20 to 40 seconds breathing deeply.",
                "Return slowly to the starting position and repeat if desired."
            ),
            coachTip = "Apply very gentle fingertip guidance; never forcefully yank on the neck or cervical spine.",
            gifUrl = "https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/levator-scapulae/side-push-neck-stretch.gif"
        ),

        // OPEN-SOURCE STRETCH (CHEST)
        StretchingExercise(
            id = "stretch_os_assisted_seated_pectoralis_major_stretch_with_stability_ball",
            name = "Assisted Seated Pectoralis Major Stretch With Stability Ball",
            targetMuscle = "Pectoralis Major & Pectoralis Minor",
            muscleGroup = "Chest",
            recommendedDurationSeconds = 30,
            timing = StretchTiming.POST_WORKOUT_STATIC,
            breathingCue = "Slow 4-second inhalation; exhale and lean forward to open the chest fibers.",
            instructions = listOf(
                "Adopt the starting position with proper body alignment.",
                "Move into the the chest stretch position.",
                "Hold for 20 to 40 seconds breathing deeply.",
                "Return slowly to the starting position and repeat if desired."
            ),
            coachTip = "Adjust your elbow height: higher targets the lower pec; lower targets the clavicular head.",
            gifUrl = "https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/pectorals/assisted-seated-pectoralis-major-stretch-with-stability-ball.gif"
        ),

        // OPEN-SOURCE STRETCH (CHEST)
        StretchingExercise(
            id = "stretch_os_behind_head_chest_stretch",
            name = "Behind Head Chest Stretch",
            targetMuscle = "Pectoralis Major & Pectoralis Minor",
            muscleGroup = "Chest",
            recommendedDurationSeconds = 30,
            timing = StretchTiming.POST_WORKOUT_STATIC,
            breathingCue = "Slow 4-second inhalation; exhale and lean forward to open the chest fibers.",
            instructions = listOf(
                "Adopt the starting position with proper body alignment.",
                "Move into the the chest stretch position.",
                "Hold for 20 to 40 seconds breathing deeply.",
                "Return slowly to the starting position and repeat if desired."
            ),
            coachTip = "Adjust your elbow height: higher targets the lower pec; lower targets the clavicular head.",
            gifUrl = "https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/pectorals/behind-head-chest-stretch.gif"
        ),

        // OPEN-SOURCE STRETCH (CHEST)
        StretchingExercise(
            id = "stretch_os_chest_and_front_of_shoulder_stretch",
            name = "Chest And Front Of Shoulder Stretch",
            targetMuscle = "Pectoralis Major & Pectoralis Minor",
            muscleGroup = "Chest",
            recommendedDurationSeconds = 30,
            timing = StretchTiming.POST_WORKOUT_STATIC,
            breathingCue = "Slow 4-second inhalation; exhale and lean forward to open the chest fibers.",
            instructions = listOf(
                "Adopt the starting position with proper body alignment.",
                "Move into the the chest stretch position.",
                "Hold for 20 to 40 seconds breathing deeply.",
                "Return slowly to the starting position and repeat if desired."
            ),
            coachTip = "Adjust your elbow height: higher targets the lower pec; lower targets the clavicular head.",
            gifUrl = "https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/pectorals/chest-and-front-of-shoulder-stretch.gif"
        ),

        // OPEN-SOURCE STRETCH (CHEST)
        StretchingExercise(
            id = "stretch_os_chest_stretch_with_exercise_ball",
            name = "Chest Stretch With Exercise Ball",
            targetMuscle = "Pectoralis Major & Pectoralis Minor",
            muscleGroup = "Chest",
            recommendedDurationSeconds = 30,
            timing = StretchTiming.POST_WORKOUT_STATIC,
            breathingCue = "Slow 4-second inhalation; exhale and lean forward to open the chest fibers.",
            instructions = listOf(
                "Adopt the starting position with proper body alignment.",
                "Move into the the chest stretch position.",
                "Hold for 20 to 40 seconds breathing deeply.",
                "Return slowly to the starting position and repeat if desired."
            ),
            coachTip = "Adjust your elbow height: higher targets the lower pec; lower targets the clavicular head.",
            gifUrl = "https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/pectorals/chest-stretch-with-exercise-ball.gif"
        ),

        // OPEN-SOURCE STRETCH (CHEST)
        StretchingExercise(
            id = "stretch_os_dynamic_chest_stretch_male",
            name = "Dynamic Chest Stretch Male",
            targetMuscle = "Pectoralis Major & Pectoralis Minor",
            muscleGroup = "Chest",
            recommendedDurationSeconds = 25,
            timing = StretchTiming.PRE_WORKOUT_DYNAMIC,
            breathingCue = "Slow 4-second inhalation; exhale and lean forward to open the chest fibers.",
            instructions = listOf(
                "Adopt the starting position with proper body alignment.",
                "Move into the the chest stretch position.",
                "Hold for 20 to 40 seconds breathing deeply.",
                "Return slowly to the starting position and repeat if desired."
            ),
            coachTip = "Adjust your elbow height: higher targets the lower pec; lower targets the clavicular head.",
            gifUrl = "https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/pectorals/dynamic-chest-stretch-male.gif"
        ),

        // OPEN-SOURCE STRETCH (LEGS)
        StretchingExercise(
            id = "stretch_os_all_fours_squad_stretch",
            name = "All Fours Squad Stretch",
            targetMuscle = "Rectus Femoris & Vastus Complex",
            muscleGroup = "Legs",
            recommendedDurationSeconds = 30,
            timing = StretchTiming.POST_WORKOUT_STATIC,
            breathingCue = "Inhale tall; exhale and tuck your pelvis under to deepen the stretch along the front of the thigh.",
            instructions = listOf(
                "Adopt the starting position with proper body alignment.",
                "Move into the the quadriceps stretch position.",
                "Hold for 20 to 40 seconds breathing deeply.",
                "Return slowly to the starting position and repeat if desired."
            ),
            coachTip = "Maintain a posterior pelvic tilt (glutes squeezed) to isolate the rectus femoris.",
            gifUrl = "https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/quads/all-fours-squad-stretch.gif"
        ),

        // OPEN-SOURCE STRETCH (LEGS)
        StretchingExercise(
            id = "stretch_os_assisted_prone_lying_quads_stretch",
            name = "Assisted Prone Lying Quads Stretch",
            targetMuscle = "Rectus Femoris & Vastus Complex",
            muscleGroup = "Legs",
            recommendedDurationSeconds = 30,
            timing = StretchTiming.POST_WORKOUT_STATIC,
            breathingCue = "Inhale tall; exhale and tuck your pelvis under to deepen the stretch along the front of the thigh.",
            instructions = listOf(
                "Adopt the starting position with proper body alignment.",
                "Move into the the quadriceps stretch position.",
                "Hold for 20 to 40 seconds breathing deeply.",
                "Return slowly to the starting position and repeat if desired."
            ),
            coachTip = "Maintain a posterior pelvic tilt (glutes squeezed) to isolate the rectus femoris.",
            gifUrl = "https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/quads/assisted-prone-lying-quads-stretch.gif"
        ),

        // OPEN-SOURCE STRETCH (LEGS)
        StretchingExercise(
            id = "stretch_os_chair_leg_extended_stretch",
            name = "Chair Leg Extended Stretch",
            targetMuscle = "Rectus Femoris & Vastus Complex",
            muscleGroup = "Legs",
            recommendedDurationSeconds = 30,
            timing = StretchTiming.POST_WORKOUT_STATIC,
            breathingCue = "Inhale tall; exhale and tuck your pelvis under to deepen the stretch along the front of the thigh.",
            instructions = listOf(
                "Adopt the starting position with proper body alignment.",
                "Move into the the quadriceps stretch position.",
                "Hold for 20 to 40 seconds breathing deeply.",
                "Return slowly to the starting position and repeat if desired."
            ),
            coachTip = "Maintain a posterior pelvic tilt (glutes squeezed) to isolate the rectus femoris.",
            gifUrl = "https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/quads/chair-leg-extended-stretch.gif"
        ),

        // OPEN-SOURCE STRETCH (LEGS)
        StretchingExercise(
            id = "stretch_os_intermediate_hip_flexor_and_quad_stretch",
            name = "Intermediate Hip Flexor And Quad Stretch",
            targetMuscle = "Rectus Femoris & Vastus Complex",
            muscleGroup = "Legs",
            recommendedDurationSeconds = 30,
            timing = StretchTiming.POST_WORKOUT_STATIC,
            breathingCue = "Inhale tall; exhale and tuck your pelvis under to deepen the stretch along the front of the thigh.",
            instructions = listOf(
                "Adopt the starting position with proper body alignment.",
                "Move into the the quadriceps stretch position.",
                "Hold for 20 to 40 seconds breathing deeply.",
                "Return slowly to the starting position and repeat if desired."
            ),
            coachTip = "Maintain a posterior pelvic tilt (glutes squeezed) to isolate the rectus femoris.",
            gifUrl = "https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/quads/intermediate-hip-flexor-and-quad-stretch.gif"
        ),

        // OPEN-SOURCE STRETCH (LEGS)
        StretchingExercise(
            id = "stretch_os_lying_side_quads_stretch",
            name = "Lying Side Quads Stretch",
            targetMuscle = "Rectus Femoris & Vastus Complex",
            muscleGroup = "Legs",
            recommendedDurationSeconds = 30,
            timing = StretchTiming.POST_WORKOUT_STATIC,
            breathingCue = "Inhale tall; exhale and tuck your pelvis under to deepen the stretch along the front of the thigh.",
            instructions = listOf(
                "Adopt the starting position with proper body alignment.",
                "Move into the the quadriceps stretch position.",
                "Hold for 20 to 40 seconds breathing deeply.",
                "Return slowly to the starting position and repeat if desired."
            ),
            coachTip = "Maintain a posterior pelvic tilt (glutes squeezed) to isolate the rectus femoris.",
            gifUrl = "https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/quads/lying-side-quads-stretch.gif"
        ),

        // OPEN-SOURCE STRETCH (BACK)
        StretchingExercise(
            id = "stretch_os_roller_back_stretch",
            name = "Roller Back Stretch",
            targetMuscle = "Thoracic Spine & Erector Spinae",
            muscleGroup = "Back",
            recommendedDurationSeconds = 30,
            timing = StretchTiming.POST_WORKOUT_STATIC,
            breathingCue = "Exhale fully as you rotate; inhale as you reset your posture.",
            instructions = listOf(
                "Adopt the starting position with proper body alignment.",
                "Move into the the lower back stretch position.",
                "Hold for 20 to 40 seconds breathing deeply.",
                "Return slowly to the starting position and repeat if desired."
            ),
            coachTip = "Focus on rotational mobility through the upper and middle back rather than twisting the lower spine.",
            gifUrl = "https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/spine/roller-back-stretch.gif"
        ),

        // OPEN-SOURCE STRETCH (BACK)
        StretchingExercise(
            id = "stretch_os_spine_stretch",
            name = "Spine Stretch",
            targetMuscle = "Thoracic Spine & Erector Spinae",
            muscleGroup = "Back",
            recommendedDurationSeconds = 30,
            timing = StretchTiming.POST_WORKOUT_STATIC,
            breathingCue = "Exhale fully as you rotate; inhale as you reset your posture.",
            instructions = listOf(
                "Adopt the starting position with proper body alignment.",
                "Move into the the lower back stretch position.",
                "Hold for 20 to 40 seconds breathing deeply.",
                "Return slowly to the starting position and repeat if desired."
            ),
            coachTip = "Focus on rotational mobility through the upper and middle back rather than twisting the lower spine.",
            gifUrl = "https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/spine/spine-stretch.gif"
        ),

        // OPEN-SOURCE STRETCH (TRICEPS)
        StretchingExercise(
            id = "stretch_os_exercise_ball_seated_triceps_stretch",
            name = "Exercise Ball Seated Triceps Stretch",
            targetMuscle = "Triceps Brachii Long & Lateral Heads",
            muscleGroup = "Triceps",
            recommendedDurationSeconds = 30,
            timing = StretchTiming.POST_WORKOUT_STATIC,
            breathingCue = "Inhale tall; exhale as you gently guide your elbow back and downward.",
            instructions = listOf(
                "Adopt the starting position with proper body alignment.",
                "Move into the the triceps stretch position.",
                "Hold for 20 to 40 seconds breathing deeply.",
                "Return slowly to the starting position and repeat if desired."
            ),
            coachTip = "Avoid arching your lower back as you reach overhead; keep your ribs pulled down.",
            gifUrl = "https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/triceps/exercise-ball-seated-triceps-stretch.gif"
        ),

        // OPEN-SOURCE STRETCH (TRICEPS)
        StretchingExercise(
            id = "stretch_os_overhead_triceps_stretch",
            name = "Overhead Triceps Stretch",
            targetMuscle = "Triceps Brachii Long & Lateral Heads",
            muscleGroup = "Triceps",
            recommendedDurationSeconds = 30,
            timing = StretchTiming.POST_WORKOUT_STATIC,
            breathingCue = "Inhale tall; exhale as you gently guide your elbow back and downward.",
            instructions = listOf(
                "Adopt the starting position with proper body alignment.",
                "Move into the the triceps stretch position.",
                "Hold for 20 to 40 seconds breathing deeply.",
                "Return slowly to the starting position and repeat if desired."
            ),
            coachTip = "Avoid arching your lower back as you reach overhead; keep your ribs pulled down.",
            gifUrl = "https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/triceps/overhead-triceps-stretch.gif"
        ),

        // OPEN-SOURCE STRETCH (TRICEPS)
        StretchingExercise(
            id = "stretch_os_triceps_stretch",
            name = "Triceps Stretch",
            targetMuscle = "Triceps Brachii Long & Lateral Heads",
            muscleGroup = "Triceps",
            recommendedDurationSeconds = 30,
            timing = StretchTiming.POST_WORKOUT_STATIC,
            breathingCue = "Inhale tall; exhale as you gently guide your elbow back and downward.",
            instructions = listOf(
                "Adopt the starting position with proper body alignment.",
                "Move into the the triceps stretch position.",
                "Hold for 20 to 40 seconds breathing deeply.",
                "Return slowly to the starting position and repeat if desired."
            ),
            coachTip = "Avoid arching your lower back as you reach overhead; keep your ribs pulled down.",
            gifUrl = "https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/triceps/triceps-stretch.gif"
        ),

        // OPEN-SOURCE STRETCH (BACK)
        StretchingExercise(
            id = "stretch_os_upper_back_stretch",
            name = "Upper Back Stretch",
            targetMuscle = "Rhomboids, Middle Trapezius & Rear Delts",
            muscleGroup = "Back",
            recommendedDurationSeconds = 30,
            timing = StretchTiming.POST_WORKOUT_STATIC,
            breathingCue = "Exhale and tuck your chin slightly, expanding between your shoulder blades.",
            instructions = listOf(
                "Adopt the starting position with proper body alignment.",
                "Move into the the upper back stretch position.",
                "Hold for 20 to 40 seconds breathing deeply.",
                "Return slowly to the starting position and repeat if desired."
            ),
            coachTip = "Actively round through your upper back and push your hands forward to open the rhomboids.",
            gifUrl = "https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/upper-back/upper-back-stretch.gif"
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

            else -> stretches.take(6)
        }
    }
}
