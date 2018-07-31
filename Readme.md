
### PokerFace
Upload a face image and the app will recognize you when it sees you later.   

[the great article](https://medium.com/@ageitgey/machine-learning-is-fun-part-4-modern-face-recognition-with-deep-learning-c3cffc121d78)

### Android
built with Kotlin, Retrofit, Rx, Picasso, FotoApparat, FaceDetector

https://github.com/RedApparat/Fotoapparat

https://github.com/RedApparat/FaceDetector

### API 

https://facerecapi.herokuapp.com

- python backend built on sanic framework and face_recognition library. 

https://github.com/channelcat/sanic

https://github.com/ageitgey/face_recognition

http://dlib.net

https://heroku.com


#### endpoints 

<b>/meet</b>

- accepts a photo and name, returns the status as json response

<b>/predict</b>

- accepts a photo and returns a name guess 

#### build and deploy
 
- backend runs in a Docker container, 

- runtime is python 3.6 and gunicorn WSGI server

- the docker image is deployed to a heroku via 
* `heroku container:login`
* `heroku container:push web`
* `heroku container:release web`

* [container docs](https://devcenter.heroku.com/articles/container-registry-and-runtime)


