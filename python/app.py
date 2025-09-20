from flask import Flask, request
from flask_restful import Resource, Api
import random

app = Flask(__name__)
api = Api(app)


def process_patient_data_and_predict_risk(patient_data_payload):
    print("Processing data:", patient_data_payload)
    simulated_risk_score = round(random.uniform(0.0, 1.0), 4)
    processed_data = {}
    for key, value in patient_data_payload.items():
        processed_data[key] = value
    return simulated_risk_score, processed_data

class PatientDataSubmission(Resource):
    def post(self):
        try:
            incoming_data = request.get_json()
            if not incoming_data:
                return {"status": "error", "success": False, "message": "No input data provided"}, 400
        except Exception as e:
            return {"status": "error", "success": False, "message": f"Invalid JSON format: {str(e)}"}, 400

        patient_data_payload = incoming_data

        try:
            risk_score, processed_data_for_response = process_patient_data_and_predict_risk(patient_data_payload)
        except Exception as e:
            print(f"Error during processing: {str(e)}")
            return {"status": "error", "success": False, "message": "Error processing patient data"}, 500

        response_payload = {
            "status": "success",
            "success": True,
            "message": "Patient data submitted and processed successfully.",
            "patient_id": random.randint(1000, 9999),
            "risk_score": risk_score,
            "data": processed_data_for_response
        }

        return response_payload, 201


api.add_resource(PatientDataSubmission, "/api/patientdata")

class HelloWorld(Resource):
    def get(self):
        return {"message": "Flask server is running!"}
api.add_resource(HelloWorld, "/hello")


if __name__ == "__main__":
    print("Starting Flask server...")
    # For consistency when using `flask run`, it's better to use `flask run --debug`
    # However, this app.run() will work if you execute `python3 app.py`
    app.run(debug=True, host='0.0.0.0', port=5000)