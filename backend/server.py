from sanic import Sanic
from sanic.response import json
from train import train
import os
import multiprocessing

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
async def test(request):
    # get name and photo
    name = request.form.get('name')
    photo = request.files.get('photo')
    train(name, photo)
    return json({'hello': name})


@app.route("/predict", methods=['GET', 'POST'])
async def predict(request):
    # photo = request.files.get('photo')
    confidence = 0.423423
    person_name = "James Bond"
    response = {"prediction": person_name, "confidence": confidence}
    return json(response)


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=port, workers=cpu_cores)
