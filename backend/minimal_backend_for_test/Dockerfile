# base image
FROM python:3.6-slim-stretch

# update
RUN apt-get -y update

# copy files to container
COPY . /root/minimal_backend_for_test

# install python API requirements
RUN cd /root/minimal_backend_for_test && \
    pip3 install -r requirements.txt

# tell the port number the container should expose
EXPOSE 5000

# run the application
CMD cd /root/minimal_backend_for_test && \
    python3 api.py