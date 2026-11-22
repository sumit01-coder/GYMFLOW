import urllib.request
import urllib.error
import json
import sys

API_KEY = "AIzaSyBaABE1ZF9KMcbMOea4WPndeLZEDBnJNNI"
PROJECT_ID = "gymflow-fitness-app"
AUTH_URL = f"https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key={API_KEY}"
FIRESTORE_BASE = f"https://firestore.googleapis.com/v1/projects/{PROJECT_ID}/databases/(default)/documents"

def sign_in(email, password):
    payload = json.dumps({
        "email": email,
        "password": password,
        "returnSecureToken": True
    }).encode("utf-8")
    req = urllib.request.Request(AUTH_URL, data=payload, headers={"Content-Type": "application/json"})
    try:
        with urllib.request.urlopen(req) as resp:
            return json.loads(resp.read().decode("utf-8"))
    except urllib.error.HTTPError as e:
        return {"error": json.loads(e.read().decode("utf-8"))}

def get_firestore_doc(collection, doc_id, id_token):
    url = f"{FIRESTORE_BASE}/{collection}/{doc_id}"
    req = urllib.request.Request(url, headers={"Authorization": f"Bearer {id_token}"})
    try:
        with urllib.request.urlopen(req) as resp:
            return json.loads(resp.read().decode("utf-8"))
    except urllib.error.HTTPError as e:
        return {"error": e.code, "message": e.read().decode("utf-8")}

def list_firestore_collection(collection_path, id_token):
    url = f"{FIRESTORE_BASE}/{collection_path}"
    req = urllib.request.Request(url, headers={"Authorization": f"Bearer {id_token}"})
    try:
        with urllib.request.urlopen(req) as resp:
            return json.loads(resp.read().decode("utf-8"))
    except urllib.error.HTTPError as e:
        return {"error": e.code, "message": e.read().decode("utf-8")}

def parse_fields(fields_dict):
    result = {}
    if not fields_dict:
        return result
    for k, v in fields_dict.items():
        if "stringValue" in v:
            result[k] = v["stringValue"]
        elif "integerValue" in v:
            result[k] = int(v["integerValue"])
        elif "doubleValue" in v:
            result[k] = float(v["doubleValue"])
        elif "booleanValue" in v:
            result[k] = v["booleanValue"]
        elif "arrayValue" in v:
            result[k] = [parse_fields({"_": item}).get("_") for item in v["arrayValue"].get("values", [])]
        elif "mapValue" in v:
            result[k] = parse_fields(v["mapValue"].get("fields", {}))
        elif "nullValue" in v:
            result[k] = None
        else:
            result[k] = v
    return result

