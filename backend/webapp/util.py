import face_recognition
import os
import pickle
from io import BytesIO

model_path = "known_faces"

def get_known_faces():
    known_faces = dict()

    if os.path.exists(model_path):
        try:
            with open(model_path, 'rb') as f:
                known_faces = pickle.load(f)
        except IOError as e:
            print(e)

    return known_faces


def save_dict(updated_known_faces):
    try:
        with open(model_path, 'wb') as f:
            pickle.dump(updated_known_faces, f, protocol=pickle.HIGHEST_PROTOCOL)
    except IOError as e:
        print(e)


def get_face_encoding(photo):
    face_encoding = None
    try:
        image_bytes = photo.read()
        stream = BytesIO(image_bytes)
        photo_array = face_recognition.load_image_file(stream)
        face_encoding = face_recognition.face_encodings(photo_array)[0]
    except IndexError as e:  # couldn't locate a face
        print(e)
    return face_encoding
