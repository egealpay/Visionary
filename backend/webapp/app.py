import face_recognition
from util import get_face_encoding, get_known_faces, save_dict
from flask import Flask, jsonify, request
import requests

app = Flask(__name__)

@app.route("/")
def hello():
    response = {"hello": "world"}
    return jsonify(response)


@app.route('/meet', methods=['GET', 'POST'])
def meet():
    # get name and photo
    name = None
    photo = None

    try:
        name = request.form['name']
        photo = request.files['photo']
    except requests.exceptions.ConnectionError as ece:
        print("Connection Error:", ece)
    except requests.exceptions.Timeout as et:
        print("Timeout Error:", et)
    except requests.exceptions.RequestException as e:
        print("Some Ambiguous Exception:", e)

    if photo is not None and name is not None:
        new_face_encoding = get_face_encoding(photo)
        if new_face_encoding is not None:
            known_faces = get_known_faces()
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
    photo = None

    try:
        photo = request.files['photo']
    except requests.exceptions.ConnectionError as ece:
        print("Connection Error:", ece)
    except requests.exceptions.Timeout as et:
        print("Timeout Error:", et)
    except requests.exceptions.RequestException as e:
        print("Some Ambiguous Exception:", e)

    if photo:
        unknown_face_encoding = get_face_encoding(photo)
        if unknown_face_encoding is not None:
            known_faces = get_known_faces()

            for name, face_encoding in known_faces.items():
                try:
                    is_found = face_recognition.compare_faces([face_encoding], unknown_face_encoding, tolerance=0.6)[0] # True, False
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


if __name__ == '__main__':
    # for local dev
    app.run(host='0.0.0.0', port=5000)
