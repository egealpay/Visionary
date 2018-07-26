from eve import Eve

app = Eve()

@app.route("/")
def hi():
    return "hey you!"


@app.route("/meet")
def meet_new_person(image):
    return "nice to meet you!"


@app.route("/recognize")
def hi():
    return "hey you!"


if __name__ == '__main__':
    app.run(host="0.0.0.0")
