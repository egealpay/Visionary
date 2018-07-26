from flask import Flask, request, render_template
from werkzeug.utils import secure_filename
import os

app = Flask(__name__)
app.config['MAX_CONTENT_LENGTH'] = 3 * 1024 * 1024  # max 3 MBs

train_dir = "training_images"
test_dir = "test_images"
ALLOWED_EXTENSIONS = {'png', 'jpg', 'jpeg'}


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

    return 'nice to meet you!'


@app.route("/predict", methods=['GET', 'POST'])
def predict():
    return 'this is you!'


if __name__ == '__main__':
    app.run(host="0.0.0.0")
