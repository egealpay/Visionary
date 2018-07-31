from sanic import Sanic
from sanic.response import json
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
    name = request.form.get('name')
    photo = request.files.get('photo')
    new_face_encoding = get_face_encoding(photo)
    known_faces = get_known_faces()
    known_faces[name] = new_face_encoding
    save_dict(known_faces)
    return json({'hello': name})


@app.route("/predict", methods=['GET', 'POST'])
async def predict(request):
    guess = 'no one'
    photo = request.files.get('photo')
    unknown_face_encoding = get_face_encoding(photo)
    known_faces = get_known_faces()
    for name, face_encoding in known_faces.items():
        is_found = face_recognition.compare_faces([face_encoding], unknown_face_encoding, tolerance=0.6)
        if is_found:
            guess = name
            break
    response = {"guess": guess}
    return json(response)


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=port, workers=cpu_cores)
