import cv2
import numpy as np
import os
import mediapipe as mp

DATA_PATH = "dataset"
SEQUENCE_LENGTH = 60
FEATURES = 63
MAX_SAMPLES = 50

os.makedirs(DATA_PATH, exist_ok=True)

mp_hands = mp.solutions.hands
hands = mp_hands.Hands(
    max_num_hands=1,
    min_detection_confidence=0.75,
    min_tracking_confidence=0.75
)

label = input("Enter sign name (father / mother / water): ")

save_path = os.path.join(DATA_PATH, label)
os.makedirs(save_path, exist_ok=True)

cap = cv2.VideoCapture(0)

recording = True   # ✅ AUTO START
frames = []

count = len(os.listdir(save_path))

print("Recording started automatically... Press Q to quit")

while True:
    ret, frame = cap.read()
    if not ret:
        break

    frame = cv2.flip(frame, 1)
    rgb = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
    result = hands.process(rgb)

    landmarks = np.zeros(FEATURES)

    if result.multi_hand_landmarks:
        hand = result.multi_hand_landmarks[0]
        landmarks = np.array([[lm.x, lm.y, lm.z] for lm in hand.landmark]).flatten()

    if recording:
        frames.append(landmarks)

        cv2.putText(frame,
                    f"Recording {len(frames)}/{SEQUENCE_LENGTH}",
                    (10, 40),
                    cv2.FONT_HERSHEY_SIMPLEX,
                    1,
                    (0, 0, 255),
                    2)

        if len(frames) == SEQUENCE_LENGTH:
            np.save(f"{save_path}/{count}.npy", np.array(frames))
            print("Saved:", count)

            count += 1
            frames = []

            if count >= MAX_SAMPLES:
                print("Dataset complete!")
                recording = False

    cv2.putText(frame,
                f"Samples: {count}/{MAX_SAMPLES}",
                (10, 80),
                cv2.FONT_HERSHEY_SIMPLEX,
                0.8,
                (0, 255, 0),
                2)

    cv2.imshow("Dataset Recorder", frame)

    key = cv2.waitKey(1) & 0xFF
    if key == ord('q'):
        break

cap.release()
cv2.destroyAllWindows()