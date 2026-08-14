
import cv2
import mediapipe as mp
import numpy as np
import tensorflow as tf
import collections
import os
import sys

# Ensure correct versions of libraries are installed if running as a standalone script
# In a Colab environment, these might already be handled, but for robustness in a script:
# !pip uninstall -y mediapipe protobuf numpy
# !pip install numpy==1.26.4 protobuf==4.25.9 mediapipe==0.10.20 opencv-contrib-python==4.11.0.86

# It's important that `actions` array is available or loaded.
# In a standalone script, you would load this from a file, e.g., labels.txt
# For this Colab context, we assume 'actions' is still in memory from previous cells.
# If running predict.py as a standalone, you would uncomment and modify the lines below:

# Load labels from labels.txt
with open('labels.txt', 'r') as f:
    actions = [line.strip() for line in f.readlines()]

# Load the TFLite model and allocate tensors
interpreter = tf.lite.Interpreter(model_path="model.tflite")
interpreter.allocate_tensors()

# Get input and output tensors
input_details = interpreter.get_input_details()
output_details = interpreter.get_output_details()

# MediaPipe Hands setup
mp_hands = mp.solutions.hands
hands = mp_hands.Hands(min_detection_confidence=0.7, min_tracking_confidence=0.7)
mp_drawing = mp.solutions.drawing_utils

# Define constants for sequence buffering and confidence
SEQUENCE_LENGTH = 60 # Must match the input shape of the model
CONFIDENCE_THRESHOLD = 0.85

# Create a deque for the sequence buffer
sequence_buffer = collections.deque(maxlen=SEQUENCE_LENGTH)

# For smoothing predictions using majority voting
PREDICTION_HISTORY_LENGTH = 10
prediction_history = collections.deque(maxlen=PREDICTION_HISTORY_LENGTH)

# Webcam setup
cap = cv2.VideoCapture(0) # 0 for default webcam
if not cap.isOpened():
    print("Error: Could not open webcam.")
    exit()

print("Webcam opened successfully. Press 'q' to quit.")

while cap.isOpened():
    ret, frame = cap.read()
    if not ret:
        break

    # Flip frame horizontally for natural selfie-view display
    frame = cv2.flip(frame, 1)

    # Convert the BGR image to RGB.
    image = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)

    # Process the image and find hands
    results = hands.process(image)

    # Convert the image back to BGR.
    image = cv2.cvtColor(image, cv2.COLOR_RGB2BGR)

    current_hand_landmarks = []

    if results.multi_hand_landmarks:
        for hand_landmarks in results.multi_hand_landmarks:
            # Draw hand landmarks on the frame
            mp_drawing.draw_landmarks(image, hand_landmarks, mp_hands.HAND_CONNECTIONS)

            # Extract landmarks and flatten them
            for landmark in hand_landmarks.landmark:
                current_hand_landmarks.extend([landmark.x, landmark.y, landmark.z])

    # Ensure 63 features are collected (21 landmarks * 3 coords)
    # Pad with zeros if no hand is detected or fewer than 21 landmarks are found (unlikely with MediaPipe)
    if len(current_hand_landmarks) == 63:
        sequence_buffer.append(current_hand_landmarks)
    else:
        # If no hand detected, or incomplete detection, append zeros or last known pose
        # Appending zeros is a simple approach, more complex solutions might involve state retention.
        sequence_buffer.append(np.zeros(63).tolist())

    # Only make predictions if the sequence buffer is full
    if len(sequence_buffer) == SEQUENCE_LENGTH:
        input_data = np.array([list(sequence_buffer)], dtype=np.float32)

        # Set the tensor
        interpreter.set_tensor(input_details[0]['index'], input_data)

        # Invoke inference
        interpreter.invoke()

        # Get the prediction results
        output_data = interpreter.get_tensor(output_details[0]['index'])
        probabilities = np.squeeze(output_data) # Remove batch dimension

        # Get the predicted class index and confidence
        predicted_class_idx = np.argmax(probabilities)
        confidence = probabilities[predicted_class_idx]

        # Apply confidence threshold
        if confidence > CONFIDENCE_THRESHOLD:
            predicted_label = actions[predicted_class_idx]
            prediction_history.append(predicted_label)

            # Apply smoothing (majority voting)
            if len(prediction_history) == PREDICTION_HISTORY_LENGTH:
                # Count occurrences of each prediction in history
                counts = collections.Counter(prediction_history)
                # Get the most common prediction
                smoothed_prediction = counts.most_common(1)[0][0]

                # Display the smoothed prediction on the frame
                cv2.putText(image, f'Sign: {smoothed_prediction} ({confidence:.2f})', 
                            (10, 30), cv2.FONT_HERSHEY_SIMPLEX, 1, (0, 255, 0), 2, cv2.LINE_AA)
            else:
                # If history is not full, just show the current prediction
                cv2.putText(image, f'Sign: {predicted_label} ({confidence:.2f})', 
                            (10, 30), cv2.FONT_HERSHEY_SIMPLEX, 1, (0, 255, 0), 2, cv2.LINE_AA)
        else:
            cv2.putText(image, 'Sign: None', (10, 30), cv2.FONT_HERSHEY_SIMPLEX, 1, (0, 0, 255), 2, cv2.LINE_AA)
    else:
        cv2.putText(image, 'Buffering...', (10, 30), cv2.FONT_HERSHEY_SIMPLEX, 1, (255, 0, 0), 2, cv2.LINE_AA)

    cv2.imshow('Sign Language Recognition', image)

    if cv2.waitKey(1) & 0xFF == ord('q'):
        break

cap.release()
cv2.destroyAllWindows()
