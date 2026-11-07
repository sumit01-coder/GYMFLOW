package com.gymflow.feature.member.exercises

object ExerciseCatalog {

    data class ExerciseDetailData(
        val exerciseId: String,
        val exerciseName: String,
        val category: String = "Chest", // "Chest", "Back", "Biceps", "Triceps", "Shoulders", "Legs", "Abs"
        val metadataSubhead: String,
        val videoTitle: String,
        val instructions: List<String>,
        val primaryMuscle: String,
        val secondaryMuscles: List<String>,
        val equipmentRequired: String,
        val coachTip: String
    )

    private val catalog: Map<String, ExerciseDetailData> = mapOf(
        // CHEST
        "ex_chest_press" to ExerciseDetailData(
            exerciseId = "ex_chest_press",
            exerciseName = "Barbell Bench Press",
            category = "Chest",
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
        "ex_chest_incline_dumbbell_press" to ExerciseDetailData(
            exerciseId = "ex_chest_incline_dumbbell_press",
            exerciseName = "Incline Dumbbell Press",
            category = "Chest",
            metadataSubhead = "4 sets · 10 reps · 24 kg",
            videoTitle = "Incline Dumbbell Press Form",
            instructions = listOf(
                "Set an adjustable bench to a 30-degree incline angle.",
                "Sit with dumbbells resting on your knees, then kick them up to shoulder level as you lie back.",
                "Press the dumbbells overhead with palms facing forward until arms are extended.",
                "Lower the dumbbells under a 3-second negative until you feel a deep upper chest stretch.",
                "Drive weights back up in a slight arc without banging the dumbbells together."
            ),
            primaryMuscle = "Clavicular Pectoralis Major (Upper Chest)",
            secondaryMuscles = listOf("Anterior Deltoids", "Triceps Brachii"),
            equipmentRequired = "Incline Bench & Dumbbells",
            coachTip = "Keep incline at 30° to emphasize upper chest rather than front deltoids."
        ),
        "ex_cable_fly" to ExerciseDetailData(
            exerciseId = "ex_cable_fly",
            exerciseName = "Cable Chest Fly",
            category = "Chest",
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
        "ex_chest_pec_deck_fly" to ExerciseDetailData(
            exerciseId = "ex_chest_pec_deck_fly",
            exerciseName = "Pec Deck Machine Fly",
            category = "Chest",
            metadataSubhead = "3 sets · 15 reps · 45 kg",
            videoTitle = "Pec Deck Machine Technique",
            instructions = listOf(
                "Adjust seat height so handles align with mid-chest level.",
                "Place forearms flat against pads (or grip handles) with elbows slightly bent.",
                "Contract your chest to bring handles together in front of you.",
                "Hold peak squeeze for 2 seconds before slowly resisting the weight back."
            ),
            primaryMuscle = "Pectoralis Major (Inner Chest)",
            secondaryMuscles = listOf("Anterior Deltoids"),
            equipmentRequired = "Pec Deck Fly Machine",
            coachTip = "Avoid letting the weight stack touch between reps to keep constant tension."
        ),
        "ex_chest_dips" to ExerciseDetailData(
            exerciseId = "ex_chest_dips",
            exerciseName = "Chest Parallel Bar Dips",
            category = "Chest",
            metadataSubhead = "3 sets · 10 reps · Bodyweight",
            videoTitle = "Chest Dip Execution & Torso Lean",
            instructions = listOf(
                "Grip parallel bars and press yourself up to arm extension.",
                "Lean your torso forward roughly 30 degrees and flare elbows slightly outward.",
                "Lower your body until shoulders drop slightly below elbow level.",
                "Press through palms to return to top without locking out harshly."
            ),
            primaryMuscle = "Lower Pectoralis Major",
            secondaryMuscles = listOf("Triceps Brachii", "Anterior Deltoids"),
            equipmentRequired = "Parallel Dip Station",
            coachTip = "Torso lean is key: upright targets triceps; 30-degree forward lean shifts tension directly onto the chest."
        ),

        // BACK
        "ex_lat_pulldown" to ExerciseDetailData(
            exerciseId = "ex_lat_pulldown",
            exerciseName = "Wide-Grip Lat Pulldown",
            category = "Back",
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
        "ex_back_seated_cable_row" to ExerciseDetailData(
            exerciseId = "ex_back_seated_cable_row",
            exerciseName = "Seated Cable Row",
            category = "Back",
            metadataSubhead = "4 sets · 10 reps · 50 kg",
            videoTitle = "Seated Close-Grip Cable Row",
            instructions = listOf(
                "Sit on the bench with feet on footplates and knees slightly bent.",
                "Grip the V-bar attachment with a neutral grip and sit tall with a braced core.",
                "Initiate by retracting your shoulder blades, then pull handle toward your lower ribcage.",
                "Squeeze rhomboids and middle lats firmly at peak contraction.",
                "Extend arms forward with control, allowing a gentle stretch in the lats."
            ),
            primaryMuscle = "Rhomboids & Middle Trapezius",
            secondaryMuscles = listOf("Latissimus Dorsi", "Biceps Brachii", "Erector Spinae"),
            equipmentRequired = "Low Pulley Cable Row Station",
            coachTip = "Keep your lower back neutral; avoid excessive rocking back and forth."
        ),
        "ex_back_barbell_bent_row" to ExerciseDetailData(
            exerciseId = "ex_back_barbell_bent_row",
            exerciseName = "Bent-Over Barbell Row",
            category = "Back",
            metadataSubhead = "4 sets · 8 reps · 60 kg",
            videoTitle = "Bent-Over Barbell Row Form",
            instructions = listOf(
                "Stand hip-width apart holding the barbell with an overhand grip.",
                "Hinge at the hips until your torso is roughly 45 degrees to the floor with flat back.",
                "Pull the bar up toward your lower abdomen, driving elbows high toward the ceiling.",
                "Squeeze mid-back muscles hard at top, then lower with control."
            ),
            primaryMuscle = "Latissimus Dorsi & Rhomboids",
            secondaryMuscles = listOf("Rear Deltoids", "Biceps", "Spinal Erectors"),
            equipmentRequired = "Olympic Barbell & Plates",
            coachTip = "Keep your neck in line with your spine; look at the floor 5 feet ahead."
        ),
        "ex_deadlift" to ExerciseDetailData(
            exerciseId = "ex_deadlift",
            exerciseName = "Barbell Deadlift",
            category = "Back",
            metadataSubhead = "4 sets · 6 reps · 100 kg",
            videoTitle = "Conventional Deadlift Setup & Pull",
            instructions = listOf(
                "Stand with feet hip-width apart, with the barbell directly over your mid-foot.",
                "Hinge at the hips and grip the bar just outside your shins.",
                "Pull your chest high, engage your lats, and pull the slack out of the barbell.",
                "Push the floor away with your legs until knees and hips lock out together.",
                "Hinge back at the hips first to lower the barbell smoothly to the platform."
            ),
            primaryMuscle = "Erector Spinae & Posterior Chain",
            secondaryMuscles = listOf("Gluteus Maximus", "Hamstrings", "Latissimus Dorsi", "Traps"),
            equipmentRequired = "Olympic Barbell & Bumper Plates",
            coachTip = "Keep the bar in contact with your shins and thighs throughout the pull to protect your lower back."
        ),
        "ex_back_single_arm_dumbbell_row" to ExerciseDetailData(
            exerciseId = "ex_back_single_arm_dumbbell_row",
            exerciseName = "Single-Arm Dumbbell Row",
            category = "Back",
            metadataSubhead = "3 sets · 12 reps · 22 kg",
            videoTitle = "Single-Arm Dumbbell Row Technique",
            instructions = listOf(
                "Place one knee and hand firmly on a flat bench for support.",
                "Keep your back flat and parallel to the floor.",
                "Grip a dumbbell with free hand and row it up toward your hip pocket.",
                "Squeeze your lat at the top, then lower under control to full extension."
            ),
            primaryMuscle = "Latissimus Dorsi",
            secondaryMuscles = listOf("Rhomboids", "Biceps", "Teres Major"),
            equipmentRequired = "Flat Bench & Dumbbell",
            coachTip = "Think of pulling your elbow toward your back pocket rather than straight up to isolate the lat."
        ),

        // SHOULDERS
        "ex_overhead_press" to ExerciseDetailData(
            exerciseId = "ex_overhead_press",
            exerciseName = "Standing Overhead Barbell Press",
            category = "Shoulders",
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
            category = "Shoulders",
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
            category = "Shoulders",
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
            category = "Shoulders",
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

        // BICEPS
        "ex_barbell_curl" to ExerciseDetailData(
            exerciseId = "ex_barbell_curl",
            exerciseName = "Standing Barbell Bicep Curl",
            category = "Biceps",
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
        "ex_bi_incline_dumbbell_curl" to ExerciseDetailData(
            exerciseId = "ex_bi_incline_dumbbell_curl",
            exerciseName = "Incline Dumbbell Bicep Curl",
            category = "Biceps",
            metadataSubhead = "3 sets · 10 reps · 12 kg",
            videoTitle = "Incline Dumbbell Curl Form",
            instructions = listOf(
                "Set an incline bench to 45 degrees and sit back with arms hanging straight down.",
                "Rotate palms forward (supination) and keep upper arms perpendicular to the floor.",
                "Curl both dumbbells upward while keeping elbows pinned behind the torso line.",
                "Squeeze biceps at the top, then lower with a controlled 3-second negative."
            ),
            primaryMuscle = "Biceps Brachii (Long Head Deep Stretch)",
            secondaryMuscles = listOf("Brachialis", "Forearms"),
            equipmentRequired = "45° Incline Bench & Dumbbells",
            coachTip = "Incline angle places the long head in maximum stretch for optimal peak development."
        ),
        "ex_bi_cable_hammer_curl" to ExerciseDetailData(
            exerciseId = "ex_bi_cable_hammer_curl",
            exerciseName = "Cable Rope Hammer Curl",
            category = "Biceps",
            metadataSubhead = "3 sets · 12 reps · 20 kg",
            videoTitle = "Cable Rope Hammer Curl Form",
            instructions = listOf(
                "Attach a rope to a low pulley cable station.",
                "Grip the rope handles with neutral grip (palms facing each other).",
                "Curl upward while keeping elbows tucked tightly to your sides.",
                "Squeeze outer biceps and forearms at the top, then lower slowly."
            ),
            primaryMuscle = "Brachialis & Brachioradialis",
            secondaryMuscles = listOf("Biceps Brachii"),
            equipmentRequired = "Low Pulley Cable Station & Rope Attachment",
            coachTip = "Neutral grip thickens the arm by targeting the brachialis muscle underneath the bicep."
        ),

        // TRICEPS
        "ex_triceps_pushdown" to ExerciseDetailData(
            exerciseId = "ex_triceps_pushdown",
            exerciseName = "Triceps Cable Pushdown",
            category = "Triceps",
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
        "ex_tri_skull_crushers" to ExerciseDetailData(
            exerciseId = "ex_tri_skull_crushers",
            exerciseName = "EZ-Bar Skull Crushers (Lying Extension)",
            category = "Triceps",
            metadataSubhead = "3 sets · 10 reps · 25 kg",
            videoTitle = "Lying Triceps Extension Setup",
            instructions = listOf(
                "Lie on a flat bench holding an EZ-curl bar above your chest with arms vertical.",
                "Angle your upper arms back slightly (roughly 75 degrees from torso).",
                "Bend your elbows to lower the bar toward the crown of your head.",
                "Extend elbows explosively to return to starting position."
            ),
            primaryMuscle = "Triceps Brachii (Long Head)",
            secondaryMuscles = listOf("Medial & Lateral Triceps Heads"),
            equipmentRequired = "Flat Bench & EZ-Bar",
            coachTip = "Angling arms back keeps continuous tension on the long head at the top lockout."
        ),
        "ex_tri_overhead_cable_extension" to ExerciseDetailData(
            exerciseId = "ex_tri_overhead_cable_extension",
            exerciseName = "Overhead Cable Triceps Extension",
            category = "Triceps",
            metadataSubhead = "3 sets · 12 reps · 18 kg",
            videoTitle = "Overhead Cable Extension Form",
            instructions = listOf(
                "Attach rope to high or mid cable pulley, step forward into a lunge stance facing away.",
                "Extend arms overhead with elbows pointing forward.",
                "Bend elbows behind head to feel a deep triceps stretch.",
                "Lock arms out straight forward and flare rope outward."
            ),
            primaryMuscle = "Triceps Brachii (Long Head Stretch)",
            secondaryMuscles = listOf("Core & Shoulders for Stabilization"),
            equipmentRequired = "Cable Station & Rope Attachment",
            coachTip = "Keep elbows tucked in; do not allow them to flare wide out to the sides."
        ),

        // LEGS
        "ex_barbell_squat" to ExerciseDetailData(
            exerciseId = "ex_barbell_squat",
            exerciseName = "Barbell Back Squat",
            category = "Legs",
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
        "ex_leg_incline_leg_press" to ExerciseDetailData(
            exerciseId = "ex_leg_incline_leg_press",
            exerciseName = "45° Incline Leg Press",
            category = "Legs",
            metadataSubhead = "4 sets · 12 reps · 140 kg",
            videoTitle = "Incline Leg Press Stance & Depth",
            instructions = listOf(
                "Sit firmly in the leg press seat with back and glutes pressed flat against pads.",
                "Place feet shoulder-width on the sled platform.",
                "Release safety locks and lower the carriage under control until knees reach 90 degrees.",
                "Drive through the whole foot to press the sled back up without locking knees."
            ),
            primaryMuscle = "Quadriceps & Gluteus Maximus",
            secondaryMuscles = listOf("Hamstrings", "Calves"),
            equipmentRequired = "45-Degree Leg Press Machine",
            coachTip = "Never allow your lower back or tailbone to round off the seat at bottom depth."
        ),
        "ex_leg_quad_extension" to ExerciseDetailData(
            exerciseId = "ex_leg_quad_extension",
            exerciseName = "Leg Extension Machine",
            category = "Legs",
            metadataSubhead = "3 sets · 15 reps · 50 kg",
            videoTitle = "Seated Leg Extension Execution",
            instructions = listOf(
                "Align knee joints directly with the machine's pivot axis.",
                "Position shin pad just above ankles.",
                "Extend legs fully, flexing quads hard at the top for 1 full second.",
                "Lower the weight smoothly over 2 seconds."
            ),
            primaryMuscle = "Quadriceps (Rectus Femoris Isolation)",
            secondaryMuscles = listOf("Tibialis Anterior"),
            equipmentRequired = "Leg Extension Machine",
            coachTip = "Hold the top contraction for 1 second to maximize teardrop quad definition."
        ),
        "ex_leg_lying_hamstring_curl" to ExerciseDetailData(
            exerciseId = "ex_leg_lying_hamstring_curl",
            exerciseName = "Lying Hamstring Curl",
            category = "Legs",
            metadataSubhead = "3 sets · 12 reps · 40 kg",
            videoTitle = "Lying Hamstring Curl Form",
            instructions = listOf(
                "Lie face down with roller pad positioned just below calf muscles.",
                "Grip side handles and keep hips pressed flat onto the bench.",
                "Curl heels toward glutes smoothly.",
                "Lower with control to full knee extension."
            ),
            primaryMuscle = "Hamstrings (Biceps Femoris, Semitendinosus)",
            secondaryMuscles = listOf("Gastrocnemius (Calves)"),
            equipmentRequired = "Lying Leg Curl Machine",
            coachTip = "Do not allow hips to lift off the bench during the curl."
        ),
        "ex_leg_romanian_deadlift" to ExerciseDetailData(
            exerciseId = "ex_leg_romanian_deadlift",
            exerciseName = "Barbell Romanian Deadlift (RDL)",
            category = "Legs",
            metadataSubhead = "4 sets · 10 reps · 70 kg",
            videoTitle = "Romanian Deadlift Hip Hinge",
            instructions = listOf(
                "Stand tall holding the barbell with shoulder-width overhand grip.",
                "Keep knees soft and push hips back behind you in a pure hip hinge.",
                "Lower the bar along your shins until you feel a deep hamstring stretch.",
                "Drive hips forward and contract glutes to return to standing upright."
            ),
            primaryMuscle = "Hamstrings & Gluteus Maximus",
            secondaryMuscles = listOf("Erector Spinae", "Forearms"),
            equipmentRequired = "Olympic Barbell & Plates",
            coachTip = "Think about pushing your hips toward the wall behind you rather than reaching down with your arms."
        ),

        // ABS & CORE
        "ex_hanging_leg_raise" to ExerciseDetailData(
            exerciseId = "ex_hanging_leg_raise",
            exerciseName = "Hanging Leg Raise",
            category = "Abs",
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
        ),
        "ex_abs_cable_rope_crunch" to ExerciseDetailData(
            exerciseId = "ex_abs_cable_rope_crunch",
            exerciseName = "Kneeling Cable Rope Crunch",
            category = "Abs",
            metadataSubhead = "3 sets · 15 reps · 35 kg",
            videoTitle = "Kneeling Cable Ab Crunch Form",
            instructions = listOf(
                "Kneel below high cable pulley holding rope ends by your ears.",
                "Hips stay fixed; initiate by flexing spine to draw elbows toward thighs.",
                "Exhale forcefully and squeeze abs hard at the bottom.",
                "Slowly extend spine back up without sitting back on your heels."
            ),
            primaryMuscle = "Rectus Abdominis (Upper & Mid Abs)",
            secondaryMuscles = listOf("Obliques"),
            equipmentRequired = "High Cable Pulley & Rope Attachment",
            coachTip = "Move through spinal flexion, not hip flexion. Do not sit back on your heels."
        ),
        "ex_abs_plank" to ExerciseDetailData(
            exerciseId = "ex_abs_plank",
            exerciseName = "Core Stabilization Plank",
            category = "Abs",
            metadataSubhead = "3 sets · 45 sec · Bodyweight",
            videoTitle = "Strict Core Plank Alignment",
            instructions = listOf(
                "Place forearms on the floor with elbows directly under shoulders.",
                "Extend legs back with toes on floor, body forming a straight line from head to heels.",
                "Brace abs tight as if about to be punched, squeeze glutes and quads.",
                "Breathe calmly while holding tension."
            ),
            primaryMuscle = "Transverse Abdominis & Deep Core",
            secondaryMuscles = listOf("Glutes", "Shoulders", "Erectors"),
            equipmentRequired = "Exercise Mat",
            coachTip = "Tuck your tailbone slightly under to eliminate lower back sag."
        )
    )

    fun getAllExercises(): List<ExerciseDetailData> = catalog.values.toList()

    fun getExercisesByCategory(categoryName: String): List<ExerciseDetailData> {
        val norm = categoryName.trim().lowercase()
        if (norm == "all") return getAllExercises()
        return catalog.values.filter { it.category.lowercase() == norm }
    }

    fun searchExercises(query: String, categoryFilter: String = "ALL"): List<ExerciseDetailData> {
        val q = query.trim().lowercase()
        val baseList = if (categoryFilter.equals("ALL", ignoreCase = true)) {
            getAllExercises()
        } else {
            getExercisesByCategory(categoryFilter)
        }

        if (q.isBlank()) return baseList

        return baseList.filter {
            it.exerciseName.lowercase().contains(q) ||
                    it.category.lowercase().contains(q) ||
                    it.primaryMuscle.lowercase().contains(q) ||
                    it.equipmentRequired.lowercase().contains(q)
        }
    }

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
            normalized.contains("raise") || normalized.contains("hanging") || normalized.contains("abs") || normalized.contains("crunch") || normalized.contains("plank") ->
                catalog["ex_hanging_leg_raise"]!!
            else ->
                catalog["ex_chest_press"]!!
        }
    }
}
