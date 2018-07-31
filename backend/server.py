from sanic import Sanic
from sanic.response import json
from sanic.exceptions import ServerError
import os
import multiprocessing
import face_recognition
from util import get_face_encoding, get_known_faces, save_dict

if 'PORT' in os.environ:
    port = int(os.environ.get('PORT'))
else:
    port = 5000

app = Sanic()

cpu_cores = multiprocessing.cpu_count()


@app.route("/")
async def hello(request):
    return json({"hello": "world"})


@app.route('/meet', methods=['GET', 'POST'])
async def meet(request):
    # get name and photo
    photo = None
    name = None
    try:
        name = request.form.get('name')
        photo = request.files.get('photo')
    except ServerError as e:
        print(e)

    if photo and name:
        new_face_encoding = get_face_encoding(photo)
        if new_face_encoding is not None:
            known_faces = get_known_faces()
            known_faces[name] = new_face_encoding
            save_dict(known_faces)
            response = {'hello': name}
        else:
            response = {'status': 'could not detect a face, please send another pic'}
    else:
        response = {'status': 'could not read the photo and name, please check the image file'}

    return json(response)


@app.route("/predict", methods=['GET', 'POST'])
async def predict(request):
    status = 'could not recognize'
    guess = 'anyone'
    photo = None
    try:
        photo = request.files.get('photo')
    except ServerError as e:
        print(e)

    if photo:
        unknown_face_encoding = get_face_encoding(photo)
        if unknown_face_encoding is not None:
            known_faces = get_known_faces()

            for name, face_encoding in known_faces.items():
                try:
                    is_found = face_recognition.compare_faces([face_encoding], unknown_face_encoding, tolerance=0.6)[0]
                    if is_found:
                        guess = name
                        status = 'found'
                        break
                except IndexError as e:
                    print(e)
            response = {"guess": guess, 'status': status}
        else:
            response = {'status': 'could not detect a face, please send another pic', 'guess': guess}
    else:
        response = {'status': 'could not read the photo, please check the image file', 'guess': guess}

    return json(response)


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=port, workers=2, access_log=True)
