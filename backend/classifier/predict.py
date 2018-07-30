
import face_recognition


def predict(test_img_path, knn_clf_model=None, distance_threshold=0.6):

    # Load image file and find face locations
    test_img = face_recognition.load_image_file(test_img_path)
    test_face_locations = face_recognition.face_locations(test_img)

    if len(test_face_locations) != 1:
        # If there are no people (or too many people) in a training image, skip the image.
        print("{}".format(test_img_path, "Didn't find a face" if len(test_face_locations) < 1 else "Found more than one face"))

    # If no faces are found in the image, return an empty result.
    if len(test_face_locations) == 0:
        return []

    # Find encodings for faces in the test iamge
    faces_encodings = face_recognition.face_encodings(test_img, known_face_locations=test_face_locations)

    # Use the KNN model to find the best matches for the test face
    closest_distances = knn_clf_model.kneighbors(faces_encodings, n_neighbors=1)
    are_matches = [closest_distances[0][i][0] <= distance_threshold for i in range(len(test_face_locations))]

    # Predict classes and remove classifications that aren't within the threshold
    return [(pred, loc) if rec else ("unknown", loc) for pred, loc, rec in zip(knn_clf_model.predict(faces_encodings), test_face_locations, are_matches)]
