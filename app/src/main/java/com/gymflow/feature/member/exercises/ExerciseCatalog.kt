package com.gymflow.feature.member.exercises

object ExerciseCatalog {

    data class ExerciseDetailData(
        val exerciseId: String,
        val exerciseName: String,
        val metadataSubhead: String,
        val videoTitle: String,
        val instructions: List<String>,
        val primaryMuscle: String,
        val secondaryMuscles: List<String>,
        val equipmentRequired: String,
        val coachTip: String
    )

    private val catalog: Map<String, ExerciseDetailData> = mapOf(
        "ex_chest_press" to ExerciseDetailData(
            exerciseId = "ex_chest_press",
            exerciseName = "Chest Press",
            metadataSubhead = "3 sets · 12 reps · 20 kg",
            videoTitle = "Barbell Bench Press Technique",
            instructions = listOf(
                "Lie flat on the bench with your feet firmly planted on the floor.",
                "Grip the barbell slightly wider than shoulder-width with a secure overhand grip.",
                "Retract your shoulder blades and brace your core tight.",
                "Lower the bar under control until it lightly touches your mid-chest.",
                "Press back up explosively while keeping your shoulder blades pinned."
            ),
            primaryMuscle = "Pectoralis Major (Middle & Lower Chest)",
            secondaryMuscles = listOf("Triceps Brachii", "Anterior Deltoids"),
            equipmentRequired = "Olympic Barbell & Flat Bench",
            coachTip = "Retract your scapula and keep your feet planted firmly on the floor for maximum pressing stability."
        ),

        "ex_lat_pulldown" to ExerciseDetailData(
            exerciseId = "ex_lat_pulldown",
            exerciseName = "Lat Pulldown",
            metadataSubhead = "4 sets · 10 reps · 55 kg",
            videoTitle = "Wide Grip Lat Pulldown Technique",
            instructions = listOf(
                "Sit securely with thighs anchored snugly under the leg cushions.",
                "Take a wide, overhand grip slightly outside shoulder width.",
                "Depress your shoulders and pull the bar smoothly down to your upper collarbone.",
                "Squeeze your lats and mid-back firmly for one full second at the bottom.",
                "Slowly control the ascent back to full arm extension without swinging."
            ),
            primaryMuscle = "Latissimus Dorsi (Outer & Mid Back)",
            secondaryMuscles = listOf("Biceps Brachii", "Teres Major", "Rhomboids"),
            equipmentRequired = "Lat Pulldown Cable Machine",
            coachTip = "Lead with your elbows and avoid leaning too far backward to maximize lat recruitment."
        ),

        "ex_barbell_squat" to ExerciseDetailData(
            exerciseId = "ex_barbell_squat",
            exerciseName = "Barbell Back Squat",
            metadataSubhead = "4 sets · 8 reps · 80 kg",
            videoTitle = "Deep Barbell Squat Form",
            instructions = listOf(
                "Rest the barbell firmly across your upper trapezius with a tight upper back.",
                "Set your feet shoulder-width apart with toes flared out 15 to 30 degrees.",
                "Inhale, brace your core 360°, and hinge at hips while breaking at the knees.",
                "Descend until hip crease drops parallel to or slightly below the top of the knees.",
                "Drive hard through the mid-foot to stand back up upright, exhaling past the sticking point."
            ),
            primaryMuscle = "Quadriceps (Vastus Lateralis, Medialis, Rectus Femoris)",
            secondaryMuscles = listOf("Gluteus Maximus", "Hamstrings", "Erector Spinae"),
            equipmentRequired = "Power Rack & Olympic Barbell",
            coachTip = "Keep your chest tall and ensure your knees track directly over your second toe throughout the descent."
        ),

        "ex_overhead_press" to ExerciseDetailData(
            exerciseId = "ex_overhead_press",
            exerciseName = "Standing Overhead Barbell Press",
            metadataSubhead = "4 sets · 8 reps · 40 kg",
            videoTitle = "Strict Standing Overhead Press Form",
            instructions = listOf(
                "Rest the barbell across your front deltoids with forearms perpendicular to the floor.",
                "Brace your glutes, thighs, and core tightly to establish a rock-solid foundation.",
                "Press the barbell straight overhead, tilting chin back slightly as the bar clears your nose.",
                "Lock elbows out overhead with the bar positioned directly above your shoulder joints.",
                "Lower the bar under strict 2-second control back to your clavicles."
            ),
            primaryMuscle = "Anterior & Lateral Deltoids",
            secondaryMuscles = listOf("Triceps Brachii", "Upper Trapezius", "Upper Chest"),
            equipmentRequired = "Olympic Barbell & Squat Stand",
            coachTip = "Do not arch your lower back. Squeeze your glutes hard throughout the entire pressing phase."
        ),

        "ex_sh_overhead_press" to ExerciseDetailData(
            exerciseId = "ex_sh_overhead_press",
            exerciseName = "Standing Overhead Barbell Press",
            metadataSubhead = "4 sets · 8 reps · 40 kg",
            videoTitle = "Strict Standing Overhead Press Form",
            instructions = listOf(
                "Rest the barbell across your front deltoids with forearms perpendicular to the floor.",
                "Brace your glutes, thighs, and core tightly to establish a rock-solid foundation.",
                "Press the barbell straight overhead, tilting chin back slightly as the bar clears your nose.",
                "Lock elbows out overhead with the bar positioned directly above your shoulder joints.",
                "Lower the bar under strict 2-second control back to your clavicles."
            ),
            primaryMuscle = "Anterior & Lateral Deltoids",
            secondaryMuscles = listOf("Triceps Brachii", "Upper Trapezius", "Upper Chest"),
            equipmentRequired = "Squat Stand / Power Rack & Barbell",
            coachTip = "Do not arch your lower back. Squeeze your glutes hard throughout the entire pressing phase."
        ),

        "ex_sh_seated_dumbbell_press" to ExerciseDetailData(
            exerciseId = "ex_sh_seated_dumbbell_press",
            exerciseName = "Seated Dumbbell Shoulder Press",
            metadataSubhead = "3 sets · 10 reps · 18 kg",
            videoTitle = "Seated Dumbbell Overhead Press Technique",
            instructions = listOf(
                "Sit upright on an incline/utility bench set to 90 degrees.",
                "Bring dumbbells up to ear level with elbows angled roughly 45 degrees forward.",
                "Press dumbbells smoothly overhead until arms are nearly straight.",
                "Lower under control over 2 seconds back to ear level."
            ),
            primaryMuscle = "Anterior & Lateral Deltoids",
            secondaryMuscles = listOf("Triceps Brachii", "Upper Trapezius"),
            equipmentRequired = "90° Upright Utility Bench & Dumbbells",
            coachTip = "Avoid excessive arch in the lower back by keeping your feet firmly planted."
        ),

        "ex_sh_cable_lateral_raise" to ExerciseDetailData(
            exerciseId = "ex_sh_cable_lateral_raise",
            exerciseName = "Cable Lateral Raise",
            metadataSubhead = "3 sets · 15 reps · 8 kg",
            videoTitle = "Cable Lateral Raise Isolation",
            instructions = listOf(
                "Set the cable pulley to its lowest point and attach a single handle.",
                "Stand sideways to the machine and grip the handle with the far hand across your body.",
                "Raise the handle outward and upward to shoulder level with a slight bend in the elbow.",
                "Pause for 1 second at shoulder height, then lower under tension over 2 seconds."
            ),
            primaryMuscle = "Lateral Deltoids (Side Delts)",
            secondaryMuscles = listOf("Anterior Deltoids", "Supraspinatus"),
            equipmentRequired = "Low Pulley Cable Column (Single Handle)",
            coachTip = "Lead with your elbow rather than your wrist to direct tension into the side delt head."
        ),

        "ex_sh_reverse_pec_deck" to ExerciseDetailData(
            exerciseId = "ex_sh_reverse_pec_deck",
            exerciseName = "Reverse Pec Deck (Rear Delts)",
            metadataSubhead = "3 sets · 15 reps · 35 kg",
            videoTitle = "Reverse Pec Deck Machine Execution",
            instructions = listOf(
                "Sit facing the machine with your chest firmly against the pad.",
                "Adjust handles so arms are parallel to the floor at shoulder height.",
                "Sweep handles outward and backward in a wide arc using your rear shoulders.",
                "Contract rear deltoids forcefully at peak contraction, then resist the return."
            ),
            primaryMuscle = "Posterior Deltoids (Rear Delts)",
            secondaryMuscles = listOf("Rhomboids", "Infraspinatus", "Middle Trapezius"),
            equipmentRequired = "Dual Rear Delt / Pec Deck Machine",
            coachTip = "Keep your chest glued to the support pad and avoid shrugging your traps up."
        ),

        "ex_cable_fly" to ExerciseDetailData(
            exerciseId = "ex_cable_fly",
            exerciseName = "Cable Chest Fly",
            metadataSubhead = "3 sets · 12 reps · 12 kg",
            videoTitle = "Standing Cable Fly Motion & Peak Squeeze",
            instructions = listOf(
                "Set dual cable pulleys at chest height and grasp each single handle firmly.",
                "Step forward into a staggered stance with a slight forward torso angle.",
                "Keeping elbows fixed with a soft bend, sweep hands forward in a wide hugging arc.",
                "Squeeze your pectorals forcefully at the midline for 1 full second.",
                "Allow arms to open slowly under cable tension until you feel a comfortable chest stretch."
            ),
            primaryMuscle = "Sternal Pectoralis Major (Inner & Mid Chest)",
            secondaryMuscles = listOf("Anterior Deltoids", "Biceps Short Head"),
            equipmentRequired = "Dual Adjustable Cable Column",
            coachTip = "Keep your elbows locked in a consistent soft bend. Focus on bringing the insides of your biceps toward each other."
        ),

        "ex_deadlift" to ExerciseDetailData(
            exerciseId = "ex_deadlift",
            exerciseName = "Barbell Deadlift",
            metadataSubhead = "4 sets · 6 reps · 100 kg",
            videoTitle = "Conventional Deadlift Setup & Pull",
            instructions = listOf(
                "Stand with feet hip-width apart, with the barbell directly over your mid-foot.",
                "Hinge at the hips and grip the bar just outside your shins.",
                "Pull your chest high, engage your lats, and pull the slack out of the barbell.",
                "Push the floor away with your legs until knees and hips lock out together.",
                "Hinge back at the hips first to lower the barbell smoothly to the platform."
            ),
            primaryMuscle = "Erector Spinae & Hamstrings",
            secondaryMuscles = listOf("Gluteus Maximus", "Latissimus Dorsi", "Forearms & Traps"),
            equipmentRequired = "Olympic Barbell & Bumper Plates",
            coachTip = "Keep the bar in contact with your shins and thighs throughout the pull to protect your lower back."
        ),

        "ex_barbell_curl" to ExerciseDetailData(
            exerciseId = "ex_barbell_curl",
            exerciseName = "Barbell Bicep Curl",
            metadataSubhead = "3 sets · 12 reps · 25 kg",
            videoTitle = "Standing Barbell Bicep Curl Technique",
            instructions = listOf(
                "Stand upright holding the barbell with an underhand shoulder-width grip.",
                "Pin your elbows firmly against your ribcage.",
                "Curl the weight up towards your shoulders by flexing your biceps.",
                "Hold and contract your biceps intensely at the top for one second.",
                "Lower the bar slowly under a 2-second negative count."
            ),
            primaryMuscle = "Biceps Brachii (Short & Long Head)",
            secondaryMuscles = listOf("Brachialis", "Brachioradialis"),
            equipmentRequired = "Straight or EZ-Curl Barbell",
            coachTip = "Keep your elbows stationary. Avoid swinging your hips or shoulders to generate momentum."
        ),

        "ex_triceps_pushdown" to ExerciseDetailData(
            exerciseId = "ex_triceps_pushdown",
            exerciseName = "Triceps Pushdown",
            metadataSubhead = "3 sets · 15 reps · 22 kg",
            videoTitle = "Cable Rope Triceps Pushdown",
            instructions = listOf(
                "Attach a rope attachment to a high cable pulley.",
                "Pin your elbows firmly to your sides with a slight forward torso lean.",
                "Push downward until your arms are fully extended.",
                "Spread the rope handles slightly apart at the bottom for peak lateral triceps contraction.",
                "Return the handles under control back to 90-degree elbow flexion."
            ),
            primaryMuscle = "Triceps Brachii (Lateral & Medial Head)",
            secondaryMuscles = listOf("Anconeus"),
            equipmentRequired = "Cable Station with Rope Attachment",
            coachTip = "Lock your upper arms in place. Only move your forearms to maintain tension strictly on the triceps."
        ),

        "ex_hanging_leg_raise" to ExerciseDetailData(
            exerciseId = "ex_hanging_leg_raise",
            exerciseName = "Hanging Leg Raise",
            metadataSubhead = "3 sets · 15 reps · Bodyweight",
            videoTitle = "Hanging Core Compression Technique",
            instructions = listOf(
                "Hang from a sturdy pull-up bar with an overhand grip and engaged shoulders.",
                "Brace your core and initiate the movement by tucking your pelvis.",
                "Raise your legs up until thighs are parallel to the floor (or higher).",
                "Pause for a moment and squeeze your lower abdominals.",
                "Lower your legs slowly under control to prevent any swinging."
            ),
            primaryMuscle = "Rectus Abdominis (Lower & Upper Abs)",
            secondaryMuscles = listOf("Iliopsoas (Hip Flexors)", "Obliques", "Grip Strength"),
            equipmentRequired = "Pull-Up Bar / Rig",
            coachTip = "Focus on curling your pelvis upward rather than simply swinging your legs up with your hip flexors."
        )
    )