def run_audit():
    print("=" * 70)
    print("  GYMFLOW LIVE SERVER DATA INTEGRATION & SYNC AUDIT")
    print("=" * 70)

    # 1. Authenticate Member
    print("\n[AUTH 1/2] Authenticating Member: sumitpatel00784@gmail.com...")
    member_auth = sign_in("sumitpatel00784@gmail.com", "1234567")
    if "error" in member_auth:
        print(f"[-] Member Auth Failed: {member_auth['error']}")
        return
    member_uid = member_auth["localId"]
    member_token = member_auth["idToken"]
    print(f"[+] Member Authenticated Successfully! UID: {member_uid}")

    # 2. Authenticate Trainer
    print("\n[AUTH 2/2] Authenticating Trainer: sumitpatel98894@gmail.com...")
    trainer_auth = sign_in("sumitpatel98894@gmail.com", "123456")
    if "error" in trainer_auth:
        print(f"[-] Trainer Auth Failed: {trainer_auth['error']}")
        return
    trainer_uid = trainer_auth["localId"]
    trainer_token = trainer_auth["idToken"]
    print(f"[+] Trainer Authenticated Successfully! UID: {trainer_uid}")

    # 3. Check Member User Document in Live Firestore
    print("\n" + "-" * 70)
    print(f"[FIRESTORE] Fetching Member Profile: users/{member_uid}...")
    member_doc = get_firestore_doc("users", member_uid, member_token)
    if "error" in member_doc:
        print(f"[-] Member document not found or error: {member_doc}")
    else:
        fields = parse_fields(member_doc.get("fields", {}))
        print(f"[+] Member Document Exists on Live Server:")
        for k, v in fields.items():
            print(f"    • {k}: {v}")

    # 4. Check Trainer User Document in Live Firestore
    print("\n" + "-" * 70)
    print(f"[FIRESTORE] Fetching Trainer Profile: users/{trainer_uid}...")
    trainer_doc = get_firestore_doc("users", trainer_uid, trainer_token)
    if "error" in trainer_doc:
        print(f"[-] Trainer document not found or error: {trainer_doc}")
    else:
        fields = parse_fields(trainer_doc.get("fields", {}))
        print(f"[+] Trainer Document Exists on Live Server:")
        for k, v in fields.items():
            print(f"    • {k}: {v}")

    # 5. Check Trainer Studio Document in Live Firestore
    print("\n" + "-" * 70)
    print(f"[FIRESTORE] Fetching Trainer Studio Record: trainers/{trainer_uid}...")
    studio_doc = get_firestore_doc("trainers", trainer_uid, trainer_token)
    if "error" in studio_doc:
        print(f"[*] Studio document note: {studio_doc}")
    else:
        fields = parse_fields(studio_doc.get("fields", {}))
        print(f"[+] Trainer Studio Document Exists on Live Server:")
        for k, v in fields.items():
            print(f"    • {k}: {v}")

    # 6. Check Connected Members Subcollection
    print("\n" + "-" * 70)
    print(f"[FIRESTORE] Fetching trainers/{trainer_uid}/connected_members...")
    connected_resp = list_firestore_collection(f"trainers/{trainer_uid}/connected_members", trainer_token)
    if "error" in connected_resp:
        print(f"[*] Connected members list note: {connected_resp}")
    else:
        docs = connected_resp.get("documents", [])
        print(f"[+] Found {len(docs)} connected member records in trainer subcollection:")
        for d in docs:
            name = d.get("name", "").split("/")[-1]
            f = parse_fields(d.get("fields", {}))
            print(f"    • [{name}]: {f}")

    # 7. Check Active Trainer Invite Codes
    print("\n" + "-" * 70)
    print(f"[FIRESTORE] Fetching trainer_invite_codes...")
    codes_resp = list_firestore_collection("trainer_invite_codes", trainer_token)
    if "error" in codes_resp:
        print(f"[*] Invite codes note: {codes_resp}")
    else:
        docs = codes_resp.get("documents", [])
        print(f"[+] Found {len(docs)} trainer invite codes in live Firestore:")
        for d in docs:
            code_id = d.get("name", "").split("/")[-1]
            f = parse_fields(d.get("fields", {}))
            print(f"    • Code [{code_id}]: Trainer='{f.get('trainerName')}' ({f.get('trainerId')}), Gym='{f.get('gymName')}'")

    # 8. Check Workout Plans
    print("\n" + "-" * 70)
    print(f"[FIRESTORE] Fetching plans...")
    plans_resp = list_firestore_collection("plans", trainer_token)
    if "error" in plans_resp:
        print(f"[*] Plans note: {plans_resp}")
    else:
        docs = plans_resp.get("documents", [])
        print(f"[+] Found {len(docs)} workout plans in live Firestore:")
        for d in docs:
            pid = d.get("name", "").split("/")[-1]
            f = parse_fields(d.get("fields", {}))
            print(f"    • Plan [{pid}]: '{f.get('name')}' for memberId='{f.get('memberId')}', trainerId='{f.get('trainerId')}'")

    # 9. Check Workout Sessions
    print("\n" + "-" * 70)
    print(f"[FIRESTORE] Fetching workout_sessions...")
    sessions_resp = list_firestore_collection("workout_sessions", member_token)
    if "error" in sessions_resp:
        print(f"[*] Sessions note: {sessions_resp}")
    else:
        docs = sessions_resp.get("documents", [])
        print(f"[+] Found {len(docs)} workout sessions in live Firestore:")
        for d in docs:
            sid = d.get("name", "").split("/")[-1]
            f = parse_fields(d.get("fields", {}))
            print(f"    • Session [{sid}]: memberId='{f.get('memberId')}', title='{f.get('workoutTitle')}', completed={f.get('isCompleted')}")

    print("\n" + "=" * 70)
    print("  AUDIT COMPLETED")
    print("=" * 70)

if __name__ == "__main__":
    run_audit()
