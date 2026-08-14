import numpy as np
import os
from sklearn.model_selection import train_test_split
import tensorflow as tf

DATA_PATH = "dataset"
SEQUENCE_LENGTH = 60
FEATURES = 63

labels = sorted(os.listdir(DATA_PATH))

X, y = [], []

for idx, label in enumerate(labels):
    folder = os.path.join(DATA_PATH, label)

    for file in os.listdir(folder):
        data = np.load(os.path.join(folder, file))
        X.append(data)
        y.append(idx)

X = np.array(X)
y = np.array(y)

# normalization (IMPORTANT FIX)
X = X.astype(np.float32)
mean = np.mean(X, axis=(0,1))
std = np.std(X, axis=(0,1)) + 1e-6
X = (X - mean) / std

X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2)

model = tf.keras.Sequential([
    tf.keras.layers.LSTM(64, return_sequences=True, input_shape=(60,63)),
    tf.keras.layers.LSTM(64),
    tf.keras.layers.Dense(64, activation="relu"),
    tf.keras.layers.Dense(len(labels), activation="softmax")
])

model.compile(optimizer="adam",
              loss="sparse_categorical_crossentropy",
              metrics=["accuracy"])

model.fit(X_train, y_train, epochs=30, validation_data=(X_test, y_test))

model.save("model.h5")

# convert to TFLite
converter = tf.lite.TFLiteConverter.from_keras_model(model)
tflite_model = converter.convert()

open("model.tflite", "wb").write(tflite_model)

print("Model trained & saved!")