    fun getExercise(id: String): ExerciseDetailData {
        // Direct key lookup
        catalog[id]?.let { return it }

        // Canonical mapping based on substrings / aliases
        val normalized = id.lowercase().replace("-", "_").replace(" ", "_")
        return when {
            normalized.contains("lat") || normalized.contains("back") || normalized.contains("row") ->
                catalog["ex_lat_pulldown"]!!
            normalized.contains("squat") || normalized.contains("leg") || normalized.contains("leg_") ->
                catalog["ex_barbell_squat"]!!
            normalized.contains("shoulder") || normalized.contains("overhead") || normalized.contains("press_shoulder") || normalized.contains("sh_") ->
                catalog["ex_overhead_press"]!!
            normalized.contains("fly") || normalized.contains("cable") || normalized.contains("pec") ->
                catalog["ex_cable_fly"]!!
            normalized.contains("deadlift") || normalized.contains("rdl") ->
                catalog["ex_deadlift"]!!
            normalized.contains("curl") || normalized.contains("bi_") ->
                catalog["ex_barbell_curl"]!!
            normalized.contains("tricep") || normalized.contains("pushdown") || normalized.contains("tri_") || normalized.contains("dip") ->
                catalog["ex_triceps_pushdown"]!!
            normalized.contains("raise") || normalized.contains("hanging") || normalized.contains("abs") || normalized.contains("crunch") ->
                catalog["ex_hanging_leg_raise"]!!
            else ->
                catalog["ex_chest_press"]!!
        }
    }
}

