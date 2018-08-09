import face_recognition
from util import get_known_faces, save_dict
from flask import Flask, jsonify, request
from requests.exceptions import RequestException, Timeout, ConnectionError
from io import BytesIO

app = Flask(__name__)
known_faces = get_known_faces()
last_seen_person = {'encoding': None, 'name': None}


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
    global last_seen_person
    photo, name = get_photo_and_name()

    if photo is None:
        status = 'could not read the photo, please check the image file'
        response = {"guess": guess, 'status': status}
        return jsonify(response)

    unknown_face_encoding = get_face_encoding(photo)
    if unknown_face_encoding is None:
        status = 'could not detect a face, please send another pic'
        response = {"guess": guess, 'status': status}
        return jsonify(response)

    # check last seen
    last_seen_face_encoding = last_seen_person['encoding']
    if last_seen_face_encoding is not None:
        is_last_seen = face_recognition.compare_faces([last_seen_face_encoding], unknown_face_encoding, tolerance=0.6)[
            0]
        if is_last_seen:
            guess = last_seen_person['name']
            status = 'found'
            response = {"guess": guess, 'status': status}
            return jsonify(response)

    # check all faces
    for name, face_encoding in known_faces.items():
        try:
            is_found = face_recognition.compare_faces([face_encoding], unknown_face_encoding, tolerance=0.6)[0]
            if is_found:
                guess = name
                status = 'found'
                last_seen_person['encoding'] = face_encoding
                last_seen_person['name'] = name
                break
        except IndexError as e:
            print(e)

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


def get_face_encoding(photo):
    face_encoding = None
    try:
        image_bytes = photo.read()
        stream = BytesIO(image_bytes)
        photo_array = face_recognition.load_image_file(stream)  # mode='L' for grayscale
        face_encoding = face_recognition.face_encodings(photo_array)[0]
    except IndexError as e:  # couldn't locate a face
        print(e)
    return face_encoding


if __name__ == '__main__':
    # for local dev
    app.run(host='0.0.0.0', port=5000)
