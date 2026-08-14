import os

DATASET_PATH = "dataset"
EXPECTED_FRAMES = 60

total_sequences = 0
correct_sequences = 0
bad_sequences = 0

print("\n🔍 DATASET VALIDATION STARTED...\n")

for label in os.listdir(DATASET_PATH):
    label_path = os.path.join(DATASET_PATH, label)

    if not os.path.isdir(label_path):
        continue

    label_total = 0
    label_bad = 0
    label_correct = 0

    print(f"\n📁 LABEL: {label}")

    for seq in os.listdir(label_path):
        seq_path = os.path.join(label_path, seq)

        if not os.path.isdir(seq_path):
            continue

        files = [f for f in os.listdir(seq_path) if f.endswith(".npy")]
        frame_count = len(files)

        label_total += 1
        total_sequences += 1

        if frame_count == EXPECTED_FRAMES:
            label_correct += 1
            correct_sequences += 1
        else:
            label_bad += 1
            bad_sequences += 1

            if frame_count < EXPECTED_FRAMES:
                print(f"   ❌ {seq} → Missing {EXPECTED_FRAMES - frame_count} frames ({frame_count}/60)")
            else:
                print(f"   ⚠️ {seq} → Extra {frame_count - EXPECTED_FRAMES} frames ({frame_count}/60)")

    print(f"   ✔ Label Summary → Total: {label_total}, Correct: {label_correct}, Bad: {label_bad}")

# ---------------- FINAL REPORT ----------------

accuracy = (correct_sequences / total_sequences) * 100 if total_sequences > 0 else 0

print("\n==========================")
print("📊 FINAL DATASET REPORT")
print("==========================")
print(f"Total Sequences   : {total_sequences}")
print(f"Correct Sequences : {correct_sequences}")
print(f"Bad Sequences     : {bad_sequences}")
print(f"Accuracy          : {accuracy:.2f}%")
print("==========================")