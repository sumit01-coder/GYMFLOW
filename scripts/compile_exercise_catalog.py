#!/usr/bin/env python3
"""
GymFlow Exercise Catalog Compiler
Fetches 1,323+ exercises from open-source repository JahelCuadrado/ExerciseGymGifsDB,
normalizes categories, equipment, default sets/reps/weights, machine setups, and instruction cues,
and exports the compiled catalog to app/src/main/assets/exercises_catalog.json.
"""

import os
import sys
import json
import urllib.request
import re

BASE_API_URL = "https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/api/en"
OUTPUT_DIR = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "app", "src", "main", "assets")
OUTPUT_FILE = os.path.join(OUTPUT_DIR, "exercises_catalog.json")

# Category normalization mapping
MUSCLE_TO_CATEGORY = {
    "pectorals": "Chest",
    "lats": "Back",
    "upper-back": "Back",
    "spine": "Back",
    "traps": "Back",
    "delts": "Shoulders",
    "levator-scapulae": "Shoulders",
    "biceps": "Biceps",
    "triceps": "Triceps",
    "forearms": "Biceps",
    "quads": "Legs",
    "hamstrings": "Legs",
    "glutes": "Legs",
    "calves": "Legs",
    "adductors": "Legs",
    "abductors": "Legs",
    "abs": "Abs",
    "serratus-anterior": "Abs",
    "cardio": "Cardio"
}

# Equipment normalization mapping
EQUIPMENT_MAP = {
    "barbell": "Barbell",
    "dumbbell": "Dumbbell",
    "cable": "Cable",
    "bodyweight": "Bodyweight",
    "band": "Resistance Band",
    "smith": "Smith Machine",
    "kettlebell": "Kettlebell",
    "lever": "Machine",
    "machine": "Machine",
    "ez-bar": "EZ-Bar",
    "sled": "Sled",
    "other": "Specialty"
}

# Machine setup notes derivation
def derive_machine_name(equipment: str, name: str, category: str) -> str:
    eq_lower = equipment.lower()
    name_lower = name.lower()

    if "smith" in eq_lower or "smith" in name_lower:
        return "Multi-Angle Smith Machine Station"
    if "cable" in eq_lower:
        if "pulldown" in name_lower:
            return "Cable Lat Pulldown Column with Bar Attachment"
        if "row" in name_lower:
            return "Low Cable Row Station with V-Grip"
        if "face" in name_lower or "triceps" in name_lower:
            return "High Cable Column with Rope Attachment"
        return "Dual Adjustable Cable Column"
    if "barbell" in eq_lower or "ez-bar" in eq_lower:
        if "incline" in name_lower:
            return "30° Incline Olympic Bench Press Station"
        if "decline" in name_lower:
            return "Decline Olympic Bench Press Station"
        if "squat" in name_lower:
            return "Heavy-Duty Power Rack & Olympic Barbell"
        if "bench" in name_lower:
            return "Flat Olympic Bench Press Station"
        if "curl" in name_lower:
            return "Preacher Bench / Olympic EZ-Curl Bar"
        return "Olympic Barbell & Bumper Plate Platform"
    if "dumbbell" in eq_lower:
        if "incline" in name_lower:
            return "Adjustable Incline Utility Bench (30°-45°)"
        if "seated" in name_lower:
            return "90° Upright Utility Bench & Dumbbell Rack"
        if "flat" in name_lower or "bench" in name_lower:
            return "Flat Utility Bench & Dumbbells"
        return "Precision Cast Iron Dumbbells"
    if "lever" in eq_lower or "machine" in eq_lower:
        if "leg press" in name_lower:
            return "Plate-Loaded 45° Sled Leg Press"
        if "hack" in name_lower:
            return "Hack Squat Sled Machine"
        if "extension" in name_lower:
            return "Pin-Loaded Leg Extension Machine"
        if "curl" in name_lower:
            return "Pin-Loaded Prone Leg Curl Machine"
        if "calf" in name_lower:
            return "Pin-Loaded Standing Calf Machine"
        if "pec deck" in name_lower or "fly" in name_lower:
            return "Dual Rear Delt / Pec Deck Machine"
        if "lat" in name_lower or "pulldown" in name_lower:
            return "Plate-Loaded Iso-Lateral Lat Pulldown"
        if "chest" in name_lower:
            return "Plate-Loaded Converging Chest Press"
        return "Selectorized / Plate-Loaded Gym Machine"
    if "bodyweight" in eq_lower:
        if "pull-up" in name_lower or "chin-up" in name_lower or "hanging" in name_lower:
            return "Multi-Grip Pull-Up Rig / Captain's Chair"
        if "dip" in name_lower:
            return "Parallel Dip Bars Station"
        return "Athletic Floor Mat / Calisthenics Rig"
    if "kettlebell" in eq_lower:
        return "Competition Kettlebell Set"
    if "band" in eq_lower:
        return "Heavy Elastic Loop Resistance Band"
    if "sled" in eq_lower:
        return "Prowler Turf Sled with Olympic Plates"
    return "Gym Training Station"

