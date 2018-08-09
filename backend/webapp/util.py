import os
import pickle

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
