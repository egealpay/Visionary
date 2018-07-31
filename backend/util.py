import face_recognition
import os
import pickle
from io import BytesIO

train_dir = "classifier/training_images"
test_dir = "classifier/test_images"
model_path = "classifier/known_faces"
allowed_extensions = {'png', 'jpg', 'jpeg'}


def get_known_faces():
    if os.path.exists(model_path):
        with open(model_path, 'rb') as f:
            known_faces = pickle.load(f)
    else:
        known_faces = {}
    return known_faces


def save_dict(updated_known_faces):
    with open(model_path, 'wb') as f:
        pickle.dump(updated_known_faces, f, protocol=pickle.HIGHEST_PROTOCOL)


def get_face_encoding(photo):
    stream = BytesIO(photo.body)
    photo_array = face_recognition.load_image_file(stream)
    face_encoding = face_recognition.face_encodings(photo_array)[0]
    return face_encoding
