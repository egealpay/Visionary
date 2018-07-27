from face_recognition.face_recognition_cli import image_files_in_folder

from knn.predict import predict
from knn.show_results import show_prediction_labels_on_image
from knn.train_model import get_knn_model

if __name__ == "__main__":

    # locations
    train_dir = "classifier/training_images"
    test_dir = "classifier/test_images"
    model_path = "classifier/trained_knn_model.clf"

    # read or train model file
    model = get_knn_model(model_path, train_dir)

    for test_img_path in image_files_in_folder(test_dir):
        print("Looking for faces in {}".format(test_img_path))

        predictions = predict(test_img_path, knn_clf_model=model)

        for name in predictions:
            print("Found {} in {}".format(name, test_img_path))
            show_prediction_labels_on_image(test_img_path, predictions)
