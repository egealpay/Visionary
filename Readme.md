
## PokerFace
Take a selfie and the app will recognize you next time.    

## Design 
There is an Android app and a backend server.

The app detects the face and the server recognizes it. 

### Android
built with Kotlin, Retrofit, Rx, Picasso and MLkit (Firebase ML Vision)

Minimum SDK 19, Android 4.4 Kitkat 

### Backend API 

https://poker-face-api.herokuapp.com

- python backend built on Flask framework and face_recognition library (built on dlib) 

https://github.com/ageitgey/face_recognition

http://dlib.net

#### endpoints 

<b>/meet</b>

- accepts a photo and name, returns the status as json response

<b>/predict</b>

- accepts a photo and returns a name guess and status

#### build and deploy
 
- the server app runs in a Docker container, 

- runtime is python 3.6 and gunicorn WSGI server

- the docker image is deployed to a heroku via 
* `heroku container:login`
* `heroku container:push web`
* `heroku container:release web`

* [container docs](https://devcenter.heroku.com/articles/container-registry-and-runtime)

## Theory

[the great article](https://medium.com/@ageitgey/machine-learning-is-fun-part-4-modern-face-recognition-with-deep-learning-c3cffc121d78)

