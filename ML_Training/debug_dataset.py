import os
import numpy as np

DATA_PATH = "dataset"

print("🔍 CHECKING DATASET...\n")

if not os.path.exists(DATA_PATH):
    print("❌ Dataset folder NOT found!")
    exit()

actions = os.listdir(DATA_PATH)

if len(actions) == 0:
    print("❌ No folders inside dataset!")
    exit()

total_files = 0

for action in actions:
    action_path = os.path.join(DATA_PATH, action)

    if not os.path.isdir(action_path):
        continue

    files = os.listdir(action_path)

    print(f"\n📁 WORD: {action}")
    print(f"   Files: {len(files)}")

    if len(files) == 0:
        print("   ❌ No data files!")
        continue

    for file in files:
        file_path = os.path.join(action_path, file)

        try:
            data = np.load(file_path)

            print(f"   ✔ {file} → shape: {data.shape}")

            # 🔥 IMPORTANT CHECKS
            if len(data.shape) != 2:
                print("      ❌ ERROR: Data should be 2D (frames, features)")

            elif data.shape[1] != 63:
                print("      ❌ ERROR: Features should be 63 (one hand) or 126 (two hands)")

            elif data.shape[0] < 50:
                print("      ⚠ WARNING: Too few frames")

            total_files += 1

        except Exception as e:
            print(f"   ❌ ERROR loading {file}: {e}")

print("\n==============================")
print(f"📊 TOTAL FILES: {total_files}")
print("==============================")

print("\n✅ DEBUG COMPLETE")