import face_recognition
from util import get_face_encoding, get_known_faces, save_dict
from flask import Flask, jsonify, request
from requests.exceptions import RequestException, Timeout, ConnectionError

app = Flask(__name__)

known_faces = get_known_faces()


@app.route("/")
def hello():
    response = {"status": "UP"}
    return jsonify(response)


@app.route('/meet', methods=['GET', 'POST'])
def meet():
    photo, name = get_photo_and_name(get_name=True)

    if photo is not None and name is not None:
        new_face_encoding = get_face_encoding(photo)
        if new_face_encoding is not None:
            known_faces[name] = new_face_encoding
            save_dict(known_faces)
            status = 'I just met you and this is crazy'
        else:
            status = 'could not detect a face, please send another pic'
    else:
        status = 'could not read the photo and name, please check the image file'

    response = {'status': status, 'hello': name}
    return jsonify(response)


@app.route("/predict", methods=['GET', 'POST'])
def predict():
    status = 'could not recognize'
    guess = 'anyone'
    photo, name = get_photo_and_name()

    if photo:
        unknown_face_encoding = get_face_encoding(photo)
        if unknown_face_encoding is not None:
            for name, face_encoding in known_faces.items():
                try:
                    is_found = face_recognition.compare_faces([face_encoding], unknown_face_encoding, tolerance=0.6)[
                        0]  # True, False
                    if is_found:
                        guess = name
                        status = 'found'
                        break
                except IndexError as e:
                    print(e)
        else:
            status = 'could not detect a face, please send another pic'
    else:
        status = 'could not read the photo, please check the image file'

    response = {"guess": guess, 'status': status}
    return jsonify(response)


def get_photo_and_name(get_name=False):
    photo = None
    name = None
    try:
        if get_name:
            name = request.form['name']
        photo = request.files['photo']
    except ConnectionError as ece:
        print("Connection Error:", ece)
    except Timeout as et:
        print("Timeout Error:", et)
    except RequestException as e:
        print("Some Ambiguous Exception:", e)

    return photo, name


if __name__ == '__main__':
    # for local dev
    app.run(host='0.0.0.0', port=5000)
