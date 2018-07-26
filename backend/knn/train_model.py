import math
from sklearn import neighbors
import os
import os.path
import pickle
import face_recognition
from face_recognition.face_recognition_cli import image_files_in_folder


# Train the KNN classifier and save it to disk
def train(train_dir, model_save_path=None, n_neighbors=None, knn_algo='ball_tree'):
    face_encodings = []
    person_names = []

    # Loop through each person in the training set
    for person_name in os.listdir(train_dir):
        person_dir = os.path.join(train_dir, person_name)
        if not os.path.isdir(person_dir):
            continue

        # Loop through each training image for the current person
        for img_path in image_files_in_folder(person_dir):
            image = face_recognition.load_image_file(img_path)
            face_bounding_boxes = face_recognition.face_locations(image)

            if len(face_bounding_boxes) != 1:
                # If there are no people (or too many people) in a training image, skip the image.
                print("Image {} not suitable for training: {}".format(img_path, "Didn't find a face" if len(
                    face_bounding_boxes) < 1 else "Found more than one face"))
            else:
                # Add face encoding for current image to the training set
                face_encodings.append(
                    face_recognition.face_encodings(image, known_face_locations=face_bounding_boxes)[0])
                person_names.append(person_name)

    # Determine how many neighbors to use for weighting in the KNN classifier
    if n_neighbors is None:
        n_neighbors = int(round(math.sqrt(len(face_encodings))))
        print("Chose n_neighbors automatically:", n_neighbors)

    # Create and training_images the KNN classifier
    knn_clf_model = neighbors.KNeighborsClassifier(n_neighbors=n_neighbors, algorithm=knn_algo, weights='distance')
    knn_clf_model.fit(face_encodings, person_names)

    # Save the trained KNN classifier
    if model_save_path is not None:
        with open(model_save_path, 'wb') as f:
            pickle.dump(knn_clf_model, f)

    return knn_clf_model


def get_knn_model(model_path, train_dir):
    if os.path.exists(model_path):
        with open(model_path, 'rb') as f:
            knn_model = pickle.load(f)
        print("Model already trained")
    else:
        print("Training KNN classifier...")
        knn_model = train(train_dir, model_save_path=model_path, n_neighbors=2)
        print("Training complete!")

    return knn_model
