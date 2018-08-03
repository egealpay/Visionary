#Grab the latest alpine image
FROM python:3.6-slim-stretch

# update
RUN apt-get -y update

# install modules needed for face recognition
RUN apt-get install -y --fix-missing \
    build-essential \
    cmake \
    gfortran \
    git \
    wget \
    curl \
    graphicsmagick \
    libgraphicsmagick1-dev \
    libatlas-dev \
    libavcodec-dev \
    libavformat-dev \
    libgtk2.0-dev \
    libjpeg-dev \
    liblapack-dev \
    libswscale-dev \
    pkg-config \
    python3-dev \
    python3-numpy \
    software-properties-common \
    zip \
    && apt-get clean && rm -rf /tmp/* /var/tmp/*

# set up dlib
RUN cd ~ && \
    mkdir -p dlib && \
    git clone -b 'v19.9' --single-branch https://github.com/davisking/dlib.git dlib/ && \
    cd  dlib/ && \
    python3 setup.py install --yes USE_AVX_INSTRUCTIONS


# copy files required for the app to run
COPY . /root/app
WORKDIR /root/app/webapp

# install python API requirements
RUN pip3 install -r requirements.txt

# Run the app.  CMD is required to run on Heroku
# $PORT is set by Heroku
CMD gunicorn --bind 0.0.0.0:$PORT wsgi -k gevent --worker-connections 10 --timeout=20 --max-requests=50 --keep-alive=2

