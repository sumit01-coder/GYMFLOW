package com.gymflow.feature.member.fatloss

object TargetFatLossCatalog {

    val zones: List<TargetFatLossZone> = listOf(
        // Zone 1: Belly & Lower Abs
        TargetFatLossZone(
            id = "belly_core",
            name = "Belly & Lower Abs",
            subtitle = "Flatten lower stomach & strengthen transverse core",
            bodyView = BodyView.FRONT,
            anatomyHighlight = "Rectus Abdominis, Transverse Abdominis & Visceral Adipose Tissue",
            scienceOverview = "Abdominal fat is composed of visceral fat (surrounding organs) and subcutaneous fat (beneath the skin). Visceral fat mobilizes rapidly in response to high-intensity cardiovascular intervals and a consistent 400-500 kcal deficit. Strengthening the deep transverse abdominis acts as an internal corset, tightening the belly wall and pulling the lower stomach inward.",
            realisticTimelineWeeks = 6,
            recommendedCaloricDeficitKcal = 500,
            nutritionKeyAdvice = listOf(
                "Prioritize high-fiber complex carbohydrates (oats, quinoa) to eliminate insulin spikes that trigger visceral fat storage.",
                "Maintain 1.8g to 2.2g of protein per kg of body weight to prevent muscle breakdown during a caloric deficit.",
                "Reduce sodium intake below 2,000mg/day to flush extracellular abdominal water retention.",
                "Avoid late-night high-glycemic snacks; keep fasting windows of at least 12 hours overnight."
            ),
            hiitBurnerTitle = "15-Min Tabata Belly Melt Circuit",
            hiitBurnerDurationMin = 15,
            hiitCircuitExercises = listOf(
                TargetZoneExercise(
                    name = "High Knees Sprint",
                    targetMuscle = "Cardio & Lower Abs",
                    repsOrDuration = "45s work / 15s rest",
                    instruction = "Drive knees up to hip height at maximum sprint cadence, engaging your core with every stride."
                ),
                TargetZoneExercise(
                    name = "Mountain Climbers",
                    targetMuscle = "Transverse Abdominis & Core",
                    repsOrDuration = "45s work / 15s rest",
                    instruction = "Maintain a flat high plank position; alternate driving your knees straight into your chest explosively."
                ),
                TargetZoneExercise(
                    name = "Bicycle Crunches",
                    targetMuscle = "Rectus Abdominis & Obliques",
                    repsOrDuration = "45s work / 15s rest",
                    instruction = "Rotate elbow to opposite knee with control. Fully extend the non-working leg parallel to the floor."
                ),
                TargetZoneExercise(
                    name = "Plank Jacks with Shoulder Taps",
                    targetMuscle = "Full Core & Shoulder Stabilizers",
                    repsOrDuration = "45s work / 15s rest",
                    instruction = "Hold a pushup plank; jump feet apart and together while tapping alternating shoulders without swaying hips."
                )
            ),
            firmingRoutineExercises = listOf(
                TargetZoneExercise(
                    name = "Hanging Knee / Leg Raises",
                    targetMuscle = "Lower Rectus Abdominis",
                    repsOrDuration = "12 - 15 reps",
                    sets = 4,
                    instruction = "Hang from a pullup bar without swinging. Curl pelvis upward toward sternum to fully contract the lower abdomen."
                ),
                TargetZoneExercise(
                    name = "Hollow Body Hold",
                    targetMuscle = "Deep Transverse Abdominis",
                    repsOrDuration = "40 - 60 seconds",
                    sets = 3,
                    instruction = "Press lower back firmly into the floor. Lift shoulders and straight legs 6 inches off ground, holding a crescent hollow."
                ),
                TargetZoneExercise(
                    name = "Deadbug Core Stabilization",
                    targetMuscle = "Deep Core & Pelvic Floor",
                    repsOrDuration = "10 reps per side",
                    sets = 3,
                    instruction = "Extend opposite arm and leg while maintaining continuous abdominal pressure against the mat."
                ),
                TargetZoneExercise(
                    name = "Cable / Dumbbell Woodchoppers",
                    targetMuscle = "Rotational Core & Obliques",
                    repsOrDuration = "12 reps per side",
                    sets = 3,
                    instruction = "Pivot hips and pull diagonally across torso with tight abdominal bracing."
                )
            )
        ),

        // Zone 2: Love Handles & Flanks
        TargetFatLossZone(
            id = "love_handles",
            name = "Love Handles & Flanks",
            subtitle = "Sculpt waistline taper & tighten lateral obliques",
            bodyView = BodyView.FRONT,
            anatomyHighlight = "External & Internal Obliques, Serratus Anterior",
            scienceOverview = "Lateral flank fat tends to store stubborn alpha-2 adrenergic fat cells. To mobilize this area, high-tension rotational exercises stimulate localized blood flow and tone the obliques, while steady caloric restriction shrinks the overlying adipose tissue, producing a sharp, athletic V-taper.",
            realisticTimelineWeeks = 8,
            recommendedCaloricDeficitKcal = 450,
            nutritionKeyAdvice = listOf(
                "Drink 3 to 4 liters of water daily to prevent dehydration-induced fluid pooling around the lower waist.",
                "Replace simple sugars with cruciferous vegetables (broccoli, spinach) which support estrogen clearance.",
                "Incorporate green tea or matcha for natural EGCG catechins that support stubborn fat oxidation during cardio."
            ),
            hiitBurnerTitle = "12-Min Flank Torch & Skater HIIT",
            hiitBurnerDurationMin = 12,
            hiitCircuitExercises = listOf(
                TargetZoneExercise(
                    name = "Speed Skater Jumps",
                    targetMuscle = "Lateral Core & Glutes",
                    repsOrDuration = "40s work / 20s rest",
                    instruction = "Bound laterally from side to side, sweeping rear foot behind and touching floor with opposing hand."
                ),
                TargetZoneExercise(
                    name = "Standing Russian Twist Punches",
                    targetMuscle = "Obliques & Core Rotators",
                    repsOrDuration = "40s work / 20s rest",
                    instruction = "Fast rotational core punches while holding a deep athletic quarter-squat."
                ),
                TargetZoneExercise(
                    name = "Spiderman Plank Crunches",
                    targetMuscle = "External Obliques",
                    repsOrDuration = "40s work / 20s rest",
                    instruction = "In pushup plank, bring right knee outward to touch right elbow; alternate sides rhythmically."
                )
            ),
            firmingRoutineExercises = listOf(
                TargetZoneExercise(
                    name = "Side Plank Hip Dips",
                    targetMuscle = "Internal & External Obliques",
                    repsOrDuration = "15 reps per side",
                    sets = 3,
                    instruction = "From an elbow side plank, lower hip until it grazes the floor, then drive hip upward past neutral."
                ),
                TargetZoneExercise(
                    name = "Russian Twists with Weight",
                    targetMuscle = "Transverse Obliques",
                    repsOrDuration = "20 total reps",
                    sets = 4,
                    instruction = "Sit with torso leaned back 45 degrees, feet elevated. Twist weight across body touching floor on both sides."
                ),
                TargetZoneExercise(
                    name = "Windshield Wipers (Floor or Bar)",
                    targetMuscle = "Deep Lateral Abdominals",
                    repsOrDuration = "10 reps per side",
                    sets = 3,
                    instruction = "Lie on back with legs straight up perpendicular to floor. Slowly sweep legs side to side in an arc."
                )
            )
        ),

        // Zone 3: Chest & Upper Pecs
        TargetFatLossZone(
            id = "chest_pecs",
            name = "Chest & Upper Pecs",
            subtitle = "Firm chest tissue & build sculpted pectoral shelf",
            bodyView = BodyView.FRONT,
            anatomyHighlight = "Pectoralis Major (Clavicular Head) & Pectoralis Minor",
            scienceOverview = "Chest fat in both men and women is often exacerbated by weak pectoral muscle development beneath the skin. Building upper-chest thickness with incline presses lifts the chest profile, while compound upper-body circuits deplete glycogen and incinerate excess chest fat.",
            realisticTimelineWeeks = 8,
            recommendedCaloricDeficitKcal = 450,
            nutritionKeyAdvice = listOf(
                "Emphasize lean whole proteins (chicken breast, egg whites, white fish) to support chest muscle protein synthesis.",
                "Avoid heavily processed soy and phytoestrogen concentrates if prone to gynecomastia.",
                "Ensure zinc and magnesium intake (pumpkin seeds, spinach) to support healthy hormone balance."
            ),
            hiitBurnerTitle = "14-Min Upper Body Push Burner",
            hiitBurnerDurationMin = 14,
            hiitCircuitExercises = listOf(
                TargetZoneExercise(
                    name = "Burpee Push-Up Explosions",
                    targetMuscle = "Pecs & Cardiovascular",
                    repsOrDuration = "40s work / 20s rest",
                    instruction = "Drop into a pushup, snap feet forward and leap explosively overhead."
                ),
                TargetZoneExercise(
                    name = "Shadow Boxing Upper Cuts",
                    targetMuscle = "Front Delts & Pectorals",
                    repsOrDuration = "40s work / 20s rest",
                    instruction = "Continuous high-speed uppercuts and hooks with tight core engagement."
                ),
                TargetZoneExercise(
                    name = "Bear Crawl to Push-Up",
                    targetMuscle = "Chest & Core Stability",
                    repsOrDuration = "40s work / 20s rest",
                    instruction = "Crawl forward 4 steps on toes and hands, perform 1 push-up, crawl back."
                )
            ),
            firmingRoutineExercises = listOf(
                TargetZoneExercise(
                    name = "Incline Dumbbell Press",
                    targetMuscle = "Upper Clavicular Pectoralis",
                    repsOrDuration = "10 - 12 reps",
                    sets = 4,
                    instruction = "Set bench to 30 degrees. Press dumbbells upward, focusing on squeezing top of chest together."
                ),
                TargetZoneExercise(
                    name = "Decline Push-Ups (Feet Elevated)",
                    targetMuscle = "Upper Chest & Shoulders",
                    repsOrDuration = "12 - 15 reps",
                    sets = 3,
                    instruction = "Elevate feet on bench or chair; lower chest to floor and press up with maximum pectoral recruitment."
                ),
                TargetZoneExercise(
                    name = "Cable / Dumbbell Flyes",
                    targetMuscle = "Sternal Pectoralis & Chest Stretch",
                    repsOrDuration = "12 - 15 reps",
                    sets = 3,
                    instruction = "Wide arc hug motion. Emphasize deep chest stretch at bottom and hard contraction at peak."
                )
            )
        ),

        // Zone 4: Arms & Triceps
        TargetFatLossZone(
            id = "arms_triceps",
            name = "Arms & Triceps",
            subtitle = "Eliminate bat-wing flab & sculpt athletic arm definition",
            bodyView = BodyView.FRONT,
            anatomyHighlight = "Triceps Brachii (Long, Lateral & Medial Heads)",
            scienceOverview = "The posterior upper arm (triceps) represents 60% of total arm muscle mass. Loose skin and fat in this area are resolved by developing all three heads of the triceps with high-tension extension movements, which stretch the skin tight over firm, dense muscle.",
            realisticTimelineWeeks = 6,
            recommendedCaloricDeficitKcal = 400,
            nutritionKeyAdvice = listOf(
                "Consume 25g-35g protein within 45 minutes post-workout to maximize arm muscle repair.",
                "Maintain adequate omega-3 fatty acids (salmon, chia seeds) to enhance skin elasticity during fat reduction."
            ),
            hiitBurnerTitle = "12-Min Arm Calorie Blitz",
            hiitBurnerDurationMin = 12,
            hiitCircuitExercises = listOf(
                TargetZoneExercise(
                    name = "Diamond Push-Up Pulses",
                    targetMuscle = "Triceps Lateral & Medial Heads",
                    repsOrDuration = "30s work / 30s rest",
                    instruction = "Hands in diamond shape beneath chest; lower and pulse with focused triceps tension."
                ),
                TargetZoneExercise(
                    name = "Bench Dips with Speed Kick",
                    targetMuscle = "Triceps & Core",
                    repsOrDuration = "40s work / 20s rest",
                    instruction = "Dip with back close to bench, kicking alternating legs at top of movement."
                ),
                TargetZoneExercise(
                    name = "Plank Up-Downs (Military Plank)",
                    targetMuscle = "Triceps & Shoulders",
                    repsOrDuration = "40s work / 20s rest",
                    instruction = "Transition from forearm plank to pushup plank one arm at a time."
                )
            ),
            firmingRoutineExercises = listOf(
                TargetZoneExercise(
                    name = "Overhead Cable / Dumbbell Triceps Extension",
                    targetMuscle = "Triceps Long Head",
                    repsOrDuration = "12 - 15 reps",
                    sets = 4,
                    instruction = "Keep elbows pinned tight next to head. Extend arms fully overhead, squeezing the back of arms."
                ),
                TargetZoneExercise(
                    name = "Triceps Rope Pushdowns",
                    targetMuscle = "Triceps Lateral Head",
                    repsOrDuration = "12 - 15 reps",
                    sets = 3,
                    instruction = "Push rope down and spread hands apart at bottom, locking out triceps completely."
                ),
                TargetZoneExercise(
                    name = "Dumbbell Kickbacks",
                    targetMuscle = "Triceps Peak Contraction",
                    repsOrDuration = "12 reps per arm",
                    sets = 3,
                    instruction = "Hinge forward, pin elbow high at side, extend forearm backward until arm is completely straight."
                )
            )
        ),

        // Zone 5: Thighs & Quads
        TargetFatLossZone(
            id = "thighs_legs",
            name = "Thighs & Inner/Outer Legs",
            subtitle = "Slim thigh circumference & firm inner and outer quad lines",
            bodyView = BodyView.FRONT,
            anatomyHighlight = "Quadriceps, Adductors (Inner Thigh), Abductors",
            scienceOverview = "The leg musculature represents the largest metabolic furnace in the human body. Heavy compound lower-body training burns more calories both during and after exercise than any other group, dramatically depleting fat stores across the lower extremities.",
            realisticTimelineWeeks = 8,
            recommendedCaloricDeficitKcal = 500,
            nutritionKeyAdvice = listOf(
                "Time high-quality carbohydrates around leg workout sessions to fuel maximum output and heavy calorie expenditure.",
                "Ensure sufficient potassium (bananas, sweet potatoes) and magnesium to prevent cramping and support lymphatic drainage."
            ),
            hiitBurnerTitle = "15-Min Lower Body Torch Circuit",
            hiitBurnerDurationMin = 15,
            hiitCircuitExercises = listOf(
                TargetZoneExercise(
                    name = "Jump Squats with Pause",
                    targetMuscle = "Quads, Glutes & Calves",
                    repsOrDuration = "40s work / 20s rest",
                    instruction = "Sink into a deep squat, pause 1 second, then leap explosively into the air."
                ),
                TargetZoneExercise(
                    name = "Alternating Jump Lunges",
                    targetMuscle = "Quads & Glutes",
                    repsOrDuration = "40s work / 20s rest",
                    instruction = "Explode upward from lunge, switching legs in mid-air and landing softly into next lunge."
                ),
                TargetZoneExercise(
                    name = "Speed Lateral Bounds",
                    targetMuscle = "Inner & Outer Thighs",
                    repsOrDuration = "40s work / 20s rest",
                    instruction = "Bound wide side to side, landing on outside foot and decelerating with quad strength."
                )
            ),
            firmingRoutineExercises = listOf(
                TargetZoneExercise(
                    name = "Goblet Squats",
                    targetMuscle = "Quadriceps & Core",
                    repsOrDuration = "12 reps",
                    sets = 4,
                    instruction = "Hold dumbbell vertically at chest. Squat until thighs break parallel, drive up through heels."
                ),
                TargetZoneExercise(
                    name = "Sumo Squats (Wide Stance)",
                    targetMuscle = "Inner Thighs (Adductors) & Glutes",
                    repsOrDuration = "12 - 15 reps",
                    sets = 4,
                    instruction = "Stand with feet 1.5x shoulder width, toes pointed 45 degrees out. Squeeze inner thighs at top."
                ),
                TargetZoneExercise(
                    name = "Walking Dumbbell Lunges",
                    targetMuscle = "Quadriceps & Hamstrings",
                    repsOrDuration = "20 total steps",
                    sets = 3,
                    instruction = "Take long strides, lowering rear knee until 1 inch off floor. Keep chest tall."
                )
            )
        ),

        // Zone 6: Glutes & Hips
        TargetFatLossZone(
            id = "glutes_hips",
            name = "Glutes & Hips",
            subtitle = "Lift and shape gluteal fold while eliminating hip fat",
            bodyView = BodyView.BACK,
            anatomyHighlight = "Gluteus Maximus, Gluteus Medius & Upper Hamstrings",
            scienceOverview = "A firm, lifted glute profile visually transforms body proportions by elevating the gluteal crease and creating an athletic waist-to-hip contour. Combining targeted progressive overload hip thrusts with caloric control yields tight, shapely glutes.",
            realisticTimelineWeeks = 8,
            recommendedCaloricDeficitKcal = 450,
            nutritionKeyAdvice = listOf(
                "Maintain adequate calorie intake on leg/glute training days so the muscle group can build while you lose overall fat.",
                "Ensure high amino acid intake to facilitate fast repair of heavy hip thrust movements."
            ),
            hiitBurnerTitle = "12-Min Posterior Power HIIT",
            hiitBurnerDurationMin = 12,
            hiitCircuitExercises = listOf(
                TargetZoneExercise(
                    name = "Glute Bridge Pulses",
                    targetMuscle = "Gluteus Maximus",
                    repsOrDuration = "45s work / 15s rest",
                    instruction = "Drive hips up into full bridge, hold and pulse at peak contraction for 45 seconds."
                ),
                TargetZoneExercise(
                    name = "Frog Pumps",
                    targetMuscle = "Gluteus Medius & Maximus",
                    repsOrDuration = "45s work / 15s rest",
                    instruction = "Soles of feet pressed together, knees wide. Pump hips upward rapidly squeezing glutes."
                ),
                TargetZoneExercise(
                    name = "Curtsy Lunge Pulses",
                    targetMuscle = "Gluteus Medius & Outer Hip",
                    repsOrDuration = "40s work / 20s rest",
                    instruction = "Step diagonally behind lead leg into a deep curtsy lunge, pulsing twice before alternating."
                )
            ),
            firmingRoutineExercises = listOf(
                TargetZoneExercise(
                    name = "Barbell / Dumbbell Hip Thrusts",
                    targetMuscle = "Gluteus Maximus",
                    repsOrDuration = "10 - 12 reps",
                    sets = 4,
                    instruction = "Upper back on bench, drive hips upward until thighs and torso align horizontally. Squeeze 2 seconds."
                ),
                TargetZoneExercise(
                    name = "Romanian Deadlifts (RDLs)",
                    targetMuscle = "Hamstrings & Lower Glute Crease",
                    repsOrDuration = "10 - 12 reps",
                    sets = 3,
                    instruction = "Hinge hips back with soft knees and flat back until deep stretch in hamstrings, then thrust forward."
                ),
                TargetZoneExercise(
                    name = "Bulgarian Split Squats",
                    targetMuscle = "Unilateral Glute & Quad",
                    repsOrDuration = "10 reps per leg",
                    sets = 3,
                    instruction = "Rear foot elevated on bench. Lower down until front thigh is parallel to ground."
                )
            )
        ),

        // Zone 7: Upper & Lower Back
        TargetFatLossZone(
            id = "back_posture",
            name = "Upper & Lower Back",
            subtitle = "Smooth bra bulge, tighten lats & restore tall athletic posture",
            bodyView = BodyView.BACK,
            anatomyHighlight = "Latissimus Dorsi, Rhomboids, Trapezius & Erector Spinae",
            scienceOverview = "Back fat and 'bra bulge' folds are aggravated by weak scapular retractors and rounded slouching shoulders. Strengthening the upper back pulls the shoulder blades back and down, immediately smoothing out back tissue and giving the illusion of an instant 2-3 inch waist reduction.",
            realisticTimelineWeeks = 6,
            recommendedCaloricDeficitKcal = 450,
            nutritionKeyAdvice = listOf(
                "Maintain optimal hydration to prevent stiff connective fascia in the back and shoulder region.",
                "Balance upper-body pulling movements with pushing movements at a 2:1 ratio to permanently correct posture."
            ),
            hiitBurnerTitle = "12-Min Back & Core Burner",
            hiitBurnerDurationMin = 12,
            hiitCircuitExercises = listOf(
                TargetZoneExercise(
                    name = "Renegade Row Push-Ups",
                    targetMuscle = "Lats, Upper Back & Core",
                    repsOrDuration = "40s work / 20s rest",
                    instruction = "Hold dumbbells in plank. Perform pushup, row dumbbell to hip on right, pushup, row on left."
                ),
                TargetZoneExercise(
                    name = "Superman Flutters",
                    targetMuscle = "Erector Spinae & Upper Back",
                    repsOrDuration = "45s work / 15s rest",
                    instruction = "Lie on belly, lift chest and thighs off floor, flutter arms and legs rhythmically."
                ),
                TargetZoneExercise(
                    name = "Speed Jumping Jacks with Back Squeeze",
                    targetMuscle = "Rhomboids & Trapezius",
                    repsOrDuration = "45s work / 15s rest",
                    instruction = "Pinch shoulder blades hard with every overhead reach."
                )
            ),
            firmingRoutineExercises = listOf(
                TargetZoneExercise(
                    name = "Lat Pulldowns / Pull-Ups",
                    targetMuscle = "Latissimus Dorsi",
                    repsOrDuration = "10 - 12 reps",
                    sets = 4,
                    instruction = "Pull bar to upper chest, driving elbows downward into your back pockets."
                ),
                TargetZoneExercise(
                    name = "Bent-Over Dumbbell Rows",
                    targetMuscle = "Rhomboids & Middle Trapezius",
                    repsOrDuration = "12 reps",
                    sets = 4,
                    instruction = "Hinge forward 45 degrees, pull dumbbells into ribcage, squeezing shoulder blades together."
                ),
                TargetZoneExercise(
                    name = "Face Pulls with External Rotation",
                    targetMuscle = "Rear Delts & Posture Muscles",
                    repsOrDuration = "15 reps",
                    sets = 3,
                    instruction = "Pull cable towards eye level, rotating thumbs back to activate lower traps."
                )
            )
        )
    )

    fun getZoneById(id: String): TargetFatLossZone? = zones.find { it.id == id }

    fun getZonesByView(view: BodyView): List<TargetFatLossZone> = zones.filter { it.bodyView == view }
}