# Default set, rep, weight parameters
def derive_workout_parameters(equipment: str, category: str):
    eq = equipment.lower()
    cat = category.lower()

    if cat == "cardio":
        return 1, 1, 0.0, 30
    if "bodyweight" in eq:
        return 3, 15, 0.0, 45
    if "barbell" in eq:
        if cat in ["legs", "back"]:
            return 4, 8, 60.0, 90
        elif cat == "chest":
            return 4, 8, 50.0, 90
        else:
            return 3, 10, 30.0, 60
    if "dumbbell" in eq:
        if cat in ["legs", "back"]:
            return 3, 10, 22.0, 60
        elif cat == "chest":
            return 3, 10, 18.0, 60
        else:
            return 3, 12, 12.0, 45
    if "cable" in eq or "machine" in eq or "smith" in eq:
        if cat == "legs":
            return 4, 12, 60.0, 60
        elif cat in ["back", "chest"]:
            return 3, 12, 40.0, 45
        else:
            return 3, 12, 25.0, 45
    if "kettlebell" in eq:
        return 3, 15, 16.0, 45
    if "band" in eq:
        return 3, 15, 0.0, 45
    return 3, 12, 20.0, 45

# Coach tip generation
def derive_coach_tip(category: str, equipment: str, name: str) -> str:
    cat = category.lower()
    name_l = name.lower()
    if "squat" in name_l:
        return "Drive through your heels, keep knees aligned over toes, and maintain an upright chest."
    if "deadlift" in name_l:
        return "Hinge firmly at the hips, lock your lats tight, and drive through the floor without rounding lower back."
    if "bench" in name_l or "press" in name_l:
        return "Retract scapulae against pad, keep forearms vertical under load, and control the eccentric tempo."
    if "curl" in name_l:
        return "Pin elbows securely to your torso, eliminate momentum, and squeeze peak contraction for 1 second."
    if "raise" in name_l or "fly" in name_l:
        return "Lead with elbows rather than wrists to keep isolation focused directly on targeted muscle fibers."
    if "pulldown" in name_l or "row" in name_l:
        return "Initiate the movement by retracting shoulder blades first, pulling with elbows rather than hands."
    if "abs" in cat:
        return "Exhale forcefully on spinal flexion and focus on pelvic tilt rather than pulling with hip flexors."
    return "Control both the eccentric and concentric phases with intentional tempo and mind-muscle connection."

