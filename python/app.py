from flask import Flask, request
from flask_restful import Resource, Api
import random
import pandas as pd
from sklearn.ensemble import RandomForestClassifier
from sklearn.preprocessing import LabelEncoder

app = Flask(__name__)
api = Api(app)


# Load and preprocess data at startup
df = pd.read_csv('alzheimers_disease_data.csv')

# Drop columns not needed for prediction
drop_cols = ['PatientID', 'Diagnosis', 'DoctorInCharge']
X = df.drop(columns=drop_cols)
y = df['Diagnosis']

# Encode categorical columns
label_encoders = {}
for col in X.select_dtypes(include=['object']).columns:
    le = LabelEncoder()
    X[col] = le.fit_transform(X[col].astype(str))
    label_encoders[col] = le

# Train model
model = RandomForestClassifier(random_state=42)
model.fit(X, y)

def process_patient_data_and_predict_risk(patient_data_payload):
    print("Processing data:", patient_data_payload)
    input_df = pd.DataFrame([patient_data_payload])
    input_df = input_df[X.columns]
    for col, le in label_encoders.items():
        input_df[col] = le.transform(input_df[col].astype(str))
    risk_score = model.predict_proba(input_df)[0][1]
    print("Calculated risk_score:", risk_score)
    processed_data = patient_data_payload
    return round(risk_score, 4), processed_data


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
        return {"message": "Flask server is running"}
api.add_resource(HelloWorld, "/hello")


if __name__ == "__main__":
    print("Starting Flask server...")
    # use flask run --debug
    app.run(debug=True, host='0.0.0.0', port=5000)