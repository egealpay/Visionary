from flask import Flask, request, render_template, jsonify
import os
import pickle
from werkzeug.utils import secure_filename


# Heroku support: bind to PORT if defined, otherwise default to 5000.
if 'PORT' in os.environ:
    port = int(os.environ.get('PORT'))
    # use '0.0.0.0' to ensure your REST API is reachable from all your
    # network (and not only your computer).
else:
    port = 5000

host = "0.0.0.0"

app = Flask(__name__)
app.config['MAX_CONTENT_LENGTH'] = 3 * 1024 * 1024  # max 3 MBs

train_dir = "classifier/training_images"
test_dir = "classifier/test_images"
model_path = "classifier/trained_knn_model.clf"

ALLOWED_EXTENSIONS = {'png', 'jpg', 'jpeg'}

#with open('known_faces', 'rb') as fp:
 #   known_faces = pickle.load(fp)


@app.route("/")
def index():
    return render_template('upload.html')


@app.route("/meet", methods=['GET', 'POST'])
def meet():
    # get name and photo
    person_name = request.form['name']
    photo = request.files['photo']

    # define path
    person_dir = os.path.join(train_dir, person_name)

    # create dir for person
    if not os.path.exists(person_dir):
        os.makedirs(person_dir)
    app.config['UPLOAD_FOLDER'] = person_dir

    # save
    number_of_images_for_person = len(os.listdir(person_dir))
    photo_name = str(number_of_images_for_person + 1)
    photo_path = os.path.join(person_dir, photo_name)
    photo.save(photo_path)
    response = {"welcome": person_name}
    return jsonify(response)


@app.route("/predict", methods=['GET', 'POST'])
def predict():
    confidence = 0.423423
    person_name = "x"
    response = {"prediction": person_name, "confidence": confidence}
    return jsonify(response)


if __name__ == '__main__':
    app.run(host=host, port=port)