def main():
    print("=" * 60)
    print("GymFlow Exercise Catalog Generator (1,323+ Movements)")
    print("=" * 60)

    # 1. Fetch muscle group list
    muscles_url = f"{BASE_API_URL}/muscles.json"
    print(f"Fetching muscle list from {muscles_url}...")
    req = urllib.request.Request(muscles_url, headers={"User-Agent": "GymFlow-Compiler/1.0"})
    with urllib.request.urlopen(req) as response:
        muscles_data = json.loads(response.read().decode("utf-8"))

    print(f"Discovered {len(muscles_data)} muscle groups.")

    all_exercises = {}
    total_raw_count = 0

    # 2. Fetch each muscle group file
    for m_info in muscles_data:
        muscle_key = m_info["muscle"]
        endpoint = m_info["endpoint"]
        group_url = f"{BASE_API_URL}/{endpoint}"
        print(f" -> Fetching {muscle_key} ({m_info['count']} items)...")

        req = urllib.request.Request(group_url, headers={"User-Agent": "GymFlow-Compiler/1.0"})
        with urllib.request.urlopen(req) as response:
            group_json = json.loads(response.read().decode("utf-8"))
            exercises_list = group_json.get("exercises", [])
            total_raw_count += len(exercises_list)

            for item in exercises_list:
                slug = item.get("slug") or item.get("name", "").lower().replace(" ", "-")
                if slug in all_exercises:
                    continue  # already processed

                raw_name = item.get("name", "").strip()
                # Clean title casing
                clean_name = " ".join(word.capitalize() for word in raw_name.split())
                raw_muscle = item.get("muscle", "").lower()
                category = MUSCLE_TO_CATEGORY.get(raw_muscle, "Chest")
                raw_equipment = item.get("equipment", "machine").lower()
                equipment = EQUIPMENT_MAP.get(raw_equipment, "Machine")

                # Secondary muscles
                sec_muscles = item.get("secondaryMuscles", [])
                muscle_group_parts = [raw_muscle.capitalize()] + [m.capitalize() for m in sec_muscles]
                muscle_group_str = ", ".join(muscle_group_parts)

                machine_name = derive_machine_name(equipment, clean_name, category)
                sets, reps, weight_kg, rest_sec = derive_workout_parameters(equipment, category)
                instructions = item.get("instructions", [])
                gif_url = item.get("gifUrl", "")
                coach_tip = derive_coach_tip(category, equipment, clean_name)

                exercise_id = f"ex_{raw_muscle}_{slug}".replace("-", "_").replace("/", "_")

                all_exercises[slug] = {
                    "id": exercise_id,
                    "name": clean_name,
                    "category": category,
                    "muscleGroup": muscle_group_str,
                    "equipment": equipment,
                    "machineName": machine_name,
                    "defaultSets": sets,
                    "defaultReps": reps,
                    "defaultWeightKg": weight_kg,
                    "defaultRestSeconds": rest_sec,
                    "mediaUrl": gif_url,  # Coil renders GIFs directly from this mediaUrl
                    "gifUrl": gif_url,
                    "instructions": instructions,
                    "coachTip": coach_tip
                }

    catalog_list = list(all_exercises.values())
    # Sort deterministically by Category then Name
    catalog_list.sort(key=lambda x: (x["category"], x["name"]))

    print("-" * 60)
    print(f"Total raw fetched: {total_raw_count}")
    print(f"Total unique normalized exercises: {len(catalog_list)}")

    # Category breakdown stats
    cat_counts = {}
    for ex in catalog_list:
        cat_counts[ex["category"]] = cat_counts.get(ex["category"], 0) + 1

    for cat, count in sorted(cat_counts.items(), key=lambda x: -x[1]):
        print(f"  • {cat}: {count} exercises")

    os.makedirs(OUTPUT_DIR, exist_ok=True)
    with open(OUTPUT_FILE, "w", encoding="utf-8") as f:
        json.dump(catalog_list, f, indent=2, ensure_ascii=False)

    file_size_kb = os.path.getsize(OUTPUT_FILE) / 1024
    print(f"\n[SUCCESS] Exported {len(catalog_list)} exercises to:\n{OUTPUT_FILE} ({file_size_kb:.1f} KB)")
    print("=" * 60)

if __name__ == "__main__":
    main()

