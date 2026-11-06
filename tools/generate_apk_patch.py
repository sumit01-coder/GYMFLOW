import os
import sys
import struct
import hashlib

MAGIC_HEADER = b"GFDIFF10"
CMD_COPY = 0x01
CMD_DATA = 0x02
CMD_EOF = 0x00

def generate_patch(source_apk_path: str, target_apk_path: str, patch_output_path: str, block_size: int = 1024):
    """
    Generates a high-compression binary delta patch between source_apk and target_apk.
    Format compatible with GymFlow's ApkDeltaPatcher.kt
    """
    print(f"Reading source APK: {source_apk_path}...")
    with open(source_apk_path, "rb") as f:
        source_bytes = f.read()

    print(f"Reading target APK: {target_apk_path}...")
    with open(target_apk_path, "rb") as f:
        target_bytes = f.read()

    source_len = len(source_bytes)
    target_len = len(target_bytes)
    target_sha256 = hashlib.sha256(target_bytes).hexdigest()

    print(f"Source size: {source_len:,} bytes")
    print(f"Target size: {target_len:,} bytes")
    print(f"Target SHA-256: {target_sha256}")

    # Build signature index for source blocks
    print(f"Indexing source APK in {block_size}-byte blocks...")
    block_index = {}
    for i in range(0, source_len - block_size + 1, block_size):
        block = source_bytes[i:i + block_size]
        h = hashlib.md5(block).digest()
        if h not in block_index:
            block_index[h] = i

    print("Computing delta diff...")
    commands = []
    t_idx = 0
    pending_data = bytearray()

    while t_idx < target_len:
        if t_idx + block_size <= target_len:
            block = target_bytes[t_idx:t_idx + block_size]
            h = hashlib.md5(block).digest()
            if h in block_index:
                # Flush pending inserted data first
                if pending_data:
                    commands.append((CMD_DATA, bytes(pending_data)))
                    pending_data.clear()

                # Found match in source
                s_idx = block_index[h]
                match_len = block_size

                # Greedily extend match forward
                while (t_idx + match_len < target_len and
                       s_idx + match_len < source_len and
                       target_bytes[t_idx + match_len] == source_bytes[s_idx + match_len]):
                    match_len += 1

                commands.append((CMD_COPY, s_idx, match_len))
                t_idx += match_len
                continue

        # No match found, accumulate as new data
        pending_data.append(target_bytes[t_idx])
        t_idx += 1

    if pending_data:
        commands.append((CMD_DATA, bytes(pending_data)))
        pending_data.clear()

    # Write patch file
    print(f"Writing patch output to: {patch_output_path}...")
    with open(patch_output_path, "wb") as out:
        out.write(MAGIC_HEADER)
        out.write(struct.pack(">Q", target_len))

        for cmd in commands:
            if cmd[0] == CMD_COPY:
                _, s_idx, match_len = cmd
                out.write(struct.pack(">BQI", CMD_COPY, s_idx, match_len))
            elif cmd[0] == CMD_DATA:
                _, data_bytes = cmd
                out.write(struct.pack(">BI", CMD_DATA, len(data_bytes)))
                out.write(data_bytes)

        out.write(struct.pack(">B", CMD_EOF))

    patch_size = os.path.getsize(patch_output_path)
    savings = (1.0 - (patch_size / target_len)) * 100
    print(f"Patch created: {patch_size:,} bytes")
    print(f"Bandwidth savings: {savings:.1f}% vs full APK")
    return patch_size, target_sha256

if __name__ == "__main__":
    if len(sys.argv) < 4:
        print("Usage: python generate_apk_patch.py <old.apk> <new.apk> <output.patch>")
        sys.exit(1)
    generate_patch(sys.argv[1], sys.argv[2], sys.argv[3])

