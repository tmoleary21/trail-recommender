from werkzeug.exceptions import HTTPException
from flask import Blueprint, Flask, jsonify, request
from flask_cors import CORS
from forecast import Point, getForecast 

app = Flask(__name__)

CORS(app)

#main_bp = Blueprint('main', __name__)

# Error handlers pulled from Zephyr
@app.errorhandler(HTTPException)
def handle_http_error(e: HTTPException):
    return jsonify(code=e.code, name=e.name, description=e.description), e.code

@app.route('/')
def home():
    return 'OLeary-Volesky NWS API Flask Server\n'

@app.route('/forecast', methods=['GET', 'POST'])
def request_forecast():
    req = request.get_json()
    point = Point(req["lat"], req["lon"])
    return jsonify(getForecast(point))
    

def run():
    app.run(host="0.0.0.0", port=6000, debug=True)

if __name__ == "__main__":
    run()
