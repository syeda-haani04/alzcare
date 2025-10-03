from google import genai
from google.genai import types
import json

client = genai.Client()

def get_next_steps(patient_data, risk_score, reliability_score):
    all_fields = [
        "Age", "Gender", "Ethnicity", "EducationLevel", "BMI", "Smoking", "AlcoholConsumption",
        "PhysicalActivity", "DietQuality", "SleepQuality", "FamilyHistoryAlzheimers",
        "CardiovascularDisease", "Diabetes", "Depression", "HeadInjury", "Hypertension",
        "SystolicBP", "DiastolicBP", "CholesterolTotal", "CholesterolLDL", "CholesterolHDL",
        "CholesterolTriglycerides", "MMSE", "FunctionalAssessment", "MemoryComplaints",
        "BehavioralProblems", "ADL", "Confusion", "Disorientation", "PersonalityChanges",
        "DifficultyCompletingTasks", "Forgetfulness"
    ]
    missing_fields = [f for f in all_fields if f not in patient_data]

    prompt = (
    "Given the following patient data and risk assessment, "
    "summarize the results and offer supportive advice. "
    "This is the final screen of the app. "
    "Do NOT suggest any further in-app tests, quizzes, or checks, as these are not available. "
    "You may suggest steps the patient can take on their own, such as lifestyle changes, talking to loved ones, or seeing a healthcare professional if concerned. "
    "Reply like you are talking to the patient, use comforting non-technical language. "
    "Keep it very brief and absolutely to the point, since the patient will not read very far.\n\n"
    f"Patient data (only fields provided by the user):\n{json.dumps(patient_data, indent=2)}\n"
    f"Risk score: {risk_score}\n"
    f"Reliability score: {reliability_score}\n"
    )
    if missing_fields:
        prompt += (
            "If given limited information, make sure the patient knows they need to fill "
            "more of the form, as in it is limited information on *their* side, but don't make it too lengthy."
        )
    prompt += (
        "These are all the questions the patient is asked:"
        "Patient Health & Lifestyle Questionnaire Summary:"
        # ... rest of your prompt ...
    )
    response = client.models.generate_content(
        model="gemini-2.5-flash",
        contents=prompt,
        config=types.GenerateContentConfig(
            thinking_config=types.ThinkingConfig(thinking_budget=0)
        ),
    )
    text = response.text
    if text and isinstance(text, str) and text.startswith('"') and text.endswith('"'):
        text = text[1:-1]
    return text if text else ""