import face_recognition

known_faces = {}
train_dir = "classifier/training_images"
test_dir = "classifier/test_images"
model_path = "classifier/trained_knn_model.clf"
allowed_extensions = {'png', 'jpg', 'jpeg'}


def train(name, photo):
    with open(photo.name, 'wb') as f:
        f.write(photo.body)
    photo_array = face_recognition.load_image_file(photo.name)
    face_locations = face_recognition.face_locations(photo_array)
    known_faces[name] = face_locations
